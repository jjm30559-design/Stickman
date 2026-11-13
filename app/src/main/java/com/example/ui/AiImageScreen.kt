package com.example.ui

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CharacterStyle
import com.example.model.StickmanPose
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassPill
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.SoftPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AiImageScreenState
import com.example.viewmodel.AiImageViewModel
import com.example.viewmodel.ImageAspectOption
import com.example.viewmodel.ImageGenUiState
import com.example.viewmodel.ImageStudioMode

@Composable
fun AiImageScreen(
    state: AiImageScreenState,
    viewModel: AiImageViewModel,
    currentPose: StickmanPose,
    characterStyle: CharacterStyle,
    onApplyBackdrop: (Bitmap) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val accent = characterStyle.glowTheme.primaryColor

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            viewModel.loadImageFromUri(context, uri)
        }
    }

    val quickPrompts = if (state.mode == ImageStudioMode.CREATE) {
        listOf(
            "Neon cyber dojo rooftop at night, dramatic anime arena background",
            "Dynamic orange stickman martial artist fighting on a glowing bridge, 4k digital art",
            "Minimalist futuristic neon stage with volumetric light beams",
        )
    } else {
        listOf(
            "Transform this stickman into a dramatic anime action poster with glowing aura",
            "Add cinematic cyberpunk city lights and rain reflections behind the figure",
            "Make it look like a bold comic book cover with speed lines",
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.ai_art_title),
                        style = MaterialTheme.typography.displaySmall,
                        color = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.ai_art_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
                LiquidGlassPill(accentColor = CyanGlow) {
                    Text(
                        text = "NANO 2.1",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                    )
                }
            }

            // Controls Card
            LiquidGlassCard(cornerRadius = 24.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Create vs Edit Toggle
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ImageStudioMode.entries.forEach { mode ->
                            val selected = state.mode == mode
                            LiquidGlassButton(
                                onClick = { viewModel.setMode(mode) },
                                isSelected = selected,
                                accentColor = accent,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ai_image_mode_${mode.name.lowercase()}"),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (mode == ImageStudioMode.CREATE) Icons.Rounded.AutoAwesome else Icons.Rounded.Edit,
                                        contentDescription = null,
                                        tint = if (selected) accent else TextMuted,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${mode.label} Image",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = TextPrimary,
                                    )
                                }
                            }
                        }
                    }

                    // Edit Mode Input Source Selector
                    if (state.mode == ImageStudioMode.EDIT) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                LiquidGlassButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                        )
                                    },
                                    accentColor = SoftPurple,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("pick_photo_button"),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, tint = SoftPurple, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Pick Photo", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                                    }
                                }

                                LiquidGlassButton(
                                    onClick = { viewModel.captureStickmanPoseAsInput(currentPose, characterStyle) },
                                    accentColor = accent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("capture_pose_button"),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.AccessibilityNew, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Use Pose", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                                    }
                                }
                            }

                            if (state.inputBitmap != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF0A1024))
                                        .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                                ) {
                                    Image(
                                        bitmap = state.inputBitmap.asImageBitmap(),
                                        contentDescription = state.inputLabel ?: "Input image",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                    IconButton(
                                        onClick = viewModel::clearInputImage,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(48.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Remove input image",
                                            tint = TextPrimary,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Aspect Ratio Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Aspect",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                        )
                        ImageAspectOption.entries.forEach { option ->
                            val selected = state.aspectOption == option
                            LiquidGlassButton(
                                onClick = { viewModel.setAspectOption(option) },
                                isSelected = selected,
                                accentColor = accent,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("aspect_${option.name.lowercase()}"),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                            ) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) TextPrimary else TextSecondary,
                                )
                            }
                        }
                    }

                    // Prompt Field
                    OutlinedTextField(
                        value = state.prompt,
                        onValueChange = viewModel::updatePrompt,
                        label = {
                            Text(
                                if (state.mode == ImageStudioMode.CREATE) "Describe image to create"
                                else "Describe how to edit this image",
                            )
                        },
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accent,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.28f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = accent,
                            unfocusedLabelColor = TextMuted,
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_image_prompt_input"),
                    )

                    // Quick Prompt Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(quickPrompts) { sample ->
                            LiquidGlassButton(
                                onClick = { viewModel.updatePrompt(sample) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = sample.take(34) + if (sample.length > 34) "…" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                )
                            }
                        }
                    }

                    LiquidGlassButton(
                        onClick = viewModel::generateOrEditImage,
                        isPrimary = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_image_button"),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.mode == ImageStudioMode.CREATE) "Generate with Nano Banana 2.1"
                                else "Edit with Nano Banana 2.1",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary,
                            )
                        }
                    }
                }
            }

            // Output Card
            LiquidGlassCard(
                cornerRadius = 26.dp,
                highlighted = state.genState is ImageGenUiState.Success,
            ) {
                when (val gen = state.genState) {
                    is ImageGenUiState.Idle -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Image,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Generated artwork appears here",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                            )
                        }
                    }
                    is ImageGenUiState.Loading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CircularProgressIndicator(color = accent)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Synthesizing with Nano Banana 2.1 (may take 30–60s)...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                        }
                    }
                    is ImageGenUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "Generation Error",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFFF7A90),
                            )
                            Text(
                                text = gen.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                        }
                    }
                    is ImageGenUiState.Success -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Image(
                                bitmap = gen.bitmap.asImageBitmap(),
                                contentDescription = "AI generated artwork",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(270.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFF0A1024)),
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                LiquidGlassButton(
                                    onClick = { viewModel.setInputBitmap(gen.bitmap, "Generated Result") },
                                    accentColor = SoftPurple,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("edit_generated_button"),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Edit, contentDescription = null, tint = SoftPurple, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Edit Result", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                                    }
                                }
                                LiquidGlassButton(
                                    onClick = { onApplyBackdrop(gen.bitmap) },
                                    isSelected = true,
                                    accentColor = accent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("apply_backdrop_button"),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Wallpaper, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Set Stage BG", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
