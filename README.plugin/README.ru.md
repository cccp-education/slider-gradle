<!-- translated from README.md rev 0.0.1 -->
# slider-gradle — Внутреннее устройство плагина

> Руководство для разработчиков и контрибьюторов плагина `slider-plugin` для Gradle.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-not%20configured-lightgrey?label=CI)](#)
[![Coverage](https://img.shields.io/static/v1?label=coverage&message=n%2Fa&color=lightgrey)]()
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.slider`
- **Toolchain**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew build -x test` · **Tests**: `./gradlew test` + `functionalTest` + `cucumberTest` (wired into `check`)
- **Coverage gate**: none (no Kover)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | **Русский** | [اردو](README.ur.md)

---

## Структура модуля

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

## Потребляемый плагин (зависимость N2 → N2)

- `education.cccp.codebase` version `0.0.1` (catalog alias
  `libs.plugins.codebase` в `slider-plugin/gradle/libs.versions.toml`) — применяется
  как родственный плагин в `slider/build.gradle.kts`. Он предоставляет примитивы
  ingestа EAGER/RAG, переиспользуемые `RagManager`.

## Ключевые зависимости

`slider/build.gradle.kts` разрешает версии из `slider-plugin/gradle/libs.versions.toml`:

- **asciidoctor-gradle** `5.0.0-alpha.1` — `asciidoctor-gradle-jvm-slides`,
  `asciidoctor-gradle-jvm-gems`, `asciidoctor-gradle-jvm` (применены как plugin ID
  `org.asciidoctor.jvm.revealjs.classic` и `org.asciidoctor.jvm.gems.classic`)。
- **asciidoctor-revealjs gem** `5.2.0` зафиксирован против шаблона Reveal.js
  `hakimel/reveal.js` tag `5.2.1`。
- **asciidoctorj-diagram** `3.2.0` + **asciidoctorj-diagram-plantuml** `1.2025.3`。
- **node-gradle** `7.1.0` — `NpxTask` для `serveSlides`, `installPlaywright`, `visualTest`。
- **langchain4j** `1.14.1` + **langchain4j-beta** `1.14.1-beta24` — `langchain4j`,
  `langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
  `langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`。
- **JGit** `7.5.0.202512021534-r` — `org.eclipse.jgit`, `jgit.ssh.jsch`,
  `jgit.archive` (bundle)。
- **docker-java** `3.7.0` + **testcontainers-postgresql** `1.21.4` — жизненный цикл pgvector。
- **Jackson** `2.21.1` — `jackson-module-kotlin`, `jackson-dataformat-yaml`。
- **Arrow** `2.2.2` + `arrow-integrations-jackson-module` `0.15.1` — типизированные FP-помощники。
- **kotlinx-coroutines** `1.10.2` — асинхронные тестовые фикстуры (`core`, `jdk8`, `test`)。
- **Playwright** `1.52.0` — визуальные E2E-тесты (`testImplementation`)。
- **commons-io** `2.13.0`, **slf4j** `2.0.17`, **logback** `1.5.32`。

Тестовый стек: **Kotless** JUnit5 (`kotlin-test-junit5`), **AssertJ** `3.27.7`,
**Mockito Kotlin** `6.2.3`, **Mockito JUnit Jupiter** `5.23.0`, **Cucumber**
`7.34.3` (`cucumber-java`, `cucumber-junit-platform-engine`, `cucumber-picocontainer`,
`junit-platform-suite`)。

## Экземпляры Ollama (глобальное ограничение)

Порты `11434–11436` запрещены. Ротируйте по `11437–11465` (29 портов)。
Авторизованные модели: `gpt-oss:120b-cloud`, `gemma4:31b-cloud`。
Локально доступные модели каталогизированы в `AssistantManager.localModels`:
`smollm:135m`, `llama3.2:3b-instruct-q8_0`, `smollm:135m-instruct-v0.2-q8_0`,
`gemma3:1b-it-fp16`。

## Тестовая матрица

| Task | Scope | Notes |
|------|-------|-------|
| `test` | JUnit5 unit tests | excludes `com.cheroliv.slider.scenarios.**` (Cucumber) and `SliderPluginFunctionalTests` |
| `functionalTest` | GradleTestKit functional tests | own source set, depends on `test` impl via `extendsFrom` trick |
| `cucumberTest` | Cucumber BDD feature files | uses JUnit Platform suite engine, excludes `junit-jupiter`, `dependsOn functionalTest.classesTaskName` |
| `check` | aggregates | `test + functionalTest + cucumberTest` |

`tasks.withType<Test>` включает `useJUnitPlatform()`, полное логирование
исключений и `-XX:+EnableDynamicAgentLoading` для подавления предупреждений о
динамических агентах. Тестовый classpath исключает `logback-classic` для
избежания конфликтов привязки。

Нет **шлюза покрытия Kover** и **выделенной задачи аудита CVE**。

## Настройка JVM

- Плагин налагает `jvmToolchain(JavaVersion.VERSION_24.ordinal)` в
  `slider/build.gradle.kts` и утверждает Java 23+ во время выполнения через
  `SliderManager.Prerequisites.checkJavaVersion()`。
- `gradle.properties` отключает daemon и Configuration Cache:
  `org.gradle.daemon=false`, `org.gradle.configuration-cache=false`。
- `buildscript.resolutionStrategy` принудительно фиксирует
  `org.jetbrains:annotations:26.0.2-1`, чтобы обойти транзитивный
  конфликт koog-utils, обходящий строгий pin Gradle для `annotations:13.0`。
- RAG-задачи должны запускаться с `--no-daemon` для перезагрузки нативной
  библиотеки ONNX.

## Команды сборки

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

## CI-конвейер

Репозиторий содержит `.github/workflows/`, но тестовый workflow **не
настроен** — только помощники по документации (`readme_plantuml.yml`、
`readme_truth.yml`) и закомментированные шаблоны (`generate_deck.yml`、
`website.yml`). В настоящее время нет PR-тестовой задачи, баджа покрытия и
задачи публикации。

## Публикация (NMCP)

Настроена через `com.gradleup.nmcp.settings` (1.5.0) в
`slider-plugin/settings.gradle.kts`。 Учётные данные читаются из
`~/.gradle/gradle.properties` (`ossrhUsername`, `ossrhPassword`)，с
`publishingType = "AUTOMATIC"`。

Блок плагина в `slider/build.gradle.kts` подключает:
- `gradlePlugin { create("slider") { id = "education.cccp.slider";
  implementationClass = "slider.SliderPlugin" } }` с тегами `revealjs`,
  `slide-generator`, `slide`, `jgit`, `asciidoc`, `langchain4j`, `ollama`,
  `mistal-ai`, `huggingface`, `gemini`, `kotlin-DSL`.
- `vcsUrl = "https://github.com/cheroliv/slider-gradle.git"`、
  `website = "https://cheroliv.com"`.
- `compatibility { features { configurationCache = false } }`
  (задача `asciidoctorRevealJs` выполняется `OUT_OF_PROCESS` через JRuby и
  несовместима с Configuration Cache).
- POM объявляет Apache 2.0, разработчика `cccp-education`, SCM, указывающий на
  `github.com/cheroliv/slider-gradle`。
- Подпись использует `useGpgCmd()` и подписывает публикации в non-CI, non-SNAPSHOT сборках。

## Статус EPIC

Из `slider-plugin/.agents/INDEX.adoc` (последняя сессия 010):

| EPIC | Description | Status |
|------|-------------|--------|
| SLD-0 | Bootstrap governance agent | ✅ DONE |
| SLD-1 | Upgrade Reveal.js 3.9.1 → 5.2.1 + Auto-Animate + transitions | ✅ DONE (5/5 US) |
| SLD-2 | Playwright E2E + Pro theme + Capsule feed | 🔴 IN PROGRESS — 4/5 US done (US-2.5 pending) |
| SLD-3 | i18n 10 languages via `i18n-contracts:0.0.1` | ☐ NEW — framed session 010 |
| Publication Maven Central 0.0.1 | NMCP, hardcoded version | ✅ 2026-06-11 |

## Участие

1. Сборка компилируется: `./gradlew build -x test`
2. Тесты зелёные: `./gradlew check`
3. Соблюдайте паттерн **единой ответственности** в `SliderManager` — каждый вложенный
   object владеет одной задачей (Prerequisites, Repositories, Plugins, Dependencies,
   Extensions, Tasks, Git, FileOps).
4. Используйте явные импорты (без wildcards), 4-пробельный отступ, открывающую скобку на
   той же строке; константы — `SCREAMING_SNAKE_CASE`.
5. Не включайте Configuration Cache — `asciidoctorRevealJs` явно
   объявлен несовместимым.

## Архитектурные документы

- [README.adoc](../README.adoc) — полный AsciiDoc-справочник (диаграммы PlantUML,
  двухшаговый RAG-конвейер, гексагональное представление, конвейер развёртывания)。
- [.agents/INDEX.adoc](../slider-plugin/.agents/INDEX.adoc) — EPIC и управление。
- [AGENT.adoc](../slider-plugin/AGENT.adoc) — абсолютные правила。
- [.agents/SESSION_CHECKLIST.adoc](../slider-plugin/.agents/SESSION_CHECKLIST.adoc) — чеклист сессии.
- [doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc](../slider-plugin/doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc) — техническая спецификация SLD-1.

## License

Apache License 2.0 — см. [LICENCE](../LICENCE)。

---

_Часть экосистемы CCCP Education — `groupId: education.cccp`。_