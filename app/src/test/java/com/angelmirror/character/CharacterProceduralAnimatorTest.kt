package com.angelmirror.character

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterProceduralAnimatorTest {
    private val animator = CharacterProceduralAnimator()

    @Test
    fun idleAnimationKeepsMotionSmallEnoughForShoulderPlacement() {
        val pose = animator.poseAt(
            elapsedSeconds = 0.4f,
            intent = CharacterAnimationIntent.Idle,
        )

        assertTrue(kotlin.math.abs(pose.verticalOffsetMeters) <= 0.0081f)
        assertTrue(kotlin.math.abs(pose.yawDegrees) <= 4.1f)
        assertTrue(kotlin.math.abs(pose.rollDegrees) <= 2.1f)
    }

    @Test
    fun searchingAnimationScansMoreThanIdle() {
        val idle = animator.poseAt(
            elapsedSeconds = 0.25f,
            intent = CharacterAnimationIntent.Idle,
        )
        val searching = animator.poseAt(
            elapsedSeconds = 0.25f,
            intent = CharacterAnimationIntent.Searching,
        )

        assertTrue(kotlin.math.abs(searching.yawDegrees) > kotlin.math.abs(idle.yawDegrees))
    }

    @Test
    fun pausedAnimationIsStill() {
        val pose = animator.poseAt(
            elapsedSeconds = 3.2f,
            intent = CharacterAnimationIntent.Paused,
        )

        assertEquals(CharacterAnimationPose(), pose)
    }
}
