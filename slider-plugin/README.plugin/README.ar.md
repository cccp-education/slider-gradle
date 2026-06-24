<!-- translated from README.md rev 0.0.1 -->
# slider-gradle — داخليات الإضافة

> دليل المطورين والمساهمين لإضافة `slider-plugin` الخاصة بـ Gradle.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-not%20configured-lightgrey?label=CI)](#)
[![Coverage](https://img.shields.io/static/v1?label=coverage&message=n%2Fa&color=lightgrey)]()
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.slider`
- **Toolchain**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew build -x test` · **Tests**: `./gradlew test` + `functionalTest` + `cucumberTest` (wired into `check`)
- **Coverage gate**: none (no Kover)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | **العربية** | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## تخطيط الوحدة

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

## الإضافة المُستهلكة (اعتماد N2 → N2)

- `education.cccp.codebase` version `0.0.1` (catalog alias
  `libs.plugins.codebase` في `slider-plugin/gradle/libs.versions.toml`) — مُطبّقة
  كإضافة شقيقة في `slider/build.gradle.kts`. توفّر بدائيات استيعاب EAGER/RAG
  التي يعيد `RagManager` استخدامها.

## الاعتمادات الأساسية

`slider/build.gradle.kts` يحل الإصدارات من `slider-plugin/gradle/libs.versions.toml`:

- **asciidoctor-gradle** `5.0.0-alpha.1` — `asciidoctor-gradle-jvm-slides`,
  `asciidoctor-gradle-jvm-gems`, `asciidoctor-gradle-jvm` (مُطبّقة كـ plugin IDs
  `org.asciidoctor.jvm.revealjs.classic` و `org.asciidoctor.jvm.gems.classic`)。
- **asciidoctor-revealjs gem** `5.2.0` مثبّتة مقابل قالب Reveal.js
  `hakimel/reveal.js` tag `5.2.1`。
- **asciidoctorj-diagram** `3.2.0` + **asciidoctorj-diagram-plantuml** `1.2025.3`。
- **node-gradle** `7.1.0` — `NpxTask` لـ `serveSlides`, `installPlaywright`, `visualTest`。
- **langchain4j** `1.14.1` + **langchain4j-beta** `1.14.1-beta24` — `langchain4j`,
  `langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
  `langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`。
- **JGit** `7.5.0.202512021534-r` — `org.eclipse.jgit`, `jgit.ssh.jsch`,
  `jgit.archive` (bundle)。
- **docker-java** `3.7.0` + **testcontainers-postgresql** `1.21.4` — دورة حياة pgvector。
- **Jackson** `2.21.1` — `jackson-module-kotlin`, `jackson-dataformat-yaml`。
- **Arrow** `2.2.2` + `arrow-integrations-jackson-module` `0.15.1` — مساعدات FP مُنمّطة。
- **kotlinx-coroutines** `1.10.2` — تجهيزات اختبار غير متزامن (`core`, `jdk8`, `test`)。
- **Playwright** `1.52.0` — اختبارات E2E بصرية (`testImplementation`)。
- **commons-io** `2.13.0`, **slf4j** `2.0.17`, **logback** `1.5.32`。

مكدّس الاختبار: **Kotless** JUnit5 (`kotlin-test-junit5`), **AssertJ** `3.27.7`,
**Mockito Kotlin** `6.2.3`, **Mockito JUnit Jupiter** `5.23.0`, **Cucumber**
`7.34.3` (`cucumber-java`, `cucumber-junit-platform-engine`, `cucumber-picocontainer`,
`junit-platform-suite`)。

## حالات Ollama (قياس عمومي)

المنافذ `11434–11436` ممنوعة. بدّل عبر `11437–11465` (29 منفذًا)。
النماذج المصرّح بها: `gpt-oss:120b-cloud`, `gemma4:31b-cloud`。
النماذج المتاحة محليًا مُفهرسة في `AssistantManager.localModels`:
`smollm:135m`, `llama3.2:3b-instruct-q8_0`, `smollm:135m-instruct-v0.2-q8_0`,
`gemma3:1b-it-fp16`。

## مصفوفة الاختبار

| Task | Scope | Notes |
|------|-------|-------|
| `test` | JUnit5 unit tests | excludes `com.cheroliv.slider.scenarios.**` (Cucumber) and `SliderPluginFunctionalTests` |
| `functionalTest` | GradleTestKit functional tests | own source set, depends on `test` impl via `extendsFrom` trick |
| `cucumberTest` | Cucumber BDD feature files | uses JUnit Platform suite engine, excludes `junit-jupiter`, `dependsOn functionalTest.classesTaskName` |
| `check` | aggregates | `test + functionalTest + cucumberTest` |

`tasks.withType<Test>` يُمكّن `useJUnitPlatform()`، تسجيل الاستثناءات الكامل و
`-XX:+EnableDynamicAgentLoading` لكتم تحذيرات الوكلاء الديناميكيين. مسار فئة
الاختبار يستبعد `logback-classic` لتفادي تعارضات الربط。

لا توجد **بوابة تغطية Kover** ولا **مهمة تدقيق CVE مخصصة**。

## ضبط JVM

- الإضافة تفرض `jvmToolchain(JavaVersion.VERSION_24.ordinal)` في
  `slider/build.gradle.kts` وتؤكد Java 23+ في وقت التشغيل عبر
  `SliderManager.Prerequisites.checkJavaVersion()`。
- `gradle.properties` يُعطّل daemon و Configuration Cache:
  `org.gradle.daemon=false`, `org.gradle.configuration-cache=false`。
- `buildscript.resolutionStrategy` يفرض
  `org.jetbrains:annotations:26.0.2-1` لمعالجة تعارض koog-utils النقلي
  الذي يتجاوز تثبيت Gradle الصارم لـ `annotations:13.0`。
- مهام RAG يجب أن تُشغّل بـ `--no-daemon` لإعادة تحميل مكتبة ONNX الأصلية。

## أوامر البناء

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

## خط أنابيب CI

المستودع يضم `.github/workflows/` لكن سير عمل الاختبار **غير
مُعدّ** — فقط مساعدات توثيق (`readme_plantuml.yml`,
`readme_truth.yml`) وقوالب معلّقة (`generate_deck.yml`,
`website.yml`)。 لا يوجد حاليًا مهمة اختبار PR، ولا شارة تغطية، ولا
مهمة نشر。

## النشر (NMCP)

مُعدّ عبر `com.gradleup.nmcp.settings` (1.5.0) في
`slider-plugin/settings.gradle.kts`。 تُقرأ بيانات الاعتماد من
`~/.gradle/gradle.properties` (`ossrhUsername`, `ossrhPassword`)，مع
`publishingType = "AUTOMATIC"`。

كتلة الإضافة في `slider/build.gradle.kts` توصل:
- `gradlePlugin { create("slider") { id = "education.cccp.slider";
  implementationClass = "slider.SliderPlugin" } }` بعلامات `revealjs`,
  `slide-generator`, `slide`, `jgit`, `asciidoc`, `langchain4j`, `ollama`,
  `mistal-ai`, `huggingface`, `gemini`, `kotlin-DSL`。
- `vcsUrl = "https://github.com/cheroliv/slider-gradle.git"`，
  `website = "https://cheroliv.com"`。
- `compatibility { features { configurationCache = false } }`
  (مهمة `asciidoctorRevealJs` تعمل `OUT_OF_PROCESS` عبر JRuby وغير متوافقة
  مع Configuration Cache)。
- يصرّح POM بـ Apache 2.0، المطور `cccp-education`， SCM يشير إلى
  `github.com/cheroliv/slider-gradle`。
- التوقيع يستخدم `useGpgCmd()` ويوقّع المنشورات في بناءات غير CI وغير SNAPSHOT。

## حالة EPIC

من `slider-plugin/.agents/INDEX.adoc` (آخر جلسة 010):

| EPIC | Description | Status |
|------|-------------|--------|
| SLD-0 | Bootstrap governance agent | ✅ DONE |
| SLD-1 | Upgrade Reveal.js 3.9.1 → 5.2.1 + Auto-Animate + transitions | ✅ DONE (5/5 US) |
| SLD-2 | Playwright E2E + Pro theme + Capsule feed | 🔴 IN PROGRESS — 4/5 US done (US-2.5 pending) |
| SLD-3 | i18n 10 languages via `i18n-contracts:0.0.1` | ☐ NEW — framed session 010 |
| Publication Maven Central 0.0.1 | NMCP, hardcoded version | ✅ 2026-06-11 |

## المساهمة

1. يُترجم البناء: `./gradlew build -x test`
2. الاختبارات خضراء: `./gradlew check`
3. احترم نمط **المسؤولية الواحدة** في `SliderManager` — كل object متداخل
   يملك اهتمامًا واحدًا (Prerequisites, Repositories, Plugins, Dependencies,
   Extensions, Tasks, Git, FileOps)。
4. استخدم واردات صريحة (بدون أحرف بدل)، مسافة بادئة 4 مسافات، القوس الافتتاحي على
   نفس السطر； الثوابت `SCREAMING_SNAKE_CASE`。
5. لا تُمكّن Configuration Cache — `asciidoctorRevealJs` مُصرّح به صراحةً
   كغير متوافق。

## وثائق البنية

- [README.adoc](../README.adoc) — مرجع AsciiDoc الكامل (مخططات PlantUML,
  خط أنابيب RAG من خطوتين, عرض سداسي, خط أنابيب النشر)。
- [.agents/INDEX.adoc](../slider-plugin/.agents/INDEX.adoc) — EPICs والحوكمة。
- [AGENT.adoc](../slider-plugin/AGENT.adoc) — القواعد المطلقة。
- [.agents/SESSION_CHECKLIST.adoc](../slider-plugin/.agents/SESSION_CHECKLIST.adoc) — قائمة مراجعة الجلسة。
- [doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc](../slider-plugin/doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc) — مواصفات SLD-1 الفنية。

## License

Apache License 2.0 — راجع [LICENCE](../LICENCE)。

---

_جزء من منظومة CCCP Education — `groupId: education.cccp`。_