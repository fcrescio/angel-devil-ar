package com.angelmirror.character

import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode

object CharacterModelNodeFactory {
    val PlaceholderAsset = CharacterAsset(
        assetPath = "models/fox.glb",
        displayName = "Khronos Fox Placeholder",
    )

    fun createPlaceholder(
        sceneView: ARSceneView,
        profile: CharacterPlacementProfile = CharacterPlacementProfiles.Default,
    ): ModelNode {
        return ModelNode(
            modelInstance = sceneView.modelLoader.createModelInstance(
                assetFileLocation = PlaceholderAsset.assetPath,
            ),
            autoAnimate = true,
            scaleToUnits = profile.scaleToUnits,
        ).apply {
            name = PlaceholderAsset.displayName
            position = Position(
                x = profile.offset.horizontalMeters,
                y = profile.offset.verticalMeters,
                z = -0.7f,
            )
            isShadowCaster = false
            isShadowReceiver = false
        }
    }
}
