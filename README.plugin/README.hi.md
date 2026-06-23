<!-- translated from README.md rev 0.0.1 -->
# slider-gradle — प्लगइन आंतरिक

> `slider-plugin` Gradle प्लगइन हेतु डेवलपर और योगदानकर्ता गाइड।

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-not%20configured-lightgrey?label=CI)](#)
[![Coverage](https://img.shields.io/static/v1?label=coverage&message=n%2Fa&color=lightgrey)]()
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.slider`
- **Toolchain**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew build -x test` · **Tests**: `./gradlew test` + `functionalTest` + `cucumberTest` (wired into `check`)
- **Coverage gate**: none (no Kover)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | **हिन्दी** | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## मॉड्यूल अभिन्यास

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

## उपभुक्त प्लगइन (N2 → N2 निर्भरता)

- `education.cccp.codebase` version `0.0.1` (catalog alias
  `libs.plugins.codebase` in `slider-plugin/gradle/libs.versions.toml`) — `slider/build.gradle.kts` में सिबलिंग प्लगइन के रूप में लागू。 यह EAGER/RAG अंतर्ग्रहण प्रिमिटिव प्रदान करता है जिन्हें `RagManager` पुनः उपयोग करता है。

## मुख्य निर्भरताएँ

`slider/build.gradle.kts` `slider-plugin/gradle/libs.versions.toml` से संस्करण हल करता है:

- **asciidoctor-gradle** `5.0.0-alpha.1` — `asciidoctor-gradle-jvm-slides`,
  `asciidoctor-gradle-jvm-gems`, `asciidoctor-gradle-jvm` (plugin IDs
  `org.asciidoctor.jvm.revealjs.classic` और `org.asciidoctor.jvm.gems.classic` के रूप में लागू)。
- **asciidoctor-revealjs gem** `5.2.0` Reveal.js टेम्पलेट
  `hakimel/reveal.js` टैग `5.2.1` के विरुद्ध नियत。
- **asciidoctorj-diagram** `3.2.0` + **asciidoctorj-diagram-plantuml** `1.2025.3`。
- **node-gradle** `7.1.0` — `serveSlides`, `installPlaywright`, `visualTest` हेतु `NpxTask`。
- **langchain4j** `1.14.1` + **langchain4j-beta** `1.14.1-beta24` — `langchain4j`,
  `langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
  `langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`。
- **JGit** `7.5.0.202512021534-r` — `org.eclipse.jgit`, `jgit.ssh.jsch`,
  `jgit.archive` (bundle)。
- **docker-java** `3.7.0` + **testcontainers-postgresql** `1.21.4` — pgvector lifecycle。
- **Jackson** `2.21.1` — `jackson-module-kotlin`, `jackson-dataformat-yaml`。
- **Arrow** `2.2.2` + `arrow-integrations-jackson-module` `0.15.1` — टाइप्ड FP सहायक।
- **kotlinx-coroutines** `1.10.2` — async टेस्ट फिक्स्चर (`core`, `jdk8`, `test`)。
- **Playwright** `1.52.0` — विज़ुअल E2E परीक्षण (`testImplementation`)。
- **commons-io** `2.13.0`, **slf4j** `2.0.17`, **logback** `1.5.32`।

टेस्ट स्टैक: **Kotless** JUnit5 (`kotlin-test-junit5`), **AssertJ** `3.27.7`,
**Mockito Kotlin** `6.2.3`, **Mockito JUnit Jupiter** `5.23.0`, **Cucumber**
`7.34.3` (`cucumber-java`, `cucumber-junit-platform-engine`, `cucumber-picocontainer`,
`junit-platform-suite`)।

## Ollama इंस्टैंस (वैश्विक बाध्यता)

पोर्ट `11434–11436` वर्जित हैं। `11437–11465` (29 पोर्ट) पर रोटेट करें।
अधिकृत मॉडल: `gpt-oss:120b-cloud`, `gemma4:31b-cloud`।
`AssistantManager.localModels` में सूचीबद्ध स्थानीय रूप से उपलब्ध मॉडल:
`smollm:135m`, `llama3.2:3b-instruct-q8_0`, `smollm:135m-instruct-v0.2-q8_0`,
`gemma3:1b-it-fp16`।

## टेस्ट मैट्रिक्स

| Task | Scope | Notes |
|------|-------|-------|
| `test` | JUnit5 unit tests | excludes `com.cheroliv.slider.scenarios.**` (Cucumber) and `SliderPluginFunctionalTests` |
| `functionalTest` | GradleTestKit functional tests | own source set, depends on `test` impl via `extendsFrom` trick |
| `cucumberTest` | Cucumber BDD feature files | uses JUnit Platform suite engine, excludes `junit-jupiter`, `dependsOn functionalTest.classesTaskName` |
| `check` | aggregates | `test + functionalTest + cucumberTest` |

`tasks.withType<Test>` `useJUnitPlatform()`, पूर्ण अपवाद लॉगिंग और
`-XX:+EnableDynamicAgentLoading` को सक्षम करता है ताकि डायनामिक एजेंट चेतावनी शांत हों। टेस्ट
classpath `logback-classic` को बाइंडिंग संघर्ष से बचने हेतु बहिष्कृत करता है।

**कोई Kover** कवरेज गेट और **कोई समर्पित CVE ऑडिट कार्य** नहीं है।

## JVM ट्यूनिंग

- प्लगइन
  `slider/build.gradle.kts` में `jvmToolchain(JavaVersion.VERSION_24.ordinal)` लागू करता है और रनटाइम पर
  `SliderManager.Prerequisites.checkJavaVersion()` द्वारा Java 23+ का दावा करता है।
- `gradle.properties` daemon और Configuration Cache अक्षम करता है:
  `org.gradle.daemon=false`, `org.gradle.configuration-cache=false`。
- एक `buildscript.resolutionStrategy`
  `org.jetbrains:annotations:26.0.2-1` को बाध्य करता है ताकि koog-utils ट्रांज़िटिव
  संघर्ष पर काबू पाया जा सके, जो Gradle की `annotations:13.0` की सख्त पिन को बायपास करता है।
- RAG कार्य `--no-daemon` से चलें ताकि नेटिव ONNX लाइब्रेरी पुनः लोड हो।

## बिल्ड कमांड

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

## CI पाइपलाइन

रिपॉज़िटरी `.github/workflows/` ले जाती है परंतु टेस्ट वर्कफ़्लो **नहीं
कॉन्फ़िगर किया गया** — केवल दस्तावेज़ सहायक (`readme_plantuml.yml`,
`readme_truth.yml`) और कमेंट-आउट टेम्पलेट (`generate_deck.yml`,
`website.yml`)। वर्तमान में कोई PR टेस्ट कार्य, कोई कवरेज बैज, और कोई
प्रकाशन कार्य नहीं है।

## प्रकाशन (NMCP)

`slider-plugin/settings.gradle.kts` में
`com.gradleup.nmcp.settings` (1.5.0) के माध्यम से कॉन्फ़िगर। क्रेडेंशियल
`~/.gradle/gradle.properties` (`ossrhUsername`, `ossrhPassword`) से पढ़े जाते हैं，साथ ही
`publishingType = "AUTOMATIC"`。

`slider/build.gradle.kts` प्लगइन ब्लॉक वायर करता है:
- `gradlePlugin { create("slider") { id = "education.cccp.slider";
  implementationClass = "slider.SliderPlugin" } }` टैग्स `revealjs`,
  `slide-generator`, `slide`, `jgit`, `asciidoc`, `langchain4j`, `ollama`,
  `mistal-ai`, `huggingface`, `gemini`, `kotlin-DSL` के साथ。
- `vcsUrl = "https://github.com/cheroliv/slider-gradle.git"`,
  `website = "https://cheroliv.com"`。
- `compatibility { features { configurationCache = false } }`
  (`asciidoctorRevealJs` कार्य JRuby द्वारा `OUT_OF_PROCESS` चलता है और
  Configuration Cache के साथ असंगत है)।
- POM Apache 2.0, डेवलपर `cccp-education`, SCM घोषित करता है जो
  `github.com/cheroliv/slider-gradle` को इंगित करता है।
- साइनिंग `useGpgCmd()` का उपयोग करता है और non-CI, non-SNAPSHOT बिल्ड पर प्रकाशन साइन करता है।

## EPIC स्थिति

`slider-plugin/.agents/INDEX.adoc` (अंतिम सत्र 010) से:

| EPIC | Description | Status |
|------|-------------|--------|
| SLD-0 | Bootstrap governance agent | ✅ DONE |
| SLD-1 | Upgrade Reveal.js 3.9.1 → 5.2.1 + Auto-Animate + transitions | ✅ DONE (5/5 US) |
| SLD-2 | Playwright E2E + Pro theme + Capsule feed | 🔴 IN PROGRESS — 4/5 US done (US-2.5 pending) |
| SLD-3 | i18n 10 languages via `i18n-contracts:0.0.1` | ☐ NEW — framed session 010 |
| Publication Maven Central 0.0.1 | NMCP, hardcoded version | ✅ 2026-06-11 |

## योगदान

1. बिल्ड संकलित हो: `./gradlew build -x test`
2. टेस्ट हरित: `./gradlew check`
3. `SliderManager` में **एकल उत्तरदायित्व** पैटर्न का पालन करें — प्रत्येक नेस्टेड
   object एक चिंता का स्वामी है (Prerequisites, Repositories, Plugins, Dependencies,
   Extensions, Tasks, Git, FileOps)।
4. स्पष्ट इम्पोर्ट (कोई वाइल्डकार्ड नहीं), 4-स्पेस इंडेंटेशन, समान पंक्ति पर ओपनिंग ब्रेस;
   स्थिरांक `SCREAMING_SNAKE_CASE`。
5. Configuration Cache सक्षम न करें — `asciidoctorRevealJs` स्पष्ट रूप से
   असंगत घोषित है।

## वास्तुकला दस्तावेज़

- [README.adoc](../README.adoc) — पूर्ण AsciiDoc संदर्भ (PlantUML आरेख,
  दो-चरण RAG पाइपलाइन, हेक्सागोनल दृश्य, तैनाती पाइपलाइन)।
- [.agents/INDEX.adoc](../slider-plugin/.agents/INDEX.adoc) — EPICs और शासन।
- [AGENT.adoc](../slider-plugin/AGENT.adoc) — निरपेक्ष नियम।
- [.agents/SESSION_CHECKLIST.adoc](../slider-plugin/.agents/SESSION_CHECKLIST.adoc) — सत्र चेकलिस्ट।
- [doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc](../slider-plugin/doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc) — SLD-1 तकनीकी विनिर्देश।

## License

Apache License 2.0 — [LICENCE](../LICENCE) देखें।

---

_CCCP Education पारिस्थितिकी का हिस्सा — `groupId: education.cccp`।_