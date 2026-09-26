package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.formatCurrency
import com.example.ui.isDateBackdated
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LedgerFlow", appName)
    }

    @Test
    fun `backdate detection and PKR currency formatting`() {
        val now = 1_758_870_000_000L
        val twoDaysEarlier = now - (2 * 86_400_000L)
        assertTrue(isDateBackdated(twoDaysEarlier, now))
        assertFalse(isDateBackdated(now, now))
        assertEquals("PKR 18,500", formatCurrency(18500.0, "PKR"))
    }
}
