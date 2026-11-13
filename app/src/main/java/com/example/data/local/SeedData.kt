package com.example.data.local

import com.example.data.model.ArabicTextUtils
import com.example.data.model.QiraatDiffItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

object SeedData {
    val initialSurahs = listOf(
        CachedSurahEntity(1, "الفاتحة", "Al-Fatihah", "meccan", 7),
        CachedSurahEntity(2, "البقرة", "Al-Baqarah", "medinan", 286),
        CachedSurahEntity(18, "الكهف", "Al-Kahf", "meccan", 110),
        CachedSurahEntity(36, "يس", "Ya-Sin", "meccan", 83),
        CachedSurahEntity(55, "الرحمن", "Ar-Rahman", "medinan", 78),
        CachedSurahEntity(67, "الملك", "Al-Mulk", "meccan", 30),
        CachedSurahEntity(108, "الكوثر", "Al-Kawthar", "meccan", 3),
        CachedSurahEntity(109, "الكافرون", "Al-Kafirun", "meccan", 6),
        CachedSurahEntity(112, "الإخلاص", "Al-Ikhlas", "meccan", 4),
        CachedSurahEntity(113, "الفلق", "Al-Falaq", "meccan", 5),
        CachedSurahEntity(114, "الناس", "An-Nas", "meccan", 6)
    )

    private fun ayah(
        riwayah: String,
        surahId: Int,
        num: Int,
        hafsCsv: String,
        text: String
    ) = CachedAyahEntity(
        riwayahCode = riwayah,
        surahId = surahId,
        ayahNumber = num,
        numberInHafsCsv = hafsCsv,
        text = text,
        textClean = ArabicTextUtils.normalizeArabic(text)
    )

    val initialAyahs: List<CachedAyahEntity> = buildList {
        // Surah Al-Fatihah - Hafs & Shubah (Kufi count)
        val kufiScripts = listOf("uthmani", "shubah", "hafs-nastaliq")
        kufiScripts.forEach { script ->
            add(ayah(script, 1, 1, "1", "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"))
            add(ayah(script, 1, 2, "2", "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ"))
            add(ayah(script, 1, 3, "3", "الرَّحْمَٰنِ الرَّحِيمِ"))
            add(ayah(script, 1, 4, "4", "مَالِكِ يَوْمِ الدِّينِ"))
            add(ayah(script, 1, 5, "5", "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ"))
            add(ayah(script, 1, 6, "6", "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ"))
            add(ayah(script, 1, 7, "7", "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ"))
        }

        // Surah Al-Fatihah - Madani / Basri scripts (Warsh, Qalun, Duri, Susi)
        val nonKufiScripts = listOf("warsh", "qalun", "duri", "susi")
        nonKufiScripts.forEach { script ->
            add(ayah(script, 1, 1, "2", "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ"))
            add(ayah(script, 1, 2, "3", "الرَّحْمَٰنِ الرَّحِيمِ"))
            add(ayah(script, 1, 3, "4", "مَلِكِ يَوْمِ الدِّينِ"))
            add(ayah(script, 1, 4, "5", "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ"))
            add(ayah(script, 1, 5, "6", "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ"))
            add(ayah(script, 1, 6, "7", "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ"))
            add(ayah(script, 1, 7, "7", "غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ"))
        }

        // Surah Al-Ikhlas (112) in uthmani for immediate offline exploration
        add(ayah("uthmani", 112, 1, "1", "قُلْ هُوَ اللَّهُ أَحَدٌ"))
        add(ayah("uthmani", 112, 2, "2", "اللَّهُ الصَّمَدُ"))
        add(ayah("uthmani", 112, 3, "3", "لَمْ يَلِدْ وَلَمْ يُولَدْ"))
        add(ayah("uthmani", 112, 4, "4", "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ"))
    }

