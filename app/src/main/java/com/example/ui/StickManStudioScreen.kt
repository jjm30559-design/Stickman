package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.LiquidGlassPill
import com.example.ui.components.StickmanCanvasStage
import com.example.ui.components.StudioControlDock
import com.example.ui.components.StudioTimelineBar
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.StickManUiState
import com.example.viewmodel.StickManViewModel

@Composable
fun StickManStudioScreen(
    uiState: StickManUiState,
    viewModel: StickManViewModel,
    modifier: Modifier = Modifier,
) {
    val accent = uiState.characterStyle.glowTheme.primaryColor

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        val isWide = maxWidth >= 680.dp

        if (isWide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    StudioHeaderBar(
                        accentColor = accent,
                        canUndo = uiState.canUndo,
                        canRedo = uiState.canRedo,
                        onUndo = viewModel::undo,
                        onRedo = viewModel::redo,
                    )
                    HeroTitleBlock()
                    StickmanCanvasStage(
                        pose = uiState.displayPose,
                        previousPose = uiState.previousFrame?.pose,
                        characterStyle = uiState.characterStyle,
                        isPlaying = uiState.isPlaying,
                        onionSkinEnabled = uiState.onionSkinEnabled,
                        showJointHandles = uiState.showJointHandles,
                        duoModeEnabled = uiState.duoModeEnabled,
                        stageBackdropBitmap = uiState.stageBackdropBitmap,
                        activeJoint = uiState.activeJoint,
                        selectedFrameNumber = uiState.selectedFrameIndex + 1,
                        totalFrames = uiState.keyframes.size,
                        onJointDragStart = viewModel::onJointDragStart,
                        onJointDrag = viewModel::onJointDrag,
                        onJointDragEnd = viewModel::onJointDragEnd,
                        onTogglePlay = viewModel::togglePlayback,
                        onToggleOnionSkin = viewModel::toggleOnionSkin,
                        onToggleJoints = viewModel::toggleShowJointHandles,
                        onToggleDuo = viewModel::toggleDuoMode,
                        onClearBackdrop = { viewModel.setStageBackdrop(null) },
                        onReset = viewModel::resetStudio,
                    )
                    StudioTimelineBar(
                        keyframes = uiState.keyframes,
                        selectedIndex = uiState.selectedFrameIndex,
                        isPlaying = uiState.isPlaying,
                        fps = uiState.fps,
                        characterStyle = uiState.characterStyle,
                        onSelectFrame = viewModel::selectFrame,
                        onAddFrame = viewModel::addKeyframe,
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(0.95f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    StudioControlDock(
                        uiState = uiState,
                        onSelectTab = viewModel::selectTab,
                        onSelectBodyStyle = viewModel::selectBodyStyle,
                        onSelectProp = viewModel::selectProp,
                        onToggleShowFace = viewModel::toggleShowFace,
                        onToggleDuoMode = viewModel::toggleDuoMode,
                        onTogglePart = viewModel::toggleBodyPart,
                        onSelectExpression = viewModel::selectExpression,
                        onSelectAccessory = viewModel::selectAccessory,
                        onSelectGlowTheme = viewModel::selectGlowTheme,
                        onSelectStrokeWeight = viewModel::selectStrokeWeight,
                        onApplyPosePreset = viewModel::applyPosePreset,
                        onMirrorPose = viewModel::mirrorCurrentPose,
                        onResetPose = viewModel::resetCurrentFramePose,
                        onToggleJointHandles = viewModel::toggleShowJointHandles,
                        onAddFrame = viewModel::addKeyframe,
                        onDuplicateFrame = viewModel::duplicateSelectedFrame,
                        onDeleteFrame = viewModel::deleteSelectedFrame,
                        onSetFps = viewModel::setFps,
                        onToggleLoop = viewModel::toggleLoop,
                        onToggleOnionSkin = viewModel::toggleOnionSkin,
                        onLoadAnimationPreset = viewModel::loadAnimationPreset,
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 540.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    StudioHeaderBar(
                        accentColor = accent,
                        canUndo = uiState.canUndo,
                        canRedo = uiState.canRedo,
                        onUndo = viewModel::undo,
                        onRedo = viewModel::redo,
                    )
                    HeroTitleBlock()
                    StickmanCanvasStage(
                        pose = uiState.displayPose,
                        previousPose = uiState.previousFrame?.pose,
                        characterStyle = uiState.characterStyle,
                        isPlaying = uiState.isPlaying,
                        onionSkinEnabled = uiState.onionSkinEnabled,
                        showJointHandles = uiState.showJointHandles,
                        duoModeEnabled = uiState.duoModeEnabled,
                        stageBackdropBitmap = uiState.stageBackdropBitmap,
                        activeJoint = uiState.activeJoint,
                        selectedFrameNumber = uiState.selectedFrameIndex + 1,
                        totalFrames = uiState.keyframes.size,
                        onJointDragStart = viewModel::onJointDragStart,
                        onJointDrag = viewModel::onJointDrag,
                        onJointDragEnd = viewModel::onJointDragEnd,
                        onTogglePlay = viewModel::togglePlayback,
                        onToggleOnionSkin = viewModel::toggleOnionSkin,
                        onToggleJoints = viewModel::toggleShowJointHandles,
                        onToggleDuo = viewModel::toggleDuoMode,
                        onClearBackdrop = { viewModel.setStageBackdrop(null) },
                        onReset = viewModel::resetStudio,
                    )
                    StudioTimelineBar(
                        keyframes = uiState.keyframes,
                        selectedIndex = uiState.selectedFrameIndex,
                        isPlaying = uiState.isPlaying,
                        fps = uiState.fps,
                        characterStyle = uiState.characterStyle,
                        onSelectFrame = viewModel::selectFrame,
                        onAddFrame = viewModel::addKeyframe,
                    )
                    StudioControlDock(
                        uiState = uiState,
                        onSelectTab = viewModel::selectTab,
                        onSelectBodyStyle = viewModel::selectBodyStyle,
                        onSelectProp = viewModel::selectProp,
                        onToggleShowFace = viewModel::toggleShowFace,
                        onToggleDuoMode = viewModel::toggleDuoMode,
                        onTogglePart = viewModel::toggleBodyPart,
                        onSelectExpression = viewModel::selectExpression,
                        onSelectAccessory = viewModel::selectAccessory,
                        onSelectGlowTheme = viewModel::selectGlowTheme,
                        onSelectStrokeWeight = viewModel::selectStrokeWeight,
                        onApplyPosePreset = viewModel::applyPosePreset,
                        onMirrorPose = viewModel::mirrorCurrentPose,
                        onResetPose = viewModel::resetCurrentFramePose,
                        onToggleJointHandles = viewModel::toggleShowJointHandles,
                        onAddFrame = viewModel::addKeyframe,
                        onDuplicateFrame = viewModel::duplicateSelectedFrame,
                        onDeleteFrame = viewModel::deleteSelectedFrame,
                        onSetFps = viewModel::setFps,
                        onToggleLoop = viewModel::toggleLoop,
                        onToggleOnionSkin = viewModel::toggleOnionSkin,
                        onLoadAnimationPreset = viewModel::loadAnimationPreset,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun StudioHeaderBar(
    accentColor: Color,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = TextPrimary, fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.brand_stickman))
                    append(" ")
                }
                withStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.brand_studio))
                }
            },
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                letterSpacing = 0.6.sp,
            ),
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onUndo,
                enabled = canUndo,
                modifier = Modifier
                    .size(48.dp)
                    .alpha(if (canUndo) 1f else 0.35f)
                    .testTag("undo_button"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Undo,
                    contentDescription = stringResource(R.string.cd_undo),
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            IconButton(
                onClick = onRedo,
                enabled = canRedo,
                modifier = Modifier
                    .size(48.dp)
                    .alpha(if (canRedo) 1f else 0.35f)
                    .testTag("redo_button"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Redo,
                    contentDescription = stringResource(R.string.cd_redo),
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            LiquidGlassPill(accentColor = CyanGlow) {
                Text(
                    text = stringResource(R.string.badge_liquid_glass),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFD9FAFF),
                )
            }
        }
    }
}

@Composable
private fun HeroTitleBlock() {
    Column(modifier = Modifier.padding(horizontal = 2.dp)) {
        Text(
            text = stringResource(R.string.hero_title),
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.hero_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
    }
}
