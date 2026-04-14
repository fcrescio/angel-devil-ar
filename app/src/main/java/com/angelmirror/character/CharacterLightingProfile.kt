package com.angelmirror.character

import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Float4

data class CharacterLightingProfile(
    val keyLightDirection: Float3,
    val keyLightColor: Float4,
    val keyLightIntensity: Float,
    val indirectLightIntensity: Float,
    val castsShadows: Boolean,
)
