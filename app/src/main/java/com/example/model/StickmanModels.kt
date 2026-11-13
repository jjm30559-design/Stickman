package com.example.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ArcticWhite
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanLine
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldPulse
import com.example.ui.theme.NeonRose
import com.example.ui.theme.SoftPurple
import com.example.ui.theme.SolarGold
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

enum class StickmanJoint {
    HEAD,
    HIP,
    LEFT_ELBOW,
    LEFT_HAND,
    RIGHT_ELBOW,
    RIGHT_HAND,
    LEFT_KNEE,
    LEFT_FOOT,
    RIGHT_KNEE,
    RIGHT_FOOT,
}

enum class StickmanBodyStyle(val label: String, val subtitle: String) {
    YOUTUBE_SOLID("YT Solid", "Classic fight stickman"),
    ANIMATOR_HOLLOW("Animator", "Thick hollow-head ring"),
    STORYTIME("Storytime", "Big expressive creator head"),
    FIGHTER_TAPERED("Action Pro", "Tapered Pivot/StickNodes"),
    NEON_WIRE("Neon Wire", "Luminous studio outline"),
}

enum class StickmanProp(val label: String) {
    NONE("None"),
    ENERGY_SWORD("Sword"),
    BO_STAFF("Staff"),
    ENERGY_ORB("Blast"),
    MIC("Mic"),
    YT_SIGN("Play Sign"),
}

enum class StickmanExpression(val label: String) {
    HAPPY("Happy"),
    WINK("Wink"),
    SURPRISED("Whoa"),
    COOL("Cool"),
    FOCUSED("Focus"),
    STARRY("Starry"),
}

enum class StickmanAccessory(val label: String) {
    NONE("None"),
    HEADBAND("Band"),
    CROWN("Crown"),
    CAP("Cap"),
    HEADPHONES("Audio"),
    HALO("Halo"),
    WIZARD("Wizard"),
}

enum class StickmanGlowTheme(
    val label: String,
    val primaryColor: Color,
    val lineColor: Color,
    val auraColor: Color,
) {
    CHOSEN_ORANGE("Chosen Orange", Color(0xFFFF8F36), Color(0xFFFFA959), Color(0xFFFF5E3A)),
    YT_CRIMSON("Crimson Red", Color(0xFFFF4D67), Color(0xFFFF6E82), Color(0xFFFF2E55)),
    YT_AZURE("Azure Blue", Color(0xFF47A1FF), Color(0xFF70BAFF), Color(0xFF3366FF)),
    CYBER_CYAN("Cyber Cyan", CyanGlow, CyanLine, ElectricBlue),
    EMERALD_PULSE("Emerald", EmeraldPulse, Color(0xFF8CFFCE), Color(0xFF39D9FF)),
    ELECTRIC_VIOLET("Violet", SoftPurple, Color(0xFFD5C4FF), Color(0xFF7B5CFF)),
    SOLAR_GOLD("Solar Gold", SolarGold, Color(0xFFFFE5A3), Color(0xFFFF9B54)),
    NEON_ROSE("Neon Rose", NeonRose, Color(0xFFFFA8D9), Color(0xFFB66EFF)),
    ARCTIC_WHITE("Arctic", ArcticWhite, Color(0xFFFFFFFF), CyanGlow),
}

enum class StrokeWeightOption(val label: String, val widthRatio: Float) {
    THIN("Fine", 0.024f),
    MEDIUM("Classic", 0.036f),
    BOLD("Bold", 0.048f),
}

enum class StudioTab(val label: String) {
    BUILD("Build"),
    POSE("Pose"),
    TIMELINE("Timeline"),
    PRESETS("Presets"),
}

data class BodyPartVisibility(
    val head: Boolean = true,
    val body: Boolean = true,
    val arms: Boolean = true,
    val legs: Boolean = true,
)

