package com.angelmirror.character

import kotlin.math.sqrt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterLightingTest {
    @Test
    fun devilKeyLightTravelsFromFrontAndBelow() {
        val lighting = CharacterPresentationProfiles.Devil.lighting
        val direction = lighting.keyLightDirection
        val length = sqrt(
            (direction.x * direction.x) +
                (direction.y * direction.y) +
                (direction.z * direction.z),
        )

        assertEquals(1.0f, length, 0.02f)
        assertEquals(0.0f, direction.x, 0.001f)
        assertTrue("light should travel upward from a low source", direction.y > 0.0f)
        assertTrue("light should travel from model-front +Z toward the face", direction.z < 0.0f)
        assertEquals(CharacterLighting.DevilFrontLowKeyIntensity, lighting.keyLightIntensity, 0.001f)
        assertEquals(CharacterLighting.DevilIndirectLightIntensity, lighting.indirectLightIntensity, 0.001f)
    }
}
