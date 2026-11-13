package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Adjust
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CharacterStyle
import com.example.model.StickmanAccessory
import com.example.model.StickmanBodyStyle
import com.example.model.StickmanExpression
import com.example.model.StickmanGlowTheme
import com.example.model.StickmanJoint
import com.example.model.StickmanPose
import com.example.model.StickmanProp
import com.example.model.StickmanRigMath
import com.example.model.StickmanSkeletonPoints
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.SoftPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun StickmanCanvasStage(
    pose: StickmanPose,
    previousPose: StickmanPose?,
    characterStyle: CharacterStyle,
    isPlaying: Boolean,
    onionSkinEnabled: Boolean,
    showJointHandles: Boolean,
    duoModeEnabled: Boolean,
    stageBackdropBitmap: Bitmap?,
    activeJoint: StickmanJoint?,
    selectedFrameNumber: Int,
    totalFrames: Int,
    onJointDragStart: (StickmanJoint) -> Unit,
    onJointDrag: (StickmanJoint, Offset, Float, Float) -> Unit,
    onJointDragEnd: () -> Unit,
    onTogglePlay: () -> Unit,
    onToggleOnionSkin: () -> Unit,
    onToggleJoints: () -> Unit,
    onToggleDuo: () -> Unit,
    onClearBackdrop: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stageDesc = stringResource(R.string.cd_stickman_stage)
    val density = LocalDensity.current
    val hitRadiusPx = with(density) { 38.dp.toPx() }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val latestPose by rememberUpdatedState(pose)
    val latestStyle by rememberUpdatedState(characterStyle)
    val latestIsPlaying by rememberUpdatedState(isPlaying)
    val latestShowJoints by rememberUpdatedState(showJointHandles)
    val latestDuo by rememberUpdatedState(duoModeEnabled)

    LiquidGlassCard(
        modifier = modifier,
        cornerRadius = 30.dp,
        highlighted = isPlaying || activeJoint != null,
        contentPadding = PaddingValues(14.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Stage Top Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${characterStyle.bodyStyle.label.uppercase()} • PREVIEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
                LiquidGlassPill(
                    accentColor = if (isPlaying) SoftPurple else characterStyle.glowTheme.primaryColor,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isPlaying) SoftPurple else characterStyle.glowTheme.primaryColor,
                                ),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlaying) {
                                stringResource(R.string.stage_playing)
                            } else {
                                stringResource(R.string.stage_frame_fmt, selectedFrameNumber, totalFrames)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                        )
                    }
                }
            }

            // Interactive Canvas Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(258.dp)
                    .padding(top = 6.dp)
                    .semantics { contentDescription = stageDesc },
            ) {
                if (stageBackdropBitmap != null) {
                    Image(
                        bitmap = stageBackdropBitmap.asImageBitmap(),
                        contentDescription = "AI stage backdrop",
                        contentScale = ContentScale.Crop,
                        alpha = 0.48f,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp)),
                    )
                    LiquidGlassPill(
                        accentColor = SoftPurple,
                        onClick = onClearBackdrop,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .testTag("clear_backdrop_pill"),
                    ) {
                        Text(
                            text = "Clear BG",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                        )
                    }
                }
                var grabbedJoint by remember { mutableStateOf<StickmanJoint?>(null) }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("stickman_stage_canvas")
                        .onSizeChanged { canvasSize = it }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { startOffset ->
                                    if (latestIsPlaying || !latestShowJoints) return@detectDragGestures
                                    val w = canvasSize.width.toFloat()
                                    val h = canvasSize.height.toFloat()
                                    if (w <= 0f || h <= 0f) return@detectDragGestures
                                    val displayOffsetPose = if (latestDuo) {
                                        latestPose.copy(hipX = latestPose.hipX - 36f)
                                    } else {
                                        latestPose
                                    }
                                    val skel = StickmanRigMath.computeSkeleton(
                                        pose = displayOffsetPose,
                                        canvasWidth = w,
                                        canvasHeight = h,
                                        bodyStyle = latestStyle.bodyStyle,
                                    )
                                    val nearest = findNearestJoint(
                                        pointer = startOffset,
                                        skeleton = skel,
                                        style = latestStyle,
                                        maxDistPx = hitRadiusPx,
                                    )
                                    grabbedJoint = nearest
                                    if (nearest != null) {
                                        onJointDragStart(nearest)
                                    }
                                },
                                onDrag = { change, _ ->
                                    val joint = grabbedJoint ?: return@detectDragGestures
                                    change.consume()
                                    val w = canvasSize.width.toFloat()
                                    val h = canvasSize.height.toFloat()
                                    if (w > 0f && h > 0f) {
                                        val adjustedPointer = if (latestDuo) {
                                            val scale = minOf(w / StickmanRigMath.STAGE_WIDTH, h / StickmanRigMath.STAGE_HEIGHT)
                                            Offset(change.position.x + 36f * scale, change.position.y)
                                        } else {
                                            change.position
                                        }
                                        onJointDrag(joint, adjustedPointer, w, h)
                                    }
                                },
                                onDragEnd = {
                                    if (grabbedJoint != null) {
                                        grabbedJoint = null
                                        onJointDragEnd()
                                    }
                                },
                                onDragCancel = {
                                    if (grabbedJoint != null) {
                                        grabbedJoint = null
                                        onJointDragEnd()
                                    }
                                },
                            )
                        },
                ) {
                    drawStageGrid(characterStyle.glowTheme.primaryColor)

                    val mainPose = if (duoModeEnabled) pose.copy(hipX = pose.hipX - 36f) else pose
                    val skel = StickmanRigMath.computeSkeleton(
                        pose = mainPose,
                        canvasWidth = size.width,
                        canvasHeight = size.height,
                        bodyStyle = characterStyle.bodyStyle,
                    )

                    // Floor glow beneath feet
                    val floorY = maxOf(skel.leftFoot.y, skel.rightFoot.y) + 12f
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                characterStyle.glowTheme.primaryColor.copy(alpha = 0.28f),
                                Color.Transparent,
                            ),
                            center = Offset(skel.hip.x, floorY.coerceIn(size.height * 0.55f, size.height * 0.92f)),
                            radius = size.minDimension * 0.34f,
                        ),
                        topLeft = Offset(
                            skel.hip.x - size.minDimension * 0.34f,
                            floorY.coerceIn(size.height * 0.55f, size.height * 0.92f) - 16f,
                        ),
                        size = Size(size.minDimension * 0.68f, 32f),
                    )

                    // Onion skin silhouette for previous frame
                    if (!isPlaying && onionSkinEnabled && previousPose != null) {
                        val ghostPose = if (duoModeEnabled) previousPose.copy(hipX = previousPose.hipX - 36f) else previousPose
                        val ghostSkel = StickmanRigMath.computeSkeleton(
                            pose = ghostPose,
                            canvasWidth = size.width,
                            canvasHeight = size.height,
                            bodyStyle = characterStyle.bodyStyle,
                        )
                        drawStickman(
                            skeleton = ghostSkel,
                            pose = ghostPose,
                            style = characterStyle,
                            isGhost = true,
                        )
                    }

                    // Rival / Co-Star Stickman when VS Duo is enabled
                    if (duoModeEnabled) {
                        val rivalTheme = if (characterStyle.glowTheme == StickmanGlowTheme.YT_AZURE) {
                            StickmanGlowTheme.YT_CRIMSON
                        } else {
                            StickmanGlowTheme.YT_AZURE
                        }
                        val rivalPose = pose.mirrored().copy(hipX = 42f - pose.hipX * 0.35f)
                        val rivalStyle = characterStyle.copy(
                            glowTheme = rivalTheme,
                            accessory = StickmanAccessory.NONE,
                            prop = if (characterStyle.prop == StickmanProp.ENERGY_SWORD) {
                                StickmanProp.ENERGY_SWORD
                            } else {
                                StickmanProp.NONE
                            },
                        )
                        val rivalSkel = StickmanRigMath.computeSkeleton(
                            pose = rivalPose,
                            canvasWidth = size.width,
                            canvasHeight = size.height,
                            bodyStyle = rivalStyle.bodyStyle,
                        )
                        drawOval(
                            brush = Brush.radialGradient(
                                colors = listOf(rivalTheme.primaryColor.copy(alpha = 0.22f), Color.Transparent),
                                center = Offset(rivalSkel.hip.x, floorY.coerceIn(size.height * 0.55f, size.height * 0.92f)),
                                radius = size.minDimension * 0.28f,
                            ),
                            topLeft = Offset(
                                rivalSkel.hip.x - size.minDimension * 0.28f,
                                floorY.coerceIn(size.height * 0.55f, size.height * 0.92f) - 14f,
                            ),
                            size = Size(size.minDimension * 0.56f, 28f),
                        )
                        drawStickman(
                            skeleton = rivalSkel,
                            pose = rivalPose,
                            style = rivalStyle,
                            isGhost = false,
                        )
                    }

                    // Active stickman
                    drawStickman(
                        skeleton = skel,
                        pose = mainPose,
                        style = characterStyle,
                        isGhost = false,
                    )

                    // Draggable joint handles
                    if (!isPlaying && showJointHandles) {
                        drawJointHandles(
                            skeleton = skel,
                            style = characterStyle,
                            activeJoint = activeJoint,
                        )
                    }
                }
            }

            // Stage Transport & Quick Toggles Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LiquidGlassButton(
                    onClick = onTogglePlay,
                    isPrimary = true,
                    modifier = Modifier
                        .weight(1.25f)
                        .testTag("play_pause_button"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = stringResource(R.string.cd_play_pause),
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(if (isPlaying) R.string.action_pause else R.string.action_play),
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                        )
                    }
                }

                LiquidGlassButton(
                    onClick = onToggleDuo,
                    isSelected = duoModeEnabled,
                    accentColor = characterStyle.glowTheme.primaryColor,
                    modifier = Modifier
                        .weight(0.68f)
                        .testTag("duo_vs_toggle"),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Groups,
                        contentDescription = stringResource(R.string.toggle_duo_vs),
                        tint = if (duoModeEnabled) characterStyle.glowTheme.primaryColor else TextMuted,
                        modifier = Modifier.size(19.dp),
                    )
                }

                LiquidGlassButton(
                    onClick = onToggleOnionSkin,
                    isSelected = onionSkinEnabled,
                    accentColor = SoftPurple,
                    modifier = Modifier
                        .weight(0.68f)
                        .testTag("onion_skin_toggle"),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Layers,
                        contentDescription = stringResource(R.string.toggle_onion_skin),
                        tint = if (onionSkinEnabled) SoftPurple else TextMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }

                LiquidGlassButton(
                    onClick = onToggleJoints,
                    isSelected = showJointHandles,
                    accentColor = characterStyle.glowTheme.primaryColor,
                    modifier = Modifier
                        .weight(0.68f)
                        .testTag("joint_handles_toggle"),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Adjust,
                        contentDescription = stringResource(R.string.toggle_joint_handles),
                        tint = if (showJointHandles) characterStyle.glowTheme.primaryColor else TextMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }

                LiquidGlassButton(
                    onClick = onReset,
                    modifier = Modifier
                        .weight(0.85f)
                        .testTag("reset_button"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.cd_reset),
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = stringResource(R.string.action_reset),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawStageGrid(accent: Color) {
    val gridColor = accent.copy(alpha = 0.06f)
    val step = size.minDimension / 6f
    var x = step
    while (x < size.width) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
        x += step
    }
    var y = step
    while (y < size.height) {
        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        y += step
    }
    val baselineY = size.height * 0.84f
    drawLine(
        brush = Brush.horizontalGradient(
            listOf(Color.Transparent, accent.copy(alpha = 0.25f), Color.Transparent),
        ),
        start = Offset(size.width * 0.12f, baselineY),
        end = Offset(size.width * 0.88f, baselineY),
        strokeWidth = 1.5f,
    )
}