data class CharacterStyle(
    val bodyStyle: StickmanBodyStyle = StickmanBodyStyle.YOUTUBE_SOLID,
    val visibility: BodyPartVisibility = BodyPartVisibility(),
    val showFace: Boolean = true,
    val expression: StickmanExpression = StickmanExpression.FOCUSED,
    val accessory: StickmanAccessory = StickmanAccessory.HEADBAND,
    val prop: StickmanProp = StickmanProp.NONE,
    val glowTheme: StickmanGlowTheme = StickmanGlowTheme.CHOSEN_ORANGE,
    val strokeWeight: StrokeWeightOption = StrokeWeightOption.BOLD,
)

data class StickmanPose(
    val hipX: Float = 0f,
    val hipY: Float = 26f,
    val torsoAngle: Float = 0f,
    val headTilt: Float = 0f,
    val leftUpperArmAngle: Float = -50f,
    val leftForearmAngle: Float = -46f,
    val rightUpperArmAngle: Float = 50f,
    val rightForearmAngle: Float = 46f,
    val leftThighAngle: Float = -30f,
    val leftShinAngle: Float = -28f,
    val rightThighAngle: Float = 30f,
    val rightShinAngle: Float = 28f,
) {
    fun mirrored(): StickmanPose = copy(
        hipX = -hipX,
        torsoAngle = -torsoAngle,
        headTilt = -headTilt,
        leftUpperArmAngle = -rightUpperArmAngle,
        leftForearmAngle = -rightForearmAngle,
        rightUpperArmAngle = -leftUpperArmAngle,
        rightForearmAngle = -leftForearmAngle,
        leftThighAngle = -rightThighAngle,
        leftShinAngle = -rightShinAngle,
        rightThighAngle = -leftThighAngle,
        rightShinAngle = -leftShinAngle,
    )
}

data class StickmanSkeletonPoints(
    val hip: Offset,
    val shoulder: Offset,
    val neckTop: Offset,
    val headCenter: Offset,
    val headRadius: Float,
    val leftElbow: Offset,
    val leftHand: Offset,
    val rightElbow: Offset,
    val rightHand: Offset,
    val leftKnee: Offset,
    val leftFoot: Offset,
    val rightKnee: Offset,
    val rightFoot: Offset,
)

data class Keyframe(
    val id: String,
    val pose: StickmanPose,
    val label: String,
)

enum class PosePreset(val label: String, val pose: StickmanPose) {
    IDLE("Idle", StickmanPose()),
    FIGHT_GUARD(
        "Guard",
        StickmanPose(
            hipY = 30f,
            torsoAngle = 10f,
            headTilt = -6f,
            leftUpperArmAngle = 35f,
            leftForearmAngle = 148f,
            rightUpperArmAngle = -20f,
            rightForearmAngle = 128f,
            leftThighAngle = -38f,
            leftShinAngle = -14f,
            rightThighAngle = 36f,
            rightShinAngle = 18f,
        ),
    ),
    WAVE(
        "Wave",
        StickmanPose(
            torsoAngle = -4f,
            headTilt = -6f,
            leftUpperArmAngle = -42f,
            leftForearmAngle = -24f,
            rightUpperArmAngle = 115f,
            rightForearmAngle = 165f,
            leftThighAngle = -22f,
            leftShinAngle = -16f,
            rightThighAngle = 24f,
            rightShinAngle = 18f,
        ),
    ),
    RUN(
        "Sprint",
        StickmanPose(
            hipY = 20f,
            torsoAngle = 22f,
            headTilt = -12f,
            leftUpperArmAngle = 58f,
            leftForearmAngle = 132f,
            rightUpperArmAngle = -58f,
            rightForearmAngle = 18f,
            leftThighAngle = -56f,
            leftShinAngle = -15f,
            rightThighAngle = 54f,
            rightShinAngle = -28f,
        ),
    ),
    JUMP(
        "Leap",
        StickmanPose(
            hipY = -18f,
            torsoAngle = -6f,
            headTilt = 5f,
            leftUpperArmAngle = -140f,
            leftForearmAngle = -165f,
            rightUpperArmAngle = 140f,
            rightForearmAngle = 165f,
            leftThighAngle = -52f,
            leftShinAngle = 12f,
            rightThighAngle = 52f,
            rightShinAngle = -12f,
        ),
    ),
    KICK(
        "Kick",
        StickmanPose(
            hipY = 18f,
            torsoAngle = -20f,
            headTilt = 10f,
            leftUpperArmAngle = -75f,
            leftForearmAngle = -135f,
            rightUpperArmAngle = 60f,
            rightForearmAngle = 110f,
            leftThighAngle = -10f,
            leftShinAngle = -6f,
            rightThighAngle = 96f,
            rightShinAngle = 92f,
        ),
    ),
}

