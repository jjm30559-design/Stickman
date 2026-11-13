package com.example

import androidx.compose.ui.geometry.Offset
import com.example.model.StickmanJoint
import com.example.model.StickmanPose
import com.example.model.StickmanRigMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot

class ExampleUnitTest {
    @Test
    fun `computeSkeleton places head above hip for upright pose`() {
        val skel = StickmanRigMath.computeSkeleton(
            pose = StickmanPose(),
            canvasWidth = 220f,
            canvasHeight = 280f,
        )
        assertTrue(skel.headCenter.y < skel.shoulder.y)
        assertTrue(skel.shoulder.y < skel.hip.y)
        assertTrue(skel.leftFoot.y > skel.hip.y)
        assertTrue(skel.rightFoot.y > skel.hip.y)
    }

    @Test
    fun `applyJointDrag right hand moves hand close to reachable target`() {
        val initial = StickmanPose()
        val skel = StickmanRigMath.computeSkeleton(initial, 220f, 280f)
        val target = Offset(skel.shoulder.x + 42f, skel.shoulder.y + 12f)
        val updated = StickmanRigMath.applyJointDrag(
            pose = initial,
            joint = StickmanJoint.RIGHT_HAND,
            pointerPx = target,
            canvasWidth = 220f,
            canvasHeight = 280f,
        )
        val updatedSkel = StickmanRigMath.computeSkeleton(updated, 220f, 280f)
        val distance = hypot(updatedSkel.rightHand.x - target.x, updatedSkel.rightHand.y - target.y)
        assertFalse(distance.isNaN())
        assertTrue("Expected hand within 3px of target, got $distance", distance < 3f)
    }

    @Test
    fun `lerpPose mid-point interpolates hip and angles smoothly`() {
        val a = StickmanPose(hipY = 10f, torsoAngle = -20f)
        val b = StickmanPose(hipY = 30f, torsoAngle = 20f)
        val mid = StickmanRigMath.lerpPose(a, b, 0.5f)
        assertEquals(20f, mid.hipY, 0.01f)
        assertEquals(0f, mid.torsoAngle, 0.01f)
    }

    @Test
    fun `mirrored flips left and right limb angles`() {
        val pose = StickmanPose(
            leftUpperArmAngle = -100f,
            rightUpperArmAngle = 30f,
        )
        val mirrored = pose.mirrored()
        assertEquals(-30f, mirrored.leftUpperArmAngle, 0.01f)
        assertEquals(100f, mirrored.rightUpperArmAngle, 0.01f)
    }
}
