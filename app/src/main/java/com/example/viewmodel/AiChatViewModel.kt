package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.UUID

private const val TAG = "AiChatViewModel"

enum class ChatModelOption(val modelId: String, val label: String) {
    GENERAL("gemini-3.8-flash", "3.8 Flash"),
    FAST("gemini-3.5-flash-lite", "Flash-Lite"),
    DEEP_REASONING("gemini-4-argon", "4 Argon"),
}

enum class ChatRolePreset(
    val label: String,
    val systemPrompt: String,
    val starterPrompts: List<String>,
) {
    DIRECTOR(
        label = "Director",
        systemPrompt = "You are a YouTube Stickman Animation Director. Help the creator plan punchy keyframe sequences, fight choreography, camera timing, FPS pacing, and engaging short video scripts. Keep answers concise and actionable.",
        starterPrompts = listOf(
            "Give me a 5-frame stickman martial arts combo",
            "Write a 15-second YouTube Shorts stickman intro",
            "How do I make a jump flip look snappy at 8 FPS?",
        ),
    ),
    ART_COACH(
        label = "Art Coach",
        systemPrompt = "You are a visual concept artist for StickMan Studio. Help users write vivid image prompts for Nano Banana 2.1, design stickman color palettes, props, and dramatic stage backgrounds.",
        starterPrompts = listOf(
            "Write a Nano Banana prompt for a cyber dojo backdrop",
            "Suggest 3 color themes for a stickman duo battle",
            "How should I frame a YouTube thumbnail pose?",
        ),
    ),
    STRATEGIST(
        label = "Assistant",
        systemPrompt = "You are a helpful, articulate creative assistant built into StickMan Studio. Answer clearly, maintain context across turns, and adapt to the user's goals.",
        starterPrompts = listOf(
            "Brainstorm 3 viral stickman storytime video ideas",
            "Break down walk cycle keyframe poses",
            "Explain anticipation and follow-through in animation",
        ),
    ),
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val isStreaming: Boolean = false,
)

data class AiChatUiState(
    val selectedModel: ChatModelOption = ChatModelOption.GENERAL,
    val selectedRole: ChatRolePreset = ChatRolePreset.DIRECTOR,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
    val errorMessage: String? = null,
)

class AiChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    private var chatSession: Chat? = null
    private var sessionModel: ChatModelOption? = null
    private var sessionRole: ChatRolePreset? = null

    fun updateInput(text: String) {
        _uiState.update { it.copy(inputText = text, errorMessage = null) }
    }

    fun selectModel(option: ChatModelOption) {
        if (option == _uiState.value.selectedModel) return
        _uiState.update { it.copy(selectedModel = option, errorMessage = null) }
        rebuildSessionFromHistory()
    }

    fun selectRole(role: ChatRolePreset) {
        if (role == _uiState.value.selectedRole) return
        _uiState.update { it.copy(selectedRole = role, errorMessage = null) }
        rebuildSessionFromHistory()
    }

    fun clearChat() {
        chatSession = null
        _uiState.update {
            it.copy(
                messages = emptyList(),
                isSending = false,
                errorMessage = null,
            )
        }
    }

    fun sendMessage(overrideText: String? = null) {
        val current = _uiState.value
        if (current.isSending) return
        val prompt = (overrideText ?: current.inputText).trim()
        if (prompt.isEmpty()) return

        val userMessage = ChatMessage(isUser = true, text = prompt)
        val assistantMsgId = UUID.randomUUID().toString()
        val placeholderAssistant = ChatMessage(
            id = assistantMsgId,
            isUser = false,
            text = "",
            isStreaming = true,
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage + placeholderAssistant,
                inputText = if (overrideText == null) "" else it.inputText,
                isSending = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            val accumulated = StringBuilder()
            try {
                val chat = ensureChatSession(excludeTrailingCount = 2)
                withTimeout(60_000L) {
                    chat.sendMessageStream(prompt).collect { chunk ->
                        val partText = chunk.text.orEmpty()
                        if (partText.isNotEmpty()) {
                            accumulated.append(partText)
                            updateAssistantMessage(
                                id = assistantMsgId,
                                text = accumulated.toString(),
                                isStreaming = true,
                            )
                        }
                    }
                }
                val finalText = accumulated.toString().trim()
                if (finalText.isNotEmpty()) {
                    updateAssistantMessage(
                        id = assistantMsgId,
                        text = finalText,
                        isStreaming = false,
                    )
                    _uiState.update { it.copy(isSending = false) }
                } else {
                    removeMessage(assistantMsgId)
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            errorMessage = "Gemini returned an empty response.",
                        )
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Chat response timed out", e)
                finalizeOrDropStream(assistantMsgId, accumulated.toString())
                _uiState.update {
                    it.copy(
                        isSending = false,
                        errorMessage = "Response timed out after 60 seconds. Please try again.",
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Gemini chat failed", e)
                finalizeOrDropStream(assistantMsgId, accumulated.toString())
                _uiState.update {
                    it.copy(
                        isSending = false,
                        errorMessage = e.localizedMessage ?: "Chat request failed.",
                    )
                }
            }
        }
    }

    private fun ensureChatSession(excludeTrailingCount: Int = 0): Chat {
        val state = _uiState.value
        if (chatSession != null && sessionModel == state.selectedModel && sessionRole == state.selectedRole) {
            return chatSession!!
        }
        return rebuildSession(state.messages.dropLast(excludeTrailingCount))
    }

    private fun rebuildSessionFromHistory() {
        val completed = _uiState.value.messages.filter { !it.isStreaming && it.text.isNotBlank() }
        if (completed.isNotEmpty()) {
            rebuildSession(completed)
        } else {
            chatSession = null
        }
    }

    private fun rebuildSession(historyMessages: List<ChatMessage>): Chat {
        val state = _uiState.value
        val model = Firebase.ai.generativeModel(
            modelName = state.selectedModel.modelId,
            systemInstruction = content { text(state.selectedRole.systemPrompt) },
        )
        val history: List<Content> = historyMessages.map { msg ->
            content(role = if (msg.isUser) "user" else "model") {
                text(msg.text)
            }
        }
        val chat = model.startChat(history = history)
        chatSession = chat
        sessionModel = state.selectedModel
        sessionRole = state.selectedRole
        return chat
    }

    private fun updateAssistantMessage(id: String, text: String, isStreaming: Boolean) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == id) msg.copy(text = text, isStreaming = isStreaming) else msg
                },
            )
        }
    }

    private fun removeMessage(id: String) {
        _uiState.update { state ->
            state.copy(messages = state.messages.filterNot { it.id == id })
        }
    }

    private fun finalizeOrDropStream(id: String, partial: String) {
        if (partial.isNotBlank()) {
            updateAssistantMessage(id, partial, isStreaming = false)
        } else {
            removeMessage(id)
        }
    }
}