enum class AnimationPreset(
    val title: String,
    val subtitle: String,
    val recommendedFps: Float,
    val frames: List<StickmanPose>,
) {
    FIGHT_COMBO(
        title = "YT Fight Combo",
        subtitle = "Guard, jab, spin kick & land",
        recommendedFps = 8f,
        frames = listOf(
            PosePreset.FIGHT_GUARD.pose,
            StickmanPose(
                hipX = 12f,
                hipY = 26f,
                torsoAngle = 18f,
                headTilt = -8f,
                leftUpperArmAngle = -28f,
                leftForearmAngle = 132f,
                rightUpperArmAngle = 92f,
                rightForearmAngle = 92f,
                leftThighAngle = -44f,
                leftShinAngle = -22f,
                rightThighAngle = 38f,
                rightShinAngle = 8f,
            ),
            StickmanPose(
                hipX = 18f,
                hipY = 12f,
                torsoAngle = -24f,
                headTilt = 14f,
                leftUpperArmAngle = -90f,
                leftForearmAngle = -145f,
                rightUpperArmAngle = 55f,
                rightForearmAngle = 125f,
                leftThighAngle = -12f,
                leftShinAngle = -6f,
                rightThighAngle = 105f,
                rightShinAngle = 102f,
            ),
            StickmanPose(
                hipX = 0f,
                hipY = 34f,
                torsoAngle = 10f,
                leftUpperArmAngle = -60f,
                leftForearmAngle = -100f,
                rightUpperArmAngle = 60f,
                rightForearmAngle = 100f,
                leftThighAngle = -42f,
                leftShinAngle = 0f,
                rightThighAngle = 42f,
                rightShinAngle = 0f,
            ),
        ),
    ),
    PARKOUR_VAULT(
        title = "Parkour Flip",
        subtitle = "Sprint, launch & 360 somersault",
        recommendedFps = 8f,
        frames = listOf(
            PosePreset.RUN.pose.copy(hipX = -26f),
            StickmanPose(
                hipX = -4f,
                hipY = 44f,
                torsoAngle = 18f,
                leftUpperArmAngle = 48f,
                leftForearmAngle = 72f,
                rightUpperArmAngle = 54f,
                rightForearmAngle = 80f,
                leftThighAngle = -56f,
                leftShinAngle = 10f,
                rightThighAngle = -34f,
                rightShinAngle = 28f,
            ),
            StickmanPose(
                hipX = 12f,
                hipY = -28f,
                torsoAngle = 168f,
                leftUpperArmAngle = -135f,
                leftForearmAngle = -25f,
                rightUpperArmAngle = 135f,
                rightForearmAngle = 25f,
                leftThighAngle = 115f,
                leftShinAngle = 15f,
                rightThighAngle = -115f,
                rightShinAngle = -15f,
            ),
            StickmanPose(
                hipX = 22f,
                hipY = 36f,
                torsoAngle = 12f,
                leftUpperArmAngle = -75f,
                leftForearmAngle = -90f,
                rightUpperArmAngle = 75f,
                rightForearmAngle = 90f,
                leftThighAngle = -44f,
                leftShinAngle = 6f,
                rightThighAngle = 44f,
                rightShinAngle = -6f,
            ),
            StickmanPose(hipX = 0f, hipY = 26f),
        ),
    ),
    SWORD_SLASH(
        title = "Sword Slash",
        subtitle = "Epic YouTube anime blade arc",
        recommendedFps = 8f,
        frames = listOf(
            PosePreset.FIGHT_GUARD.pose.copy(
                rightUpperArmAngle = 148f,
                rightForearmAngle = 172f,
            ),
            StickmanPose(
                hipX = -8f,
                hipY = 18f,
                torsoAngle = -16f,
                headTilt = 8f,
                leftUpperArmAngle = -65f,
                leftForearmAngle = -95f,
                rightUpperArmAngle = 165f,
                rightForearmAngle = 142f,
                leftThighAngle = -42f,
                leftShinAngle = -12f,
                rightThighAngle = 40f,
                rightShinAngle = 22f,
            ),
            StickmanPose(
                hipX = 20f,
                hipY = 30f,
                torsoAngle = 24f,
                headTilt = -12f,
                leftUpperArmAngle = -85f,
                leftForearmAngle = -115f,
                rightUpperArmAngle = 72f,
                rightForearmAngle = 58f,
                leftThighAngle = -52f,
                leftShinAngle = -35f,
                rightThighAngle = 48f,
                rightShinAngle = 6f,
            ),
            PosePreset.FIGHT_GUARD.pose,
        ),
    ),
    STORYTIME_TALK(
        title = "Storytime Talk",
        subtitle = "Animated YouTube creator gestures",
        recommendedFps = 6f,
        frames = listOf(
            StickmanPose(
                hipY = 24f,
                torsoAngle = -4f,
                headTilt = -5f,
                leftUpperArmAngle = -48f,
                leftForearmAngle = -115f,
                rightUpperArmAngle = 62f,
                rightForearmAngle = 112f,
                leftThighAngle = -24f,
                leftShinAngle = -20f,
                rightThighAngle = 24f,
                rightShinAngle = 20f,
            ),
            StickmanPose(
                hipY = 18f,
                torsoAngle = 8f,
                headTilt = 6f,
                leftUpperArmAngle = -88f,
                leftForearmAngle = -132f,
                rightUpperArmAngle = 118f,
                rightForearmAngle = 148f,
                leftThighAngle = -28f,
                leftShinAngle = -22f,
                rightThighAngle = 28f,
                rightShinAngle = 22f,
            ),
            StickmanPose(
                hipY = 26f,
                torsoAngle = -8f,
                headTilt = 4f,
                leftUpperArmAngle = -35f,
                leftForearmAngle = -155f,
                rightUpperArmAngle = 45f,
                rightForearmAngle = 135f,
                leftThighAngle = -22f,
                leftShinAngle = -22f,
                rightThighAngle = 22f,
                rightShinAngle = 22f,
            ),
        ),
    ),
    BOUNCE(
        title = "Studio Bounce",
        subtitle = "Classic upbeat idle groove",
        recommendedFps = 6f,
        frames = listOf(
            StickmanPose(hipY = 26f, torsoAngle = 0f),
            StickmanPose(
                hipY = 8f,
                torsoAngle = -5f,
                headTilt = -4f,
                leftUpperArmAngle = -68f,
                leftForearmAngle = -85f,
                rightUpperArmAngle = 68f,
                rightForearmAngle = 85f,
                leftThighAngle = -36f,
                leftShinAngle = -18f,
                rightThighAngle = 36f,
                rightShinAngle = 18f,
            ),
            StickmanPose(
                hipY = 28f,
                torsoAngle = 5f,
                headTilt = 4f,
                leftUpperArmAngle = -42f,
                leftForearmAngle = -32f,
                rightUpperArmAngle = 42f,
                rightForearmAngle = 32f,
                leftThighAngle = -26f,
                leftShinAngle = -28f,
                rightThighAngle = 26f,
                rightShinAngle = 28f,
            ),
        ),
    ),
    WALK_CYCLE(
        title = "Walk Cycle",
        subtitle = "Smooth 4-beat stride",
        recommendedFps = 8f,
        frames = listOf(
            StickmanPose(
                hipY = 22f,
                torsoAngle = 8f,
                leftUpperArmAngle = 42f,
                leftForearmAngle = 58f,
                rightUpperArmAngle = -38f,
                rightForearmAngle = -22f,
                leftThighAngle = -42f,
                leftShinAngle = -35f,
                rightThighAngle = 38f,
                rightShinAngle = 12f,
            ),
            StickmanPose(
                hipY = 16f,
                torsoAngle = 6f,
                leftUpperArmAngle = 8f,
                leftForearmAngle = 18f,
                rightUpperArmAngle = -8f,
                rightForearmAngle = 4f,
                leftThighAngle = -10f,
                leftShinAngle = -8f,
                rightThighAngle = 22f,
                rightShinAngle = -38f,
            ),
            StickmanPose(
                hipY = 22f,
                torsoAngle = 8f,
                leftUpperArmAngle = -38f,
                leftForearmAngle = -22f,
                rightUpperArmAngle = 42f,
                rightForearmAngle = 58f,
                leftThighAngle = 38f,
                leftShinAngle = 12f,
                rightThighAngle = -42f,
                rightShinAngle = -35f,
            ),
            StickmanPose(
                hipY = 16f,
                torsoAngle = 6f,
                leftUpperArmAngle = -8f,
                leftForearmAngle = 4f,
                rightUpperArmAngle = 8f,
                rightForearmAngle = 18f,
                leftThighAngle = 22f,
                leftShinAngle = -38f,
                rightThighAngle = -10f,
                rightShinAngle = -8f,
            ),
        ),
    ),
}

