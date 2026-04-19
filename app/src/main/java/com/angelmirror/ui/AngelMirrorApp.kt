package com.angelmirror.ui

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.angelmirror.ar.AndroidArAvailabilityChecker
import com.angelmirror.ar.ArAvailabilityState
import com.angelmirror.ar.ArHostView
import com.angelmirror.ar.ArSessionStatus
import com.angelmirror.character.CharacterAnimationIntentMapper
import com.angelmirror.character.CharacterPlacementDebugState
import com.angelmirror.interaction.CompanionAction
import com.angelmirror.interaction.CompanionActions
import com.angelmirror.interaction.CompanionInteractionState
import com.angelmirror.interaction.CompanionReactionEngine
import com.angelmirror.interaction.CompanionReactionExpiry
import com.angelmirror.interaction.CompanionSignal
import com.angelmirror.permissions.CameraPermissionChecker
import com.angelmirror.permissions.CameraPermissionState
import com.angelmirror.permissions.MicrophonePermissionChecker
import com.angelmirror.permissions.MicrophonePermissionState
import com.angelmirror.util.BuildInfo
import com.angelmirror.voice.AndroidVoiceCommandRecognizer
import com.angelmirror.voice.VoiceRecognitionState
import kotlinx.coroutines.delay

@Composable
fun AngelMirrorApp() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ReadinessScreen()
        }
    }
}

@Composable
private fun ReadinessScreen() {
    val context = LocalContext.current
    var cameraPermission by remember {
        mutableStateOf(CameraPermissionChecker.check(context))
    }
    var microphonePermission by remember {
        mutableStateOf(MicrophonePermissionChecker.check(context))
    }
    var arAvailability by remember {
        mutableStateOf(AndroidArAvailabilityChecker.check(context))
    }
    var arSessionStatus by remember {
        mutableStateOf<ArSessionStatus>(ArSessionStatus.NotStarted)
    }
    var companionInteraction by remember {
        mutableStateOf(CompanionInteractionState())
    }
    var companionReactionExpiry by remember {
        mutableStateOf<CompanionReactionExpiry?>(null)
    }
    val companionReactionEngine = remember {
        CompanionReactionEngine()
    }
    var placementDebug by remember {
        mutableStateOf<CharacterPlacementDebugState?>(null)
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        cameraPermission = if (granted) {
            CameraPermissionState.Granted
        } else {
            CameraPermissionState.Denied
        }
        arAvailability = AndroidArAvailabilityChecker.check(context)
    }
    val microphoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        microphonePermission = if (granted) {
            MicrophonePermissionState.Granted
        } else {
            MicrophonePermissionState.Denied
        }
    }

    LaunchedEffect(context) {
        cameraPermission = CameraPermissionChecker.check(context)
        microphonePermission = MicrophonePermissionChecker.check(context)
        arAvailability = AndroidArAvailabilityChecker.check(context)
    }

    val isReadyForAr = cameraPermission == CameraPermissionState.Granted &&
        arAvailability == ArAvailabilityState.Ready

    val applyCompanionSignal: (CompanionSignal) -> Unit = { signal ->
        val result = companionReactionEngine.dispatch(
            current = companionInteraction,
            signal = signal,
        )
        companionInteraction = result.state
        companionReactionExpiry = result.expiry
    }

    if (isReadyForAr) {
        ArExperienceScreen(
            status = arSessionStatus,
            companionInteraction = companionInteraction,
            companionReactionExpiry = companionReactionExpiry,
            microphonePermission = microphonePermission,
            placementDebug = placementDebug,
            onStatusChanged = {
                arSessionStatus = it
                it.toCompanionSignal()?.let(applyCompanionSignal)
            },
            onCompanionSignal = applyCompanionSignal,
            onRequestMicrophonePermission = {
                microphoneLauncher.launch(Manifest.permission.RECORD_AUDIO)
            },
            onPlacementDebugChanged = {
                placementDebug = it
            },
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Angel Mirror AR",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = readinessMessage(context, cameraPermission, arAvailability),
            style = MaterialTheme.typography.bodyLarge,
        )
        BuildBadge(
            modifier = Modifier.padding(top = 12.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
        )
        if (cameraPermission != CameraPermissionState.Granted) {
            Button(
                onClick = {
                    cameraLauncher.launch(Manifest.permission.CAMERA)
                },
            ) {
                Text(text = "Allow camera")
            }
        }
    }
}

