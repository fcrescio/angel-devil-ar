package com.angelmirror.character

interface CharacterAssetAnimator {
    fun apply(
        elapsedSeconds: Float,
        intent: CharacterAnimationIntent,
    )
}

object NoopCharacterAssetAnimator : CharacterAssetAnimator {
    override fun apply(
        elapsedSeconds: Float,
        intent: CharacterAnimationIntent,
    ) = Unit
}
