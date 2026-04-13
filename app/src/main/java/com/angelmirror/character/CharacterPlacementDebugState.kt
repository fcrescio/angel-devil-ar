package com.angelmirror.character

import java.util.Locale

data class CharacterPlacementDebugState(
    val profileName: String,
    val offset: ShoulderPlacementOffset,
    val latestPlacement: ShoulderPlacement? = null,
    val tracking: Boolean = false,
    val animationIntent: CharacterAnimationIntent? = null,
) {
    val summary: String
        get() {
            val placement = latestPlacement
            val placementText = if (placement == null) {
                "placement: waiting"
            } else {
                "placement: x=${placement.x.format()}, y=${placement.y.format()}, z=${placement.z.format()}"
            }

            val animationText = if (animationIntent == null) {
                "animation: none"
            } else {
                "animation: ${animationIntent.name}"
            }

            return "profile: $profileName\n" +
                "offset: x=${offset.horizontalMeters.format()}, y=${offset.verticalMeters.format()}, z=${offset.depthMeters.format()}\n" +
                placementText +
                "\n" +
                animationText
        }

    private fun Float.format(): String {
        return String.format(Locale.US, "%.2fm", this)
    }
}
