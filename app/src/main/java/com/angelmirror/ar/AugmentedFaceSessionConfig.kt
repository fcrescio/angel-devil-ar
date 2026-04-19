package com.angelmirror.ar

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import com.google.ar.core.CameraConfig
import com.google.ar.core.CameraConfigFilter
import com.google.ar.core.Config
import com.google.ar.core.Session
import kotlin.math.atan
import kotlin.math.min

object AugmentedFaceSessionConfig {
    val FrontCameraFeatures: Set<Session.Feature> = setOf(Session.Feature.FRONT_CAMERA)

    fun apply(@Suppress("UNUSED_PARAMETER") session: Session, config: Config) {
        config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
        config.planeFindingMode = Config.PlaneFindingMode.DISABLED
        config.lightEstimationMode = Config.LightEstimationMode.DISABLED
    }

    fun selectCameraConfig(context: Context, session: Session): CameraConfig {
        val filter = CameraConfigFilter(session)
            .setFacingDirection(CameraConfig.FacingDirection.FRONT)
        val supported = session.getSupportedCameraConfigs(filter)
        val cameraManager = context.getSystemService(CameraManager::class.java)
        val horizontalFovByConfig = supported.associateWith { config ->
            cameraManager?.horizontalFovDegrees(config.cameraId)
        }
        val selected = supported.maxWithOrNull(cameraConfigPreference(horizontalFovByConfig))
            ?: session.cameraConfig
        Log.i(
            Tag,
            "Selected front camera config ${selected.debugSummary(horizontalFovByConfig[selected])} " +
                "from ${supported.size} supported configs.",
        )
        supported.forEach { config ->
            Log.d(Tag, "Supported front camera config ${config.debugSummary(horizontalFovByConfig[config])}")
        }
        return selected
    }

    private fun CameraManager.horizontalFovDegrees(cameraId: String): Float? {
        return runCatching {
            val characteristics = getCameraCharacteristics(cameraId)
            val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
            val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
            val widestFocalLength = focalLengths?.minOrNull()
            if (sensorSize == null || widestFocalLength == null || widestFocalLength <= 0f) {
                null
            } else {
                radiansToDegrees(2f * atan(sensorSize.width / (2f * widestFocalLength)))
            }
        }.getOrNull()
    }

    private fun radiansToDegrees(radians: Float): Float = radians * DegreesPerRadian

    private fun CameraConfig.debugSummary(horizontalFovDegrees: Float?): String {
        val fov = horizontalFovDegrees?.let { "%.1f".format(it) } ?: "unknown"
        return "camera=${cameraId}, horizontalFov=$fov, " +
            "texture=${textureSize.width}x${textureSize.height}, " +
            "image=${imageSize.width}x${imageSize.height}, fps=${fpsRange}"
    }

    private fun cameraConfigPreference(
        horizontalFovByConfig: Map<CameraConfig, Float?>,
    ): Comparator<CameraConfig> {
        return compareBy(
            { config -> horizontalFovByConfig[config] ?: Float.NEGATIVE_INFINITY },
            { config -> config.textureSize.width.toLong() * config.textureSize.height.toLong() },
            { config -> config.imageSize.width.toLong() * config.imageSize.height.toLong() },
            { config -> config.fpsRange.upper },
        )
    }

    private const val Tag = "AngelMirrorCamera"
    private const val DegreesPerRadian = 57.29578f
}