object StickmanRigMath {
    const val STAGE_WIDTH = 220f
    const val STAGE_HEIGHT = 280f

    private const val TORSO_LENGTH = 86f
    private const val SHOULDER_RATIO = 0.72f
    private const val BASE_HEAD_RADIUS = 25f
    private const val BASE_NECK_GAP = 27f
    private const val UPPER_ARM_LEN = 32f
    private const val FOREARM_LEN = 32f
    private const val THIGH_LEN = 37f
    private const val SHIN_LEN = 37f

    fun computeSkeleton(
        pose: StickmanPose,
        canvasWidth: Float,
        canvasHeight: Float,
        bodyStyle: StickmanBodyStyle = StickmanBodyStyle.YOUTUBE_SOLID,
    ): StickmanSkeletonPoints {
        val scale = minOf(canvasWidth / STAGE_WIDTH, canvasHeight / STAGE_HEIGHT)
        val cx = canvasWidth * 0.5f + pose.hipX * scale
        val cy = canvasHeight * 0.5f + pose.hipY * scale

        val headScale = when (bodyStyle) {
            StickmanBodyStyle.STORYTIME -> 1.24f
            StickmanBodyStyle.YOUTUBE_SOLID -> 1.04f
            StickmanBodyStyle.ANIMATOR_HOLLOW -> 1.06f
            StickmanBodyStyle.FIGHTER_TAPERED -> 0.96f
            StickmanBodyStyle.NEON_WIRE -> 1.0f
        }

        val hip = Offset(cx, cy)
        val torsoRad = pose.torsoAngle * (PI.toFloat() / 180f)
        val torsoDir = Offset(sin(torsoRad), -cos(torsoRad))

        val shoulder = hip + torsoDir * (TORSO_LENGTH * SHOULDER_RATIO * scale)
        val neckTop = hip + torsoDir * (TORSO_LENGTH * scale)

        val headRad = (pose.torsoAngle + pose.headTilt) * (PI.toFloat() / 180f)
        val headDir = Offset(sin(headRad), -cos(headRad))
        val headRadius = BASE_HEAD_RADIUS * headScale * scale
        val headCenter = neckTop + headDir * ((BASE_NECK_GAP * headScale) * scale)

        val leftElbow = limbOffset(shoulder, pose.leftUpperArmAngle, UPPER_ARM_LEN * scale)
        val leftHand = limbOffset(leftElbow, pose.leftForearmAngle, FOREARM_LEN * scale)
        val rightElbow = limbOffset(shoulder, pose.rightUpperArmAngle, UPPER_ARM_LEN * scale)
        val rightHand = limbOffset(rightElbow, pose.rightForearmAngle, FOREARM_LEN * scale)

        val leftKnee = limbOffset(hip, pose.leftThighAngle, THIGH_LEN * scale)
        val leftFoot = limbOffset(leftKnee, pose.leftShinAngle, SHIN_LEN * scale)
        val rightKnee = limbOffset(hip, pose.rightThighAngle, THIGH_LEN * scale)
        val rightFoot = limbOffset(rightKnee, pose.rightShinAngle, SHIN_LEN * scale)

        return StickmanSkeletonPoints(
            hip = hip,
            shoulder = shoulder,
            neckTop = neckTop,
            headCenter = headCenter,
            headRadius = headRadius,
            leftElbow = leftElbow,
            leftHand = leftHand,
            rightElbow = rightElbow,
            rightHand = rightHand,
            leftKnee = leftKnee,
            leftFoot = leftFoot,
            rightKnee = rightKnee,
            rightFoot = rightFoot,
        )
    }

