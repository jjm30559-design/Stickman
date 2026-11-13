package com.example.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CharacterStyle
import com.example.model.StickmanPose
import com.example.model.StickmanRigMath
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AspectRatio
import com.google.firebase.ai.type.ImageConfig
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ImageSize
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

private const val TAG = "AiImageViewModel"
private const val IMAGE_MODEL_NAME = "gemini-nano-banana-2.1"

enum class ImageAspectOption(val label: String, val aspectRatio: AspectRatio) {
    SQUARE("1:1", AspectRatio.SQUARE_1x1),
    LANDSCAPE("16:9", AspectRatio.LANDSCAPE_16x9),
    PORTRAIT("9:16", AspectRatio.PORTRAIT_9x16),
}

enum class ImageStudioMode(val label: String) {
    CREATE("Create"),
    EDIT("Edit"),
}

sealed interface ImageGenUiState {
    data object Idle : ImageGenUiState
    data object Loading : ImageGenUiState
    data class Success(val bitmap: Bitmap) : ImageGenUiState
    data class Error(val message: String) : ImageGenUiState
}

data class AiImageScreenState(
    val mode: ImageStudioMode = ImageStudioMode.CREATE,
    val aspectOption: ImageAspectOption = ImageAspectOption.SQUARE,
    val prompt: String = "",
    val inputBitmap: Bitmap? = null,
    val inputLabel: String? = null,
    val genState: ImageGenUiState = ImageGenUiState.Idle,
)

class AiImageViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AiImageScreenState())
    val uiState: StateFlow<AiImageScreenState> = _uiState.asStateFlow()

    fun setMode(mode: ImageStudioMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    fun setAspectOption(option: ImageAspectOption) {
        _uiState.update { it.copy(aspectOption = option) }
    }

    fun updatePrompt(prompt: String) {
        _uiState.update { it.copy(prompt = prompt) }
    }

    fun clearInputImage() {
        _uiState.update { it.copy(inputBitmap = null, inputLabel = null) }
    }

    fun setInputBitmap(bitmap: Bitmap, label: String) {
        _uiState.update {
            it.copy(
                mode = ImageStudioMode.EDIT,
                inputBitmap = downscaleIfNeeded(bitmap, 1024),
                inputLabel = label,
            )
        }
    }

    fun loadImageFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }.getOrNull()
            }
            if (loaded != null) {
                setInputBitmap(loaded, "Picked Photo")
            } else {
                _uiState.update {
                    it.copy(genState = ImageGenUiState.Error("Unable to read the selected image."))
                }
            }
        }
    }

    fun captureStickmanPoseAsInput(pose: StickmanPose, style: CharacterStyle) {
        val bitmap = renderStickmanPoseBitmap(pose, style)
        setInputBitmap(bitmap, "Stickman Pose")
    }

    fun generateOrEditImage() {
        val current = _uiState.value
        val trimmedPrompt = current.prompt.trim()
        if (trimmedPrompt.isEmpty()) {
            _uiState.update {
                it.copy(genState = ImageGenUiState.Error("Please enter a prompt first."))
            }
            return
        }
        if (current.mode == ImageStudioMode.EDIT && current.inputBitmap == null) {
            _uiState.update {
                it.copy(genState = ImageGenUiState.Error("Pick a photo or capture your Stickman pose to edit."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(genState = ImageGenUiState.Loading) }
            try {
                val config = generationConfig {
                    responseModalities = listOf(ResponseModality.IMAGE)
                    imageConfig = ImageConfig.builder()
                        .setAspectRatio(current.aspectOption.aspectRatio)
                        .setImageSize(ImageSize.SIZE_1K)
                        .build()
                }
                val model = Firebase.ai.generativeModel(
                    modelName = IMAGE_MODEL_NAME,
                    generationConfig = config,
                )

                val resultBitmap = withTimeout(60_000L) {
                    val response = if (current.mode == ImageStudioMode.EDIT && current.inputBitmap != null) {
                        model.generateContent(
                            content {
                                image(current.inputBitmap)
                                text(trimmedPrompt)
                            },
                        )
                    } else {
                        model.generateContent(trimmedPrompt)
                    }
                    response.candidates.firstOrNull()
                        ?.content?.parts?.filterIsInstance<ImagePart>()
                        ?.firstOrNull()
                        ?.image
                }

                if (resultBitmap != null) {
                    _uiState.update { it.copy(genState = ImageGenUiState.Success(resultBitmap)) }
                } else {
                    _uiState.update {
                        it.copy(genState = ImageGenUiState.Error("No image was returned by Nano Banana 2.1."))
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Image generation timed out", e)
                _uiState.update {
                    it.copy(genState = ImageGenUiState.Error("Image generation timed out after 60 seconds. Please try again."))
                }
            } catch (e: QuotaExceededException) {
                Log.e(TAG, "Image quota exceeded", e)
                _uiState.update {
                    it.copy(genState = ImageGenUiState.Error("Quota exceeded or Blaze billing plan required for image generation."))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Image generation failed", e)
                _uiState.update {
                    it.copy(genState = ImageGenUiState.Error(e.localizedMessage ?: "Image generation failed."))
                }
            }
        }
    }

    private fun downscaleIfNeeded(bitmap: Bitmap, maxDim: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxDim && h <= maxDim) return bitmap
        val ratio = minOf(maxDim.toFloat() / w, maxDim.toFloat() / h)
        return Bitmap.createScaledBitmap(
            bitmap,
            (w * ratio).toInt().coerceAtLeast(1),
            (h * ratio).toInt().coerceAtLeast(1),
            true,
        )
    }

    private fun renderStickmanPoseBitmap(pose: StickmanPose, characterStyle: CharacterStyle): Bitmap {
        val size = 512
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(android.graphics.Color.rgb(10, 15, 36))

        val skel = StickmanRigMath.computeSkeleton(
            pose = pose,
            canvasWidth = size.toFloat(),
            canvasHeight = size.toFloat(),
            bodyStyle = characterStyle.bodyStyle,
        )
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = characterStyle.glowTheme.lineColor.toArgb()
            strokeWidth = 18f
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = characterStyle.glowTheme.primaryColor.toArgb()
            style = Paint.Style.FILL
        }

        if (characterStyle.visibility.legs) {
            canvas.drawLine(skel.hip.x, skel.hip.y, skel.leftKnee.x, skel.leftKnee.y, linePaint)
            canvas.drawLine(skel.leftKnee.x, skel.leftKnee.y, skel.leftFoot.x, skel.leftFoot.y, linePaint)
            canvas.drawLine(skel.hip.x, skel.hip.y, skel.rightKnee.x, skel.rightKnee.y, linePaint)
            canvas.drawLine(skel.rightKnee.x, skel.rightKnee.y, skel.rightFoot.x, skel.rightFoot.y, linePaint)
        }
        if (characterStyle.visibility.body) {
            canvas.drawLine(skel.hip.x, skel.hip.y, skel.neckTop.x, skel.neckTop.y, linePaint)
        }
        if (characterStyle.visibility.arms) {
            canvas.drawLine(skel.shoulder.x, skel.shoulder.y, skel.leftElbow.x, skel.leftElbow.y, linePaint)
            canvas.drawLine(skel.leftElbow.x, skel.leftElbow.y, skel.leftHand.x, skel.leftHand.y, linePaint)
            canvas.drawLine(skel.shoulder.x, skel.shoulder.y, skel.rightElbow.x, skel.rightElbow.y, linePaint)
            canvas.drawLine(skel.rightElbow.x, skel.rightElbow.y, skel.rightHand.x, skel.rightHand.y, linePaint)
        }
        if (characterStyle.visibility.head) {
            canvas.drawCircle(skel.headCenter.x, skel.headCenter.y, skel.headRadius, fillPaint)
            canvas.drawCircle(skel.headCenter.x, skel.headCenter.y, skel.headRadius, linePaint)
        }
        return bmp
    }
}
