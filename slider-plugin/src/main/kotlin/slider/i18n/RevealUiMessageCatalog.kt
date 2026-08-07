package slider.i18n

import contracts.i18n.LanguageCatalog

/**
 * Catalog of [RevealUiMessages] for every language supported by [LanguageCatalog].
 *
 * Acts as a static message bundle factory: each call to [all] returns the
 * full immutable list of 10 localized UI message sets. The catalog is the
 * single source of truth for Reveal.js UI labels across the 10 supported
 * languages (en, zh, hi, es, fr, ar, bn, pt, ru, ur).
 *
 * Consumed by [RevealUiMessagesWriter] to generate `messages_{code}.js`
 * files consumed by the Reveal.js i18n plugin.
 */
object RevealUiMessageCatalog {

    /** Returns the [RevealUiMessages] for the given ISO 639-1 code, or null if unknown. */
    fun findByCode(code: String): RevealUiMessages? = all().find { it.languageCode == code }

    /** Returns the full list of [RevealUiMessages] for the 10 supported languages. */
    fun all(): List<RevealUiMessages> = listOf(
        RevealUiMessages(
            languageCode = "en",
            nav = RevealUiNavMessages(
                prev = "Previous slide",
                next = "Next slide",
                up = "Up slide",
                help = "Help",
            ),
            controls = RevealUiControlsMessages(
                overview = "Overview",
                speakerNotes = "Speaker notes",
                fullscreen = "Fullscreen",
            ),
        ),
        RevealUiMessages(
            languageCode = "zh",
            nav = RevealUiNavMessages(
                prev = "上一张幻灯片",
                next = "下一张幻灯片",
                up = "向上幻灯片",
                help = "帮助",
            ),
            controls = RevealUiControlsMessages(
                overview = "概览",
                speakerNotes = "演讲者备注",
                fullscreen = "全屏",
            ),
        ),
        RevealUiMessages(
            languageCode = "hi",
            nav = RevealUiNavMessages(
                prev = "पिछली स्लाइड",
                next = "अगली स्लाइड",
                up = "ऊपर स्लाइड",
                help = "सहायता",
            ),
            controls = RevealUiControlsMessages(
                overview = "ओवरव्यू",
                speakerNotes = "स्पीकर नोट्स",
                fullscreen = "फ़ुलस्क्रीन",
            ),
        ),
        RevealUiMessages(
            languageCode = "es",
            nav = RevealUiNavMessages(
                prev = "Diapositiva anterior",
                next = "Diapositiva siguiente",
                up = "Diapositiva superior",
                help = "Ayuda",
            ),
            controls = RevealUiControlsMessages(
                overview = "Vista general",
                speakerNotes = "Notas del orador",
                fullscreen = "Pantalla completa",
            ),
        ),
        RevealUiMessages(
            languageCode = "fr",
            nav = RevealUiNavMessages(
                prev = "Diapositive précédente",
                next = "Diapositive suivante",
                up = "Diapositive parente",
                help = "Aide",
            ),
            controls = RevealUiControlsMessages(
                overview = "Vue d'ensemble",
                speakerNotes = "Notes du présentateur",
                fullscreen = "Plein écran",
            ),
        ),
        RevealUiMessages(
            languageCode = "ar",
            nav = RevealUiNavMessages(
                prev = "الشريحة السابقة",
                next = "الشريحة التالية",
                up = "الشريحة الأصل",
                help = "مساعدة",
            ),
            controls = RevealUiControlsMessages(
                overview = "نظرة عامة",
                speakerNotes = "ملاحظات المتحدث",
                fullscreen = "ملء الشاشة",
            ),
        ),
        RevealUiMessages(
            languageCode = "bn",
            nav = RevealUiNavMessages(
                prev = "পূর্ববর্তী স্লাইড",
                next = "পরবর্তী স্লাইড",
                up = "উপরের স্লাইড",
                help = "সাহায্য",
            ),
            controls = RevealUiControlsMessages(
                overview = "ওভারভিউ",
                speakerNotes = "স্পিকার নোট",
                fullscreen = "পূর্ণ স্ক্রিন",
            ),
        ),
        RevealUiMessages(
            languageCode = "pt",
            nav = RevealUiNavMessages(
                prev = "Diapositivo anterior",
                next = "Diapositivo seguinte",
                up = "Diapositivo superior",
                help = "Ajuda",
            ),
            controls = RevealUiControlsMessages(
                overview = "Visão geral",
                speakerNotes = "Notas do apresentador",
                fullscreen = "Ecrã inteiro",
            ),
        ),
        RevealUiMessages(
            languageCode = "ru",
            nav = RevealUiNavMessages(
                prev = "Предыдущий слайд",
                next = "Следующий слайд",
                up = "Вверх",
                help = "Справка",
            ),
            controls = RevealUiControlsMessages(
                overview = "Обзор",
                speakerNotes = "Заметки докладчика",
                fullscreen = "Полный экран",
            ),
        ),
        RevealUiMessages(
            languageCode = "ur",
            nav = RevealUiNavMessages(
                prev = "پچھلا سلائیڈ",
                next = "اگلا سلائیڈ",
                up = "اوپر سلائیڈ",
                help = "مدد",
            ),
            controls = RevealUiControlsMessages(
                overview = "جائزہ",
                speakerNotes = "اسپیکر نوٹس",
                fullscreen = "پوری سکرین",
            ),
        ),
    )
}