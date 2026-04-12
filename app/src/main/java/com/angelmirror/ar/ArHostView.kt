package com.angelmirror.ar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.angelmirror.character.CharacterModelNodeFactory
import io.github.sceneview.ar.ARSceneView

@Composable
fun ArHostView(
    modifier: Modifier = Modifier,
    onStatusChanged: (ArSessionStatus) -> Unit,
) {
    val context = LocalContext.current
    val sceneView = remember {
        var characterPreviewAttached = false
        var characterPreviewFailure: String? = null

        ARSceneView(
            context = context,
            sessionFeatures = AugmentedFaceSessionConfig.FrontCameraFeatures,
            sessionConfiguration = AugmentedFaceSessionConfig::apply,
            onSessionCreated = {
                onStatusChanged(ArSessionStatus.Creating)
            },
            onSessionResumed = {
                val failure = characterPreviewFailure
                when {
                    failure != null -> onStatusChanged(ArSessionStatus.Failed(failure))
                    characterPreviewAttached -> onStatusChanged(ArSessionStatus.CharacterPreviewReady)
                    else -> onStatusChanged(ArSessionStatus.Running)
                }
            },
            onSessionPaused = {
                onStatusChanged(ArSessionStatus.Paused)
            },
            onSessionFailed = { exception ->
                onStatusChanged(
                    ArSessionStatus.Failed(
                        exception.message ?: exception::class.java.simpleName,
                    ),
                )
            },
            onTrackingFailureChanged = { reason ->
                if (reason == null) {
                    onStatusChanged(ArSessionStatus.Running)
                } else {
                    onStatusChanged(ArSessionStatus.TrackingIssue(reason.name))
                }
            },
        ).apply {
            runCatching {
                addChildNode(CharacterModelNodeFactory.createPlaceholder(this))
                characterPreviewAttached = true
            }.onSuccess {
                characterPreviewFailure = null
            }.onFailure { exception ->
                characterPreviewFailure = exception.message ?: exception::class.java.simpleName
            }
        }
    }

    DisposableEffect(Unit) {
        onStatusChanged(ArSessionStatus.Creating)
        onDispose {}
    }

    DisposableEffect(sceneView) {
        onDispose {
            sceneView.destroy()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { sceneView },
    )
}
