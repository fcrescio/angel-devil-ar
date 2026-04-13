package com.angelmirror.ui

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.angelmirror.ar.AndroidArAvailabilityChecker
import com.angelmirror.ar.ArAvailabilityState
import com.angelmirror.ar.ArHostView
import com.angelmirror.ar.ArSessionStatus
import com.angelmirror.character.CharacterPlacementDebugState
import com.angelmirror.permissions.CameraPermissionChecker
import com.angelmirror.permissions.CameraPermissionState

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
    var arAvailability by remember {
        mutableStateOf(AndroidArAvailabilityChecker.check(context))
    }
    var arSessionStatus by remember {
        mutableStateOf<ArSessionStatus>(ArSessionStatus.NotStarted)
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

    LaunchedEffect(context) {
        cameraPermission = CameraPermissionChecker.check(context)
        arAvailability = AndroidArAvailabilityChecker.check(context)
    }

    val isReadyForAr = cameraPermission == CameraPermissionState.Granted &&
        arAvailability == ArAvailabilityState.Ready

    if (isReadyForAr) {
        ArExperienceScreen(
            status = arSessionStatus,
            placementDebug = placementDebug,
            onStatusChanged = {
                arSessionStatus = it
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
    placementDebug: CharacterPlacementDebugState?,
    onStatusChanged: (ArSessionStatus) -> Unit,
    onPlacementDebugChanged: (CharacterPlacementDebugState) -> Unit,
) {
    var showDebug by remember {
        mutableStateOf(false)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ArHostView(
            modifier = Modifier.fillMaxSize(),
            onStatusChanged = onStatusChanged,
            onPlacementDebugChanged = onPlacementDebugChanged,
        )
        Text(
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.56f))
                .padding(16.dp),
            text = status.message,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )
        TextButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(Color.Black.copy(alpha = 0.42f)),
            onClick = {
                showDebug = !showDebug
            },
        ) {
            Text(
                text = if (showDebug) "Hide debug" else "Debug",
                color = Color.White,
            )
        }
        if (showDebug && placementDebug != null) {
            Text(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(Color.Black.copy(alpha = 0.62f))
                    .padding(16.dp),
                text = placementDebug.summary,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
            )
        }
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
