package com.angelmirror.character

import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.node.ModelNode

class DevilRigAnimator(
    private val modelNode: ModelNode,
    private val motion: DevilProceduralMotion = DevilProceduralMotion(),
) : CharacterAssetAnimator {
    private val transformManager = modelNode.engine.transformManager
    private var baseScale: Float3? = null
    private var restTransforms: Map<Int, FloatArray>? = null

    override fun apply(
        elapsedSeconds: Float,
        directive: CharacterAnimationDirective,
    ) {
        val pose = motion.poseAt(
            elapsedSeconds = elapsedSeconds,
            directive = directive,
        )
        applyBodyScale(pose)
        applyJointRotations(pose)
    }

    private fun applyBodyScale(pose: DevilRigPose) {
        val scale = baseScale ?: modelNode.scale.also {
            baseScale = it
        }
        modelNode.scale = Float3(
            x = scale.x * pose.bodyScaleX,
            y = scale.y * pose.bodyScaleY,
            z = scale.z * pose.bodyScaleZ,
        )
    }

    private fun applyJointRotations(pose: DevilRigPose) {
        if (modelNode.skinCount == 0) return

        val joints = modelNode.modelInstance.getJointsAt(0)
        val rest = restTransforms ?: captureRestTransforms(joints).also {
            restTransforms = it
        }
        if (rest.isEmpty()) return

        val rotationsByJoint = pose.jointRotations.associateBy { it.jointIndex }

        transformManager.openLocalTransformTransaction()
        rest.forEach { (jointIndex, restTransform) ->
            val entity = joints.getOrNull(jointIndex) ?: return@forEach
            if (!transformManager.hasComponent(entity)) return@forEach

            val instance = transformManager.getInstance(entity)
            val rotation = rotationsByJoint[jointIndex]
            val transform = if (rotation == null) {
                restTransform
            } else {
                restTransform.multiply(rotation.toMatrix())
            }
            transformManager.setTransform(
                instance,
                transform,
            )
        }
        transformManager.commitLocalTransformTransaction()
        modelNode.animator.updateBoneMatrices()
    }

    private fun captureRestTransforms(joints: IntArray): Map<Int, FloatArray> {
        return AnimatedJoints.mapNotNull { jointIndex ->
            val entity = joints.getOrNull(jointIndex) ?: return@mapNotNull null
            if (!transformManager.hasComponent(entity)) return@mapNotNull null

            val transform = transformManager.getTransform(
                transformManager.getInstance(entity),
                FloatArray(MatrixSize),
            )
            jointIndex to transform.copyOf()
        }.toMap()
    }

    private fun DevilJointRotation.toMatrix(): FloatArray {
        return rotationMatrix(
            axis = axis,
            degrees = degrees,
        )
    }

    private fun FloatArray.multiply(other: FloatArray): FloatArray {
        val result = FloatArray(MatrixSize)
        for (column in 0 until MatrixWidth) {
            for (row in 0 until MatrixWidth) {
                var value = 0.0f
                for (index in 0 until MatrixWidth) {
                    value += this[(index * MatrixWidth) + row] * other[(column * MatrixWidth) + index]
                }
                result[(column * MatrixWidth) + row] = value
            }
        }
        return result
    }

    private fun rotationMatrix(
        axis: DevilJointAxis,
        degrees: Float,
    ): FloatArray {
        val radians = Math.toRadians(degrees.toDouble())
        val sin = kotlin.math.sin(radians).toFloat()
        val cos = kotlin.math.cos(radians).toFloat()

        return when (axis) {
            DevilJointAxis.X -> floatArrayOf(
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, cos, sin, 0.0f,
                0.0f, -sin, cos, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f,
            )
            DevilJointAxis.Y -> floatArrayOf(
                cos, 0.0f, -sin, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                sin, 0.0f, cos, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f,
            )
            DevilJointAxis.Z -> floatArrayOf(
                cos, sin, 0.0f, 0.0f,
                -sin, cos, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f,
            )
        }
    }

    private companion object {
        const val MatrixWidth = 4
        const val MatrixSize = MatrixWidth * MatrixWidth

        val AnimatedJoints = listOf(
            DevilProceduralMotion.RightWingRootJoint,
            DevilProceduralMotion.RightWingTipJoint,
            DevilProceduralMotion.LeftWingRootJoint,
            DevilProceduralMotion.LeftWingTipJoint,
            DevilProceduralMotion.TailRootJoint,
            DevilProceduralMotion.TailMidJoint,
            DevilProceduralMotion.TailTipJoint,
        )
    }
}
