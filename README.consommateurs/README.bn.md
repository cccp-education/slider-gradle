<!-- translated from README.md rev 0.0.1 -->
# slider-gradle — ভোক্তা নির্দেশিকা

> RAG-বর্ধিত Gradle প্লাগইন যা AsciiDoc উৎস থেকে Reveal.js উপস্থাপনা তৈরি করে।

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-not%20configured-lightgrey?label=CI)](#)
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.slider`
- **Toolchain**: Java 24+ · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew asciidoctorRevealJs` · **Tests**: `./gradlew test` (JUnit5) + `cucumberTest` (Cucumber) + `functionalTest` (Gradle Test Kit)
- **Coverage**: not measured (no Kover)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | **বাংলা** | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## এটি কী করে

`slider-gradle` AsciiDoc উৎসকে ইন্টারঅ্যাক্টিভ **Reveal.js** HTML
উপস্থাপনায় কম্পাইল করে এবং pgvector স্টোরে সমর্থিত দুই-ধাপের **RAG + LLM** পাইপলাইনের মাধ্যমে স্বাভাবিক-ভাষা বিষয় থেকে ঐচ্ছিকভাবে সম্পূর্ণ ডেক তৈরি করে। একটি LLM মানব পর্যালোচনার জন্য
`*-deck-context.yml` প্রস্তাব করে, তারপর প্রকল্পের উদাহরণ দ্বারা সমৃদ্ধ চূড়ান্ত AsciiDoc ডেক
তৈরি করে।

এটি CCCP Education মাল্টি-প্লাগইন ইকোসিস্টেমের অংশ (MIAMI borough, N2) এবং
`codebase-gradle` (plugin id `education.cccp.codebase` version `0.0.1`) গ্রহণ করে।

```
subject → proposeDeckContext (RAG+LLM) → *-deck-context.yml → review → generateDeck (RAG+LLM) → <slug>_<lang>-deck.adoc → asciidoctorRevealJs → HTML deck
```

## দ্রুত শুরু

### 1. প্লাগইন প্রয়োগ করুন

```gradle
plugins {
    id("education.cccp.slider") version "0.0.1"
}
```

### 2. DSL কনফিগার করুন

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

প্রথম চালানোর সময় প্লাগইন একটি বান্ডেলকৃত
`slides.zip` থেকে `slides/` ফোল্ডার, একটি ডিফল্ট `slides-context.yml` এবং একটি ব্যবহার-প্রস্তুত
`slides/misc/example-deck-context.yml` স্ক্যাফোল্ড করে। বিদ্যমান বিষয়বস্তু কখনো ওভাররাইট হয় না।

### 3. স্লাইডগুলি কম্পাইল করুন

```bash
./gradlew asciidoctorRevealJs
```

আউটপুট `build/docs/asciidocRevealJs/`-এ লেখা হয় (প্রতিটি ডেকের জন্য একটি HTML ফাইল, পাশাপাশি একটি
`index.html` ড্যাশবোর্ড এবং একটি `slides.json` ম্যানিফেস্ট)।

## উপলব্ধ কাজ

| Task | Group | Description |
|------|-------|-------------|
| `asciidoctorRevealJs`     | generate | AsciiDoc উৎসকে Reveal.js HTML উপস্থাপনায় কম্পাইল করে (`cleanBuild`-এর উপর নির্ভর করে)। |
| `asciidoctor`             | generate | মানক Asciidoctor HTML রূপান্তর (`asciidoctorRevealJs`-এর উপর নির্ভর করে)। |
| `cleanBuild`              | build    | তৈরি করা উপস্থাপনা শিল্পকর্ম মোছে (`slides.json`, `images/`, `.html`)। |
| `generateDashboard`       | generate | প্রতিটি ডেক তালিকাভুক্ত করে `index.html` এবং `slides.json` তৈরি করে (`asciidoctorRevealJs` চূড়ান্ত করে)। |
| `serveSlides`             | info     | `npx serve` এর মাধ্যমে তৈরি ডেক স্থানীয়ভাবে পরিবেশন করে (`asciidoctorRevealJs`-এর উপর নির্ভর করে)। |
| `deploySlides`            | deploy   | তৈরি স্লাইডগুলি `slides-context.yml`-এ কনফিগার করা দূরবর্তী Git সংগ্রহস্থলে স্থাপন করে (`asciidoctor`-এর উপর নির্ভর করে)। |
| `generateCapsule`         | generate | AsciiDoc ডেক থেকে বক্তার নোট বের করে `build/capsule/`-এ ভিডিও স্ক্রিপ্ট তৈরি করে। |
| `installPlaywright`       | setup    | ভিজ্যুয়াল পরীক্ষার জন্য Playwright Chromium ব্রাউজার ইনস্টল করে। |
| `visualTest`              | slider   | তৈরি স্লাইডে Playwright ভিজ্যুয়াল স্ন্যাপশট পরীক্ষা চালায়। |
| `reportTests`             | verify   | `check` চালায় এবং Firefox-এ ইউনিট পরীক্ষা প্রতিবেদন খোলে। |
| `reportFunctionalTests`   | verify   | `check` চালায় এবং Firefox-এ কার্যকরী পরীক্ষা প্রতিবেদন খোলে। |
| `collectRagIndex`         | collect  | RAG এম্বেডিং সূচকের সম্পূর্ণ পুনর্নির্মাণ বাধ্য করে। |
| `generateDeckContext`     | generate | RAG + LLM ব্যবহার করে একটি বিষয়ের জন্য `*-deck-context.yml` প্রস্তাব করে (ধাপ 1/2)। |
| `generateDeck`            | generate | একটি `*-deck-context.yml` থেকে সম্পূর্ণ AsciiDoc/Reveal.js ডেক তৈরি করে (ধাপ 2/2)। |
| `helloOllama*` / `helloOllamaStream*`       | slider-ai | Ollama মডেল স্মোক পরীক্ষা। |
| `helloGemini*` / `helloGeminiStream*`       | slider-ai | Gemini মডেল স্মোক পরীক্ষা। |
| `helloMistral*` / `helloMistralStream*`     | slider-ai | Mistral মডেল স্মোক পরীক্ষা। |
| `helloHuggingFace*` / `helloHuggingFaceStream*` | slider-ai | HuggingFace মডেল স্মোক পরীক্ষা। |

> প্রকল্প KDoc-এ উল্লেখিত প্লাগইন ID (`reindexRag`, `proposeDeckContext`)
> আসলে যথাক্রমে **`collectRagIndex`** এবং **`generateDeckContext`** হিসাবে নিবন্ধিত।
> কমান্ড লাইনে নিবন্ধিত নাম ব্যবহার করুন।

## এক্সটেনশন DSL

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

Reveal.js সংস্করণ **5.2.0**-এ নির্ধারিত (gem `asciidoctor-revealjs:5.2.0@gem`)
`hakimel/reveal.js` ট্যাগ `5.2.1` এবং `talaria.css` থিম দিয়ে কনফিগার করা। DSL একটি একক `configPath` বৈশিষ্ট্য প্রকাশ করে; উন্নত ডেক বিকল্প
(থিম, ট্রানজিশন, নোট) প্রতিটি deck-context YAML-এ থাকে, DSL-এ নয়।

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

## সাধারণ ওয়ার্কফ্লো

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

> RAG কাজ সর্বদা `--no-daemon` দিয়ে চালান: Gradle ডেমন JVM পুনরায় ব্যবহার করে এবং
> নেটিভ ONNX লাইব্রেরি (`libtokenizers.so`) পুনরায় লোড করতে বাধা দেয়, যার ফলে দ্বিতীয় বিল্ডে
> `UnsatisfiedLinkError` ঘটে।

## Provider নির্বাচন

| `-Pai.provider` | Default model | API key location |
|-----------------|---------------|------------------|
| `ollama` _(default)_ | `smollm:135m` | none — local inference |
| `gemini` | `gemini-2.5-flash` | `slides-context.yml` → `ai.gemini[0]` |
| `mistral` | `mistral-small-latest` | `slides-context.yml` → `ai.mistral[0]` |
| `huggingface` | `Llama-3.1-8B-Instruct:sambanova` | `slides-context.yml` → `ai.huggingface[0]` |

অজানা বা অনুপস্থিত মান একটি লগ করা সতর্কতা সহ `ollama`-এ পশ্চাৎপাত করে।

## ফাইল নামকরণ সম্মেলন

| File | Pattern | Example |
|------|---------|---------|
| Generation context | `<slug>-deck-context.yml` | `kotlin-inline-functions-and-reification-deck-context.yml` |
| Generated AsciiDoc deck | `<slug>_<lang>-deck.adoc` | `kotlin-inline-functions-and-reification_en-deck.adoc` |
| Compiled HTML deck | `<slug>_<lang>-deck.html` | `kotlin-inline-functions-and-reification_en-deck.html` |

`<slug>` কে `subject` থেকে kebab-case-এ উদ্ভূত করা হয় (উচ্চারণ চিহ্ন স্বাভাবিকীকৃত); `<lang>` হল
`-Planguage` এর মাধ্যমে পাস করা ISO 639-1 কোড (ডিফল্ট `fr`)।

## পূর্বশর্ত

- **Java** 24+ (প্লাগইন রানটাইমে Java 23+ রক্ষা করে; Kotlin 2.3.20 টুলচেইন)
- **Gradle** 9.5.1+
- **Node.js / npx** `serveSlides` এবং `visualTest` কাজের জন্য
- **Docker** RAG পাইপলাইন দ্বারা ব্যবহৃত pgvector কন্টেইনারের জন্য
- **Internet connection** Reveal.js gem নির্ভরতা ডাউনলোড করতে

## বিল্ড ও পরীক্ষা

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

## সমস্যা সমাধান

| Symptom | Fix |
|---------|-----|
| `UnsatisfiedLinkError: libtokenizers.so` on second RAG run | run RAG tasks with `--no-daemon` |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| pgvector container stuck | `docker rm -f $(docker ps -q -f name=pgvector)` then retry |
| `asciidoctorRevealJs` Configuration Cache failure | expected — Configuration Cache is disabled (`configurationCache = false` on the portal) |
| `npx: command not found` | install Node.js and ensure `npx` is on `PATH` |

সম্পূর্ণ স্থাপত্য তথ্যসূত্রের জন্য [README.adoc](../README.adoc) দেখুন।

## License

Apache License 2.0 — [LICENCE](../LICENCE) দেখুন।

---

_CCCP Education ইকোসিস্টেমের অংশ — `groupId: education.cccp`।_