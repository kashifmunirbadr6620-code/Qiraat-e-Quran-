package com.example

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("QiraatAsharah", appName)
    }

    @Test
    fun `hero banner and launcher drawables resolve without error`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertNotNull(ContextCompat.getDrawable(context, R.drawable.img_quran_banner))
        assertNotNull(ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground))
        assertNotNull(ContextCompat.getDrawable(context, R.drawable.ic_launcher_background))
    }
}
