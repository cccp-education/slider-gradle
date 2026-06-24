<!-- translated from README.md rev 0.0.1 -->
# slider-gradle — پلگ ان کی اندرونی تفصیلات

> `slider-plugin` Gradle پلگ ان کے لیے ڈویلپر اور شراکت داروں کی رہنمائی۔

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-not%20configured-lightgrey?label=CI)](#)
[![Coverage](https://img.shields.io/static/v1?label=coverage&message=n%2Fa&color=lightgrey)]()
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.slider`
- **Toolchain**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew build -x test` · **Tests**: `./gradlew test` + `functionalTest` + `cucumberTest` (wired into `check`)
- **Coverage gate**: none (no Kover)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | **اردو**

---

## ماڈیول ترتیب

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

## استعمال شدہ پلگ ان (N2 → N2 انحصار)

- `education.cccp.codebase` version `0.0.1` (catalog alias
  `libs.plugins.codebase` in `slider-plugin/gradle/libs.versions.toml`) — `slider/build.gradle.kts` میں sibling پلگ ان کے طور پر لاگو。 یہ EAGER/RAG ingestion primitives فراہم کرتا ہے جو `RagManager` دوبارہ استعمال کرتا ہے۔

## اہم انحصارات

`slider/build.gradle.kts` `slider-plugin/gradle/libs.versions.toml` سے ورژن حل کرتا ہے:

- **asciidoctor-gradle** `5.0.0-alpha.1` — `asciidoctor-gradle-jvm-slides`,
  `asciidoctor-gradle-jvm-gems`, `asciidoctor-gradle-jvm` (plugin IDs
  `org.asciidoctor.jvm.revealjs.classic` اور `org.asciidoctor.jvm.gems.classic` کے طور پر لاگو)。
- **asciidoctor-revealjs gem** `5.2.0` Reveal.js template
  `hakimel/reveal.js` tag `5.2.1` کے مقابلے میں مقفل。
- **asciidoctorj-diagram** `3.2.0` + **asciidoctorj-diagram-plantuml** `1.2025.3`。
- **node-gradle** `7.1.0` — `serveSlides`, `installPlaywright`, `visualTest` کے لیے `NpxTask`。
- **langchain4j** `1.14.1` + **langchain4j-beta** `1.14.1-beta24` — `langchain4j`,
  `langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
  `langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`。
- **JGit** `7.5.0.202512021534-r` — `org.eclipse.jgit`, `jgit.ssh.jsch`,
  `jgit.archive` (bundle)۔
- **docker-java** `3.7.0` + **testcontainers-postgresql** `1.21.4` — pgvector lifecycle۔
- **Jackson** `2.21.1` — `jackson-module-kotlin`, `jackson-dataformat-yaml`۔
- **Arrow** `2.2.2` + `arrow-integrations-jackson-module` `0.15.1` — typed FP helpers۔
- **kotlinx-coroutines** `1.10.2` — async test fixtures (`core`, `jdk8`, `test`)۔
- **Playwright** `1.52.0` — visual E2E tests (`testImplementation`)۔
- **commons-io** `2.13.0`, **slf4j** `2.0.17`, **logback** `1.5.32`۔

ٹیسٹ اسٹیک: **Kotless** JUnit5 (`kotlin-test-junit5`), **AssertJ** `3.27.7`,
**Mockito Kotlin** `6.2.3`, **Mockito JUnit Jupiter** `5.23.0`, **Cucumber**
`7.34.3` (`cucumber-java`, `cucumber-junit-platform-engine`, `cucumber-picocontainer`,
`junit-platform-suite`)۔

## Ollama انسٹنس (عالمی پابندی)

پورٹس `11434–11436` ممنوع ہیں۔ `11437–11465` (29 پورٹس) پر گھمائیں۔
اجازت یافتہ ماڈلز: `gpt-oss:120b-cloud`, `gemma4:31b-cloud`۔
`AssistantManager.localModels` میں مقامی طور پر دستیاب ماڈلز:
`smollm:135m`, `llama3.2:3b-instruct-q8_0`, `smollm:135m-instruct-v0.2-q8_0`,
`gemma3:1b-it-fp16`۔

## ٹیسٹ میٹرکس

| Task | Scope | Notes |
|------|-------|-------|
| `test` | JUnit5 unit tests | excludes `com.cheroliv.slider.scenarios.**` (Cucumber) and `SliderPluginFunctionalTests` |
| `functionalTest` | GradleTestKit functional tests | own source set, depends on `test` impl via `extendsFrom` trick |
| `cucumberTest` | Cucumber BDD feature files | uses JUnit Platform suite engine, excludes `junit-jupiter`, `dependsOn functionalTest.classesTaskName` |
| `check` | aggregates | `test + functionalTest + cucumberTest` |

`tasks.withType<Test>` `useJUnitPlatform()`, مکمل استثنائی لاگنگ اور
`-XX:+EnableDynamicAgentLoading` کو فعال کرتا ہے تاکہ dynamic agent وارننگ خاموش ہوں۔ ٹیسٹ
classpath binding تنازعات سے بچنے کے لیے `logback-classic` خارج کرتا ہے۔

**کوئی Kover** کوریج گیٹ اور **کوئی مخصوص CVE آڈٹ ٹاسک** نہیں ہے۔

## JVM ٹیوننگ

- پلگ ان
  `slider/build.gradle.kts` میں `jvmToolchain(JavaVersion.VERSION_24.ordinal)` نافذ کرتا ہے اور runtime میں
  `SliderManager.Prerequisites.checkJavaVersion()` کے ذریعے Java 23+ کا دعویٰ کرتا ہے۔
- `gradle.properties` daemon اور Configuration Cache غیر فعال کرتا ہے:
  `org.gradle.daemon=false`, `org.gradle.configuration-cache=false`।
- ایک `buildscript.resolutionStrategy`
  `org.jetbrains:annotations:26.0.2-1` کو مجبور کرتا ہے تاکہ koog-utils transitive
  تنازعے سے نمٹا جا سکے جو Gradle کی `annotations:13.0` کی سخت پن کو بائی پاس کرتا ہے۔
- RAG ٹاسک `--no-daemon` کے ساتھ چلیں تاکہ native ONNX لائبریری دوبارہ لوڈ ہو۔

## بلڈ کمانڈز

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

## CI پائپ لائن

ریپوزیٹری `.github/workflows/` رکھتی ہے لیکن ٹیسٹ ورک فلو **کنفیگر
نہیں** — صرف دستاویزی معاونین (`readme_plantuml.yml`,
`readme_truth.yml`) اور کمنٹ آؤٹ ٹیمپلیٹس (`generate_deck.yml`,
`website.yml`)۔ فی الحال کوئی PR ٹیسٹ کام، کوئی کوریج بیج اور کوئی
پبلیکیشن کام نہیں ہے۔

## پبلیکیشن (NMCP)

`slider-plugin/settings.gradle.kts` میں
`com.gradleup.nmcp.settings` (1.5.0) کے ذریعے کنفیگر。 اسناد
`~/.gradle/gradle.properties` (`ossrhUsername`, `ossrhPassword`) سے پڑھے جاتے ہیں，کے ساتھ
`publishingType = "AUTOMATIC"`。

`slider/build.gradle.kts` پلگ ان بلاک وائر کرتا ہے:
- `gradlePlugin { create("slider") { id = "education.cccp.slider";
  implementationClass = "slider.SliderPlugin" } }` ٹیگز `revealjs`,
  `slide-generator`, `slide`, `jgit`, `asciidoc`, `langchain4j`, `ollama`,
  `mistal-ai`, `huggingface`, `gemini`, `kotlin-DSL` کے ساتھ۔
- `vcsUrl = "https://github.com/cheroliv/slider-gradle.git"`,
  `website = "https://cheroliv.com"`۔
- `compatibility { features { configurationCache = false } }`
  (`asciidoctorRevealJs` کام JRuby کے ذریعے `OUT_OF_PROCESS` چلتا ہے اور
  Configuration Cache کے ساتھ غیر مطابق ہے)۔
- POM Apache 2.0، ڈویلپر `cccp-education`، SCM کا اعلان کرتا ہے جو
  `github.com/cheroliv/slider-gradle` کی نشاندہی کرتا ہے۔
- سائننگ `useGpgCmd()` استعمال کرتی ہے اور non-CI, non-SNAPSHOT بلڈز پر پبلیکیشنز سائن کرتی ہے۔

## EPIC صورتحال

`slider-plugin/.agents/INDEX.adoc` (آخری سیشن 010) سے:

| EPIC | Description | Status |
|------|-------------|--------|
| SLD-0 | Bootstrap governance agent | ✅ DONE |
| SLD-1 | Upgrade Reveal.js 3.9.1 → 5.2.1 + Auto-Animate + transitions | ✅ DONE (5/5 US) |
| SLD-2 | Playwright E2E + Pro theme + Capsule feed | 🔴 IN PROGRESS — 4/5 US done (US-2.5 pending) |
| SLD-3 | i18n 10 languages via `i18n-contracts:0.0.1` | ☐ NEW — framed session 010 |
| Publication Maven Central 0.0.1 | NMCP, hardcoded version | ✅ 2026-06-11 |

## شراکت

1. بلڈ مرتب ہو: `./gradlew build -x test`
2. ٹیسٹ سبز: `./gradlew check`
3. `SliderManager` میں **واحد ذمہ داری** پیٹرن کی پاسداری کریں — ہر nested
   object ایک تشویش کا مالک ہے (Prerequisites, Repositories, Plugins, Dependencies,
   Extensions, Tasks, Git, FileOps)۔
4. واضح درآمدات (کوئی وائلڈ کارڈ نہیں)، 4-اسپیس انڈینٹیشن، اوپننگ بریکٹ اسی
   لائن پر؛ مستقلات `SCREAMING_SNAKE_CASE`۔
5. Configuration Cache فعال نہ کریں — `asciidoctorRevealJs` واضح طور پر
   غیر مطابق قرار دیا گیا ہے۔

## فن تعمیر دستاویزات

- [README.adoc](../README.adoc) — مکمل AsciiDoc حوالہ (PlantUML خاکے،
  دو مرحلہ RAG پائپ لائن، ہیکساگونل منظر، تعیناتی پائپ لائن)۔
- [.agents/INDEX.adoc](../slider-plugin/.agents/INDEX.adoc) — EPICs اور گورننس۔
- [AGENT.adoc](../slider-plugin/AGENT.adoc) — مطلق قوانین۔
- [.agents/SESSION_CHECKLIST.adoc](../slider-plugin/.agents/SESSION_CHECKLIST.adoc) — سیشن چیک لسٹ۔
- [doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc](../slider-plugin/doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc) — SLD-1 تکنیکی تفصیل۔

## License

Apache License 2.0 — [LICENCE](../LICENCE) دیکھیں۔

---

_CCCP Education ماحولیاتی نظام کا حصہ — `groupId: education.cccp`۔_