    fun initialDiffs(moshi: Moshi): List<QiraatDiffEntity> {
        val listType = Types.newParameterizedType(List::class.java, QiraatDiffItem::class.java)
        val adapter = moshi.adapter<List<QiraatDiffItem>>(listType)

        fun entity(
            surahId: Int,
            ayahHafs: Int,
            title: String,
            matches: List<String>,
            items: List<QiraatDiffItem>
        ) = QiraatDiffEntity(
            surahId = surahId,
            ayahHafs = ayahHafs,
            wordTitle = title,
            matchTokensCsv = matches.joinToString(","),
            itemsJson = adapter.toJson(items),
            isUserEdited = false
        )

        return listOf(
            entity(
                surahId = 1,
                ayahHafs = 4,
                title = "مَالِكِ / مَلِكِ",
                matches = listOf("مالك", "ملك"),
                items = listOf(
                    QiraatDiffItem(
                        reading = "مَالِكِ",
                        rule = "فرش الحروف",
                        readers = "عاصم، کسائی، یعقوب، خلف العاشر",
                        note = "میم کے بعد الف کے ساتھ"
                    ),
                    QiraatDiffItem(
                        reading = "مَلِكِ",
                        rule = "فرش الحروف",
                        readers = "نافع، ابن کثیر، ابو عمرو، ابن عامر، حمزہ، ابو جعفر",
                        note = "میم کے بعد بغیر الف کے"
                    )
                )
            ),
            entity(
                surahId = 1,
                ayahHafs = 6,
                title = "الصِّرَاطَ",
                matches = listOf("الصراط", "الصرط"),
                items = listOf(
                    QiraatDiffItem(
                        reading = "الصِّرَاطَ",
                        rule = "فرش الحروف",
                        readers = "جمہور قراء (نافع، عاصم، بزی، ابو عمرو، ابن عامر، کسائی، ابو جعفر، خلف العاشر)",
                        note = "خالص صاد کے ساتھ"
                    ),
                    QiraatDiffItem(
                        reading = "السِّرَاطَ",
                        rule = "فرش الحروف",
                        readers = "قنبل عن ابن کثیر، رویس عن یعقوب",
                        note = "صاد کی جگہ خالص سین کے ساتھ"
                    ),
                    QiraatDiffItem(
                        reading = "الصِّرَاطَ (إشمام)",
                        rule = "فرش الحروف",
                        readers = "خلف و خلاد عن حمزہ",
                        note = "صاد کو زا کی آواز ملا کر (إشمام)"
                    )
                )
            ),
            entity(
                surahId = 1,
                ayahHafs = 7,
                title = "صِرَاطَ",
                matches = listOf("صراط", "صرط"),
                items = listOf(
                    QiraatDiffItem(
                        reading = "صِرَاطَ",
                        rule = "فرش الحروف",
                        readers = "جمہور قراء مع خلاد عن حمزہ (مشہور وجہ)",
                        note = "خالص صاد کے ساتھ"
                    ),
                    QiraatDiffItem(
                        reading = "سِرَاطَ",
                        rule = "فرش الحروف",
                        readers = "قنبل عن ابن کثیر، رویس عن یعقوب",
                        note = "خالص سین کے ساتھ"
                    ),
                    QiraatDiffItem(
                        reading = "صِرَاطَ (إشمام)",
                        rule = "فرش الحروف",
                        readers = "خلف عن حمزہ",
                        note = "صاد کو زا کی آواز ملا کر"
                    )
                )
            ),
            entity(
                surahId = 1,
                ayahHafs = 7,
                title = "عَلَيْهِمْ",
                matches = listOf("عليهم", "عليهمو"),
                items = listOf(
                    QiraatDiffItem(
                        reading = "عَلَيْهِمْ",
                        rule = "أصول القراءات",
                        readers = "ورش، ابو عمرو، ابن عامر، عاصم، کسائی، خلف العاشر",
                        note = "ہاء کے کسرہ اور میمِ جمع کے سکون کے ساتھ"
                    ),
                    QiraatDiffItem(
                        reading = "عَلَيْهِمُو",
                        rule = "أصول (صلہ میمِ جمع)",
                        readers = "ابن کثیر، ابو جعفر، قالون (بخلف)",
                        note = "میمِ جمع کو واو کے صلہ کے ساتھ"
                    ),
                    QiraatDiffItem(
                        reading = "عَلَيْهُمْ",
                        rule = "أصول القراءات",
                        readers = "حمزہ (خلف و خلاد)، یعقوب (رویس و روح)",
                        note = "ہاء کے ضمہ (پیش) اور میمِ جمع کے سکون کے ساتھ"
                    )
                )
            ),
            entity(
                surahId = 2,
                ayahHafs = 2,
                title = "فِيهِ ۛ هُدًى",
                matches = listOf("فيه"),
                items = listOf(
                    QiraatDiffItem(
                        reading = "فِيهِ هُدًى",
                        rule = "أصول (ہائے کنایہ)",
                        readers = "جمہور قراء (نافع، عاصم، حمزہ، کسائی وغیرہ)",
                        note = "ہائے کنایہ کا بغیر صلہ"
                    ),
                    QiraatDiffItem(
                        reading = "فِيهِۦ هُدًى",
                        rule = "أصول (ہائے کنایہ)",
                        readers = "ابن کثیر (بزی و قنبل)",
                        note = "ہائے کنایہ کو یاء کے صلہ کے ساتھ (صلہ صغریٰ)"
                    )
                )
            ),
            entity(
                surahId = 2,
                ayahHafs = 3,
                title = "يُؤْمِنُونَ",
                matches = listOf("يؤمنون", "يومنون"),
                items = listOf(
                    QiraatDiffItem(
                        reading = "يُؤْمِنُونَ",
                        rule = "أصول (ہمزہ مفرد)",
                        readers = "حفص، قالون، ابن کثیر، ابن عامر، حمزہ، کسائی، خلف العاشر",
                        note = "تحقیقِ ہمزہ کے ساتھ"
                    ),
                    QiraatDiffItem(
                        reading = "يُومِنُونَ",
                        rule = "أصول (ابدالِ ہمزہ)",
                        readers = "ورش، السوسی، ابو جعفر",
                        note = "ہمزہ ساکنہ کو واو سے بدل کر (ابدال)"
                    )
                )
            ),
            entity(
                surahId = 112,
                ayahHafs = 4,
                title = "كُفُوًا",
                matches = listOf("كفوا", "كفؤا"),
                items = listOf(
                    QiraatDiffItem(
                        reading = "كُفُوًا",
                        rule = "فرش الحروف",
                        readers = "حفص عن عاصم، حمزہ (وقف کے علاوہ)",
                        note = "فاء کے ضمہ اور واوِ مفتوحہ کے ساتھ"
                    ),
                    QiraatDiffItem(
                        reading = "كُفُؤًا",
                        rule = "فرش الحروف",
                        readers = "نافع، ابن کثیر، ابو عمرو، ابن عامر، شعبہ، کسائی، ابو جعفر، یعقوب، خلف العاشر",
                        note = "فاء کے ضمہ اور ہمزہ کے ساتھ"
                    ),
                    QiraatDiffItem(
                        reading = "كُفْؤًا",
                        rule = "فرش الحروف",
                        readers = "حمزہ (ایک وجہ میں)",
                        note = "فاء کے سکون اور ہمزہ کے ساتھ"
                    )
                )
            )
        )
    }
}
