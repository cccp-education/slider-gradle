<!-- translated from README.md rev 0.0.1 -->
# slider-gradle — دليل المستهلك

> إضافة Gradle معزّزة بـ RAG تُنشئ عروض Reveal.js التقديمية من مصادر AsciiDoc.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-not%20configured-lightgrey?label=CI)](#)
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.slider`
- **Toolchain**: Java 24+ · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew asciidoctorRevealJs` · **Tests**: `./gradlew test` (JUnit5) + `cucumberTest` (Cucumber) + `functionalTest` (Gradle Test Kit)
- **Coverage**: not measured (no Kover)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | **العربية** | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## ماذا يفعل

`slider-gradle` يُترجم مصادر AsciiDoc إلى عروض **Reveal.js** HTML تفاعلية،
ويُنشئ اختياريًا عروضًا كاملة من مواضيع باللغة الطبيعية عبر خط أنابيب من خطوتين
**RAG + LLM** مدعوم بمخزن pgvector. يُقترح LLM ملف
`*-deck-context.yml` للمراجعة البشرية، ثم يُنشئ عرض AsciiDoc النهائي
مُعزّزًا بأمثلة من المشروع.

وهو جزء من منظومة CCCP Education متعددة الإضافات (MIAMI borough, N2) ويستهلك
`codebase-gradle` (plugin id `education.cccp.codebase` version `0.0.1`).

```
subject → proposeDeckContext (RAG+LLM) → *-deck-context.yml → review → generateDeck (RAG+LLM) → <slug>_<lang>-deck.adoc → asciidoctorRevealJs → HTML deck
```

## البداية السريعة

### 1. تطبيق الإضافة

```gradle
plugins {
    id("education.cccp.slider") version "0.0.1"
}
```

### 2. ضبط DSL

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

عند التشغيل الأول، تُنشئ الإضافة مجلد `slides/` (من
`slides.zip` مُدمج)، وملف `slides-context.yml` افتراضيًا، وملف
`slides/misc/example-deck-context.yml` جاهزًا للاستخدام. لا تُستبدل المحتويات الموجودة أبدًا.

### 3. ترجمة الشرائح

```bash
./gradlew asciidoctorRevealJs
```

تُكتب المخرجات إلى `build/docs/asciidocRevealJs/` (ملف HTML لكل عرض، بالإضافة إلى
لوحة `index.html` وبيان `slides.json`).

## المهام المتاحة

| Task | Group | Description |
|------|-------|-------------|
| `asciidoctorRevealJs`     | generate | يُترجم مصادر AsciiDoc إلى عرض Reveal.js HTML (يعتمد على `cleanBuild`). |
| `asciidoctor`             | generate | تحويل Asciidoctor HTML القياسي (يعتمد على `asciidoctorRevealJs`). |
| `cleanBuild`              | build    | يحذف مُخرجات العرض المُنشأة (`slides.json`, `images/`, `.html`). |
| `generateDashboard`       | generate | يُنشئ `index.html` و `slides.json` يُدرجان كل عرض (يُنهي `asciidoctorRevealJs`). |
| `serveSlides`             | info     | يخدم العرض المُنشأ محليًا عبر `npx serve` (يعتمد على `asciidoctorRevealJs`). |
| `deploySlides`            | deploy   | يُنشّر الشرائح المُنشأة إلى مستودع Git البعيد المُعد في `slides-context.yml` (يعتمد على `asciidoctor`). |
| `generateCapsule`         | generate | يستخرج ملاحظات المتحدث من عروض AsciiDoc إلى نص فيديو في `build/capsule/`. |
| `installPlaywright`       | setup    | يُثبّت متصفح Playwright Chromium للاختبارات البصرية. |
| `visualTest`              | slider   | يُشغّل اختبارات لقطات Playwright البصرية على الشرائح المُنشأة. |
| `reportTests`             | verify   | يُشغّل `check` ويفتح تقرير اختبارات الوحدة في Firefox. |
| `reportFunctionalTests`   | verify   | يُشغّل `check` ويفتح تقرير الاختبارات الوظيفية في Firefox. |
| `collectRagIndex`         | collect  | يفرض إعادة بناء كامل لمؤشر تضمين RAG. |
| `generateDeckContext`     | generate | يُقترح `*-deck-context.yml` لموضوع باستخدام RAG + LLM (الخطوة 1/2). |
| `generateDeck`            | generate | يُنشئ عرض AsciiDoc/Reveal.js كامل من `*-deck-context.yml` (الخطوة 2/2). |
| `helloOllama*` / `helloOllamaStream*`       | slider-ai | اختبارات دخان لنماذج Ollama. |
| `helloGemini*` / `helloGeminiStream*`       | slider-ai | اختبارات دخان لنماذج Gemini. |
| `helloMistral*` / `helloMistralStream*`     | slider-ai | اختبارات دخان لنماذج Mistral. |
| `helloHuggingFace*` / `helloHuggingFaceStream*` | slider-ai | اختبارات دخان لنماذج HuggingFace. |

> معرفات الإضافات المُشار إليها في KDoc للمشروع (`reindexRag`,
> `proposeDeckContext`) مُسجّلة فعليًا باسم **`collectRagIndex`** و
> **`generateDeckContext`** على التوالي. استخدم الأسماء المُسجّلة في سطر الأوامر.

## DSL الامتداد

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

Reveal.js مثبّت على الإصدار **5.2.0** (gem `asciidoctor-revealjs:5.2.0@gem`)
مقابل وسم `hakimel/reveal.js` `5.2.1` ومُعدّ بسمة `talaria.css`. يُعرض DSL خاصية `configPath` واحدة فقط؛
خيارات العرض المتقدمة (السمة، الانتقالات، الملاحظات) موجودة في كل YAML deck-context، وليس في DSL.

## deck-context.yml

```yaml
subject: "Kotlin inline functions and reification"
audience: "intermediate Kotlin developers"
duration: 60
language: "en"                          # ISO 639-1 code
outputFile: "kotlin-inline-functions-and-reification_en-deck.adoc"
author:
  name: "cheroliv"
  email: "cheroliv@example.com"
revealjs:
  theme: "sky"
  slideNumber: "c/t"
  width: 1408
  height: 792
notes:
  speakerNotes: true
  pageNotes: true
  pageNotesStyle: "DETAILED"            # MINIMAL | DETAILED | EXERCISES_ONLY
slides:
  - title: "Why inline?"
    speakerHint: "Start from the JVM cost of lambdas."
    pageNotesHint: "JMH benchmarks: inline vs non-inline."
```

## تدفقات العمل النموذجية

```bash
# Full AI pipeline — propose → review → generate → compile
./gradlew generateDeckContext \
  -Psubject="Kotlin inline functions and reification" \
  -Planguage=en \
  -Pai.provider=gemini \
  --no-daemon
#   review slides/misc/<slug>-deck-context.yml
./gradlew generateDeck \
  -Pdeck.context=slides/misc/<slug>-deck-context.yml \
  -Pai.provider=gemini \
  --no-daemon
./gradlew asciidoctorRevealJs serveSlides

# Local preview only
./gradlew serveSlides

# Clean rebuild
./gradlew cleanBuild asciidoctorRevealJs

# Publish to remote slides repository
./gradlew asciidoctorRevealJs deploySlides
```

> شغّل مهام RAG دائمًا بـ `--no-daemon`: خادم Gradle يعيد استخدام JVM و
> يمنع إعادة تحميل مكتبة ONNX الأصلية (`libtokenizers.so`)، مما يسبب
> `UnsatisfiedLinkError` عند البناء الثاني.

## اختيار Provider

| `-Pai.provider` | Default model | API key location |
|-----------------|---------------|------------------|
| `ollama` _(default)_ | `smollm:135m` | none — local inference |
| `gemini` | `gemini-2.5-flash` | `slides-context.yml` → `ai.gemini[0]` |
| `mistral` | `mistral-small-latest` | `slides-context.yml` → `ai.mistral[0]` |
| `huggingface` | `Llama-3.1-8B-Instruct:sambanova` | `slides-context.yml` → `ai.huggingface[0]` |

القيم غير المعروفة أو المفقودة ترجع إلى `ollama` مع تحذير مُسجّل.

## اصطلاح تسمية الملفات

| File | Pattern | Example |
|------|---------|---------|
| Generation context | `<slug>-deck-context.yml` | `kotlin-inline-functions-and-reification-deck-context.yml` |
| Generated AsciiDoc deck | `<slug>_<lang>-deck.adoc` | `kotlin-inline-functions-and-reification_en-deck.adoc` |
| Compiled HTML deck | `<slug>_<lang>-deck.html` | `kotlin-inline-functions-and-reification_en-deck.html` |

`<slug>` مُشتق من `subject` بصيغة kebab-case (تطبيع التشكيل); `<lang>` هو
رمز ISO 639-1 المُمرر عبر `-Planguage` (افتراضي `fr`).

## المتطلبات المسبقة

- **Java** 24+ (الإضافة تتطلب Java 23+ في وقت التشغيل; Kotlin 2.3.20 toolchain)
- **Gradle** 9.5.1+
- **Node.js / npx** لمهام `serveSlides` و `visualTest`
- **Docker** لحاوية pgvector التي يستخدمها خط أنابيب RAG
- **Internet connection** لتنزيل تبعيات Reveal.js gem

## البناء والاختبار

```bash
./gradlew asciidoctorRevealJs          # compile slides
./gradlew serveSlides                  # local preview
./gradlew cleanBuild                   # wipe build output
./gradlew test                         # JUnit5 unit tests (excludes Cucumber & functional)
./gradlew functionalTest               # GradleTestKit functional tests
./gradlew cucumberTest                 # Cucumber BDD feature files
./gradlew check                        # test + functionalTest + cucumberTest
./gradlew publishToMavenLocal          # local publish
```

## استكشاف الأخطاء وإصلاحها

| Symptom | Fix |
|---------|-----|
| `UnsatisfiedLinkError: libtokenizers.so` on second RAG run | run RAG tasks with `--no-daemon` |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| pgvector container stuck | `docker rm -f $(docker ps -q -f name=pgvector)` then retry |
| `asciidoctorRevealJs` Configuration Cache failure | expected — Configuration Cache is disabled (`configurationCache = false` on the portal) |
| `npx: command not found` | install Node.js and ensure `npx` is on `PATH` |

راجع [README.adoc](../README.adoc) للمرجع المعماري الكامل.

## License

Apache License 2.0 — راجع [LICENCE](../LICENCE).

---

_جزء من منظومة CCCP Education — `groupId: education.cccp`._