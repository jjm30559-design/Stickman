package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.DeepIndigoOrb
import com.example.ui.theme.DeepPurpleOrb
import com.example.ui.theme.DeepSpaceBottom
import com.example.ui.theme.DeepSpaceMid
import com.example.ui.theme.DeepSpaceNavy
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassBorderSoft
import com.example.ui.theme.GlassBottomTint
import com.example.ui.theme.GlassMid
import com.example.ui.theme.GlassTopHighlight
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.SoftPurple
import com.example.ui.theme.VioletAccent

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(DeepSpaceNavy, DeepSpaceMid, DeepSpaceBottom),
                ),
            )
            .drawBehind {
                // Top-left indigo atmospheric orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(DeepIndigoOrb.copy(alpha = 0.65f), Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.10f),
                        radius = size.maxDimension * 0.42f,
                    ),
                    center = Offset(size.width * 0.15f, size.height * 0.10f),
                    radius = size.maxDimension * 0.42f,
                )
                // Top-right deep purple orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(DeepPurpleOrb.copy(alpha = 0.55f), Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.24f),
                        radius = size.maxDimension * 0.38f,
                    ),
                    center = Offset(size.width * 0.88f, size.height * 0.24f),
                    radius = size.maxDimension * 0.38f,
                )
                // Lower-left soft cyan glass glow orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyanGlow.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(size.width * 0.04f, size.height * 0.78f),
                        radius = size.maxDimension * 0.30f,
                    ),
                    center = Offset(size.width * 0.04f, size.height * 0.78f),
                    radius = size.maxDimension * 0.30f,
                )
                // Mid-right soft violet glow orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(SoftPurple.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(size.width * 0.96f, size.height * 0.52f),
                        radius = size.maxDimension * 0.32f,
                    ),
                    center = Offset(size.width * 0.96f, size.height * 0.52f),
                    radius = size.maxDimension * 0.32f,
                )
            },
        content = content,
    )
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    highlighted: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val borderBrush = if (highlighted) {
        Brush.linearGradient(listOf(GlassBorderCyan, GlassBorderSoft))
    } else {
        Brush.linearGradient(listOf(GlassBorderLight, GlassBorderSoft, GlassBorderSoft.copy(alpha = 0.18f)))
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(GlassTopHighlight, GlassMid, GlassBottomTint),
                ),
            )
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun LiquidGlassPill(
    modifier: Modifier = Modifier,
    accentColor: Color = CyanGlow,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(50)
    val clickModifier = if (onClick != null) {
        Modifier
            .defaultMinSize(minHeight = 40.dp)
            .clickable(
                role = Role.Button,
                onClick = onClick,
            )
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.06f),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.42f),
                shape = shape,
            )
            .then(clickModifier)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    isSelected: Boolean = false,
    accentColor: Color = CyanGlow,
    shape: Shape = RoundedCornerShape(16.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = 700f),
        label = "glassButtonScale",
    )

    val bgBrush = when {
        isPrimary -> Brush.linearGradient(
            colors = listOf(IndigoAccent.copy(alpha = 0.85f), VioletAccent.copy(alpha = 0.80f)),
        )
        isSelected -> Brush.linearGradient(
            colors = listOf(accentColor.copy(alpha = 0.28f), Color.White.copy(alpha = 0.09f)),
        )
        else -> Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.05f)),
        )
    }

    val border = when {
        isPrimary -> BorderStroke(1.dp, Color(0x99C8E3FF))
        isSelected -> BorderStroke(1.2.dp, accentColor.copy(alpha = 0.78f))
        else -> BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
    }

    Surface(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = accentColor),
                role = Role.Button,
                onClick = onClick,
            ),
        shape = shape,
        color = Color.Transparent,
        border = border,
    ) {
        Box(
            modifier = Modifier
                .background(bgBrush)
                .padding(contentPadding),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}
