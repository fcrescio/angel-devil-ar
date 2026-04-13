package com.angelmirror.character

import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterPlacementDebugStateTest {

    @Test
    fun `summary includes animation intent when present`() {
        val offset = ShoulderPlacementOffset(
            horizontalMeters = 0.15f,
            verticalMeters = 0.1f,
            depthMeters = -0.2f,
        )
        val debugState = CharacterPlacementDebugState(
            profileName = "ShoulderRight",
            offset = offset,
            latestPlacement = ShoulderPlacement(0.1f, 0.2f, 0.3f),
            tracking = true,
            animationIntent = CharacterAnimationIntent.Idle,
        )

        val summary = debugState.summary
        assertTrue(
            "Expected summary to include animation intent",
            summary.contains("animation: Idle"),
        )
    }

    @Test
    fun `summary shows none when animation intent is absent`() {
        val offset = ShoulderPlacementOffset(
            horizontalMeters = 0.15f,
            verticalMeters = 0.1f,
            depthMeters = -0.2f,
        )
        val debugState = CharacterPlacementDebugState(
            profileName = "ShoulderRight",
            offset = offset,
            latestPlacement = null,
            tracking = false,
            animationIntent = null,
        )

        val summary = debugState.summary
        assertTrue(
            "Expected summary to show animation: none",
            summary.contains("animation: none"),
        )
    }

    @Test
    fun `summary includes all fields in correct order`() {
        val offset = ShoulderPlacementOffset(
            horizontalMeters = 0.15f,
            verticalMeters = 0.1f,
            depthMeters = -0.2f,
        )
        val debugState = CharacterPlacementDebugState(
            profileName = "TestProfile",
            offset = offset,
            latestPlacement = ShoulderPlacement(0.1f, 0.2f, 0.3f),
            tracking = true,
            animationIntent = CharacterAnimationIntent.Searching,
        )

        val summary = debugState.summary
        assertTrue(
            "Expected summary to include profile",
            summary.contains("profile: TestProfile"),
        )
        assertTrue(
            "Expected summary to include offset",
            summary.contains("offset:"),
        )
        assertTrue(
            "Expected summary to include placement",
            summary.contains("placement:"),
        )
        assertTrue(
            "Expected summary to include animation",
            summary.contains("animation: Searching"),
        )
    }
}