fun DrawScope.drawStickman(
    skeleton: StickmanSkeletonPoints,
    pose: StickmanPose,
    style: CharacterStyle,
    isGhost: Boolean = false,
) {
    val baseStroke = (size.minDimension * style.strokeWeight.widthRatio).coerceAtLeast(2.4f)
    val styleMultiplier = when (style.bodyStyle) {
        StickmanBodyStyle.ANIMATOR_HOLLOW -> 1.18f
        StickmanBodyStyle.YOUTUBE_SOLID -> 1.12f
        StickmanBodyStyle.FIGHTER_TAPERED -> 1.05f
        StickmanBodyStyle.STORYTIME -> 1.04f
        StickmanBodyStyle.NEON_WIRE -> 1.0f
    }
    val strokePx = baseStroke * styleMultiplier
    val glowWidth = strokePx * 2.25f
    val mainLineColor = if (isGhost) SoftPurple.copy(alpha = 0.26f) else style.glowTheme.lineColor
    val solidFillColor = if (isGhost) SoftPurple.copy(alpha = 0.22f) else style.glowTheme.primaryColor
    val glowColor = if (isGhost) SoftPurple.copy(alpha = 0.10f) else style.glowTheme.primaryColor.copy(alpha = 0.28f)

    fun drawLimbSegment(start: Offset, end: Offset, startRadius: Float, endRadius: Float) {
        if (style.bodyStyle == StickmanBodyStyle.FIGHTER_TAPERED) {
            drawTaperedBone(
                start = start,
                end = end,
                startRadius = startRadius,
                endRadius = endRadius,
                fillColor = solidFillColor,
                highlightColor = mainLineColor,
                isGhost = isGhost,
            )
        } else {
            if (!isGhost) {
                drawLine(
                    color = glowColor,
                    start = start,
                    end = end,
                    strokeWidth = glowWidth,
                    cap = StrokeCap.Round,
                )
            }
            drawLine(
                color = if (style.bodyStyle == StickmanBodyStyle.STORYTIME) Color(0xFFF3F6FF) else mainLineColor,
                start = start,
                end = end,
                strokeWidth = strokePx,
                cap = StrokeCap.Round,
            )
        }
    }

    // Legs
    if (style.visibility.legs) {
        drawLimbSegment(skeleton.hip, skeleton.leftKnee, strokePx * 0.68f, strokePx * 0.52f)
        drawLimbSegment(skeleton.leftKnee, skeleton.leftFoot, strokePx * 0.52f, strokePx * 0.36f)
        drawLimbSegment(skeleton.hip, skeleton.rightKnee, strokePx * 0.68f, strokePx * 0.52f)
        drawLimbSegment(skeleton.rightKnee, skeleton.rightFoot, strokePx * 0.52f, strokePx * 0.36f)
    }

    // Torso
    if (style.visibility.body) {
        drawLimbSegment(skeleton.hip, skeleton.neckTop, strokePx * 0.64f, strokePx * 0.85f)
    }

    // Arms
    if (style.visibility.arms) {
        drawLimbSegment(skeleton.shoulder, skeleton.leftElbow, strokePx * 0.56f, strokePx * 0.44f)
        drawLimbSegment(skeleton.leftElbow, skeleton.leftHand, strokePx * 0.44f, strokePx * 0.34f)
        drawLimbSegment(skeleton.shoulder, skeleton.rightElbow, strokePx * 0.56f, strokePx * 0.44f)
        drawLimbSegment(skeleton.rightElbow, skeleton.rightHand, strokePx * 0.44f, strokePx * 0.34f)

        // Held Prop in right hand
        if (!isGhost && style.prop != StickmanProp.NONE) {
            drawHandProp(
                handPos = skeleton.rightHand,
                forearmAngleDeg = pose.rightForearmAngle,
                prop = style.prop,
                scaleRef = skeleton.headRadius,
                accentColor = style.glowTheme.primaryColor,
                lineColor = mainLineColor,
            )
        }
    }

    // Head + Expression + Accessory
    if (style.visibility.head) {
        val headStroke = when (style.bodyStyle) {
            StickmanBodyStyle.ANIMATOR_HOLLOW -> strokePx * 1.15f
            StickmanBodyStyle.STORYTIME -> strokePx * 0.72f
            else -> strokePx * 0.90f
        }

        if (!isGhost) {
            drawCircle(
                color = glowColor,
                radius = skeleton.headRadius,
                center = skeleton.headCenter,
                style = Stroke(width = headStroke * 2.2f),
            )
        }

        when (style.bodyStyle) {
            StickmanBodyStyle.YOUTUBE_SOLID, StickmanBodyStyle.FIGHTER_TAPERED -> {
                drawCircle(
                    color = solidFillColor,
                    radius = skeleton.headRadius,
                    center = skeleton.headCenter,
                )
                drawCircle(
                    color = mainLineColor.copy(alpha = if (isGhost) 0.2f else 0.7f),
                    radius = skeleton.headRadius,
                    center = skeleton.headCenter,
                    style = Stroke(width = (headStroke * 0.45f).coerceAtLeast(1.5f)),
                )
            }
            StickmanBodyStyle.STORYTIME -> {
                drawCircle(
                    color = if (isGhost) SoftPurple.copy(alpha = 0.16f) else Color(0xFFF6F9FF),
                    radius = skeleton.headRadius,
                    center = skeleton.headCenter,
                )
                drawCircle(
                    color = if (isGhost) mainLineColor else style.glowTheme.primaryColor,
                    radius = skeleton.headRadius,
                    center = skeleton.headCenter,
                    style = Stroke(width = headStroke),
                )
            }
            StickmanBodyStyle.ANIMATOR_HOLLOW, StickmanBodyStyle.NEON_WIRE -> {
                drawCircle(
                    color = mainLineColor,
                    radius = skeleton.headRadius,
                    center = skeleton.headCenter,
                    style = Stroke(width = headStroke),
                )
            }
        }

        if (!isGhost) {
            val totalHeadAngle = pose.torsoAngle + pose.headTilt
            rotate(degrees = totalHeadAngle, pivot = skeleton.headCenter) {
                if (style.showFace) {
                    val faceColor = when (style.bodyStyle) {
                        StickmanBodyStyle.STORYTIME -> Color(0xFF101833)
                        StickmanBodyStyle.YOUTUBE_SOLID, StickmanBodyStyle.FIGHTER_TAPERED -> Color(0xFF0A1024)
                        else -> mainLineColor
                    }
                    val faceAccent = when (style.bodyStyle) {
                        StickmanBodyStyle.STORYTIME -> style.glowTheme.primaryColor
                        StickmanBodyStyle.YOUTUBE_SOLID, StickmanBodyStyle.FIGHTER_TAPERED -> Color(0xFFFFFFFF)
                        else -> style.glowTheme.primaryColor
                    }
                    drawExpression(
                        center = skeleton.headCenter,
                        radius = skeleton.headRadius,
                        expression = style.expression,
                        color = faceColor,
                        accent = faceAccent,
                    )
                }
                drawAccessory(
                    center = skeleton.headCenter,
                    radius = skeleton.headRadius,
                    accessory = style.accessory,
                    lineColor = mainLineColor,
                    accentColor = style.glowTheme.primaryColor,
                )
            }
        }
    }
}

