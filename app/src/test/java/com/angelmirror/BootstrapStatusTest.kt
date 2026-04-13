package com.angelmirror

import com.angelmirror.app.BootstrapStatus
import com.angelmirror.character.CharacterModelNodeFactory
import com.angelmirror.character.CharacterPlacementProfiles
import com.angelmirror.character.ShoulderPlacementSolver
import com.angelmirror.character.ShoulderPlacementOffset
import com.angelmirror.ar.ArAvailabilityState
import com.angelmirror.interaction.CompanionCues
import com.angelmirror.interaction.CompanionInteractionReducer
import com.angelmirror.interaction.CompanionInteractionState
import com.angelmirror.interaction.CompanionMood
import com.angelmirror.interaction.CompanionSignal
import com.angelmirror.tracking.FacePose
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class BootstrapStatusTest {
    @Test
    fun bootstrapStatusHasReadableMessage() {
        assertTrue(BootstrapStatus.Ready.message.isNotBlank())
    }

    @Test
    fun arAvailabilityStatesHaveReadableMessages() {
        ArAvailabilityState.entries.forEach { state ->
            assertTrue(state.message.isNotBlank())
        }
    }

    @Test
    fun placeholderCharacterAssetIsGlb() {
        assertTrue(CharacterModelNodeFactory.PlaceholderAsset.assetPath.endsWith(".glb"))
    }

    @Test
    fun shoulderPreviewOffsetPlacesCharacterLowAndToTheSide() {
        val offset = CharacterPlacementProfiles.Default.offset

        assertTrue(offset.horizontalMeters > 0f)
        assertTrue(offset.verticalMeters > -0.1f)
        assertTrue(offset.verticalMeters < 0.1f)
        assertTrue(offset.depthMeters < 0f)
    }

    @Test
    fun shoulderPlacementAppliesOffsetToFacePose() {
        val placement = ShoulderPlacementSolver.solve(
            facePose = FacePose(centerX = 1f, centerY = 2f, centerZ = 3f),
            offset = ShoulderPlacementOffset(
                horizontalMeters = 0.25f,
                verticalMeters = -0.1f,
                depthMeters = -0.2f,
            ),
        )

        assertEquals(1.25f, placement.x, 0.001f)
        assertEquals(1.9f, placement.y, 0.001f)
        assertEquals(2.8f, placement.z, 0.001f)
    }

    @Test
    fun companionInteractionStartsWithoutVoiceOrLlmSideEffects() {
        val state = CompanionInteractionState()

        assertEquals(CompanionCues.WarmingUp, state.cue)
        assertEquals(false, state.voiceInputEnabled)
        assertEquals(false, state.llmResponseEnabled)
    }

    @Test
    fun companionInteractionTracksCharacterPlacement() {
        val state = CompanionInteractionReducer.reduce(
            current = CompanionInteractionState(),
            signal = CompanionSignal.CharacterPlaced,
        )

        assertEquals(CompanionMood.Present, state.cue.mood)
        assertEquals("present", state.cue.id)
        assertEquals(false, state.voiceInputEnabled)
        assertEquals(false, state.llmResponseEnabled)
    }

    @Test
    fun companionInteractionReportsBlockedArState() {
        val state = CompanionInteractionReducer.reduce(
            current = CompanionInteractionState(),
            signal = CompanionSignal.ArSessionFailed("camera unavailable"),
        )

        assertEquals(CompanionMood.Blocked, state.cue.mood)
        assertTrue(state.cue.text.contains("camera unavailable"))
        assertEquals(false, state.voiceInputEnabled)
        assertEquals(false, state.llmResponseEnabled)
    }

    @Test
    fun companionInteractionHandlesPausedState() {
        val state = CompanionInteractionReducer.reduce(
            current = CompanionInteractionState(),
            signal = CompanionSignal.ArSessionPaused,
        )

        assertEquals(CompanionMood.Paused, state.cue.mood)
        assertEquals("paused", state.cue.id)
        assertEquals("AR is paused.", state.cue.text)
        assertEquals(false, state.voiceInputEnabled)
        assertEquals(false, state.llmResponseEnabled)
    }
}
