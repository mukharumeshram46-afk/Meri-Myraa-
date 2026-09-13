package com.example

import com.example.actions.ActionRegistry
import com.example.actions.AssistantAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun parseFlashlightCommand() {
        val actionOn = ActionRegistry.parseCommand("flashlight on")
        assertTrue(actionOn is AssistantAction.ToggleFlashlight)
        assertEquals(true, (actionOn as AssistantAction.ToggleFlashlight).enable)

        val actionOff = ActionRegistry.parseCommand("torch band karo")
        assertTrue(actionOff is AssistantAction.ToggleFlashlight)
        assertEquals(false, (actionOff as AssistantAction.ToggleFlashlight).enable)
    }

    @Test
    fun parseAppLaunchCommand() {
        val action = ActionRegistry.parseCommand("WhatsApp open karo")
        assertTrue(action is AssistantAction.LaunchApp)
        assertEquals("whatsapp", (action as AssistantAction.LaunchApp).appName)
    }

    @Test
    fun parseTimerCommand() {
        val action = ActionRegistry.parseCommand("5 minute ka timer lagao")
        assertTrue(action is AssistantAction.SetTimer)
        assertEquals(300, (action as AssistantAction.SetTimer).durationSeconds)
    }
}
