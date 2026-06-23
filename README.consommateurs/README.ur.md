<!-- translated from README.md rev 0.0.1 -->
# slider-gradle — صارفین کی رہنمائی

> RAG کے ساتھ بڑھا ہوا Gradle پلگ ان جو AsciiDoc ذرائع سے Reveal.js پیشکشات تیار کرتا ہے۔

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-not%20configured-lightgrey?label=CI)](#)
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.slider`
- **Toolchain**: Java 24+ · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew asciidoctorRevealJs` · **Tests**: `./gradlew test` (JUnit5) + `cucumberTest` (Cucumber) + `functionalTest` (Gradle Test Kit)
- **Coverage**: not measured (no Kover)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | **اردو**

---

## یہ کیا کرتا ہے

`slider-gradle` AsciiDoc ذرائع کو انٹرایکٹو **Reveal.js** HTML
پیشکشات میں مرتب کرتا ہے، اور اختیاری طور پر pgvector اسٹور پر مبنی دو مرحلہ **RAG + LLM** پائپ لائن کے ذریعے قدرتی زبان کے مضوعات سے مکمل ڈیک تیار کرتا ہے۔ ایک LLM انسانی جائزے کے لیے
`*-deck-context.yml` تجویز کرتا ہے، پھر پروجیکٹ کی مثالوں سے مالامال حتمی AsciiDoc ڈیک
تیار کرتا ہے۔

یہ CCCP Education ملٹی پلگ ان ماحولیاتی نظام کا حصہ ہے (MIAMI borough, N2) اور
`codebase-gradle` (plugin id `education.cccp.codebase` version `0.0.1`) استعمال کرتا ہے۔

```
subject → proposeDeckContext (RAG+LLM) → *-deck-context.yml → review → generateDeck (RAG+LLM) → <slug>_<lang>-deck.adoc → asciidoctorRevealJs → HTML deck
```

## فوری آغاز

### 1. پلگ ان لاگو کریں

```gradle
plugins {
    id("education.cccp.slider") version "0.0.1"
}
```

### 2. DSL کو ترتیب دیں

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

پہلی چلانے پر پلگ ان ایک بنڈل شدہ
`slides.zip` سے `slides/` فولڈر، ایک طے شدہ `slides-context.yml` اور استعمال کے لیے تیار
`slides/misc/example-deck-context.yml` کا scaffold بناتا ہے۔ موجودہ مواد کبھی اووررائٹ نہیں ہوتا۔

### 3. سلائیڈز مرتب کریں

```bash
./gradlew asciidoctorRevealJs
```

آؤٹ پٹ `build/docs/asciidocRevealJs/` میں لکھا جاتا ہے (ہر ڈیک کے لیے ایک HTML فائل، اس کے علاوہ ایک
`index.html` ڈیش بورڈ اور ایک `slides.json` مینیفیسٹ)۔

## دستیاب کام

| Task | Group | Description |
|------|-------|-------------|
| `asciidoctorRevealJs`     | generate | AsciiDoc ذرائع کو Reveal.js HTML پیشکش میں مرتب کرتا ہے (`cleanBuild` پر منحصر)۔ |
| `asciidoctor`             | generate | معیاری Asciidoctor HTML تبدیلی (`asciidoctorRevealJs` پر منحصر)۔ |
| `cleanBuild`              | build    | تیار کردہ پیشکش دستاویزات حذف کرتا ہے (`slides.json`, `images/`, `.html`)۔ |
| `generateDashboard`       | generate | ہر ڈیک کو فہرست کرتے ہوئے `index.html` اور `slides.json` تیار کرتا ہے (`asciidoctorRevealJs` مکمل کرتا ہے)۔ |
| `serveSlides`             | info     | `npx serve` کے ذریعے تیار ڈیک مقامی طور پر پیش کرتا ہے (`asciidoctorRevealJs` پر منحصر)۔ |
| `deploySlides`            | deploy   | تیار سلائیڈز کو `slides-context.yml` میں ترتیب کردہ ریموٹ Git ریپوزیٹری میں تعینات کرتا ہے (`asciidoctor` پر منحصر)۔ |
| `generateCapsule`         | generate | AsciiDoc ڈیک سے اسپیکر نوٹس نکال کر `build/capsule/` میں ویڈیو اسکرپٹ بناتا ہے۔ |
| `installPlaywright`       | setup    | بصری امتحان کے لیے Playwright Chromium براؤزر انسٹال کرتا ہے۔ |
| `visualTest`              | slider   | تیار سلائیڈز پر Playwright بصری اسنیپ شاٹ امتحان چلاتا ہے۔ |
| `reportTests`             | verify   | `check` چلاتا ہے اور Firefox میں یونٹ امتحان رپورٹ کھولتا ہے۔ |
| `reportFunctionalTests`   | verify   | `check` چلاتا ہے اور Firefox میں فنکشنل امتحان رپورٹ کھولتا ہے۔ |
| `collectRagIndex`         | collect  | RAG ایمبیڈنگ انڈیکس کی مکمل تعمیر نو پر مجبور کرتا ہے۔ |
| `generateDeckContext`     | generate | RAG + LLM استعمال کرتے ہوئے کسی موضوع کے لیے `*-deck-context.yml` تجویز کرتا ہے (مرحلہ 1/2)۔ |
| `generateDeck`            | generate | `*-deck-context.yml` سے مکمل AsciiDoc/Reveal.js ڈیک تیار کرتا ہے (مرحلہ 2/2)۔ |
| `helloOllama*` / `helloOllamaStream*`       | slider-ai | Ollama ماڈل اسموک امتحان۔ |
| `helloGemini*` / `helloGeminiStream*`       | slider-ai | Gemini ماڈل اسموک امتحان۔ |
| `helloMistral*` / `helloMistralStream*`     | slider-ai | Mistral ماڈل اسموک امتحان۔ |
| `helloHuggingFace*` / `helloHuggingFaceStream*` | slider-ai | HuggingFace ماڈل اسموک امتحان۔ |

> پروجیکٹ KDoc میں حوالہ دیے گئے پلگ ان IDs (`reindexRag`, `proposeDeckContext`)
> درحقیقت بالترتیب **`collectRagIndex`** اور **`generateDeckContext`** کے طور پر رجسٹرڈ ہیں۔
> کمانڈ لائن پر رجسٹرڈ نام استعمال کریں۔

## توسیعی DSL

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

Reveal.js ورژن **5.2.0** پر مقفل ہے (gem `asciidoctor-revealjs:5.2.0@gem`)
`hakimel/reveal.js` ٹیگ `5.2.1` کے مقابلے میں اور `talaria.css` تھیم کے ساتھ ترتیب دیا گیا ہے۔ DSL ایک واحد `configPath` خصوصیت فراہم کرتا ہے؛
ایڈوانسڈ ڈیک اختیارات (تھیم، تبدیلیاں، نوٹس) ہر deck-context YAML میں رہتے ہیں، DSL میں نہیں۔

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

## عام ورک فلوز

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

> RAG کام ہمیشہ `--no-daemon` کے ساتھ چلائیں: Gradle daemon JVM کو دوبارہ استعمال کرتا ہے اور
> native ONNX لائبریری (`libtokenizers.so`) کو دوبارہ لوڈ ہونے سے روکتا ہے، جس سے دوسری بلڈ پر
> `UnsatisfiedLinkError` ہوتا ہے۔

## Provider انتخاب

| `-Pai.provider` | Default model | API key location |
|-----------------|---------------|------------------|
| `ollama` _(default)_ | `smollm:135m` | none — local inference |
| `gemini` | `gemini-2.5-flash` | `slides-context.yml` → `ai.gemini[0]` |
| `mistral` | `mistral-small-latest` | `slides-context.yml` → `ai.mistral[0]` |
| `huggingface` | `Llama-3.1-8B-Instruct:sambanova` | `slides-context.yml` → `ai.huggingface[0]` |

نامعلوم یا غائب اقدار ایک لاگ شدہ تنبیہ کے ساتھ `ollama` پر لوٹ جاتی ہیں۔

## فائل نام کنونشن

| File | Pattern | Example |
|------|---------|---------|
| Generation context | `<slug>-deck-context.yml` | `kotlin-inline-functions-and-reification-deck-context.yml` |
| Generated AsciiDoc deck | `<slug>_<lang>-deck.adoc` | `kotlin-inline-functions-and-reification_en-deck.adoc` |
| Compiled HTML deck | `<slug>_<lang>-deck.html` | `kotlin-inline-functions-and-reification_en-deck.html` |

`<slug>` کو `subject` سے kebab-case میں اخذ کیا جاتا ہے (لہجے معمول پر لائے گئے); `<lang>` وہ
ISO 639-1 کوڈ ہے جو `-Planguage` کے ذریعے پاس کیا جاتا ہے (طے شدہ `fr`)۔

## پیشگی شرائط

- **Java** 24+ (پلگ ان رن ٹائم پر Java 23+ کی حفاظت کرتا ہے; Kotlin 2.3.20 toolchain)
- **Gradle** 9.5.1+
- **Node.js / npx** `serveSlides` اور `visualTest` کاموں کے لیے
- **Docker** RAG پائپ لائن کے استعمال کردہ pgvector کنٹینر کے لیے
- **Internet connection** Reveal.js gem انحصار ڈاؤن لوڈ کرنے کے لیے

## بلڈ اور امتحان

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

## مسئلہ حل

| Symptom | Fix |
|---------|-----|
| `UnsatisfiedLinkError: libtokenizers.so` on second RAG run | run RAG tasks with `--no-daemon` |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| pgvector container stuck | `docker rm -f $(docker ps -q -f name=pgvector)` then retry |
| `asciidoctorRevealJs` Configuration Cache failure | expected — Configuration Cache is disabled (`configurationCache = false` on the portal) |
| `npx: command not found` | install Node.js and ensure `npx` is on `PATH` |

مکمل تعمیراتی حوالہ کے لیے [README.adoc](../README.adoc) دیکھیں۔

## License

Apache License 2.0 — [LICENCE](../LICENCE) دیکھیں۔

---

_CCCP Education ماحولیاتی نظام کا حصہ — `groupId: education.cccp`۔_