    private fun limbOffset(origin: Offset, angleDeg: Float, lengthPx: Float): Offset {
        val rad = angleDeg * (PI.toFloat() / 180f)
        return Offset(
            x = origin.x + sin(rad) * lengthPx,
            y = origin.y + cos(rad) * lengthPx,
        )
    }

    fun applyJointDrag(
        pose: StickmanPose,
        joint: StickmanJoint,
        pointerPx: Offset,
        canvasWidth: Float,
        canvasHeight: Float,
        bodyStyle: StickmanBodyStyle = StickmanBodyStyle.YOUTUBE_SOLID,
    ): StickmanPose {
        val scale = minOf(canvasWidth / STAGE_WIDTH, canvasHeight / STAGE_HEIGHT).coerceAtLeast(0.1f)
        val skeleton = computeSkeleton(pose, canvasWidth, canvasHeight, bodyStyle)

        return when (joint) {
            StickmanJoint.HIP -> {
                val stageX = ((pointerPx.x - canvasWidth * 0.5f) / scale).coerceIn(-68f, 68f)
                val stageY = ((pointerPx.y - canvasHeight * 0.5f) / scale).coerceIn(-65f, 75f)
                pose.copy(hipX = stageX, hipY = stageY)
            }
            StickmanJoint.HEAD -> {
                val dx = pointerPx.x - skeleton.hip.x
                val dy = pointerPx.y - skeleton.hip.y
                val angle = (atan2(dx, -dy) * (180f / PI.toFloat())).coerceIn(-85f, 85f)
                pose.copy(torsoAngle = angle)
            }
            StickmanJoint.LEFT_ELBOW -> {
                pose.copy(leftUpperArmAngle = angleDownDeg(skeleton.shoulder, pointerPx))
            }
            StickmanJoint.RIGHT_ELBOW -> {
                pose.copy(rightUpperArmAngle = angleDownDeg(skeleton.shoulder, pointerPx))
            }
            StickmanJoint.LEFT_HAND -> {
                val (upper, lower) = solveTwoBoneIK(
                    origin = skeleton.shoulder,
                    target = pointerPx,
                    l1 = UPPER_ARM_LEN * scale,
                    l2 = FOREARM_LEN * scale,
                    bendSign = -1f,
                )
                pose.copy(leftUpperArmAngle = upper, leftForearmAngle = lower)
            }
            StickmanJoint.RIGHT_HAND -> {
                val (upper, lower) = solveTwoBoneIK(
                    origin = skeleton.shoulder,
                    target = pointerPx,
                    l1 = UPPER_ARM_LEN * scale,
                    l2 = FOREARM_LEN * scale,
                    bendSign = 1f,
                )
                pose.copy(rightUpperArmAngle = upper, rightForearmAngle = lower)
            }
            StickmanJoint.LEFT_KNEE -> {
                pose.copy(leftThighAngle = angleDownDeg(skeleton.hip, pointerPx))
            }
            StickmanJoint.RIGHT_KNEE -> {
                pose.copy(rightThighAngle = angleDownDeg(skeleton.hip, pointerPx))
            }
            StickmanJoint.LEFT_FOOT -> {
                val (thigh, shin) = solveTwoBoneIK(
                    origin = skeleton.hip,
                    target = pointerPx,
                    l1 = THIGH_LEN * scale,
                    l2 = SHIN_LEN * scale,
                    bendSign = -1f,
                )
                pose.copy(leftThighAngle = thigh, leftShinAngle = shin)
            }
            StickmanJoint.RIGHT_FOOT -> {
                val (thigh, shin) = solveTwoBoneIK(
                    origin = skeleton.hip,
                    target = pointerPx,
                    l1 = THIGH_LEN * scale,
                    l2 = SHIN_LEN * scale,
                    bendSign = 1f,
                )
                pose.copy(rightThighAngle = thigh, rightShinAngle = shin)
            }
        }
    }