private fun DrawScope.drawTaperedBone(
    start: Offset,
    end: Offset,
    startRadius: Float,
    endRadius: Float,
    fillColor: Color,
    highlightColor: Color,
    isGhost: Boolean,
) {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val len = hypot(dx, dy).coerceAtLeast(1f)
    val nx = -dy / len
    val ny = dx / len

    val path = Path().apply {
        moveTo(start.x + nx * startRadius, start.y + ny * startRadius)
        lineTo(end.x + nx * endRadius, end.y + ny * endRadius)
        lineTo(end.x - nx * endRadius, end.y - ny * endRadius)
        lineTo(start.x - nx * startRadius, start.y - ny * startRadius)
        close()
    }
    if (!isGhost) {
        drawPath(path, fillColor.copy(alpha = 0.25f), style = Stroke(width = startRadius * 1.4f))
    }
    drawPath(path, fillColor)
    drawCircle(fillColor, startRadius, start)
    drawCircle(fillColor, endRadius, end)
    if (!isGhost) {
        drawLine(
            color = highlightColor.copy(alpha = 0.45f),
            start = start,
            end = end,
            strokeWidth = (endRadius * 0.6f).coerceAtLeast(1.5f),
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawHandProp(
    handPos: Offset,
    forearmAngleDeg: Float,
    prop: StickmanProp,
    scaleRef: Float,
    accentColor: Color,
    lineColor: Color,
) {
    val u = scaleRef.coerceAtLeast(6f)
    rotate(degrees = -forearmAngleDeg, pivot = handPos) {
        when (prop) {
            StickmanProp.NONE -> Unit
            StickmanProp.ENERGY_SWORD -> {
                // Hilt
                drawLine(
                    color = Color(0xFFD8E4FF),
                    start = Offset(handPos.x, handPos.y - u * 0.45f),
                    end = Offset(handPos.x, handPos.y + u * 0.25f),
                    strokeWidth = (u * 0.20f).coerceAtLeast(2f),
                    cap = StrokeCap.Round,
                )
                // Crossguard
                drawLine(
                    color = Color(0xFFFFD479),
                    start = Offset(handPos.x - u * 0.42f, handPos.y + u * 0.22f),
                    end = Offset(handPos.x + u * 0.42f, handPos.y + u * 0.22f),
                    strokeWidth = (u * 0.16f).coerceAtLeast(2f),
                    cap = StrokeCap.Round,
                )
                // Glowing Blade
                drawLine(
                    color = accentColor.copy(alpha = 0.35f),
                    start = Offset(handPos.x, handPos.y + u * 0.26f),
                    end = Offset(handPos.x, handPos.y + u * 3.1f),
                    strokeWidth = (u * 0.50f).coerceAtLeast(4f),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = Color.White,
                    start = Offset(handPos.x, handPos.y + u * 0.26f),
                    end = Offset(handPos.x, handPos.y + u * 3.05f),
                    strokeWidth = (u * 0.18f).coerceAtLeast(2f),
                    cap = StrokeCap.Round,
                )
            }
            StickmanProp.BO_STAFF -> {
                drawLine(
                    color = Color(0xFFFFD479).copy(alpha = 0.3f),
                    start = Offset(handPos.x - u * 1.9f, handPos.y),
                    end = Offset(handPos.x + u * 2.3f, handPos.y),
                    strokeWidth = (u * 0.38f).coerceAtLeast(3f),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = Color(0xFFFFE5A3),
                    start = Offset(handPos.x - u * 1.9f, handPos.y),
                    end = Offset(handPos.x + u * 2.3f, handPos.y),
                    strokeWidth = (u * 0.18f).coerceAtLeast(2f),
                    cap = StrokeCap.Round,
                )
            }
            StickmanProp.ENERGY_ORB -> {
                val orbCenter = Offset(handPos.x, handPos.y + u * 0.48f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, accentColor, Color.Transparent),
                        center = orbCenter,
                        radius = u * 0.95f,
                    ),
                    radius = u * 0.95f,
                    center = orbCenter,
                )
            }
            StickmanProp.MIC -> {
                drawLine(
                    color = Color(0xFF9FB2D9),
                    start = Offset(handPos.x, handPos.y - u * 0.25f),
                    end = Offset(handPos.x, handPos.y + u * 0.95f),
                    strokeWidth = (u * 0.18f).coerceAtLeast(2f),
                    cap = StrokeCap.Round,
                )
                drawCircle(
                    color = accentColor,
                    radius = u * 0.34f,
                    center = Offset(handPos.x, handPos.y + u * 1.10f),
                )
            }
            StickmanProp.YT_SIGN -> {
                val boxTop = handPos.y + u * 0.25f
                drawRoundRect(
                    color = Color(0xFFFF3D57),
                    topLeft = Offset(handPos.x - u * 0.85f, boxTop),
                    size = Size(u * 1.7f, u * 1.15f),
                    cornerRadius = CornerRadius(u * 0.28f),
                )
                val tri = Path().apply {
                    moveTo(handPos.x - u * 0.22f, boxTop + u * 0.28f)
                    lineTo(handPos.x + u * 0.34f, boxTop + u * 0.58f)
                    lineTo(handPos.x - u * 0.22f, boxTop + u * 0.88f)
                    close()
                }
                drawPath(tri, Color.White)
            }
        }
    }
}

private fun DrawScope.drawExpression(
    center: Offset,
    radius: Float,
    expression: StickmanExpression,
    color: Color,
    accent: Color,
) {
    val eyeOffsetX = radius * 0.34f
    val eyeY = center.y - radius * 0.10f
    val eyeRadius = (radius * 0.105f).coerceAtLeast(1.5f)
    val detailStroke = (radius * 0.10f).coerceAtLeast(1.5f)

    when (expression) {
        StickmanExpression.HAPPY -> {
            drawCircle(color, eyeRadius, Offset(center.x - eyeOffsetX, eyeY))
            drawCircle(color, eyeRadius, Offset(center.x + eyeOffsetX, eyeY))
            val mouth = Path().apply {
                moveTo(center.x - radius * 0.28f, center.y + radius * 0.28f)
                quadraticTo(
                    center.x,
                    center.y + radius * 0.54f,
                    center.x + radius * 0.28f,
                    center.y + radius * 0.28f,
                )
            }
            drawPath(mouth, color, style = Stroke(width = detailStroke, cap = StrokeCap.Round))
        }
        StickmanExpression.WINK -> {
            drawCircle(color, eyeRadius, Offset(center.x - eyeOffsetX, eyeY))
            val wink = Path().apply {
                moveTo(center.x + eyeOffsetX - radius * 0.14f, eyeY + radius * 0.04f)
                quadraticTo(
                    center.x + eyeOffsetX,
                    eyeY - radius * 0.12f,
                    center.x + eyeOffsetX + radius * 0.14f,
                    eyeY + radius * 0.04f,
                )
            }
            drawPath(wink, color, style = Stroke(width = detailStroke, cap = StrokeCap.Round))
            val grin = Path().apply {
                moveTo(center.x - radius * 0.24f, center.y + radius * 0.30f)
                quadraticTo(
                    center.x + radius * 0.04f,
                    center.y + radius * 0.52f,
                    center.x + radius * 0.30f,
                    center.y + radius * 0.22f,
                )
            }
            drawPath(grin, color, style = Stroke(width = detailStroke, cap = StrokeCap.Round))
        }
        StickmanExpression.SURPRISED -> {
            drawCircle(color, eyeRadius * 1.25f, Offset(center.x - eyeOffsetX, eyeY))
            drawCircle(color, eyeRadius * 1.25f, Offset(center.x + eyeOffsetX, eyeY))
            drawCircle(
                color = color,
                radius = radius * 0.18f,
                center = Offset(center.x, center.y + radius * 0.34f),
                style = Stroke(width = detailStroke),
            )
        }
        StickmanExpression.COOL -> {
            val lensW = radius * 0.42f
            val lensH = radius * 0.25f
            drawRoundRect(
                color = color,
                topLeft = Offset(center.x - eyeOffsetX - lensW * 0.48f, eyeY - lensH * 0.5f),
                size = Size(lensW, lensH),
                cornerRadius = CornerRadius(lensH * 0.35f),
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(center.x + eyeOffsetX - lensW * 0.52f, eyeY - lensH * 0.5f),
                size = Size(lensW, lensH),
                cornerRadius = CornerRadius(lensH * 0.35f),
            )
            drawLine(
                color = accent,
                start = Offset(center.x - radius * 0.12f, eyeY - lensH * 0.1f),
                end = Offset(center.x + radius * 0.12f, eyeY - lensH * 0.1f),
                strokeWidth = detailStroke,
            )
            val smirk = Path().apply {
                moveTo(center.x - radius * 0.22f, center.y + radius * 0.32f)
                quadraticTo(
                    center.x + radius * 0.08f,
                    center.y + radius * 0.44f,
                    center.x + radius * 0.28f,
                    center.y + radius * 0.24f,
                )
            }
            drawPath(smirk, color, style = Stroke(width = detailStroke, cap = StrokeCap.Round))
        }
        StickmanExpression.FOCUSED -> {
            drawLine(
                color,
                Offset(center.x - eyeOffsetX - radius * 0.14f, eyeY - radius * 0.16f),
                Offset(center.x - eyeOffsetX + radius * 0.12f, eyeY - radius * 0.05f),
                strokeWidth = detailStroke,
                cap = StrokeCap.Round,
            )
            drawLine(
                color,
                Offset(center.x + eyeOffsetX + radius * 0.14f, eyeY - radius * 0.16f),
                Offset(center.x + eyeOffsetX - radius * 0.12f, eyeY - radius * 0.05f),
                strokeWidth = detailStroke,
                cap = StrokeCap.Round,
            )
            drawCircle(color, eyeRadius, Offset(center.x - eyeOffsetX, eyeY + radius * 0.06f))
            drawCircle(color, eyeRadius, Offset(center.x + eyeOffsetX, eyeY + radius * 0.06f))
            drawLine(
                color,
                Offset(center.x - radius * 0.20f, center.y + radius * 0.34f),
                Offset(center.x + radius * 0.20f, center.y + radius * 0.34f),
                strokeWidth = detailStroke,
                cap = StrokeCap.Round,
            )
        }
        StickmanExpression.STARRY -> {
            listOf(-eyeOffsetX, eyeOffsetX).forEach { ox ->
                val ex = center.x + ox
                val s = radius * 0.16f
                drawLine(accent, Offset(ex - s, eyeY), Offset(ex + s, eyeY), strokeWidth = detailStroke, cap = StrokeCap.Round)
                drawLine(accent, Offset(ex, eyeY - s), Offset(ex, eyeY + s), strokeWidth = detailStroke, cap = StrokeCap.Round)
            }
            val smile = Path().apply {
                moveTo(center.x - radius * 0.26f, center.y + radius * 0.28f)
                quadraticTo(
                    center.x,
                    center.y + radius * 0.56f,
                    center.x + radius * 0.26f,
                    center.y + radius * 0.28f,
                )
            }
            drawPath(smile, color, style = Stroke(width = detailStroke, cap = StrokeCap.Round))
        }
    }
}

private fun DrawScope.drawAccessory(
    center: Offset,
    radius: Float,
    accessory: StickmanAccessory,
    lineColor: Color,
    accentColor: Color,
) {
    val strokeW = (radius * 0.12f).coerceAtLeast(1.8f)
    when (accessory) {
        StickmanAccessory.NONE -> Unit
        StickmanAccessory.HEADBAND -> {
            val bandY = center.y - radius * 0.42f
            drawLine(
                color = Color(0xFFFF3E5B),
                start = Offset(center.x - radius * 0.96f, bandY),
                end = Offset(center.x + radius * 0.96f, bandY),
                strokeWidth = strokeW * 1.55f,
                cap = StrokeCap.Round,
            )
            // Flowing headband ribbons behind left side
            val ribbon1 = Path().apply {
                moveTo(center.x - radius * 0.94f, bandY)
                quadraticTo(
                    center.x - radius * 1.45f,
                    bandY - radius * 0.20f,
                    center.x - radius * 1.62f,
                    bandY + radius * 0.16f,
                )
            }
            val ribbon2 = Path().apply {
                moveTo(center.x - radius * 0.92f, bandY + strokeW * 0.5f)
                quadraticTo(
                    center.x - radius * 1.38f,
                    bandY + radius * 0.18f,
                    center.x - radius * 1.48f,
                    bandY + radius * 0.52f,
                )
            }
            drawPath(ribbon1, Color(0xFFFF3E5B), style = Stroke(width = strokeW * 0.95f, cap = StrokeCap.Round))
            drawPath(ribbon2, Color(0xFFFF7087), style = Stroke(width = strokeW * 0.85f, cap = StrokeCap.Round))
        }
        StickmanAccessory.CROWN -> {
            val baseY = center.y - radius * 0.92f
            val path = Path().apply {
                moveTo(center.x - radius * 0.55f, baseY)
                lineTo(center.x - radius * 0.65f, baseY - radius * 0.55f)
                lineTo(center.x - radius * 0.25f, baseY - radius * 0.25f)
                lineTo(center.x, baseY - radius * 0.70f)
                lineTo(center.x + radius * 0.25f, baseY - radius * 0.25f)
                lineTo(center.x + radius * 0.65f, baseY - radius * 0.55f)
                lineTo(center.x + radius * 0.55f, baseY)
                close()
            }
            drawPath(path, Color(0xFFFFD479).copy(alpha = 0.34f))
            drawPath(
                path,
                Color(0xFFFFD479),
                style = Stroke(width = strokeW, join = StrokeJoin.Round, cap = StrokeCap.Round),
            )
        }
        StickmanAccessory.CAP -> {
            val dome = Path().apply {
                moveTo(center.x - radius * 0.92f, center.y - radius * 0.45f)
                quadraticTo(
                    center.x,
                    center.y - radius * 1.45f,
                    center.x + radius * 0.92f,
                    center.y - radius * 0.45f,
                )
                close()
            }
            drawPath(dome, accentColor.copy(alpha = 0.30f))
            drawPath(dome, lineColor, style = Stroke(width = strokeW, join = StrokeJoin.Round))
            drawLine(
                color = accentColor,
                start = Offset(center.x - radius * 0.95f, center.y - radius * 0.45f),
                end = Offset(center.x + radius * 1.38f, center.y - radius * 0.40f),
                strokeWidth = strokeW * 1.25f,
                cap = StrokeCap.Round,
            )
        }
        StickmanAccessory.HEADPHONES -> {
            val arcPath = Path().apply {
                moveTo(center.x - radius * 1.05f, center.y - radius * 0.05f)
                quadraticTo(
                    center.x,
                    center.y - radius * 1.65f,
                    center.x + radius * 1.05f,
                    center.y - radius * 0.05f,
                )
            }
            drawPath(arcPath, accentColor, style = Stroke(width = strokeW * 1.2f, cap = StrokeCap.Round))
            drawRoundRect(
                color = accentColor,
                topLeft = Offset(center.x - radius * 1.28f, center.y - radius * 0.28f),
                size = Size(radius * 0.32f, radius * 0.62f),
                cornerRadius = CornerRadius(radius * 0.16f),
            )
            drawRoundRect(
                color = accentColor,
                topLeft = Offset(center.x + radius * 0.96f, center.y - radius * 0.28f),
                size = Size(radius * 0.32f, radius * 0.62f),
                cornerRadius = CornerRadius(radius * 0.16f),
            )
        }
        StickmanAccessory.HALO -> {
            drawOval(
                color = Color(0xFFFFECC2),
                topLeft = Offset(center.x - radius * 0.72f, center.y - radius * 1.56f),
                size = Size(radius * 1.44f, radius * 0.40f),
                style = Stroke(width = strokeW),
            )
        }
        StickmanAccessory.WIZARD -> {
            val brimY = center.y - radius * 0.78f
            val hat = Path().apply {
                moveTo(center.x - radius * 0.72f, brimY)
                lineTo(center.x + radius * 0.12f, brimY - radius * 1.15f)
                lineTo(center.x + radius * 0.72f, brimY)
                close()
            }
            drawPath(hat, SoftPurple.copy(alpha = 0.34f))
            drawPath(hat, SoftPurple, style = Stroke(width = strokeW, join = StrokeJoin.Round))
            drawLine(
                color = lineColor,
                start = Offset(center.x - radius * 1.12f, brimY),
                end = Offset(center.x + radius * 1.12f, brimY),
                strokeWidth = strokeW * 1.2f,
                cap = StrokeCap.Round,
            )
        }
    }
}

private fun DrawScope.drawJointHandles(
    skeleton: StickmanSkeletonPoints,
    style: CharacterStyle,
    activeJoint: StickmanJoint?,
) {
    val handlePoints = buildList {
        add(StickmanJoint.HIP to skeleton.hip)
        if (style.visibility.head) add(StickmanJoint.HEAD to skeleton.headCenter)
        if (style.visibility.arms) {
            add(StickmanJoint.LEFT_ELBOW to skeleton.leftElbow)
            add(StickmanJoint.LEFT_HAND to skeleton.leftHand)
            add(StickmanJoint.RIGHT_ELBOW to skeleton.rightElbow)
            add(StickmanJoint.RIGHT_HAND to skeleton.rightHand)
        }
        if (style.visibility.legs) {
            add(StickmanJoint.LEFT_KNEE to skeleton.leftKnee)
            add(StickmanJoint.LEFT_FOOT to skeleton.leftFoot)
            add(StickmanJoint.RIGHT_KNEE to skeleton.rightKnee)
            add(StickmanJoint.RIGHT_FOOT to skeleton.rightFoot)
        }
    }

    handlePoints.forEach { (joint, pos) ->
        val isActive = joint == activeJoint
        val isEndEffector = joint == StickmanJoint.LEFT_HAND ||
            joint == StickmanJoint.RIGHT_HAND ||
            joint == StickmanJoint.LEFT_FOOT ||
            joint == StickmanJoint.RIGHT_FOOT ||
            joint == StickmanJoint.HEAD ||
            joint == StickmanJoint.HIP
        val ringColor = if (isActive) SoftPurple else Color.White
        val radius = if (isActive) 13f else if (isEndEffector) 9.5f else 7f

        if (isActive) {
            drawCircle(
                color = SoftPurple.copy(alpha = 0.35f),
                radius = radius * 1.85f,
                center = pos,
            )
        }
        drawCircle(
            color = Color(0xFF0A1026),
            radius = radius,
            center = pos,
        )
        drawCircle(
            color = ringColor,
            radius = radius,
            center = pos,
            style = Stroke(width = if (isActive) 3.2f else 2.2f),
        )
    }
}

private fun findNearestJoint(
    pointer: Offset,
    skeleton: StickmanSkeletonPoints,
    style: CharacterStyle,
    maxDistPx: Float,
): StickmanJoint? {
    val candidates = buildList {
        if (style.visibility.arms) {
            add(StickmanJoint.LEFT_HAND to skeleton.leftHand)
            add(StickmanJoint.RIGHT_HAND to skeleton.rightHand)
            add(StickmanJoint.LEFT_ELBOW to skeleton.leftElbow)
            add(StickmanJoint.RIGHT_ELBOW to skeleton.rightElbow)
        }
        if (style.visibility.legs) {
            add(StickmanJoint.LEFT_FOOT to skeleton.leftFoot)
            add(StickmanJoint.RIGHT_FOOT to skeleton.rightFoot)
            add(StickmanJoint.LEFT_KNEE to skeleton.leftKnee)
            add(StickmanJoint.RIGHT_KNEE to skeleton.rightKnee)
        }
        if (style.visibility.head) {
            add(StickmanJoint.HEAD to skeleton.headCenter)
        }
        add(StickmanJoint.HIP to skeleton.hip)
    }

    var bestJoint: StickmanJoint? = null
    var bestDist = maxDistPx
    for ((joint, pos) in candidates) {
        val dist = hypot(pointer.x - pos.x, pointer.y - pos.y)
        if (dist <= bestDist) {
            bestDist = dist
            bestJoint = joint
        }
    }
    return bestJoint
}
