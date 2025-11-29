package com.example.myapplication

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.myapplication", appContext.packageName)
    }

    @Test
    fun sharedPreferences_test() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = appContext.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

        prefs.edit().putString("test_key", "test_value").apply()

        val value = prefs.getString("test_key", "")
        assertEquals("test_value", value)

        prefs.edit().remove("test_key").apply()
    }

    @Test
    fun locale_configuration_test() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertNotNull(appContext.resources.configuration.locales)
    }
}