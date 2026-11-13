package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.SoftPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AiChatUiState
import com.example.viewmodel.AiChatViewModel
import com.example.viewmodel.ChatMessage
import com.example.viewmodel.ChatModelOption
import com.example.viewmodel.ChatRolePreset

@Composable
fun AiChatScreen(
    state: AiChatUiState,
    viewModel: AiChatViewModel,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.text?.length) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Header + Clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.ai_chat_title),
                        style = MaterialTheme.typography.displaySmall,
                        color = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.ai_chat_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                IconButton(
                    onClick = viewModel::clearChat,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("clear_chat_button"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteSweep,
                        contentDescription = "Clear conversation",
                        tint = TextSecondary,
                    )
                }
            }

            // Role & Model Selector Dock
            LiquidGlassCard(
                cornerRadius = 20.dp,
                contentPadding = PaddingValues(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ChatRolePreset.entries) { role ->
                            val selected = role == state.selectedRole
                            LiquidGlassButton(
                                onClick = { viewModel.selectRole(role) },
                                isSelected = selected,
                                accentColor = SoftPurple,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("chat_role_${role.name.lowercase()}"),
                            ) {
                                Text(
                                    text = "Role: ${role.label}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) TextPrimary else TextSecondary,
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ChatModelOption.entries.forEach { modelOption ->
                            val selected = modelOption == state.selectedModel
                            LiquidGlassButton(
                                onClick = { viewModel.selectModel(modelOption) },
                                isSelected = selected,
                                accentColor = accentColor,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_model_${modelOption.name.lowercase()}"),
                            ) {
                                Text(
                                    text = modelOption.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) TextPrimary else TextMuted,
                                )
                            }
                        }
                    }
                }
            }

            // Scrollable Conversation Thread
            LiquidGlassCard(
                cornerRadius = 24.dp,
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                if (state.messages.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = CyanGlow,
                            modifier = Modifier.size(32.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ask your ${state.selectedRole.label} anything",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.selectedRole.starterPrompts.forEachIndexed { idx, prompt ->
                                LiquidGlassButton(
                                    onClick = { viewModel.sendMessage(prompt) },
                                    accentColor = accentColor,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("starter_prompt_$idx"),
                                ) {
                                    Text(
                                        text = prompt,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("chat_message_list"),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.messages, key = { it.id }) { msg ->
                            ChatBubble(
                                message = msg,
                                accentColor = accentColor,
                                roleLabel = state.selectedRole.label,
                            )
                        }
                    }
                }
            }

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF7A90),
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }

            // Message Input Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = state.inputText,
                    onValueChange = viewModel::updateInput,
                    placeholder = { Text("Message Gemini (${state.selectedModel.label})...") },
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.28f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedPlaceholderColor = TextMuted,
                        unfocusedPlaceholderColor = TextMuted,
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                )

                LiquidGlassButton(
                    onClick = { viewModel.sendMessage() },
                    isPrimary = true,
                    shape = CircleShape,
                    contentPadding = PaddingValues(14.dp),
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("chat_send_button"),
                ) {
                    if (state.isSending) {
                        CircularProgressIndicator(
                            color = TextPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send message",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    accentColor: Color,
    roleLabel: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
    ) {
        val bubbleShape = RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = if (message.isUser) 18.dp else 4.dp,
            bottomEnd = if (message.isUser) 4.dp else 18.dp,
        )
        Box(
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(bubbleShape)
                .background(
                    if (message.isUser) {
                        Brush.linearGradient(
                            listOf(accentColor.copy(alpha = 0.30f), Color.White.copy(alpha = 0.12f)),
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.14f), SoftPurple.copy(alpha = 0.10f)),
                        )
                    },
                )
                .border(
                    width = 1.dp,
                    color = if (message.isUser) accentColor.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.20f),
                    shape = bubbleShape,
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Column {
                if (!message.isUser) {
                    Text(
                        text = if (message.isStreaming) "$roleLabel • typing…" else roleLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftPurple,
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                }
                Text(
                    text = message.text.ifEmpty { "…" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                )
            }
        }
    }
}
