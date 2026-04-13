package com.angelmirror.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ARDiagnosticLoggerTest {

    @Test
    fun debugLoggerCapturesEvents() {
        val logger = ARDiagnosticLogger(isDebugEnabled = true)

        val event1 = ARDiagnosticEvent.TrackingState(state = "active", faceCount = 1)
        val event2 = ARDiagnosticEvent.PlacementUpdate(
            horizontal = 0.15f,
            vertical = -0.01f,
            depth = -0.08f,
        )
        val event3 = ARDiagnosticEvent.AnimationIntent(intent = "idle")

        logger.log(event1)
        logger.log(event2)
        logger.log(event3)

        val events = logger.getEvents()
        assertEquals(3, events.size)
        assertEquals(event1, events[0])
        assertEquals(event2, events[1])
        assertEquals(event3, events[2])
    }

    @Test
    fun releaseLoggerDiscardsEvents() {
        val logger = ARDiagnosticLogger(isDebugEnabled = false)

        val event1 = ARDiagnosticEvent.TrackingState(state = "active", faceCount = 1)
        val event2 = ARDiagnosticEvent.PlacementUpdate(
            horizontal = 0.15f,
            vertical = -0.01f,
            depth = -0.08f,
        )

        logger.log(event1)
        logger.log(event2)

        val events = logger.getEvents()
        assertTrue(events.isEmpty())
    }

    @Test
    fun clearRemovesAllEvents() {
        val logger = ARDiagnosticLogger(isDebugEnabled = true)

        logger.log(ARDiagnosticEvent.TrackingState(state = "active", faceCount = 1))
        logger.log(
            ARDiagnosticEvent.PlacementUpdate(
                horizontal = 0.15f,
                vertical = -0.01f,
                depth = -0.08f,
            ),
        )

        assertEquals(2, logger.getEvents().size)

        logger.clear()

        val events = logger.getEvents()
        assertTrue(events.isEmpty())
    }

    @Test
    fun trackingStateEventHasTimestamp() {
        val logger = ARDiagnosticLogger(isDebugEnabled = true)
        val before = System.currentTimeMillis()

        val event = ARDiagnosticEvent.TrackingState(state = "active", faceCount = 1)
        logger.log(event)

        val after = System.currentTimeMillis()
        val events = logger.getEvents()

        assertEquals(1, events.size)
        assertTrue(events[0].timestampMs >= before)
        assertTrue(events[0].timestampMs <= after)
    }

    @Test
    fun placementUpdateEventPreservesCoordinates() {
        val logger = ARDiagnosticLogger(isDebugEnabled = true)

        val horizontal = 0.25f
        val vertical = -0.1f
        val depth = -0.2f

        val event = ARDiagnosticEvent.PlacementUpdate(
            horizontal = horizontal,
            vertical = vertical,
            depth = depth,
        )
        logger.log(event)

        val events = logger.getEvents()
        val placement = (events[0] as ARDiagnosticEvent.PlacementUpdate)

        assertEquals(horizontal, placement.horizontal, 0.001f)
        assertEquals(vertical, placement.vertical, 0.001f)
        assertEquals(depth, placement.depth, 0.001f)
    }

    @Test
    fun animationIntentEventPreservesIntent() {
        val logger = ARDiagnosticLogger(isDebugEnabled = true)

        val intent = "searching"
        val event = ARDiagnosticEvent.AnimationIntent(intent = intent)
        logger.log(event)

        val events = logger.getEvents()
        val animationEvent = (events[0] as ARDiagnosticEvent.AnimationIntent)

        assertEquals(intent, animationEvent.intent)
    }

    @Test
    fun getEventsReturnsImmutableList() {
        val logger = ARDiagnosticLogger(isDebugEnabled = true)

        logger.log(ARDiagnosticEvent.TrackingState(state = "active", faceCount = 1))

        val events = logger.getEvents()
        val originalSize = events.size

        val mutableEvents = events.toMutableList()
        mutableEvents.add(ARDiagnosticEvent.TrackingState(state = "blocked", faceCount = 0))

        val eventsAfter = logger.getEvents()
        assertEquals(originalSize, eventsAfter.size)
    }

    @Test
    fun multipleLogCallsAreThreadSafe() {
        val logger = ARDiagnosticLogger(isDebugEnabled = true)
        val eventCount = 100

        val threads = (1..10).map { threadId ->
            Thread {
                repeat(eventCount) { i ->
                    logger.log(ARDiagnosticEvent.TrackingState(state = "thread-$threadId", faceCount = i))
                }
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        val events = logger.getEvents()
        assertEquals(eventCount * 10, events.size)
    }
}
