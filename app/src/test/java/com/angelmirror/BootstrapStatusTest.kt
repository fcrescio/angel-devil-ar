package com.angelmirror

import com.angelmirror.app.BootstrapStatus
import com.angelmirror.character.CharacterAnimationIntent
import com.angelmirror.character.CharacterAnimationIntentMapper
import com.angelmirror.character.CharacterModelNodeFactory
import com.angelmirror.character.CharacterPlacementProfiles
import com.angelmirror.character.CharacterPresentationProfiles
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
        assertTrue(CharacterPresentationProfiles.Default.asset.assetPath.endsWith(".glb"))
    }

    @Test
    fun placeholderCharacterUsesAppOwnedGrotesqueImp() {
        val profile = CharacterPresentationProfiles.Default

        assertEquals("trellis-winged-devil", profile.id)
        assertEquals("models/trellis_winged_devil.glb", profile.asset.assetPath)
        assertEquals("Trellis Winged Devil", profile.asset.displayName)
        assertEquals(CharacterAnimationIntent.Appearing, profile.initialAnimationIntent)
        assertEquals(180.0f, profile.assetYawCorrectionDegrees, 0.001f)
        assertEquals(profile.asset, CharacterModelNodeFactory.PlaceholderAsset)
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

    @Test
    fun characterAnimationIntentMapperMapsAllMoods() {
        assertEquals(
            CharacterAnimationIntent.Appearing,
            CharacterAnimationIntentMapper.fromMood(CompanionMood.WarmingUp),
        )
        assertEquals(
            CharacterAnimationIntent.Idle,
            CharacterAnimationIntentMapper.fromMood(CompanionMood.Present),
        )
        assertEquals(
            CharacterAnimationIntent.Searching,
            CharacterAnimationIntentMapper.fromMood(CompanionMood.Searching),
        )
        assertEquals(
            CharacterAnimationIntent.Blocked,
            CharacterAnimationIntentMapper.fromMood(CompanionMood.Blocked),
        )
        assertEquals(
            CharacterAnimationIntent.Paused,
            CharacterAnimationIntentMapper.fromMood(CompanionMood.Paused),
        )
    }

    @Test
    fun defaultPlacementProfileKeepsValidatedPixel7Offsets() {
        val profile = CharacterPlacementProfiles.Default

        assertEquals("pixel7-ear-shoulder", profile.name)
        assertEquals(0.15f, profile.offset.horizontalMeters, 0.001f)
        assertEquals(-0.01f, profile.offset.verticalMeters, 0.001f)
        assertEquals(-0.08f, profile.offset.depthMeters, 0.001f)
        assertEquals(0.18f, profile.scaleToUnits, 0.001f)
    }

    @Test
    fun shoulderPlacementHandlesZeroOffset() {
        val placement = ShoulderPlacementSolver.solve(
            facePose = FacePose(centerX = 1f, centerY = 2f, centerZ = 3f),
            offset = ShoulderPlacementOffset(
                horizontalMeters = 0f,
                verticalMeters = 0f,
                depthMeters = 0f,
            ),
        )

        assertEquals(1f, placement.x, 0.001f)
        assertEquals(2f, placement.y, 0.001f)
        assertEquals(3f, placement.z, 0.001f)
    }

    @Test
    fun shoulderPlacementHandlesNegativeHorizontalOffset() {
        val placement = ShoulderPlacementSolver.solve(
            facePose = FacePose(centerX = 0f, centerY = 0f, centerZ = 0f),
            offset = ShoulderPlacementOffset(
                horizontalMeters = -0.15f,
                verticalMeters = 0f,
                depthMeters = 0f,
            ),
        )

        assertEquals(-0.15f, placement.x, 0.001f)
        assertEquals(0f, placement.y, 0.001f)
        assertEquals(0f, placement.z, 0.001f)
    }
}
