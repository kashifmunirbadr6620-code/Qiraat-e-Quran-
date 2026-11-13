package com.example.data.model

import com.squareup.moshi.JsonClass

data class RiwayahInfo(
    val code: String,
    val label: String,
    val qari: String,
    val countingSchool: String,
    val description: String
)

data class RawiItem(
    val name: String,
    val liveScriptCode: String? = null,
    val signatureNote: String
)

data class QariImamInfo(
    val number: Int,
    val name: String,
    val city: String,
    val countingSchool: String,
    val rawis: List<RawiItem>,
    val keyUsul: String
)

@JsonClass(generateAdapter = true)
data class QiraatDiffItem(
    val reading: String,
    val rule: String,
    val readers: String,
    val note: String = ""
)

data class QiraatDiffEntry(
    val id: Long = 0,
    val surahId: Int,
    val ayahHafs: Int,
    val wordTitle: String,
    val matchTokens: List<String>,
    val items: List<QiraatDiffItem>,
    val isUserEdited: Boolean = false
)

object QiraatCatalog {
    val liveRiwayat = listOf(
        RiwayahInfo(
            code = "uthmani",
            label = "حفص عن عاصم",
            qari = "عاصم الکوفی",
            countingSchool = "کوفی شمار",
            description = "کوفی شمار • مصحفِ مدینہ (سب سے عام)"
        ),
        RiwayahInfo(
            code = "shubah",
            label = "شعبہ عن عاصم",
            qari = "عاصم الکوفی",
            countingSchool = "کوفی شمار",
            description = "کوفی شمار • عاصم کے پہلے راوی"
        ),
        RiwayahInfo(
            code = "warsh",
            label = "ورش عن نافع",
            qari = "نافع المدنی",
            countingSchool = "مدنی ثانی",
            description = "مدنی شمار • نقل، مدِ بدل، تغلیظِ لام"
        ),
        RiwayahInfo(
            code = "qalun",
            label = "قالون عن نافع",
            qari = "نافع المدنی",
            countingSchool = "مدنی اول/ثانی",
            description = "مدنی شمار • صلہ میمِ جمع بخلف، قصرِ منفصل"
        ),
        RiwayahInfo(
            code = "duri",
            label = "الدوری عن ابی عمرو",
            qari = "ابو عمرو البصری",
            countingSchool = "بصری شمار",
            description = "بصری شمار • امالہ، توسط و قصر"
        ),
        RiwayahInfo(
            code = "susi",
            label = "السوسی عن ابی عمرو",
            qari = "ابو عمرو البصری",
            countingSchool = "بصری شمار",
            description = "بصری شمار • ادغامِ کبیر، ابدالِ ہمزہ"
        ),
        RiwayahInfo(
            code = "hafs-nastaliq",
            label = "حفص (خطِ نستعلیق)",
            qari = "عاصم الکوفی",
            countingSchool = "کوفی شمار",
            description = "برصغیر پاک و ہند کا معروف رسم الخط"
        )
    )

