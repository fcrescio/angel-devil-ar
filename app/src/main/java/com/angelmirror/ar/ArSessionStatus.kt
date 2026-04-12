package com.angelmirror.ar

sealed interface ArSessionStatus {
    val message: String

    data object NotStarted : ArSessionStatus {
        override val message: String = "AR session has not started."
    }

    data object Creating : ArSessionStatus {
        override val message: String = "Starting front camera AR session."
    }

    data object Running : ArSessionStatus {
        override val message: String = "Front camera AR session is running."
    }

    data object CharacterPreviewReady : ArSessionStatus {
        override val message: String = "AR session is running with the placeholder character."
    }

    data object Paused : ArSessionStatus {
        override val message: String = "AR session is paused."
    }

    data class TrackingIssue(
        val reason: String,
    ) : ArSessionStatus {
        override val message: String = "Tracking issue: $reason"
    }

    data class Failed(
        val reason: String,
    ) : ArSessionStatus {
        override val message: String = "AR session failed: $reason"
    }
}
