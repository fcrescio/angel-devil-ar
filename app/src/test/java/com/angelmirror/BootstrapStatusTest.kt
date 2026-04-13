package com.angelmirror

import com.angelmirror.app.BootstrapStatus
import com.angelmirror.character.CharacterModelNodeFactory
import com.angelmirror.character.ShoulderPlacementSolver
import com.angelmirror.character.ShoulderPlacementOffset
import com.angelmirror.ar.ArAvailabilityState
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
        val offset = CharacterModelNodeFactory.ShoulderPreviewOffset

        assertTrue(offset.horizontalMeters > 0f)
        assertTrue(offset.verticalMeters < -0.25f)
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
}
