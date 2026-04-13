package com.angelmirror.util

class ARDiagnosticLogger(
    private val isDebugEnabled: Boolean,
) {
    private val events = mutableListOf<ARDiagnosticEvent>()

    fun log(event: ARDiagnosticEvent) {
        if (!isDebugEnabled) return
        synchronized(events) {
            events.add(event)
        }
    }

    fun getEvents(): List<ARDiagnosticEvent> {
        synchronized(events) {
            return events.toList()
        }
    }

    fun clear() {
        synchronized(events) {
            events.clear()
        }
    }
}
