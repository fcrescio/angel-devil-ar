package com.angelmirror.interaction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompanionInteractionTest {
    @Test
    fun quickActionsExposeStableManualSignals() {
        val actions = CompanionActions.QuickActions

        assertEquals(3, actions.size)
        assertEquals("greet", actions[0].id)
        assertEquals("Hey", actions[0].label)
        assertEquals(CompanionSignal.UserGreeted, actions[0].signal)
        assertEquals("provoke", actions[1].id)
        assertEquals("Boo", actions[1].label)
        assertEquals(CompanionSignal.UserProvoked, actions[1].signal)
        assertEquals("reassure", actions[2].id)
        assertEquals("Easy", actions[2].label)
        assertEquals(CompanionSignal.UserReassured, actions[2].signal)
    }

    @Test
    fun userGreetingTriggersGreetingMoodWithoutVoiceOrLlm() {
        val state = CompanionInteractionReducer.reduce(
            current = CompanionInteractionState(),
            signal = CompanionSignal.UserGreeted,
        )

        assertEquals("greeted", state.cue.id)
        assertEquals(CompanionMood.Greeting, state.cue.mood)
        assertEquals(2_200L, state.cue.durationMillis)
        assertEquals(1, state.eventId)
        assertEquals(1, state.manualActionStreak)
        assertFalse(state.voiceInputEnabled)
        assertFalse(state.llmResponseEnabled)
    }

    @Test
    fun userProvocationTriggersBlockedCueWithoutVoiceOrLlm() {
        val state = CompanionInteractionReducer.reduce(
            current = CompanionInteractionState(),
            signal = CompanionSignal.UserProvoked,
        )

        assertEquals("provoked", state.cue.id)
        assertEquals(CompanionMood.Blocked, state.cue.mood)
        assertEquals(2_800L, state.cue.durationMillis)
        assertTrue(state.cue.text.isNotBlank())
        assertFalse(state.voiceInputEnabled)
        assertFalse(state.llmResponseEnabled)
    }

    @Test
    fun userReassuranceTriggersCalmingMood() {
        val provoked = CompanionInteractionReducer.reduce(
            current = CompanionInteractionState(),
            signal = CompanionSignal.UserProvoked,
        )

        val reassured = CompanionInteractionReducer.reduce(
            current = provoked,
            signal = CompanionSignal.UserReassured,
        )

        assertEquals("reassured", reassured.cue.id)
        assertEquals(CompanionMood.Calming, reassured.cue.mood)
        assertEquals(2_000L, reassured.cue.durationMillis)
        assertFalse(reassured.voiceInputEnabled)
        assertFalse(reassured.llmResponseEnabled)
    }

    @Test
    fun matchingCueExpiryReturnsTransientManualCueToPresent() {
        val greeted = CompanionInteractionReducer.reduce(
            current = CompanionInteractionState(),
            signal = CompanionSignal.UserGreeted,
        )

        val settled = CompanionInteractionReducer.reduce(
            current = greeted,
            signal = CompanionSignal.CueExpired("greeted"),
        )

        assertEquals(CompanionCues.Present, settled.cue)
        assertEquals(0, settled.manualActionStreak)
        assertEquals(null, settled.lastManualActionId)
        assertEquals(greeted.eventId + 1, settled.eventId)
    }

    @Test
    fun staleCueExpiryDoesNotOverrideNewerState() {
        val greeted = CompanionInteractionReducer.reduce(
            current = CompanionInteractionState(),
            signal = CompanionSignal.UserGreeted,
        )
        val searching = CompanionInteractionReducer.reduce(
            current = greeted,
            signal = CompanionSignal.FaceLost,
        )

        val afterStaleExpiry = CompanionInteractionReducer.reduce(
            current = searching,
            signal = CompanionSignal.CueExpired("greeted"),
        )

        assertEquals(searching, afterStaleExpiry)
    }

    @Test
    fun repeatedManualActionEscalatesStreakAndCueText() {
        val first = CompanionInteractionReducer.reduce(
            current = CompanionInteractionState(),
            signal = CompanionSignal.UserProvoked,
        )
        val second = CompanionInteractionReducer.reduce(
            current = first,
            signal = CompanionSignal.UserProvoked,
        )
        val third = CompanionInteractionReducer.reduce(
            current = second,
            signal = CompanionSignal.UserProvoked,
        )

        assertEquals(1, first.manualActionStreak)
        assertEquals("Careful.", first.cue.text)
        assertEquals(2, second.manualActionStreak)
        assertEquals("Again?", second.cue.text)
        assertEquals(3, third.manualActionStreak)
        assertEquals("Enough.", third.cue.text)
        assertTrue(third.summary.contains("streak: 3"))
    }
}
