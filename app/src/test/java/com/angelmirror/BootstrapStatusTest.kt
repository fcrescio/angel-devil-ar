package com.angelmirror

import com.angelmirror.app.BootstrapStatus
import org.junit.Assert.assertTrue
import org.junit.Test

class BootstrapStatusTest {
    @Test
    fun bootstrapStatusHasReadableMessage() {
        assertTrue(BootstrapStatus.Ready.message.isNotBlank())
    }
}