    val allTenImams = listOf(
        QariImamInfo(
            number = 1,
            name = "امام نافع المدنی",
            city = "مدینہ منورہ",
            countingSchool = "مدنی اول / مدنی ثانی",
            rawis = listOf(
                RawiItem("قالون", "qalun", "قصر و توسطِ منفصل، صلہ میمِ جمع بخلف"),
                RawiItem("ورش", "warsh", "نقلِ حرکت، مدِ بدل (2/4/6)، تقلیل و تغلیظِ لام")
            ),
            keyUsul = "مَلِكِ يَوْمِ الدِّينِ، بسم اللہ الگ آیت شمار نہیں"
        ),
        QariImamInfo(
            number = 2,
            name = "امام ابن کثیر المکی",
            city = "مکہ مکرمہ",
            countingSchool = "مکی شمار",
            rawis = listOf(
                RawiItem("البزی", null, "صلہ میمِ جمع، صلہ ہائے کنایہ، تشدیدِ تاء بخلف"),
                RawiItem("قنبل", null, "السِّرَاطَ (خالص سین)، صلہ میمِ جمع و ہائے کنایہ")
            ),
            keyUsul = "صلہ میمِ جمع و ہائے کنایہ، قصرِ منفصل"
        ),
        QariImamInfo(
            number = 3,
            name = "امام ابو عمرو البصری",
            city = "بصرہ",
            countingSchool = "بصری شمار",
            rawis = listOf(
                RawiItem("الدوری", "duri", "امالہ کبریٰ (الفاظِ مخصوصہ)، قصر و توسط"),
                RawiItem("السوسی", "susi", "ادغامِ کبیر، ابدالِ ہمزہ ساکنہ")
            ),
            keyUsul = "امالہ، ادغامِ کبیر (سوسی)، مَلِكِ يَوْمِ الدِّينِ"
        ),
        QariImamInfo(
            number = 4,
            name = "امام ابن عامر الشامی",
            city = "دمشق (شام)",
            countingSchool = "شامی شمار",
            rawis = listOf(
                RawiItem("ہشام", null, "تغییرِ ہمزہ عند الوقف، توسطِ متصل و منفصل"),
                RawiItem("ابن ذکوان", null, "سکتہ کے بغیر، بعض الفاظ میں امالہ")
            ),
            keyUsul = "شامی عدّ الآي، مَلِكِ يَوْمِ الدِّينِ"
        ),
        QariImamInfo(
            number = 5,
            name = "امام عاصم الکوفی",
            city = "کوفہ",
            countingSchool = "کوفی شمار",
            rawis = listOf(
                RawiItem("شعبہ", "shubah", "بیوت، رضوان، لدنی میں خاص وجوہ، مَالِكِ"),
                RawiItem("حفص", "uthmani", "مَالِكِ يَوْمِ الدِّينِ، بسم اللہ پہلی آیت")
            ),
            keyUsul = "مَالِكِ (الف کے ساتھ)، کوفی شمار (6236 آیات)"
        ),
        QariImamInfo(
            number = 6,
            name = "امام حمزہ الکوفی",
            city = "کوفہ",
            countingSchool = "کوفی شمار",
            rawis = listOf(
                RawiItem("خلف عن حمزہ", null, "بغیر غنہ ادغامِ لام و را، الصِّرَاطَ میں إشمام"),
                RawiItem("خلاد", null, "پہلی الصِّرَاطَ میں إشمام، عَلَيْهُمْ، توسطِ مد")
            ),
            keyUsul = "عَلَيْهُمْ (ضمِ ہاء)، اشباعِ مد، وقف پر ہمزہ کی تخفیف"
        ),
        QariImamInfo(
            number = 7,
            name = "امام الکسائی الکوفی",
            city = "کوفہ",
            countingSchool = "کوفی شمار",
            rawis = listOf(
                RawiItem("ابو الحارث", null, "امالہ ہائے تانیث، مَالِكِ يَوْمِ الدِّينِ"),
                RawiItem("الدوری عن الکسائی", null, "امالہ کبریٰ و ہائے تانیث، توسطِ مد")
            ),
            keyUsul = "مَالِكِ (الف کے ساتھ)، ہائے تانیث پر امالہ"
        ),
        QariImamInfo(
            number = 8,
            name = "امام ابو جعفر المدنی",
            city = "مدینہ منورہ",
            countingSchool = "مدنی شمار",
            rawis = listOf(
                RawiItem("ابن وردان", null, "صلہ میمِ جمع، ابدالِ ہمزہ ساکنہ، المٓ میں سکت"),
                RawiItem("ابن جماز", null, "صلہ میمِ جمع، قصرِ منفصل، حروفِ مقطعات میں سکت")
            ),
            keyUsul = "حروفِ مقطعات پر سکت، صلہ میمِ جمع، ابدالِ ہمزہ"
        ),
        QariImamInfo(
            number = 9,
            name = "امام یعقوب الحضرمی",
            city = "بصرہ",
            countingSchool = "بصری شمار",
            rawis = listOf(
                RawiItem("رویس", null, "السِّرَاطَ (خالص سین)، عَلَيْهُمْ، مَالِكِ"),
                RawiItem("روح", null, "الصِّرَاطَ (خالص صاد)، عَلَيْهُمْ، مَالِكِ")
            ),
            keyUsul = "عَلَيْهُمْ (ضمِ ہاء)، مَالِكِ، ہائے سکت"
        ),
        QariImamInfo(
            number = 10,
            name = "امام خلف العاشر",
            city = "بغداد / کوفہ",
            countingSchool = "کوفی شمار",
            rawis = listOf(
                RawiItem("اسحاق", null, "مَالِكِ، الصِّرَاطَ (خالص صاد)، توسطِ مد"),
                RawiItem("ادریس", null, "سکتِ مفصول بخلف، مَالِكِ يَوْمِ الدِّينِ")
            ),
            keyUsul = "مَالِكِ (الف کے ساتھ)، خالص صاد، عَلَيْهِمْ"
        )
    )

    val allQurraAndRuwatChips = listOf(
        "نافع", "قالون", "ورش",
        "ابن کثیر", "بزی", "قنبل",
        "ابو عمرو", "دوری عن ابی عمرو", "سوسی",
        "ابن عامر", "ہشام", "ابن ذکوان",
        "عاصم", "شعبہ", "حفص",
        "حمزہ", "خلف عن حمزہ", "خلاد",
        "کسائی", "ابو الحارث", "دوری کسائی",
        "ابو جعفر", "ابن وردان", "ابن جماز",
        "یعقوب", "رویس", "روح",
        "خلف العاشر", "اسحاق", "ادریس"
    )

    val ruleTypes = listOf(
        "فرش الحروف",
        "أصول القراءات",
        "أصول (مد و قصر)",
        "أصول (امالہ و تقلیل)",
        "أصول (صلہ میمِ جمع)",
        "أصول (ہمزہ و ابدال)",
        "أصول (ادغام)"
    )
}

object ArabicTextUtils {
    private val diacriticsRegex = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u0640]")

    fun normalizeArabic(input: String): String {
        return input
            .replace(diacriticsRegex, "")
            .replace("ٱ", "ا")
            .replace("أ", "ا")
            .replace("إ", "ا")
            .replace("آ", "ا")
            .trim()
    }

    fun toArabicDigits(number: Int): String {
        val digits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return number.toString().map { ch ->
            if (ch.isDigit()) digits[ch - '0'] else ch
        }.joinToString("")
    }

    fun isMeccan(type: String): Boolean {
        val lower = type.lowercase()
        return lower.contains("mek") || lower.contains("mak")
    }
}
