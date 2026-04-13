package com.angelmirror.character

import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode

object CharacterModelNodeFactory {
    val PlaceholderAsset = CharacterAsset(
        assetPath = "models/fox.glb",
        displayName = "Khronos Fox Placeholder",
    )

    fun createPlaceholder(sceneView: ARSceneView): ModelNode {
        return ModelNode(
            modelInstance = sceneView.modelLoader.createModelInstance(
                assetFileLocation = PlaceholderAsset.assetPath,
            ),
            autoAnimate = true,
            scaleToUnits = 0.18f,
        ).apply {
            name = PlaceholderAsset.displayName
            position = Position(
                x = ShoulderPreviewOffset.horizontalMeters,
                y = ShoulderPreviewOffset.verticalMeters,
                z = -0.7f,
            )
            isShadowCaster = false
            isShadowReceiver = false
        }
    }

    val ShoulderPreviewOffset = ShoulderPlacementOffset(
        horizontalMeters = 0.22f,
        verticalMeters = -0.34f,
        depthMeters = -0.08f,
    )
}
