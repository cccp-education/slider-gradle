<!-- master source — other languages are translations of this file -->
# slider-gradle — Plugin Internals

> Developer & contributor guide for the `slider-plugin` Gradle plugin.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-not%20configured-lightgrey?label=CI)](#)
[![Coverage](https://img.shields.io/static/v1?label=coverage&message=n%2Fa&color=lightgrey)]()
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.slider`
- **Toolchain**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew build -x test` · **Tests**: `./gradlew test` + `functionalTest` + `cucumberTest` (wired into `check`)
- **Coverage gate**: none (no Kover)

🌐 Languages: **EN** | [Français](README.fr.md)

---

## Module layout

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

## Consumed plugin (N2 → N2 dependency)

- `education.cccp.codebase` version `0.0.1` (catalog alias
  `libs.plugins.codebase` in `slider-plugin/gradle/libs.versions.toml`) — applied
  as a sibling plugin in `slider/build.gradle.kts`. It provides the EAGER/RAG
  ingestion primitives reused by `RagManager`.

## Key dependencies

`slider/build.gradle.kts` resolves versions from `slider-plugin/gradle/libs.versions.toml`:

- **asciidoctor-gradle** `5.0.0-alpha.1` — `asciidoctor-gradle-jvm-slides`,
  `asciidoctor-gradle-jvm-gems`, `asciidoctor-gradle-jvm` (applied as plugin IDs
  `org.asciidoctor.jvm.revealjs.classic` and `org.asciidoctor.jvm.gems.classic`).
- **asciidoctor-revealjs gem** `5.2.0` pinned against Reveal.js template
  `hakimel/reveal.js` tag `5.2.1`.
- **asciidoctorj-diagram** `3.2.0` + **asciidoctorj-diagram-plantuml** `1.2025.3`.
- **node-gradle** `7.1.0` — `NpxTask` for `serveSlides`, `installPlaywright`, `visualTest`.
- **langchain4j** `1.14.1` + **langchain4j-beta** `1.14.1-beta24` — `langchain4j`,
  `langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
  `langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`.
- **JGit** `7.5.0.202512021534-r` — `org.eclipse.jgit`, `jgit.ssh.jsch`,
  `jgit.archive` (bundle).
- **docker-java** `3.7.0` + **testcontainers-postgresql** `1.21.4` — pgvector lifecycle.
- **Jackson** `2.21.1` — `jackson-module-kotlin`, `jackson-dataformat-yaml`.
- **Arrow** `2.2.2` + `arrow-integrations-jackson-module` `0.15.1` — typed FP helpers.
- **kotlinx-coroutines** `1.10.2` — async test fixtures (`core`, `jdk8`, `test`).
- **Playwright** `1.52.0` — visual E2E tests (`testImplementation`).
- **commons-io** `2.13.0`, **slf4j** `2.0.17`, **logback** `1.5.32`.

Test stack: **Kotless** JUnit5 (`kotlin-test-junit5`), **AssertJ** `3.27.7`,
**Mockito Kotlin** `6.2.3`, **Mockito JUnit Jupiter** `5.23.0`, **Cucumber**
`7.34.3` (`cucumber-java`, `cucumber-junit-platform-engine`, `cucumber-picocontainer`,
`junit-platform-suite`).

## Ollama instances (global constraint)

Ports `11434–11436` are forbidden. Rotate over `11437–11465` (29 ports).
Authorized models: `gpt-oss:120b-cloud`, `gemma4:31b-cloud`.
Locally available models catalogued in `AssistantManager.localModels`:
`smollm:135m`, `llama3.2:3b-instruct-q8_0`, `smollm:135m-instruct-v0.2-q8_0`,
`gemma3:1b-it-fp16`.

## Test matrix

| Task | Scope | Notes |
|------|-------|-------|
| `test` | JUnit5 unit tests | excludes `com.cheroliv.slider.scenarios.**` (Cucumber) and `SliderPluginFunctionalTests` |
| `functionalTest` | GradleTestKit functional tests | own source set, depends on `test` impl via `extendsFrom` trick |
| `cucumberTest` | Cucumber BDD feature files | uses JUnit Platform suite engine, excludes `junit-jupiter`, `dependsOn functionalTest.classesTaskName` |
| `check` | aggregates | `test + functionalTest + cucumberTest` |

`tasks.withType<Test>` enables `useJUnitPlatform()`, full exception logging and
`-XX:+EnableDynamicAgentLoading` to silence dynamic agent warnings. The test
classpath excludes `logback-classic` to avoid binding conflicts.

There is **no Kover** coverage gate and **no dedicated CVE audit task**.

## JVM tuning

- Plugin imposes `jvmToolchain(JavaVersion.VERSION_24.ordinal)` in
  `slider/build.gradle.kts` and asserts Java 23+ at runtime via
  `SliderManager.Prerequisites.checkJavaVersion()`.
- `gradle.properties` disables the daemon and Configuration Cache:
  `org.gradle.daemon=false`, `org.gradle.configuration-cache=false`.
- A `buildscript.resolutionStrategy` forces
  `org.jetbrains:annotations:26.0.2-1` to work around a koog-utils transitive
  conflict that bypasses Gradle's strict pin of `annotations:13.0`.
- RAG tasks must run with `--no-daemon` to reload the native ONNX library.

## Build commands

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

## CI pipeline

The repository ships `.github/workflows/` but the test workflow is **not
configured** — only documentation helpers (`readme_plantuml.yml`,
`readme_truth.yml`) and commented-out templates (`generate_deck.yml`,
`website.yml`). There is currently no PR test job, no coverage badge, and no
publish job.

## Publication (NMCP)

Configured via `com.gradleup.nmcp.settings` (1.5.0) in
`slider-plugin/settings.gradle.kts`. Credentials are read from
`~/.gradle/gradle.properties` (`ossrhUsername`, `ossrhPassword`), with
`publishingType = "AUTOMATIC"`.

The `slider/build.gradle.kts` plugin block wires:
- `gradlePlugin { create("slider") { id = "education.cccp.slider";
  implementationClass = "slider.SliderPlugin" } }` with tags `revealjs`,
  `slide-generator`, `slide`, `jgit`, `asciidoc`, `langchain4j`, `ollama`,
  `mistal-ai`, `huggingface`, `gemini`, `kotlin-DSL`.
- `vcsUrl = "https://github.com/cheroliv/slider-gradle.git"`,
  `website = "https://cheroliv.com"`.
- `compatibility { features { configurationCache = false } }` (the
  `asciidoctorRevealJs` task runs `OUT_OF_PROCESS` via JRuby and is incompatible
  with the Configuration Cache).
- POM declares Apache 2.0, developer `cccp-education`, SCM pointing to
  `github.com/cheroliv/slider-gradle`.
- Signing uses `useGpgCmd()` and signs publications on non-CI, non-SNAPSHOT builds.

## EPIC status

From `slider-plugin/.agents/INDEX.adoc` (last session 010):

| EPIC | Description | Status |
|------|-------------|--------|
| SLD-0 | Bootstrap governance agent | ✅ DONE |
| SLD-1 | Upgrade Reveal.js 3.9.1 → 5.2.1 + Auto-Animate + transitions | ✅ DONE (5/5 US) |
| SLD-2 | Playwright E2E + Pro theme + Capsule feed | 🔴 IN PROGRESS — 4/5 US done (US-2.5 pending) |
| SLD-3 | i18n 10 languages via `i18n-contracts:0.0.1` | ☐ NEW — framed session 010 |
| Publication Maven Central 0.0.1 | NMCP, hardcoded version | ✅ 2026-06-11 |

## Contributing

1. Build compiles: `./gradlew build -x test`
2. Tests green: `./gradlew check`
3. Respect the **single responsibility** pattern in `SliderManager` — each nested
   object owns one concern (Prerequisites, Repositories, Plugins, Dependencies,
   Extensions, Tasks, Git, FileOps).
4. Use explicit imports (no wildcards), 4-space indentation, opening brace on
   same line; constants are `SCREAMING_SNAKE_CASE`.
5. Do not enable the Configuration Cache — `asciidoctorRevealJs` is explicitly
   declared incompatible.

## Architecture docs

- [README.adoc](../README.adoc) — full AsciiDoc reference (PlantUML diagrams,
  two-step RAG pipeline, hexagonal view, deployment pipeline).
- [.agents/INDEX.adoc](../slider-plugin/.agents/INDEX.adoc) — EPICs & governance.
- [AGENT.adoc](../slider-plugin/AGENT.adoc) — absolute rules.
- [.agents/SESSION_CHECKLIST.adoc](../slider-plugin/.agents/SESSION_CHECKLIST.adoc) — session checklist.
- [doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc](../slider-plugin/doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc) — SLD-1 technical spec.

## License

Apache License 2.0 — see [LICENCE](../LICENCE).

---

_Part of the CCCP Education ecosystem — `groupId: education.cccp`._