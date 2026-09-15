package slider.i18n

import contracts.i18n.LanguageCatalog

/**
 * Catalog of [RevealUiMessages] for every language supported by [LanguageCatalog].
 *
 * Acts as a static message bundle factory: each call to [all] returns the
 * full immutable list of localized UI message sets. The catalog is the
 * single source of truth for Reveal.js UI labels across every supported
 * language exposed by [LanguageCatalog].
 *
 * Consumed by [RevealUiMessagesWriter] to generate `messages_{code}.js`
 * files consumed by the Reveal.js i18n plugin.
 */
object RevealUiMessageCatalog {

    /** Returns the [RevealUiMessages] for the given ISO 639-1 code, or null if unknown. */
    fun findByCode(code: String): RevealUiMessages? = all().find { it.languageCode == code }

    /** Returns the full list of [RevealUiMessages] for every supported language. */
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
        RevealUiMessages(
            languageCode = "it",
            nav = RevealUiNavMessages(
                prev = "Diapositiva precedente",
                next = "Diapositiva successiva",
                up = "Diapositiva superiore",
                help = "Aiuto",
            ),
            controls = RevealUiControlsMessages(
                overview = "Panoramica",
                speakerNotes = "Note del relatore",
                fullscreen = "Schermo intero",
            ),
        ),
        RevealUiMessages(
            languageCode = "nl",
            nav = RevealUiNavMessages(
                prev = "Vorige dia",
                next = "Volgende dia",
                up = "Bovenliggende dia",
                help = "Help",
            ),
            controls = RevealUiControlsMessages(
                overview = "Overzicht",
                speakerNotes = "Sprekernotities",
                fullscreen = "Volledig scherm",
            ),
        ),
        RevealUiMessages(
            languageCode = "de",
            nav = RevealUiNavMessages(
                prev = "Vorherige Folie",
                next = "Nächste Folie",
                up = "Übergeordnete Folie",
                help = "Hilfe",
            ),
            controls = RevealUiControlsMessages(
                overview = "Übersicht",
                speakerNotes = "Notizen",
                fullscreen = "Vollbild",
            ),
        ),
        RevealUiMessages(
            languageCode = "el",
            nav = RevealUiNavMessages(
                prev = "Προηγούμενη διαφάνεια",
                next = "Επόμενη διαφάνεια",
                up = "Γονική διαφάνεια",
                help = "Βοήθεια",
            ),
            controls = RevealUiControlsMessages(
                overview = "Επισκόπηση",
                speakerNotes = "Σημειώσεις ομιλητή",
                fullscreen = "Πλήρης οθόνη",
            ),
        ),
        RevealUiMessages(
            languageCode = "tr",
            nav = RevealUiNavMessages(
                prev = "Önceki slayt",
                next = "Sonraki slayt",
                up = "Üst slayt",
                help = "Yardım",
            ),
            controls = RevealUiControlsMessages(
                overview = "Genel bakış",
                speakerNotes = "Konuşmacı notları",
                fullscreen = "Tam ekran",
            ),
        ),
        RevealUiMessages(
            languageCode = "vi",
            nav = RevealUiNavMessages(
                prev = "Trang trước",
                next = "Trang tiếp theo",
                up = "Trang cha",
                help = "Trợ giúp",
            ),
            controls = RevealUiControlsMessages(
                overview = "Tổng quan",
                speakerNotes = "Ghi chú diễn giả",
                fullscreen = "Toàn màn hình",
            ),
        ),
        RevealUiMessages(
            languageCode = "th",
            nav = RevealUiNavMessages(
                prev = "สไลด์ก่อนหน้า",
                next = "สไลด์ถัดไป",
                up = "สไลด์แม่",
                help = "ช่วยเหลือ",
            ),
            controls = RevealUiControlsMessages(
                overview = "ภาพรวม",
                speakerNotes = "บันทึกผู้บรรยาย",
                fullscreen = "เต็มหน้าจอ",
            ),
        ),
        RevealUiMessages(
            languageCode = "id",
            nav = RevealUiNavMessages(
                prev = "Slide sebelumnya",
                next = "Slide berikutnya",
                up = "Slide induk",
                help = "Bantuan",
            ),
            controls = RevealUiControlsMessages(
                overview = "Ikhtisar",
                speakerNotes = "Catatan pembicara",
                fullscreen = "Layar penuh",
            ),
        ),
        RevealUiMessages(
            languageCode = "ko",
            nav = RevealUiNavMessages(
                prev = "이전 슬라이드",
                next = "다음 슬라이드",
                up = "상위 슬라이드",
                help = "도움말",
            ),
            controls = RevealUiControlsMessages(
                overview = "개요",
                speakerNotes = "발표자 노트",
                fullscreen = "전체 화면",
            ),
        ),
        RevealUiMessages(
            languageCode = "ja",
            nav = RevealUiNavMessages(
                prev = "前のスライド",
                next = "次のスライド",
                up = "親スライド",
                help = "ヘルプ",
            ),
            controls = RevealUiControlsMessages(
                overview = "概要",
                speakerNotes = "スピーカーノート",
                fullscreen = "全画面表示",
            ),
        ),
        RevealUiMessages(
            languageCode = "sr",
            nav = RevealUiNavMessages(
                prev = "Претходни слајд",
                next = "Следећи слајд",
                up = "Надређени слајд",
                help = "Помоћ",
            ),
            controls = RevealUiControlsMessages(
                overview = "Преглед",
                speakerNotes = "Белешке говорника",
                fullscreen = "Цео екран",
            ),
        ),
        RevealUiMessages(
            languageCode = "fa",
            nav = RevealUiNavMessages(
                prev = "اسلاید قبلی",
                next = "اسلاید بعدی",
                up = "اسلاید والد",
                help = "راهنما",
            ),
            controls = RevealUiControlsMessages(
                overview = "نمای کلی",
                speakerNotes = "یادداشت‌های سخنران",
                fullscreen = "تمام صفحه",
            ),
        ),
    )
}