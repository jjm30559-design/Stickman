package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Adjust
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LinearScale
import androidx.compose.material.icons.rounded.Loop
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AnimationPreset
import com.example.model.CharacterStyle
import com.example.model.PosePreset
import com.example.model.StickmanAccessory
import com.example.model.StickmanBodyStyle
import com.example.model.StickmanExpression
import com.example.model.StickmanGlowTheme
import com.example.model.StickmanPose
import com.example.model.StickmanProp
import com.example.model.StickmanRigMath
import com.example.model.StrokeWeightOption
import com.example.model.StudioTab
import com.example.ui.theme.SoftPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.StickManUiState

@Composable
fun StudioControlDock(
    uiState: StickManUiState,
    onSelectTab: (StudioTab) -> Unit,
    onSelectBodyStyle: (StickmanBodyStyle) -> Unit,
    onSelectProp: (StickmanProp) -> Unit,
    onToggleShowFace: () -> Unit,
    onToggleDuoMode: () -> Unit,
    onTogglePart: (String) -> Unit,
    onSelectExpression: (StickmanExpression) -> Unit,
    onSelectAccessory: (StickmanAccessory) -> Unit,
    onSelectGlowTheme: (StickmanGlowTheme) -> Unit,
    onSelectStrokeWeight: (StrokeWeightOption) -> Unit,
    onApplyPosePreset: (PosePreset) -> Unit,
    onMirrorPose: () -> Unit,
    onResetPose: () -> Unit,
    onToggleJointHandles: () -> Unit,
    onAddFrame: () -> Unit,
    onDuplicateFrame: () -> Unit,
    onDeleteFrame: () -> Unit,
    onSetFps: (Float) -> Unit,
    onToggleLoop: () -> Unit,
    onToggleOnionSkin: () -> Unit,
    onLoadAnimationPreset: (AnimationPreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = uiState.characterStyle.glowTheme.primaryColor

    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 26.dp,
        contentPadding = PaddingValues(16.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Segmented Tab Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StudioTab.entries.forEach { tab ->
                    val selected = tab == uiState.activeTab
                    val tabIcon = when (tab) {
                        StudioTab.BUILD -> Icons.Rounded.Build
                        StudioTab.POSE -> Icons.Rounded.Gesture
                        StudioTab.TIMELINE -> Icons.Rounded.Animation
                        StudioTab.PRESETS -> Icons.Rounded.MovieFilter
                    }
                    LiquidGlassButton(
                        onClick = { onSelectTab(tab) },
                        isSelected = selected,
                        accentColor = accent,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_${tab.name.lowercase()}"),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = tabIcon,
                                contentDescription = tab.label,
                                tint = if (selected) accent else TextMuted,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) TextPrimary else TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            when (uiState.activeTab) {
                StudioTab.BUILD -> BuildControlsPanel(
                    style = uiState.characterStyle,
                    onSelectBodyStyle = onSelectBodyStyle,
                    onSelectProp = onSelectProp,
                    onToggleShowFace = onToggleShowFace,
                    onTogglePart = onTogglePart,
                    onSelectExpression = onSelectExpression,
                    onSelectAccessory = onSelectAccessory,
                    onSelectGlowTheme = onSelectGlowTheme,
                    onSelectStrokeWeight = onSelectStrokeWeight,
                )
                StudioTab.POSE -> PoseControlsPanel(
                    accent = accent,
                    showJointHandles = uiState.showJointHandles,
                    duoModeEnabled = uiState.duoModeEnabled,
                    onApplyPosePreset = onApplyPosePreset,
                    onMirrorPose = onMirrorPose,
                    onResetPose = onResetPose,
                    onToggleJointHandles = onToggleJointHandles,
                    onToggleDuoMode = onToggleDuoMode,
                )
                StudioTab.TIMELINE -> TimelineControlsPanel(
                    uiState = uiState,
                    accent = accent,
                    onAddFrame = onAddFrame,
                    onDuplicateFrame = onDuplicateFrame,
                    onDeleteFrame = onDeleteFrame,
                    onSetFps = onSetFps,
                    onToggleLoop = onToggleLoop,
                    onToggleOnionSkin = onToggleOnionSkin,
                )
                StudioTab.PRESETS -> PresetsControlsPanel(
                    activePresetTitle = uiState.activePresetTitle,
                    style = uiState.characterStyle,
                    onLoadAnimationPreset = onLoadAnimationPreset,
                )
            }
        }
    }
}

@Composable
private fun BuildControlsPanel(
    style: CharacterStyle,
    onSelectBodyStyle: (StickmanBodyStyle) -> Unit,
    onSelectProp: (StickmanProp) -> Unit,
    onToggleShowFace: () -> Unit,
    onTogglePart: (String) -> Unit,
    onSelectExpression: (StickmanExpression) -> Unit,
    onSelectAccessory: (StickmanAccessory) -> Unit,
    onSelectGlowTheme: (StickmanGlowTheme) -> Unit,
    onSelectStrokeWeight: (StrokeWeightOption) -> Unit,
) {
    val accent = style.glowTheme.primaryColor
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Section: YouTube Stickman Style
        Column {
            SectionHeader(
                title = stringResource(R.string.section_yt_style),
                hint = style.bodyStyle.subtitle,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(StickmanBodyStyle.entries) { bodyStyle ->
                    val selected = bodyStyle == style.bodyStyle
                    LiquidGlassButton(
                        onClick = { onSelectBodyStyle(bodyStyle) },
                        isSelected = selected,
                        accentColor = accent,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("body_style_${bodyStyle.name.lowercase()}"),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Canvas(modifier = Modifier.size(22.dp)) {
                                val miniStyle = style.copy(
                                    bodyStyle = bodyStyle,
                                    accessory = StickmanAccessory.NONE,
                                    prop = StickmanProp.NONE,
                                    showFace = bodyStyle == StickmanBodyStyle.STORYTIME,
                                )
                                val skel = StickmanRigMath.computeSkeleton(
                                    pose = StickmanPose(),
                                    canvasWidth = size.width,
                                    canvasHeight = size.height,
                                    bodyStyle = bodyStyle,
                                )
                                drawStickman(skeleton = skel, pose = StickmanPose(), style = miniStyle)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = bodyStyle.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) TextPrimary else TextSecondary,
                            )
                        }
                    }
                }
            }
        }

        // Section: Hand Prop
        Column {
            SectionHeader(title = stringResource(R.string.section_prop), hint = style.prop.label)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(StickmanProp.entries) { prop ->
                    val selected = prop == style.prop
                    LiquidGlassButton(
                        onClick = { onSelectProp(prop) },
                        isSelected = selected,
                        accentColor = accent,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("prop_${prop.name.lowercase()}"),
                    ) {
                        Text(
                            text = prop.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) TextPrimary else TextSecondary,
                        )
                    }
                }
            }
        }

        // Section: Color Squad
        Column {
            SectionHeader(title = stringResource(R.string.section_glow_color), hint = style.glowTheme.label)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items(StickmanGlowTheme.entries) { theme ->
                    val selected = theme == style.glowTheme
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 42.dp, minHeight = 42.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(theme.primaryColor.copy(alpha = 0.25f))
                            .border(
                                width = if (selected) 2.5.dp else 1.dp,
                                color = if (selected) Color.White else theme.primaryColor.copy(alpha = 0.7f),
                                shape = CircleShape,
                            )
                            .clickable(role = Role.Button) { onSelectGlowTheme(theme) }
                            .testTag("glow_${theme.name.lowercase()}"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(theme.primaryColor),
                        )
                    }
                }
            }
        }

        // Section: Expression + Face Toggle
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.section_expression),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )
                LiquidGlassPill(
                    accentColor = if (style.showFace) accent else TextMuted,
                    onClick = onToggleShowFace,
                    modifier = Modifier.testTag("toggle_face_pill"),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (style.showFace) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            contentDescription = stringResource(R.string.toggle_face),
                            tint = if (style.showFace) accent else TextMuted,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (style.showFace) "Face On" else "Faceless",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(StickmanExpression.entries) { expr ->
                    val selected = style.showFace && expr == style.expression
                    LiquidGlassButton(
                        onClick = { onSelectExpression(expr) },
                        isSelected = selected,
                        accentColor = accent,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("expr_${expr.name.lowercase()}"),
                    ) {
                        Text(
                            text = expr.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) TextPrimary else TextSecondary,
                        )
                    }
                }
            }
        }

        // Section: Accessory
        Column {
            SectionHeader(title = stringResource(R.string.section_accessory), hint = style.accessory.label)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(StickmanAccessory.entries) { acc ->
                    val selected = acc == style.accessory
                    LiquidGlassButton(
                        onClick = { onSelectAccessory(acc) },
                        isSelected = selected,
                        accentColor = SoftPurple,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("accessory_${acc.name.lowercase()}"),
                    ) {
                        Text(
                            text = acc.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) TextPrimary else TextSecondary,
                        )
                    }
                }
            }
        }

        // Section: Body Parts
        Column {
            SectionHeader(
                title = stringResource(R.string.section_parts_title),
                hint = stringResource(R.string.section_parts_hint),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PartToggleTile(
                    label = stringResource(R.string.part_head),
                    icon = Icons.Rounded.Face,
                    enabled = style.visibility.head,
                    accent = accent,
                    onClick = { onTogglePart("head") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_part_head"),
                )
                PartToggleTile(
                    label = stringResource(R.string.part_body),
                    icon = Icons.Rounded.LinearScale,
                    enabled = style.visibility.body,
                    accent = accent,
                    onClick = { onTogglePart("body") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_part_body"),
                )
                PartToggleTile(
                    label = stringResource(R.string.part_arms),
                    icon = Icons.Rounded.AccessibilityNew,
                    enabled = style.visibility.arms,
                    accent = accent,
                    onClick = { onTogglePart("arms") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_part_arms"),
                )
                PartToggleTile(
                    label = stringResource(R.string.part_legs),
                    icon = Icons.Rounded.DirectionsWalk,
                    enabled = style.visibility.legs,
                    accent = accent,
                    onClick = { onTogglePart("legs") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_part_legs"),
                )
            }
        }

        // Section: Line Weight
        Column {
            SectionHeader(title = stringResource(R.string.section_stroke_weight), hint = style.strokeWeight.label)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StrokeWeightOption.entries.forEach { opt ->
                    val selected = opt == style.strokeWeight
                    LiquidGlassButton(
                        onClick = { onSelectStrokeWeight(opt) },
                        isSelected = selected,
                        accentColor = accent,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stroke_${opt.name.lowercase()}"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    ) {
                        Text(
                            text = opt.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) TextPrimary else TextSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PoseControlsPanel(
    accent: Color,
    showJointHandles: Boolean,
    duoModeEnabled: Boolean,
    onApplyPosePreset: (PosePreset) -> Unit,
    onMirrorPose: () -> Unit,
    onResetPose: () -> Unit,
    onToggleJointHandles: () -> Unit,
    onToggleDuoMode: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.12f))
                .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.pose_drag_hint),
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
            )
        }

        SectionHeader(title = stringResource(R.string.section_pose_presets), hint = "Applies to active frame")

        val rows = PosePreset.entries.chunked(3)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            rows.forEach { rowPresets ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowPresets.forEach { preset ->
                        LiquidGlassButton(
                            onClick = { onApplyPosePreset(preset) },
                            accentColor = accent,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pose_preset_${preset.name.lowercase()}"),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                        ) {
                            Text(
                                text = preset.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary,
                            )
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiquidGlassButton(
                onClick = onMirrorPose,
                accentColor = accent,
                modifier = Modifier
                    .weight(1f)
                    .testTag("mirror_pose_button"),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Flip, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(stringResource(R.string.action_mirror_pose), style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
            }
            LiquidGlassButton(
                onClick = onToggleDuoMode,
                isSelected = duoModeEnabled,
                accentColor = accent,
                modifier = Modifier
                    .weight(1f)
                    .testTag("pose_tab_duo_button"),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Groups,
                        contentDescription = null,
                        tint = if (duoModeEnabled) accent else TextMuted,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(stringResource(R.string.toggle_duo_vs), style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiquidGlassButton(
                onClick = onToggleJointHandles,
                isSelected = showJointHandles,
                accentColor = accent,
                modifier = Modifier
                    .weight(1f)
                    .testTag("pose_tab_joints_button"),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Adjust,
                        contentDescription = null,
                        tint = if (showJointHandles) accent else TextMuted,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Handles", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
            }
            LiquidGlassButton(
                onClick = onResetPose,
                modifier = Modifier
                    .weight(1f)
                    .testTag("reset_frame_pose_button"),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.RestartAlt, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stand", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun TimelineControlsPanel(
    uiState: StickManUiState,
    accent: Color,
    onAddFrame: () -> Unit,
    onDuplicateFrame: () -> Unit,
    onDeleteFrame: () -> Unit,
    onSetFps: (Float) -> Unit,
    onToggleLoop: () -> Unit,
    onToggleOnionSkin: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(
            title = "Keyframe Actions",
            hint = "Frame ${uiState.selectedFrameIndex + 1} of ${uiState.keyframes.size}",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiquidGlassButton(
                onClick = onAddFrame,
                isSelected = true,
                accentColor = accent,
                modifier = Modifier
                    .weight(1f)
                    .testTag("panel_add_frame_button"),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.action_add_frame), style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
            }
            LiquidGlassButton(
                onClick = onDuplicateFrame,
                modifier = Modifier
                    .weight(1f)
                    .testTag("duplicate_frame_button"),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clone", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
            }
            LiquidGlassButton(
                onClick = onDeleteFrame,
                modifier = Modifier
                    .weight(0.9f)
                    .alpha(if (uiState.keyframes.size > 1) 1f else 0.45f)
                    .testTag("delete_frame_button"),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.action_delete_frame), style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                }
            }
        }

        SectionHeader(title = stringResource(R.string.section_fps), hint = "${uiState.fps.toInt()} FPS")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(4f, 6f, 8f, 12f, 16f).forEach { fpsOption ->
                val selected = uiState.fps == fpsOption
                LiquidGlassButton(
                    onClick = { onSetFps(fpsOption) },
                    isSelected = selected,
                    accentColor = accent,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("fps_${fpsOption.toInt()}"),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = "${fpsOption.toInt()}x",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) TextPrimary else TextSecondary,
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiquidGlassButton(
                onClick = onToggleLoop,
                isSelected = uiState.isLooping,
                accentColor = accent,
                modifier = Modifier
                    .weight(1f)
                    .testTag("loop_toggle_button"),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Loop,
                        contentDescription = null,
                        tint = if (uiState.isLooping) accent else TextMuted,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${stringResource(R.string.toggle_loop)}: ${if (uiState.isLooping) "On" else "Off"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                    )
                }
            }

            LiquidGlassButton(
                onClick = onToggleOnionSkin,
                isSelected = uiState.onionSkinEnabled,
                accentColor = SoftPurple,
                modifier = Modifier
                    .weight(1f)
                    .testTag("panel_onion_toggle"),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Layers,
                        contentDescription = null,
                        tint = if (uiState.onionSkinEnabled) SoftPurple else TextMuted,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Onion: ${if (uiState.onionSkinEnabled) "On" else "Off"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetsControlsPanel(
    activePresetTitle: String,
    style: CharacterStyle,
    onLoadAnimationPreset: (AnimationPreset) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(
            title = stringResource(R.string.section_starter_animations),
            hint = stringResource(R.string.section_starter_hint),
        )
        AnimationPreset.entries.forEach { preset ->
            val selected = preset.title == activePresetTitle
            LiquidGlassButton(
                onClick = { onLoadAnimationPreset(preset) },
                isSelected = selected,
                accentColor = style.glowTheme.primaryColor,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("preset_${preset.name.lowercase()}"),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.06f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Canvas(modifier = Modifier.size(32.dp)) {
                                val pose = preset.frames.getOrElse(1) { preset.frames.first() }
                                val skel = StickmanRigMath.computeSkeleton(
                                    pose = pose,
                                    canvasWidth = size.width,
                                    canvasHeight = size.height,
                                    bodyStyle = style.bodyStyle,
                                )
                                drawStickman(skeleton = skel, pose = pose, style = style)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                            )
                            Text(
                                text = preset.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                            )
                        }
                    }

                    Text(
                        text = "${preset.frames.size}F • ${preset.recommendedFps.toInt()} FPS",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) style.glowTheme.primaryColor else TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun PartToggleTile(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LiquidGlassButton(
        onClick = onClick,
        isSelected = enabled,
        accentColor = accent,
        modifier = modifier.alpha(if (enabled) 1f else 0.52f),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) accent else TextMuted,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (enabled) TextPrimary else TextMuted,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, hint: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
        )
        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )
    }
}
