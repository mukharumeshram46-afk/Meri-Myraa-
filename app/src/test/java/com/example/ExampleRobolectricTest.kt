package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.actions.ActionRegistry
import com.example.actions.AssistantAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `verify app name resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MYRAA", appName)
    }

    @Test
    fun `verify action registry flashlight command parsing`() {
        val action = ActionRegistry.parseCommand("flashlight on karo")
        assertTrue(action is AssistantAction.ToggleFlashlight)
        assertEquals(true, (action as AssistantAction.ToggleFlashlight).enable)
    }

    @Test
    fun `verify action registry app launch command parsing`() {
        val action = ActionRegistry.parseCommand("WhatsApp kholo")
        assertTrue(action is AssistantAction.LaunchApp)
        assertEquals("whatsapp", (action as AssistantAction.LaunchApp).appName)
    }
}
