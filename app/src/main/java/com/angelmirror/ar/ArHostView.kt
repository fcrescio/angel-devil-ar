package com.angelmirror.ar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.angelmirror.character.CharacterAnimationIntent
import com.angelmirror.character.CharacterLighting
import com.angelmirror.character.CharacterModelNodeFactory
import com.angelmirror.character.CharacterPlacementDebugState
import com.angelmirror.character.CharacterPlacementProfiles
import com.angelmirror.character.CharacterPresentationProfiles
import com.angelmirror.character.DevilRigAnimator
import com.angelmirror.character.FaceRelativeCharacterController
import io.github.sceneview.ar.ARSceneView

@Composable
fun ArHostView(
    modifier: Modifier = Modifier,
    animationIntent: CharacterAnimationIntent = CharacterPresentationProfiles.Default.initialAnimationIntent,
    onStatusChanged: (ArSessionStatus) -> Unit,
    onPlacementDebugChanged: (CharacterPlacementDebugState) -> Unit = {},
) {
    val context = LocalContext.current
    val latestAnimationIntent = rememberUpdatedState(animationIntent)
    val characterControllerRef = remember {
        arrayOfNulls<FaceRelativeCharacterController>(1)
    }
    val sceneView = remember {
        var characterPreviewAttached = false
        var characterPreviewFailure: String? = null
        var faceAnchored = false
        var faceMissingFrameCount = 0

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
            onSessionUpdated = { _, frame ->
                characterControllerRef[0]?.animationIntent = latestAnimationIntent.value
                val didUpdate = characterControllerRef[0]?.update(frame) == true
                if (didUpdate) {
                    faceMissingFrameCount = 0
                    if (!faceAnchored) {
                        faceAnchored = true
                        onStatusChanged(ArSessionStatus.FaceAnchoredCharacter)
                    }
                } else if (characterPreviewAttached) {
                    faceMissingFrameCount += 1
                    if (faceMissingFrameCount == MissingFaceStatusFrameThreshold) {
                        faceAnchored = false
                        onStatusChanged(ArSessionStatus.SearchingForFace)
                    }
                }
            },
        ).apply {
            val presentationProfile = CharacterPresentationProfiles.Default
            CharacterLighting.apply(this, presentationProfile.lighting)
            runCatching {
                val profile = CharacterPlacementProfiles.Default
                val characterNode = CharacterModelNodeFactory.createPlaceholder(
                    sceneView = this,
                    profile = profile,
                    presentationProfile = presentationProfile,
                )
                characterControllerRef[0] = FaceRelativeCharacterController(
                    modelNode = characterNode,
                    profile = profile,
                    baseYawDegrees = presentationProfile.assetYawCorrectionDegrees,
                    initialAnimationIntent = presentationProfile.initialAnimationIntent,
                    assetAnimator = DevilRigAnimator(characterNode),
                    onDebugStateChanged = onPlacementDebugChanged,
                )
                addChildNode(characterNode)
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
        update = {
            characterControllerRef[0]?.animationIntent = latestAnimationIntent.value
        },
    )
}

private const val MissingFaceStatusFrameThreshold = 20
