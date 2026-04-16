package com.angelmirror.character

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DevilProceduralMotionTest {
    private val motion = DevilProceduralMotion()

    @Test
    fun idlePoseBreathesAndMovesAppendages() {
        val pose = motion.poseAt(
            elapsedSeconds = 0.4f,
            intent = CharacterAnimationIntent.Idle,
        )

        assertTrue(pose.bodyScaleY > 0.98f)
        assertTrue(pose.bodyScaleY < 1.03f)
        assertTrue(pose.jointRotations.size >= 7)
        assertTrue(pose.jointRotations.any { it.jointIndex == DevilProceduralMotion.RightWingRootJoint })
        assertTrue(pose.jointRotations.any { it.jointIndex == DevilProceduralMotion.LeftWingRootJoint })
        assertTrue(pose.jointRotations.any { it.jointIndex == DevilProceduralMotion.TailRootJoint })
        assertTrue(pose.jointRotations.none { it.jointIndex in ArmJointCandidates })
    }

    @Test
    fun idleWingMotionIsMirrored() {
        val pose = motion.poseAt(
            elapsedSeconds = 0.25f,
            intent = CharacterAnimationIntent.Idle,
        )
        val rightWing = pose.jointRotations.first {
            it.jointIndex == DevilProceduralMotion.RightWingRootJoint
        }
        val leftWing = pose.jointRotations.first {
            it.jointIndex == DevilProceduralMotion.LeftWingRootJoint
        }

        assertTrue(rightWing.degrees * leftWing.degrees < 0.0f)
        assertTrue(kotlin.math.abs(rightWing.degrees) <= 10.0f)
        assertTrue(kotlin.math.abs(leftWing.degrees) <= 10.0f)
    }

    @Test
    fun idleWingTipLagsRootForLessMechanicalFlap() {
        val pose = motion.poseAt(
            elapsedSeconds = 0.42f,
            intent = CharacterAnimationIntent.Idle,
        )
        val rightWing = pose.jointRotations.first {
            it.jointIndex == DevilProceduralMotion.RightWingRootJoint
        }
        val rightTip = pose.jointRotations.first {
            it.jointIndex == DevilProceduralMotion.RightWingTipJoint
        }

        assertTrue(kotlin.math.abs(rightWing.degrees - rightTip.degrees) > 0.25f)
    }

    @Test
    fun appearingStartsCompactThenSettlesTowardIdle() {
        val start = motion.poseAt(
            elapsedSeconds = 0.0f,
            intent = CharacterAnimationIntent.Appearing,
        )
        val settled = motion.poseAt(
            elapsedSeconds = 1.4f,
            intent = CharacterAnimationIntent.Appearing,
        )

        assertTrue(start.bodyScaleY < 0.9f)
        assertTrue(settled.bodyScaleY > 0.97f)
        assertTrue(start.jointRotations.none { it.jointIndex in ArmJointCandidates })
    }

    @Test
    fun searchingIsMoreAlertThanIdle() {
        val idle = motion.poseAt(
            elapsedSeconds = 0.5f,
            intent = CharacterAnimationIntent.Idle,
        )
        val searching = motion.poseAt(
            elapsedSeconds = 0.5f,
            intent = CharacterAnimationIntent.Searching,
        )
        val idleTail = idle.rotationFor(DevilProceduralMotion.TailRootJoint)
        val searchingTail = searching.rotationFor(DevilProceduralMotion.TailRootJoint)

        assertTrue(kotlin.math.abs(searchingTail) > kotlin.math.abs(idleTail))
        assertTrue(searching.bodyScaleY >= idle.bodyScaleY)
    }

    @Test
    fun blockedFoldsWingsAndKeepsTailActive() {
        val pose = motion.poseAt(
            elapsedSeconds = 0.25f,
            intent = CharacterAnimationIntent.Blocked,
        )
        val rightWing = pose.rotationFor(DevilProceduralMotion.RightWingRootJoint)
        val leftWing = pose.rotationFor(DevilProceduralMotion.LeftWingRootJoint)
        val tail = pose.rotationFor(DevilProceduralMotion.TailRootJoint)

        assertTrue(rightWing < 0.0f)
        assertTrue(leftWing > 0.0f)
        assertTrue(kotlin.math.abs(tail) > 1.0f)
        assertTrue(pose.jointRotations.none { it.jointIndex in ArmJointCandidates })
    }

    @Test
    fun pausedPoseIsNeutral() {
        val pose = motion.poseAt(
            elapsedSeconds = 3.0f,
            intent = CharacterAnimationIntent.Paused,
        )

        assertEquals(DevilRigPose(), pose)
    }

    private fun DevilRigPose.rotationFor(jointIndex: Int): Float {
        return jointRotations.first { it.jointIndex == jointIndex }.degrees
    }

    private companion object {
        val ArmJointCandidates = setOf(11, 16, 31, 36)
    }
}
