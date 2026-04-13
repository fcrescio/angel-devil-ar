package com.angelmirror.character

data class CharacterPlacementProfile(
    val name: String,
    val offset: ShoulderPlacementOffset,
    val scaleToUnits: Float,
)

object CharacterPlacementProfiles {
    val Pixel7EarShoulder = CharacterPlacementProfile(
        name = "pixel7-ear-shoulder",
        offset = ShoulderPlacementOffset(
            horizontalMeters = 0.15f,
            verticalMeters = -0.01f,
            depthMeters = -0.08f,
        ),
        scaleToUnits = 0.18f,
    )

    val Default = Pixel7EarShoulder
}
