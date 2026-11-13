package com.example.viewmodel

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.StudioPreferencesRepository
import com.example.model.AnimationPreset
import com.example.model.BodyPartVisibility
import com.example.model.CharacterStyle
import com.example.model.Keyframe
import com.example.model.PosePreset
import com.example.model.StickmanAccessory
import com.example.model.StickmanBodyStyle
import com.example.model.StickmanExpression
import com.example.model.StickmanGlowTheme
import com.example.model.StickmanJoint
import com.example.model.StickmanPose
import com.example.model.StickmanProp
import com.example.model.StickmanRigMath
import com.example.model.StrokeWeightOption
import com.example.model.StudioTab
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

data class StickManUiState(
    val characterStyle: CharacterStyle = CharacterStyle(),
    val keyframes: List<Keyframe> = defaultStarterFrames(),
    val selectedFrameIndex: Int = 0,
    val isPlaying: Boolean = false,
    val playbackPosition: Float = 0f,
    val fps: Float = 8f,
    val isLooping: Boolean = true,
    val onionSkinEnabled: Boolean = true,
    val showJointHandles: Boolean = true,
    val duoModeEnabled: Boolean = false,
    val stageBackdropBitmap: Bitmap? = null,
    val activeTab: StudioTab = StudioTab.BUILD,
    val activeJoint: StickmanJoint? = null,
    val activePresetTitle: String = AnimationPreset.FIGHT_COMBO.title,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
) {
    val activeFrame: Keyframe
        get() = keyframes.getOrElse(selectedFrameIndex) { keyframes.first() }

    val previousFrame: Keyframe?
        get() = if (selectedFrameIndex > 0) keyframes.getOrNull(selectedFrameIndex - 1)
        else if (keyframes.size > 1) keyframes.last() else null

    val displayPose: StickmanPose
        get() {
            if (!isPlaying || keyframes.size <= 1) return activeFrame.pose
            val count = keyframes.size
            val pos = playbackPosition.coerceIn(0f, count.toFloat())
            val indexA = pos.toInt().coerceIn(0, count - 1)
            val indexB = if (isLooping) (indexA + 1) % count else (indexA + 1).coerceAtMost(count - 1)
            val fraction = pos - indexA
            return StickmanRigMath.lerpPose(
                keyframes[indexA].pose,
                keyframes[indexB].pose,
                fraction,
            )
        }
}

private fun defaultStarterFrames(): List<Keyframe> =
    AnimationPreset.FIGHT_COMBO.frames.mapIndexed { index, pose ->
        Keyframe(
            id = UUID.randomUUID().toString(),
            pose = pose,
            label = "F${index + 1}",
        )
    }

private data class HistorySnapshot(
    val characterStyle: CharacterStyle,
    val keyframes: List<Keyframe>,
    val selectedFrameIndex: Int,
)

