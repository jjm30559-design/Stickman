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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CharacterStyle
import com.example.model.Keyframe
import com.example.model.StickmanRigMath
import com.example.ui.theme.SoftPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StudioTimelineBar(
    keyframes: List<Keyframe>,
    selectedIndex: Int,
    isPlaying: Boolean,
    fps: Float,
    characterStyle: CharacterStyle,
    onSelectFrame: (Int) -> Unit,
    onAddFrame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex) {
        if (selectedIndex in keyframes.indices) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "ANIMATION TIMELINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
                Text(
                    text = "${keyframes.size} FRAMES • ${fps.toInt()} FPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = characterStyle.glowTheme.primaryColor,
                )
            }

            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                itemsIndexed(keyframes, key = { _, kf -> kf.id }) { index, frame ->
                    val isSelected = index == selectedIndex
                    val accent = if (isPlaying && isSelected) SoftPurple else characterStyle.glowTheme.primaryColor
                    val shape = RoundedCornerShape(14.dp)

                    Box(
                        modifier = Modifier
                            .size(width = 58.dp, height = 68.dp)
                            .clip(shape)
                            .background(
                                if (isSelected) {
                                    Brush.linearGradient(
                                        listOf(accent.copy(alpha = 0.25f), Color.White.copy(alpha = 0.08f)),
                                    )
                                } else {
                                    Brush.linearGradient(
                                        listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.03f)),
                                    )
                                },
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) accent else Color.White.copy(alpha = 0.18f),
                                shape = shape,
                            )
                            .clickable(role = Role.Button) { onSelectFrame(index) }
                            .testTag("timeline_frame_$index")
                            .padding(4.dp),
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 12.dp),
                        ) {
                            val skel = StickmanRigMath.computeSkeleton(
                                pose = frame.pose,
                                canvasWidth = size.width,
                                canvasHeight = size.height,
                                bodyStyle = characterStyle.bodyStyle,
                            )
                            drawStickman(
                                skeleton = skel,
                                pose = frame.pose,
                                style = characterStyle,
                                isGhost = false,
                            )
                        }
                        Text(
                            text = frame.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) TextPrimary else TextMuted,
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }
                }

                item(key = "add_frame_btn") {
                    val addShape = RoundedCornerShape(14.dp)
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 54.dp, minHeight = 68.dp)
                            .size(width = 54.dp, height = 68.dp)
                            .clip(addShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, characterStyle.glowTheme.primaryColor.copy(alpha = 0.45f), addShape)
                            .clickable(role = Role.Button, onClick = onAddFrame)
                            .testTag("timeline_add_frame_button"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = stringResource(R.string.action_add_frame),
                                tint = characterStyle.glowTheme.primaryColor,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "ADD",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}
