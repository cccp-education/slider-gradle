<!-- translated from README.md rev 0.0.1 -->
# slider-gradle — প্লাগইন অভ্যন্তরীণ বিষয়

> `slider-plugin` Gradle প্লাগইনের ডেভেলপার ও অবদানকারী গাইড।

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-not%20configured-lightgrey?label=CI)](#)
[![Coverage](https://img.shields.io/static/v1?label=coverage&message=n%2Fa&color=lightgrey)]()
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.slider`
- **Toolchain**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew build -x test` · **Tests**: `./gradlew test` + `functionalTest` + `cucumberTest` (wired into `check`)
- **Coverage gate**: none (no Kover)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | **বাংলা** | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## মডিউল বিন্যাস

```
slider-plugin/
├── settings.gradle.kts          # nmcp settings (centralPortal), includeBuild("slider")
└── slider/
    ├── build.gradle.kts          # plugin definition, publication, signing, test setup
    └── src/
        ├── main/kotlin/
        │   ├── slider/
        │   │   ├── SliderPlugin.kt        # Plugin entry point — thin orchestrator
        │   │   ├── SliderManager.kt       # Prerequisites, Repositories, Plugins, Dependencies,
        │   │   │                            Extensions, Tasks, Git, FileOps (object DSL)
        │   │   ├── Slides.kt                # RevealJsSlides constants (task names, layout)
        │   │   └── models.kt                # SlidesConfiguration, DeckContext, AuthorContext…
        │   ├── slider/ai/
        │   │   ├── AssistantManager.kt     # LLM provider resolution, model catalogs, chat tasks
        │   │   ├── PgVectorService.kt       # BuildService — docker-java pgvector lifecycle
        │   │   ├── RagManager.kt             # RAG retrieval / reindex against pgvector
        │   │   ├── RagTask.kt                # Base class for RAG tasks (service injection)
        │   │   └── RagTasks.kt               # collectRagIndex, generateDeckContext, generateDeck
        │   └── slider/translate/
        │       ├── TranslatorManager.kt      # Translation orchestration
        │       └── TranslatorPlugin.kt       # Translation plugin
        ├── test/                              # JUnit5 unit tests + Cucumber features/steps
        │   ├── features/                      # 4 .feature files (BDD)
        │   └── scenarios/                     # Cucumber step definitions
        └── functionalTest/                    # GradleTestKit functional tests
```

## গৃহীত প্লাগইন (N2 → N2 নির্ভরতা)

- `education.cccp.codebase` version `0.0.1` (catalog alias
  `libs.plugins.codebase` in `slider-plugin/gradle/libs.versions.toml`) — `slider/build.gradle.kts`-এ সিবলিং প্লাগইন হিসাবে প্রয়োগ করা হয়েছে। এটি `RagManager` দ্বারা পুনরায় ব্যবহৃত EAGER/RAG ইনজেশন আদিম সরবরাহ করে।

## মূল নির্ভরতা

`slider/build.gradle.kts` `slider-plugin/gradle/libs.versions.toml` থেকে সংস্করণ সমাধান করে:

- **asciidoctor-gradle** `5.0.0-alpha.1` — `asciidoctor-gradle-jvm-slides`,
  `asciidoctor-gradle-jvm-gems`, `asciidoctor-gradle-jvm` (plugin ID
  `org.asciidoctor.jvm.revealjs.classic` এবং `org.asciidoctor.jvm.gems.classic` হিসাবে প্রয়োগ)।
- **asciidoctor-revealjs gem** `5.2.0` Reveal.js টেমপ্লেট
  `hakimel/reveal.js` ট্যাগ `5.2.1` এর বিপরীতে নির্ধারিত।
- **asciidoctorj-diagram** `3.2.0` + **asciidoctorj-diagram-plantuml** `1.2025.3`।
- **node-gradle** `7.1.0` — `serveSlides`, `installPlaywright`, `visualTest` এর জন্য `NpxTask`।
- **langchain4j** `1.14.1` + **langchain4j-beta** `1.14.1-beta24` — `langchain4j`,
  `langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
  `langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`।
- **JGit** `7.5.0.202512021534-r` — `org.eclipse.jgit`, `jgit.ssh.jsch`,
  `jgit.archive` (bundle)।
- **docker-java** `3.7.0` + **testcontainers-postgresql** `1.21.4` — pgvector lifecycle।
- **Jackson** `2.21.1` — `jackson-module-kotlin`, `jackson-dataformat-yaml`।
- **Arrow** `2.2.2` + `arrow-integrations-jackson-module` `0.15.1` — টাইপড FP সহায়ক।
- **kotlinx-coroutines** `1.10.2` — async পরীক্ষা ফিক্সচার (`core`, `jdk8`, `test`)।
- **Playwright** `1.52.0` — ভিজ্যুয়াল E2E পরীক্ষা (`testImplementation`)।
- **commons-io** `2.13.0`, **slf4j** `2.0.17`, **logback** `1.5.32`।

পরীক্ষা স্ট্যাক: **Kotless** JUnit5 (`kotlin-test-junit5`), **AssertJ** `3.27.7`,
**Mockito Kotlin** `6.2.3`, **Mockito JUnit Jupiter** `5.23.0`, **Cucumber**
`7.34.3` (`cucumber-java`, `cucumber-junit-platform-engine`, `cucumber-picocontainer`,
`junit-platform-suite`)।

## Ollama ইনস্ট্যান্স (বৈশ্বিক সীমাবদ্ধতা)

পোর্ট `11434–11436` নিষিদ্ধ। `11437–11465` (29 পোর্ট) উপর ঘোরান।
অনুমোদিত মডেল: `gpt-oss:120b-cloud`, `gemma4:31b-cloud`।
`AssistantManager.localModels`-এ স্থানীয়ভাবে উপলব্ধ মডেল:
`smollm:135m`, `llama3.2:3b-instruct-q8_0`, `smollm:135m-instruct-v0.2-q8_0`,
`gemma3:1b-it-fp16`।

## পরীক্ষা ম্যাট্রিক্স

| Task | Scope | Notes |
|------|-------|-------|
| `test` | JUnit5 unit tests | excludes `com.cheroliv.slider.scenarios.**` (Cucumber) and `SliderPluginFunctionalTests` |
| `functionalTest` | GradleTestKit functional tests | own source set, depends on `test` impl via `extendsFrom` trick |
| `cucumberTest` | Cucumber BDD feature files | uses JUnit Platform suite engine, excludes `junit-jupiter`, `dependsOn functionalTest.classesTaskName` |
| `check` | aggregates | `test + functionalTest + cucumberTest` |

`tasks.withType<Test>` `useJUnitPlatform()`, সম্পূর্ণ ব্যতিক্রম লগিং এবং
`-XX:+EnableDynamicAgentLoading` সক্ষম করে যাতে ডায়নামিক এজেন্ট সতর্কতা নীরব হয়। পরীক্ষা
classpath বাইন্ডিং দ্বন্দ্ব এড়াতে `logback-classic` বাদ দেয়।

**কোনো Kover** কভারেজ গেট এবং **কোনো সমর্পিত CVE অডিট কাজ** নেই।

## JVM টিউনিং

- প্লাগইন
  `slider/build.gradle.kts`-এ `jvmToolchain(JavaVersion.VERSION_24.ordinal)` আরোপ করে এবং রানটাইমে
  `SliderManager.Prerequisites.checkJavaVersion()`-এর মাধ্যমে Java 23+ নিশ্চিত করে।
- `gradle.properties` daemon এবং Configuration Cache নিষ্ক্রিয় করে:
  `org.gradle.daemon=false`, `org.gradle.configuration-cache=false`।
- একটি `buildscript.resolutionStrategy`
  `org.jetbrains:annotations:26.0.2-1` বাধ্য করে যাতে koog-utils ট্রানজিটিভ
  দ্বন্দ্ব মোকাবিলা করা যায় যা Gradle-এর `annotations:13.0` এর কঠোর পিন এড়িয়ে যায়।
- RAG কাজগুলি `--no-daemon` দিয়ে চলবে যাতে নেটিভ ONNX লাইব্রেরি পুনরায় লোড হয়।

## বিল্ড কমান্ড

```bash
./gradlew build                       # full build (compile + tests)
./gradlew build -x test                # compile only (skips test/functionalTest/cucumberTest)
./gradlew test                         # JUnit5 unit tests
./gradlew functionalTest               # GradleTestKit functional tests
./gradlew cucumberTest                 # Cucumber BDD scenarios
./gradlew check                         # test + functionalTest + cucumberTest
./gradlew asciidoctorRevealJs           # build slides from bundled sources
./gradlew publishToMavenLocal          # local publish
./gradlew publishAggregationToCentralPortal --no-daemon   # Maven Central
```

## CI পাইপলাইন

রিপোজিটরিতে `.github/workflows/` রয়েছে তবে পরীক্ষা ওয়ার্কফ্লো **কনফিগার করা
নেই** — শুধু ডকুমেন্টেশন সহায়ক (`readme_plantuml.yml`,
`readme_truth.yml`) এবং কমেন্ট-আউট টেমপ্লেট (`generate_deck.yml`,
`website.yml`)। বর্তমানে কোনো PR পরীক্ষা কাজ, কোনো কভারেজ ব্যাজ এবং কোনো
প্রকাশন কাজ নেই।

## প্রকাশন (NMCP)

`slider-plugin/settings.gradle.kts`-এ
`com.gradleup.nmcp.settings` (1.5.0) এর মাধ্যমে কনফিগার করা। শংসাপত্র
`~/.gradle/gradle.properties` (`ossrhUsername`, `ossrhPassword`) থেকে পড়া হয়，সাথে
`publishingType = "AUTOMATIC"`。

`slider/build.gradle.kts` প্লাগইন ব্লক যুক্ত করে:
- `gradlePlugin { create("slider") { id = "education.cccp.slider";
  implementationClass = "slider.SliderPlugin" } }` ট্যাগ `revealjs`,
  `slide-generator`, `slide`, `jgit`, `asciidoc`, `langchain4j`, `ollama`,
  `mistal-ai`, `huggingface`, `gemini`, `kotlin-DSL` সহ।
- `vcsUrl = "https://github.com/cheroliv/slider-gradle.git"`,
  `website = "https://cheroliv.com"`।
- `compatibility { features { configurationCache = false } }`
  (`asciidoctorRevealJs` কাজ JRuby দ্বারা `OUT_OF_PROCESS` চলে এবং
  Configuration Cache-এর সাথে বেমান)।
- POM Apache 2.0, ডেভেলপার `cccp-education`, SCM ঘোষণা করে যা
  `github.com/cheroliv/slider-gradle` নির্দেশ করে।
- সাইনিং `useGpgCmd()` ব্যবহার করে এবং non-CI, non-SNAPSHOT বিল্ডে প্রকাশন সাইন করে।

## EPIC অবস্থা

`slider-plugin/.agents/INDEX.adoc` (শেষ সেশন 010) থেকে:

| EPIC | Description | Status |
|------|-------------|--------|
| SLD-0 | Bootstrap governance agent | ✅ DONE |
| SLD-1 | Upgrade Reveal.js 3.9.1 → 5.2.1 + Auto-Animate + transitions | ✅ DONE (5/5 US) |
| SLD-2 | Playwright E2E + Pro theme + Capsule feed | 🔴 IN PROGRESS — 4/5 US done (US-2.5 pending) |
| SLD-3 | i18n 10 languages via `i18n-contracts:0.0.1` | ☐ NEW — framed session 010 |
| Publication Maven Central 0.0.1 | NMCP, hardcoded version | ✅ 2026-06-11 |

## অবদান

1. বিল্ড কম্পাইল হয়: `./gradlew build -x test`
2. পরীক্ষা সবুজ: `./gradlew check`
3. `SliderManager`-এ **একক দায়িত্ব** প্যাটার্ন মেনে চলুন — প্রতিটি নেস্টেড
   object একটি কনসার্নের মালিক (Prerequisites, Repositories, Plugins, Dependencies,
   Extensions, Tasks, Git, FileOps)।
4. স্পষ্ট imports (কোনো ওয়াইল্ডকার্ড নেই), 4-স্পেস ইন্ডেন্টেশন, ওপেনিং বন্ধনী একই
   লাইনে; ধ্রুবক `SCREAMING_SNAKE_CASE`।
5. Configuration Cache সক্ষম করবেন না — `asciidoctorRevealJs` স্পষ্টভাবে
   বেমান হিসাবে ঘোষণা করা হয়েছে।

## স্থাপত্য নথি

- [README.adoc](../README.adoc) — সম্পূর্ণ AsciiDoc তথ্যসূত্র (PlantUML চিত্র,
  দুই-ধাপ RAG পাইপলাইন, হেক্সাগোনাল ভিউ, ডেপ্লয়মেন্ট পাইপলাইন)।
- [.agents/INDEX.adoc](../slider-plugin/.agents/INDEX.adoc) — EPICs এবং গভর্নেন্স।
- [AGENT.adoc](../slider-plugin/AGENT.adoc) — পরম নিয়ম।
- [.agents/SESSION_CHECKLIST.adoc](../slider-plugin/.agents/SESSION_CHECKLIST.adoc) — সেশন চেকলিস্ট।
- [doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc](../slider-plugin/doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc) — SLD-1 প্রযুক্তিগত স্পেক।

## License

Apache License 2.0 — [LICENCE](../LICENCE) দেখুন।

---

_CCCP Education ইকোসিস্টেমের অংশ — `groupId: education.cccp`।_