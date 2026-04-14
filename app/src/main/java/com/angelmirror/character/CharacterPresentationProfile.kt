package com.angelmirror.character

data class CharacterPresentationProfile(
    val id: String,
    val asset: CharacterAsset,
    val lighting: CharacterLightingProfile,
    val initialAnimationIntent: CharacterAnimationIntent,
)

object CharacterPresentationProfiles {
    val Devil = CharacterPresentationProfile(
        id = "grotesque-shoulder-imp",
        asset = CharacterAsset(
            assetPath = "models/grotesque_imp.glb",
            displayName = "Grotesque Shoulder Imp",
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
