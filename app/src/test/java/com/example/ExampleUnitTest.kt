package com.example

import com.example.data.model.ArabicTextUtils
import com.example.data.model.QiraatCatalog
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun arabicNormalization_stripsDiacriticsAndNormalizesAlef() {
        assertEquals("مالك", ArabicTextUtils.normalizeArabic("مَالِكِ"))
        assertEquals("ملك", ArabicTextUtils.normalizeArabic("مَلِكِ"))
        assertEquals("انعمت", ArabicTextUtils.normalizeArabic("أَنْعَمْتَ"))
    }

    @Test
    fun arabicDigits_convertsCorrectly() {
        assertEquals("١١٤", ArabicTextUtils.toArabicDigits(114))
        assertEquals("٧", ArabicTextUtils.toArabicDigits(7))
    }

    @Test
    fun qiraatCatalog_containsTenImamsAndTwentyRawis() {
        assertEquals(10, QiraatCatalog.allTenImams.size)
        val totalRawis = QiraatCatalog.allTenImams.sumOf { it.rawis.size }
        assertEquals(20, totalRawis)
    }
}