@Composable
private fun ArExperienceScreen(
    status: ArSessionStatus,
    companionInteraction: CompanionInteractionState,
    companionReactionExpiry: CompanionReactionExpiry?,
    microphonePermission: MicrophonePermissionState,
    placementDebug: CharacterPlacementDebugState?,
    onStatusChanged: (ArSessionStatus) -> Unit,
    onCompanionSignal: (CompanionSignal) -> Unit,
    onRequestMicrophonePermission: () -> Unit,
    onPlacementDebugChanged: (CharacterPlacementDebugState) -> Unit,
) {
    val context = LocalContext.current
    var showDebug by remember {
        mutableStateOf(false)
    }
    var voiceState by remember {
        mutableStateOf<VoiceRecognitionState>(VoiceRecognitionState.Idle)
    }
    val latestOnCompanionSignal = rememberUpdatedState(onCompanionSignal)
    val voiceRecognizer = remember(context) {
        AndroidVoiceCommandRecognizer(
            context = context,
            onStateChanged = { state ->
                voiceState = state
            },
            onCommand = { signal ->
                latestOnCompanionSignal.value(signal)
            },
        )
    }
    val companionCue = companionInteraction.cue

    val animationDirective = remember(
        companionCue.mood,
        companionInteraction.manualActionStreak,
    ) {
        CharacterAnimationIntentMapper.fromInteractionState(companionInteraction)
    }

    LaunchedEffect(companionReactionExpiry) {
        val expiry = companionReactionExpiry ?: return@LaunchedEffect
        delay(expiry.delayMillis)
        onCompanionSignal(CompanionSignal.CueExpired(expiry.cueId))
    }

    DisposableEffect(voiceRecognizer) {
        onDispose {
            voiceRecognizer.destroy()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        ArHostView(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .aspectRatio(ArPreviewAspectRatio),
            animationDirective = animationDirective,
            onStatusChanged = onStatusChanged,
            onPlacementDebugChanged = onPlacementDebugChanged,
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars.union(WindowInsets.displayCutout))
                .background(Color.Black.copy(alpha = 0.56f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = companionCue.text,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = status.message,
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodySmall,
                )
                BuildBadge()
            }
            TextButton(
                onClick = {
                    showDebug = !showDebug
                },
            ) {
                Text(
                    text = if (showDebug) "Hide debug" else "Debug",
                    color = Color.White,
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (showDebug && placementDebug != null) {
                val debugStateWithAnimation = placementDebug.copy(
                    animationIntent = animationDirective.intent,
                )
                PlacementDebugOverlay(
                    modifier = Modifier.fillMaxWidth(),
                    summary = debugStateWithAnimation.summary +
                        "\n" +
                        companionInteraction.summary +
                        "\nexpiry: ${companionReactionExpiry?.delayMillis ?: "persistent"}" +
                        "\n${voiceState.summary}" +
                        "\nintensity: ${animationDirective.clampedIntensity}" +
                        "\npreview: camera aspect fit" +
                        "\n${BuildInfo.DisplayBuild}",
                )
            }
            if (voiceState != VoiceRecognitionState.Idle) {
                VoiceStatusOverlay(
                    modifier = Modifier.fillMaxWidth(),
                    voiceState = voiceState,
                )
            }
            CompanionActionBar(
                actions = CompanionActions.QuickActions,
                microphonePermission = microphonePermission,
                voiceState = voiceState,
                onListen = {
                    if (microphonePermission == MicrophonePermissionState.Granted) {
                        voiceRecognizer.startListening()
                    } else {
                        onRequestMicrophonePermission()
                    }
                },
                onAction = { action ->
                    onCompanionSignal(action.signal)
                },
            )
        }
    }
}

@Composable
private fun BuildBadge(
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.64f),
) {
    Text(
        modifier = modifier,
        text = BuildInfo.DisplayBuild,
        color = color,
        style = MaterialTheme.typography.labelSmall,
    )
}

@Composable
private fun CompanionActionBar(
    actions: List<CompanionAction>,
    microphonePermission: MicrophonePermissionState,
    voiceState: VoiceRecognitionState,
    onListen: () -> Unit,
    onAction: (CompanionAction) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.Black.copy(alpha = 0.62f),
                shape = RoundedCornerShape(8.dp),
            )
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            enabled = voiceState != VoiceRecognitionState.Listening,
            onClick = onListen,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF38624C),
                contentColor = Color.White,
            ),
        ) {
            Text(text = microphonePermission.listenButtonLabel(voiceState))
        }
        actions.forEachIndexed { index, action ->
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    onAction(action)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A4A4A),
                    contentColor = Color.White,
                ),
            ) {
                Text(text = action.label)
            }
        }
    }
}

private fun MicrophonePermissionState.listenButtonLabel(
    voiceState: VoiceRecognitionState,
): String {
    return when {
        voiceState == VoiceRecognitionState.Listening -> "Listening"
        this == MicrophonePermissionState.Granted -> "Listen"
        else -> "Mic"
    }
}

@Composable
private fun VoiceStatusOverlay(
    voiceState: VoiceRecognitionState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.62f),
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = voiceState.summary,
            color = Color.White.copy(alpha = 0.86f),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun PlacementDebugOverlay(
    summary: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.72f),
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = summary,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 16.sp,
        )
    }
}

private const val ArPreviewAspectRatio = 9f / 16f

private fun ArSessionStatus.toCompanionSignal(): CompanionSignal? {
    return when (this) {
        ArSessionStatus.Creating -> CompanionSignal.ArSessionStarting
        ArSessionStatus.FaceAnchoredCharacter -> CompanionSignal.CharacterPlaced
        ArSessionStatus.SearchingForFace -> CompanionSignal.FaceLost
        is ArSessionStatus.TrackingIssue -> CompanionSignal.FaceLost
        is ArSessionStatus.Failed -> CompanionSignal.ArSessionFailed(reason)
        ArSessionStatus.Paused -> CompanionSignal.ArSessionPaused
        ArSessionStatus.NotStarted,
        ArSessionStatus.Running,
        ArSessionStatus.CharacterPreviewReady,
        -> null
    }
}

private fun readinessMessage(
    context: Context,
    cameraPermission: CameraPermissionState,
    arAvailability: ArAvailabilityState,
): String {
    val appName = context.getString(com.angelmirror.R.string.app_name)
    return when (cameraPermission) {
        CameraPermissionState.Unknown -> "$appName is checking camera permission."
        CameraPermissionState.Denied -> "$appName needs camera permission to start the selfie AR mirror."
        CameraPermissionState.Granted -> arAvailability.message
    }
}
