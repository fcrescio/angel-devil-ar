package com.angelmirror

import com.angelmirror.app.BootstrapStatus
import com.angelmirror.ar.ArAvailabilityState
import org.junit.Assert.assertTrue
import org.junit.Test

class BootstrapStatusTest {
    @Test
    fun bootstrapStatusHasReadableMessage() {
        assertTrue(BootstrapStatus.Ready.message.isNotBlank())
    }

    @Test
    fun arAvailabilityStatesHaveReadableMessages() {
        ArAvailabilityState.entries.forEach { state ->
            assertTrue(state.message.isNotBlank())
        }
    }
}