    private fun angleDownDeg(from: Offset, to: Offset): Float {
        return atan2(to.x - from.x, to.y - from.y) * (180f / PI.toFloat())
    }

    private fun solveTwoBoneIK(
        origin: Offset,
        target: Offset,
        l1: Float,
        l2: Float,
        bendSign: Float,
    ): Pair<Float, Float> {
        val dx = target.x - origin.x
        val dy = target.y - origin.y
        val rawDist = hypot(dx, dy)
        val minDist = abs(l1 - l2) + 2f
        val maxDist = l1 + l2 - 0.5f
        val dist = rawDist.coerceIn(minDist, maxDist)
        val baseAngle = atan2(dx, dy)
        val cosAlpha = ((l1 * l1 + dist * dist - l2 * l2) / (2f * l1 * dist)).coerceIn(-1f, 1f)
        val alpha = acos(cosAlpha)
        val angle1Rad = baseAngle + bendSign * alpha
        val jointPos = Offset(
            x = origin.x + l1 * sin(angle1Rad),
            y = origin.y + l1 * cos(angle1Rad),
        )
        val angle2Rad = atan2(target.x - jointPos.x, target.y - jointPos.y)
        val radToDeg = 180f / PI.toFloat()
        return (angle1Rad * radToDeg) to (angle2Rad * radToDeg)
    }

