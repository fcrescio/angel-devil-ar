package com.angelmirror.ar

enum class ArAvailabilityState(val message: String) {
    Checking("Checking ARCore availability."),
    Ready("ARCore is installed and ready."),
    NeedsInstall("ARCore is supported but Google Play Services for AR must be installed or updated."),
    Unsupported("This device is not supported by ARCore."),
    Unknown("ARCore availability could not be determined yet."),
}
