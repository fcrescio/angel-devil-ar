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
            scaleToUnits = 0.32f,
        ).apply {
            name = PlaceholderAsset.displayName
            position = Position(
                x = 0.28f,
                y = -0.18f,
                z = -0.72f,
            )
            isShadowCaster = false
            isShadowReceiver = false
        }
    }
}
