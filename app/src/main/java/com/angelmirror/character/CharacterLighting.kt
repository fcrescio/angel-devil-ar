package com.angelmirror.character

import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Float4
import io.github.sceneview.ar.ARSceneView

object CharacterLighting {
    val DevilFrontLowKeyDirection = Float3(0.0f, 0.34f, -0.94f)
    val DevilFrontLowKeyColor = Float4(1.0f, 0.86f, 0.72f, 1.0f)

    const val DevilFrontLowKeyIntensity = 520_000.0f
    const val DevilIndirectLightIntensity = 80_000.0f

    fun apply(sceneView: ARSceneView, profile: CharacterLightingProfile) {
        sceneView.mainLightNode?.apply {
            lightDirection = profile.keyLightDirection
            color = profile.keyLightColor
            intensity = profile.keyLightIntensity
            isShadowCaster = profile.castsShadows
        }
        sceneView.indirectLight?.intensity = profile.indirectLightIntensity
    }

    fun applyDevilFrontLowKey(sceneView: ARSceneView) {
        apply(sceneView, CharacterPresentationProfiles.Devil.lighting)
    }
}
