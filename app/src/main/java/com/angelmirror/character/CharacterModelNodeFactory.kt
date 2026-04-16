package com.angelmirror.character

import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode

object CharacterModelNodeFactory {
    val PlaceholderAsset = CharacterPresentationProfiles.Default.asset

    fun createPlaceholder(
        sceneView: ARSceneView,
        profile: CharacterPlacementProfile = CharacterPlacementProfiles.Default,
        presentationProfile: CharacterPresentationProfile = CharacterPresentationProfiles.Default,
    ): ModelNode {
        return ModelNode(
            modelInstance = sceneView.modelLoader.createModelInstance(
                assetFileLocation = presentationProfile.asset.assetPath,
            ),
            autoAnimate = true,
            scaleToUnits = profile.scaleToUnits,
        ).apply {
            name = presentationProfile.asset.displayName
            position = Position(
                x = profile.offset.horizontalMeters,
                y = profile.offset.verticalMeters,
                z = -0.7f,
            )
            rotation = Float3(
                0.0f,
                presentationProfile.assetYawCorrectionDegrees,
                0.0f,
            )
            isShadowCaster = false
            isShadowReceiver = false
        }
    }
}
