package com.angelmirror.character

import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Float4
import io.github.sceneview.ar.ARSceneView

object CharacterLighting {
    val DevilFrontLowKeyDirection = Float3(0.0f, 0.45f, -0.89f)
    val DevilFrontLowKeyColor = Float4(1.0f, 0.64f, 0.44f, 1.0f)

    const val DevilFrontLowKeyIntensity = 180_000.0f
    const val DevilIndirectLightIntensity = 18_000.0f

    fun applyDevilFrontLowKey(sceneView: ARSceneView) {
        sceneView.mainLightNode?.apply {
            lightDirection = DevilFrontLowKeyDirection
            color = DevilFrontLowKeyColor
            intensity = DevilFrontLowKeyIntensity
            isShadowCaster = false
        }
        sceneView.indirectLight?.intensity = DevilIndirectLightIntensity
    }
}