class StickManViewModel(
    private val preferencesRepository: StudioPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StickManUiState())
    val uiState: StateFlow<StickManUiState> = _uiState.asStateFlow()

    private val undoStack = ArrayDeque<HistorySnapshot>()
    private val redoStack = ArrayDeque<HistorySnapshot>()
    private var playbackJob: Job? = null
    private var dragSnapshotTaken = false

    init {
        viewModelScope.launch {
            val prefs = preferencesRepository.preferencesFlow.first()
            _uiState.update { state ->
                state.copy(
                    characterStyle = state.characterStyle.copy(
                        bodyStyle = prefs.bodyStyle,
                        glowTheme = prefs.glowTheme,
                        strokeWeight = prefs.strokeWeight,
                        showFace = prefs.showFace,
                    ),
                    fps = prefs.fps,
                    onionSkinEnabled = prefs.onionSkinEnabled,
                    showJointHandles = prefs.showJointHandles,
                    isLooping = prefs.isLooping,
                )
            }
        }
    }

    private fun pushHistory() {
        val current = _uiState.value
        undoStack.addLast(
            HistorySnapshot(
                characterStyle = current.characterStyle,
                keyframes = current.keyframes,
                selectedFrameIndex = current.selectedFrameIndex,
            ),
        )
        if (undoStack.size > 30) undoStack.removeFirst()
        redoStack.clear()
        syncHistoryFlags()
    }

    private fun syncHistoryFlags() {
        _uiState.update {
            it.copy(
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
            )
        }
    }

    fun undo() {
        val prev = undoStack.removeLastOrNull() ?: return
        stopPlayback()
        val current = _uiState.value
        redoStack.addLast(
            HistorySnapshot(
                characterStyle = current.characterStyle,
                keyframes = current.keyframes,
                selectedFrameIndex = current.selectedFrameIndex,
            ),
        )
        _uiState.update {
            it.copy(
                characterStyle = prev.characterStyle,
                keyframes = prev.keyframes,
                selectedFrameIndex = prev.selectedFrameIndex.coerceIn(0, prev.keyframes.lastIndex),
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
            )
        }
    }

    fun redo() {
        val next = redoStack.removeLastOrNull() ?: return
        stopPlayback()
        val current = _uiState.value
        undoStack.addLast(
            HistorySnapshot(
                characterStyle = current.characterStyle,
                keyframes = current.keyframes,
                selectedFrameIndex = current.selectedFrameIndex,
            ),
        )
        _uiState.update {
            it.copy(
                characterStyle = next.characterStyle,
                keyframes = next.keyframes,
                selectedFrameIndex = next.selectedFrameIndex.coerceIn(0, next.keyframes.lastIndex),
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
            )
        }
    }

    fun selectTab(tab: StudioTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun selectBodyStyle(bodyStyle: StickmanBodyStyle) {
        pushHistory()
        val defaultShowFace = when (bodyStyle) {
            StickmanBodyStyle.ANIMATOR_HOLLOW -> false
            StickmanBodyStyle.STORYTIME -> true
            else -> _uiState.value.characterStyle.showFace
        }
        _uiState.update {
            it.copy(
                characterStyle = it.characterStyle.copy(
                    bodyStyle = bodyStyle,
                    showFace = defaultShowFace,
                ),
            )
        }
        viewModelScope.launch {
            preferencesRepository.setBodyStyle(bodyStyle)
            preferencesRepository.setShowFace(defaultShowFace)
        }
    }

    fun toggleShowFace() {
        pushHistory()
        val next = !_uiState.value.characterStyle.showFace
        _uiState.update {
            it.copy(characterStyle = it.characterStyle.copy(showFace = next))
        }
        viewModelScope.launch { preferencesRepository.setShowFace(next) }
    }

    fun selectProp(prop: StickmanProp) {
        pushHistory()
        _uiState.update {
            it.copy(characterStyle = it.characterStyle.copy(prop = prop))
        }
    }

    fun toggleDuoMode() {
        _uiState.update { it.copy(duoModeEnabled = !it.duoModeEnabled) }
    }

    fun setStageBackdrop(bitmap: Bitmap?) {
        _uiState.update { it.copy(stageBackdropBitmap = bitmap) }
    }

    fun toggleBodyPart(part: String) {
        pushHistory()
        _uiState.update { state ->
            val v = state.characterStyle.visibility
            val updated = when (part) {
                "head" -> v.copy(head = !v.head)
                "body" -> v.copy(body = !v.body)
                "arms" -> v.copy(arms = !v.arms)
                "legs" -> v.copy(legs = !v.legs)
                else -> v
            }
            state.copy(characterStyle = state.characterStyle.copy(visibility = updated))
        }
    }

    fun selectExpression(expression: StickmanExpression) {
        pushHistory()
        _uiState.update {
            it.copy(
                characterStyle = it.characterStyle.copy(
                    expression = expression,
                    showFace = true,
                ),
            )
        }
        viewModelScope.launch { preferencesRepository.setShowFace(true) }
    }

    fun selectAccessory(accessory: StickmanAccessory) {
        pushHistory()
        _uiState.update {
            it.copy(characterStyle = it.characterStyle.copy(accessory = accessory))
        }
    }

    fun selectGlowTheme(theme: StickmanGlowTheme) {
        pushHistory()
        _uiState.update {
            it.copy(characterStyle = it.characterStyle.copy(glowTheme = theme))
        }
        viewModelScope.launch { preferencesRepository.setGlowTheme(theme) }
    }

    fun selectStrokeWeight(weight: StrokeWeightOption) {
        pushHistory()
        _uiState.update {
            it.copy(characterStyle = it.characterStyle.copy(strokeWeight = weight))
        }
        viewModelScope.launch { preferencesRepository.setStrokeWeight(weight) }
    }

    fun selectFrame(index: Int) {
        stopPlayback()
        _uiState.update {
            val clamped = index.coerceIn(0, it.keyframes.lastIndex)
            it.copy(selectedFrameIndex = clamped, playbackPosition = clamped.toFloat())
        }
    }

    fun addKeyframe() {
        stopPlayback()
        pushHistory()
        _uiState.update { state ->
            val insertAt = state.selectedFrameIndex + 1
            val currentPose = state.activeFrame.pose
            val updated = state.keyframes.toMutableList().apply {
                add(
                    insertAt,
                    Keyframe(
                        id = UUID.randomUUID().toString(),
                        pose = currentPose,
                        label = "F${size + 1}",
                    ),
                )
            }.mapIndexed { idx, kf -> kf.copy(label = "F${idx + 1}") }
            state.copy(
                keyframes = updated,
                selectedFrameIndex = insertAt,
                playbackPosition = insertAt.toFloat(),
            )
        }
    }

    fun duplicateSelectedFrame() {
        addKeyframe()
    }

    fun deleteSelectedFrame() {
        val state = _uiState.value
        if (state.keyframes.size <= 1) return
        stopPlayback()
        pushHistory()
        _uiState.update { current ->
            val removeIdx = current.selectedFrameIndex
            val remaining = current.keyframes.toMutableList().apply { removeAt(removeIdx) }
                .mapIndexed { idx, kf -> kf.copy(label = "F${idx + 1}") }
            val nextIdx = removeIdx.coerceAtMost(remaining.lastIndex)
            current.copy(
                keyframes = remaining,
                selectedFrameIndex = nextIdx,
                playbackPosition = nextIdx.toFloat(),
            )
        }
    }

    fun applyPosePreset(preset: PosePreset) {
        stopPlayback()
        pushHistory()
        updateActiveFramePose(preset.pose)
    }

    fun mirrorCurrentPose() {
        stopPlayback()
        pushHistory()
        updateActiveFramePose(_uiState.value.activeFrame.pose.mirrored())
    }

    fun resetCurrentFramePose() {
        stopPlayback()
        pushHistory()
        updateActiveFramePose(StickmanPose())
    }

    fun loadAnimationPreset(preset: AnimationPreset) {
        stopPlayback()
        pushHistory()
        val frames = preset.frames.mapIndexed { idx, pose ->
            Keyframe(
                id = UUID.randomUUID().toString(),
                pose = pose,
                label = "F${idx + 1}",
            )
        }
        _uiState.update {
            it.copy(
                keyframes = frames,
                selectedFrameIndex = 0,
                playbackPosition = 0f,
                fps = preset.recommendedFps,
                activePresetTitle = preset.title,
            )
        }
        viewModelScope.launch { preferencesRepository.setFps(preset.recommendedFps) }
    }

    fun onJointDragStart(joint: StickmanJoint) {
        stopPlayback()
        if (!dragSnapshotTaken) {
            pushHistory()
            dragSnapshotTaken = true
        }
        _uiState.update { it.copy(activeJoint = joint) }
    }

    fun onJointDrag(
        joint: StickmanJoint,
        pointerPx: Offset,
        canvasWidth: Float,
        canvasHeight: Float,
    ) {
        val currentPose = _uiState.value.activeFrame.pose
        val bodyStyle = _uiState.value.characterStyle.bodyStyle
        val updatedPose = StickmanRigMath.applyJointDrag(
            pose = currentPose,
            joint = joint,
            pointerPx = pointerPx,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            bodyStyle = bodyStyle,
        )
        updateActiveFramePose(updatedPose)
    }

    fun onJointDragEnd() {
        dragSnapshotTaken = false
        _uiState.update { it.copy(activeJoint = null) }
    }

    private fun updateActiveFramePose(pose: StickmanPose) {
        _uiState.update { state ->
            val idx = state.selectedFrameIndex
            val updatedList = state.keyframes.toMutableList()
            updatedList[idx] = updatedList[idx].copy(pose = pose)
            state.copy(keyframes = updatedList)
        }
    }

    fun togglePlayback() {
        if (_uiState.value.isPlaying) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        if (_uiState.value.keyframes.size <= 1) return
        playbackJob?.cancel()
        _uiState.update {
            it.copy(
                isPlaying = true,
                activeJoint = null,
                playbackPosition = it.selectedFrameIndex.toFloat(),
            )
        }
        playbackJob = viewModelScope.launch {
            var lastTimeNanos = System.nanoTime()
            while (isActive && _uiState.value.isPlaying) {
                delay(16L)
                val now = System.nanoTime()
                val dt = ((now - lastTimeNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                lastTimeNanos = now

                val state = _uiState.value
                val count = state.keyframes.size
                if (count <= 1) {
                    stopPlayback()
                    break
                }
                val nextPos = state.playbackPosition + dt * state.fps
                if (state.isLooping) {
                    val wrapped = nextPos % count.toFloat()
                    _uiState.update {
                        it.copy(
                            playbackPosition = wrapped,
                            selectedFrameIndex = wrapped.toInt().coerceIn(0, count - 1),
                        )
                    }
                } else {
                    if (nextPos >= (count - 1).toFloat()) {
                        _uiState.update {
                            it.copy(
                                isPlaying = false,
                                playbackPosition = (count - 1).toFloat(),
                                selectedFrameIndex = count - 1,
                            )
                        }
                        break
                    } else {
                        _uiState.update {
                            it.copy(
                                playbackPosition = nextPos,
                                selectedFrameIndex = nextPos.toInt().coerceIn(0, count - 1),
                            )
                        }
                    }
                }
            }
        }
    }

    fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun setFps(fps: Float) {
        _uiState.update { it.copy(fps = fps) }
        viewModelScope.launch { preferencesRepository.setFps(fps) }
    }

    fun toggleOnionSkin() {
        val next = !_uiState.value.onionSkinEnabled
        _uiState.update { it.copy(onionSkinEnabled = next) }
        viewModelScope.launch { preferencesRepository.setOnionSkin(next) }
    }

    fun toggleShowJointHandles() {
        val next = !_uiState.value.showJointHandles
        _uiState.update { it.copy(showJointHandles = next) }
        viewModelScope.launch { preferencesRepository.setShowJoints(next) }
    }

    fun toggleLoop() {
        val next = !_uiState.value.isLooping
        _uiState.update { it.copy(isLooping = next) }
        viewModelScope.launch { preferencesRepository.setLooping(next) }
    }

    fun resetStudio() {
        stopPlayback()
        pushHistory()
        _uiState.update { state ->
            state.copy(
                characterStyle = state.characterStyle.copy(
                    visibility = BodyPartVisibility(),
                    expression = StickmanExpression.FOCUSED,
                    accessory = StickmanAccessory.HEADBAND,
                    prop = StickmanProp.NONE,
                    showFace = true,
                ),
                duoModeEnabled = false,
                keyframes = defaultStarterFrames(),
                selectedFrameIndex = 0,
                playbackPosition = 0f,
                activePresetTitle = AnimationPreset.FIGHT_COMBO.title,
            )
        }
    }

    class Factory(
        private val preferencesRepository: StudioPreferencesRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StickManViewModel(preferencesRepository) as T
        }
    }
}