    fun lerpPose(a: StickmanPose, b: StickmanPose, rawT: Float): StickmanPose {
        val t = rawT.coerceIn(0f, 1f)
        val eased = t * t * (3f - 2f * t)
        return StickmanPose(
            hipX = lerp(a.hipX, b.hipX, eased),
            hipY = lerp(a.hipY, b.hipY, eased),
            torsoAngle = lerpAngleDeg(a.torsoAngle, b.torsoAngle, eased),
            headTilt = lerpAngleDeg(a.headTilt, b.headTilt, eased),
            leftUpperArmAngle = lerpAngleDeg(a.leftUpperArmAngle, b.leftUpperArmAngle, eased),
            leftForearmAngle = lerpAngleDeg(a.leftForearmAngle, b.leftForearmAngle, eased),
            rightUpperArmAngle = lerpAngleDeg(a.rightUpperArmAngle, b.rightUpperArmAngle, eased),
            rightForearmAngle = lerpAngleDeg(a.rightForearmAngle, b.rightForearmAngle, eased),
            leftThighAngle = lerpAngleDeg(a.leftThighAngle, b.leftThighAngle, eased),
            leftShinAngle = lerpAngleDeg(a.leftShinAngle, b.leftShinAngle, eased),
            rightThighAngle = lerpAngleDeg(a.rightThighAngle, b.rightThighAngle, eased),
            rightShinAngle = lerpAngleDeg(a.rightShinAngle, b.rightShinAngle, eased),
        )
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

    private fun lerpAngleDeg(a: Float, b: Float, t: Float): Float {
        var diff = ((b - a) % 360f + 540f) % 360f - 180f
        if (diff < -180f) diff += 360f
        return a + diff * t
    }
}
