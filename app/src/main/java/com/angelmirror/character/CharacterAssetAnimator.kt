package com.angelmirror.character

interface CharacterAssetAnimator {
    fun apply(
        elapsedSeconds: Float,
        directive: CharacterAnimationDirective,
    )
}

object NoopCharacterAssetAnimator : CharacterAssetAnimator {
    override fun apply(
        elapsedSeconds: Float,
        directive: CharacterAnimationDirective,
    ) = Unit
}
