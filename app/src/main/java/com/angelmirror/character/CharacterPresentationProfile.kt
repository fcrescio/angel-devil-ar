package com.angelmirror.character

data class CharacterPresentationProfile(
    val id: String,
    val asset: CharacterAsset,
    val lighting: CharacterLightingProfile,
    val initialAnimationIntent: CharacterAnimationIntent,
)

object CharacterPresentationProfiles {
    val Devil = CharacterPresentationProfile(
        id = "trellis-winged-devil",
        asset = CharacterAsset(
            assetPath = "models/trellis_winged_devil.glb",
            displayName = "Trellis Winged Devil",
        ),
        lighting = CharacterLightingProfile(
            keyLightDirection = CharacterLighting.DevilFrontLowKeyDirection,
            keyLightColor = CharacterLighting.DevilFrontLowKeyColor,
            keyLightIntensity = CharacterLighting.DevilFrontLowKeyIntensity,
            indirectLightIntensity = CharacterLighting.DevilIndirectLightIntensity,
            castsShadows = false,
        ),
        initialAnimationIntent = CharacterAnimationIntent.Appearing,
    )

    val Default = Devil
}
