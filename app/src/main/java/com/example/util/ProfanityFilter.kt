package com.example.util

import java.util.Locale

object ProfanityFilter {
    // List of common offensive words in Arabic (Fus'ha & dialects) and English
    private val badWords = setOf(
        // English
        "fuck", "shit", "asshole", "bitch", "bastard", "cunt", "dick", "pussy", "whore", "slut",
        
        // Arabic (Common insults, curse words, and offensive language)
        "كلب", "حمار", "قواد", "كلبة", "حيوان", "تفه", "تفاهة", "غبي", "غباء", "حقير",
        "سافل", "سافلة", "عرص", "خنيث", "متخلف", "مغفل", "بزاق", "قذر", "قذارة", "وسخ",
        "تيس", "يافع", "كلاب", "حمير", "بهيمة", "بهائم", "سرق", "حرامي", "لعنة", "ملعون",
        "ملعونة", "شرموط", "شرموطة", "قحبة", "كس", "زب", "طيز", "عرصة", "لعن", "يلعن"
    )

    /**
     * Normalizes Arabic text to standard letters for robust filtering.
     * Removes diacritics, maps variation of Alef to bare Alef, Yeh to bare Yeh, etc.
     */
    private fun normalizeArabic(text: String): String {
        var str = text
        // Remove diacritics (tashkeel)
        val diacritics = charArrayOf(
            '\u064B', '\u064C', '\u064D', '\u064E', '\u064F', '\u0650', '\u0651', '\u0652'
        )
        for (c in diacritics) {
            str = str.replace(c.toString(), "")
        }

        // Normalize Alef variations
        str = str.replace('\u0622', '\u0627') // ALEF WITH MADDA ABOVE -> ALEF
        str = str.replace('\u0623', '\u0627') // ALEF WITH HAMZA ABOVE -> ALEF
        str = str.replace('\u0625', '\u0627') // ALEF WITH HAMZA BELOW -> ALEF

        // Normalize Teh Marbuta
        str = str.replace('\u0629', '\u0647') // TEH MARBUTA -> HEH

        // Normalize Alef Maksura / Yeh
        str = str.replace('\u0649', '\u064A') // ALEF MAKSURA -> YEH

        return str
    }

    /**
     * Smart profanity filter check.
     * Returns true if the content contains any profane words.
     */
    fun hasProfanity(content: String): Boolean {
        if (content.isBlank()) return false

        // Normalize English
        val englishNormalized = content.lowercase(Locale.ROOT)

        // Normalize Arabic
        val arabicNormalized = normalizeArabic(englishNormalized)

        // Split words by common punctuation and spacing
        val words = arabicNormalized.split("\\s+|[.,!_?()\\-\\/\\\\*\\#]".toRegex())

        for (word in words) {
            val cleanWord = word.trim()
            if (cleanWord.length >= 2) {
                // Exact match check
                if (badWords.contains(cleanWord)) {
                    return true
                }
            }
        }

        // Substring check for offensive phrases/substrings
        for (badWord in badWords) {
            if (badWord.length > 2 && arabicNormalized.contains(badWord)) {
                // Ensure it's not a false positive within benign words (e.g., "حيوان" vs "حيوية" is different, simple check)
                if (badWord == "كلب" && arabicNormalized.contains("كلب")) return true
                if (badWord == "حمار" && arabicNormalized.contains("حمار")) return true
                if (badWord != "كلب" && badWord != "حمار" && badWord != "وسخ") {
                    return true
                }
            }
        }

        return false
    }
}
