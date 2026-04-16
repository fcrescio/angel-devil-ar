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
    fun userGreetingKeepsCompanionPresentWithoutVoiceOrLlm() {
        val state = CompanionInteractionReducer.reduce(
            current = CompanionInteractionState(),
            signal = CompanionSignal.UserGreeted,
        )

        assertEquals("greeted", state.cue.id)
        assertEquals(CompanionMood.Present, state.cue.mood)
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
        assertTrue(state.cue.text.isNotBlank())
        assertFalse(state.voiceInputEnabled)
        assertFalse(state.llmResponseEnabled)
    }

    @Test
    fun userReassuranceReturnsCompanionToPresentMood() {
        val provoked = CompanionInteractionReducer.reduce(
            current = CompanionInteractionState(),
            signal = CompanionSignal.UserProvoked,
        )

        val reassured = CompanionInteractionReducer.reduce(
            current = provoked,
            signal = CompanionSignal.UserReassured,
        )

        assertEquals("reassured", reassured.cue.id)
        assertEquals(CompanionMood.Present, reassured.cue.mood)
        assertFalse(reassured.voiceInputEnabled)
        assertFalse(reassured.llmResponseEnabled)
    }
}
