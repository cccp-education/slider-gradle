<!-- translated from README.md rev 0.0.1 -->
# slider-gradle — Introducción del Plugin

> Guía para desarrolladores y colaboradores del plugin de Gradle `slider-plugin`.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-not%20configured-lightgrey?label=CI)](#)
[![Coverage](https://img.shields.io/static/v1?label=coverage&message=n%2Fa&color=lightgrey)]()
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.slider`
- **Toolchain**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew build -x test` · **Tests**: `./gradlew test` + `functionalTest` + `cucumberTest` (wired into `check`)
- **Coverage gate**: none (no Kover)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | **Español** | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## Disposición del módulo

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

## Plugin consumido (dependencia N2 → N2)

- `education.cccp.codebase` version `0.0.1` (catalog alias
  `libs.plugins.codebase` en `slider-plugin/gradle/libs.versions.toml`) — aplicado
  como plugin hermano en `slider/build.gradle.kts`. Proporciona las primitivas
  de ingesta EAGER/RAG reutilizadas por `RagManager`.

## Dependencias clave

`slider/build.gradle.kts` resuelve versiones desde `slider-plugin/gradle/libs.versions.toml`:

- **asciidoctor-gradle** `5.0.0-alpha.1` — `asciidoctor-gradle-jvm-slides`,
  `asciidoctor-gradle-jvm-gems`, `asciidoctor-gradle-jvm` (aplicados como plugin IDs
  `org.asciidoctor.jvm.revealjs.classic` y `org.asciidoctor.jvm.gems.classic`).
- **asciidoctor-revealjs gem** `5.2.0` fijada frente a la plantilla Reveal.js
  `hakimel/reveal.js` tag `5.2.1`.
- **asciidoctorj-diagram** `3.2.0` + **asciidoctorj-diagram-plantuml** `1.2025.3`.
- **node-gradle** `7.1.0` — `NpxTask` para `serveSlides`, `installPlaywright`, `visualTest`.
- **langchain4j** `1.14.1` + **langchain4j-beta** `1.14.1-beta24` — `langchain4j`,
  `langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
  `langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`.
- **JGit** `7.5.0.202512021534-r` — `org.eclipse.jgit`, `jgit.ssh.jsch`,
  `jgit.archive` (bundle).
- **docker-java** `3.7.0` + **testcontainers-postgresql** `1.21.4` — ciclo de vida pgvector.
- **Jackson** `2.21.1` — `jackson-module-kotlin`, `jackson-dataformat-yaml`.
- **Arrow** `2.2.2` + `arrow-integrations-jackson-module` `0.15.1` — helpers FP tipados.
- **kotlinx-coroutines** `1.10.2` — fixtures de test asíncronos (`core`, `jdk8`, `test`).
- **Playwright** `1.52.0` — pruebas E2E visuales (`testImplementation`).
- **commons-io** `2.13.0`, **slf4j** `2.0.17`, **logback** `1.5.32`.

Pila de pruebas: **Kotless** JUnit5 (`kotlin-test-junit5`), **AssertJ** `3.27.7`,
**Mockito Kotlin** `6.2.3`, **Mockito JUnit Jupiter** `5.23.0`, **Cucumber**
`7.34.3` (`cucumber-java`, `cucumber-junit-platform-engine`, `cucumber-picocontainer`,
`junit-platform-suite`).

## Instancias de Ollama (restricción global)

Los puertos `11434–11436` están prohibidos. Rote sobre `11437–11465` (29 puertos).
Modelos autorizados: `gpt-oss:120b-cloud`, `gemma4:31b-cloud`.
Modelos localmente disponibles catalogados en `AssistantManager.localModels`:
`smollm:135m`, `llama3.2:3b-instruct-q8_0`, `smollm:135m-instruct-v0.2-q8_0`,
`gemma3:1b-it-fp16`.

## Matriz de pruebas

| Task | Scope | Notes |
|------|-------|-------|
| `test` | JUnit5 unit tests | excludes `com.cheroliv.slider.scenarios.**` (Cucumber) and `SliderPluginFunctionalTests` |
| `functionalTest` | GradleTestKit functional tests | own source set, depends on `test` impl via `extendsFrom` trick |
| `cucumberTest` | Cucumber BDD feature files | uses JUnit Platform suite engine, excludes `junit-jupiter`, `dependsOn functionalTest.classesTaskName` |
| `check` | aggregates | `test + functionalTest + cucumberTest` |

`tasks.withType<Test>` habilita `useJUnitPlatform()`, registro completo de
excepciones y `-XX:+EnableDynamicAgentLoading` para silenciar las advertencias de
agentes dinámicos. El classpath de pruebas excluye `logback-classic` para evitar
conflictos de enlace.

No hay **puerta de cobertura Kover** ni **tarea dedicada de auditoría CVE**.

## Ajuste de JVM

- El plugin impone `jvmToolchain(JavaVersion.VERSION_24.ordinal)` en
  `slider/build.gradle.kts` y exige Java 23+ en runtime vía
  `SliderManager.Prerequisites.checkJavaVersion()`.
- `gradle.properties` desactiva el daemon y el Configuration Cache:
  `org.gradle.daemon=false`, `org.gradle.configuration-cache=false`.
- Un `buildscript.resolutionStrategy` fuerza
  `org.jetbrains:annotations:26.0.2-1` para sortear un conflicto transitivo de
  koog-utils que evita el pin estricto de Gradle de `annotations:13.0`.
- Las tareas RAG deben ejecutarse con `--no-daemon` para recargar la biblioteca
  nativa ONNX.

## Comandos de construcción

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

## Pipeline de CI

El repositorio incluye `.github/workflows/` pero el workflow de pruebas **no está
configurado** — solo helpers de documentación (`readme_plantuml.yml`,
`readme_truth.yml`) y plantillas comentadas (`generate_deck.yml`,
`website.yml`). Actualmente no hay job de pruebas de PR, ni badge de cobertura, ni
job de publicación.

## Publicación (NMCP)

Configurada vía `com.gradleup.nmcp.settings` (1.5.0) en
`slider-plugin/settings.gradle.kts`. Las credenciales se leen de
`~/.gradle/gradle.properties` (`ossrhUsername`, `ossrhPassword`), con
`publishingType = "AUTOMATIC"`.

El bloque de plugin en `slider/build.gradle.kts` cablea:
- `gradlePlugin { create("slider") { id = "education.cccp.slider";
  implementationClass = "slider.SliderPlugin" } }` con tags `revealjs`,
  `slide-generator`, `slide`, `jgit`, `asciidoc`, `langchain4j`, `ollama`,
  `mistal-ai`, `huggingface`, `gemini`, `kotlin-DSL`.
- `vcsUrl = "https://github.com/cheroliv/slider-gradle.git"`,
  `website = "https://cheroliv.com"`.
- `compatibility { features { configurationCache = false } }` (la tarea
  `asciidoctorRevealJs` se ejecuta `OUT_OF_PROCESS` vía JRuby y es incompatible
  con el Configuration Cache).
- El POM declara Apache 2.0, desarrollador `cccp-education`, SCM apuntando a
  `github.com/cheroliv/slider-gradle`.
- La firma usa `useGpgCmd()` y firma las publicaciones en builds no-CI, no-SNAPSHOT.

## Estado de EPICs

Desde `slider-plugin/.agents/INDEX.adoc` (última sesión 010):

| EPIC | Description | Status |
|------|-------------|--------|
| SLD-0 | Bootstrap governance agent | ✅ DONE |
| SLD-1 | Upgrade Reveal.js 3.9.1 → 5.2.1 + Auto-Animate + transitions | ✅ DONE (5/5 US) |
| SLD-2 | Playwright E2E + Pro theme + Capsule feed | 🔴 IN PROGRESS — 4/5 US done (US-2.5 pending) |
| SLD-3 | i18n 10 languages via `i18n-contracts:0.0.1` | ☐ NEW — framed session 010 |
| Publication Maven Central 0.0.1 | NMCP, hardcoded version | ✅ 2026-06-11 |

## Contribución

1. La compilación pasa: `./gradlew build -x test`
2. Pruebas en verde: `./gradlew check`
3. Respete el patrón de **responsabilidad única** en `SliderManager` — cada object
   anidado posee una preocupación (Prerequisites, Repositories, Plugins, Dependencies,
   Extensions, Tasks, Git, FileOps).
4. Use imports explícitos (sin comodines), indentación de 4 espacios, llave de
   apertura en la misma línea; las constantes son `SCREAMING_SNAKE_CASE`.
5. No habilite el Configuration Cache — `asciidoctorRevealJs` está declarado
   explícitamente como incompatible.

## Documentos de arquitectura

- [README.adoc](../README.adoc) — referencia completa en AsciiDoc (diagramas PlantUML,
  pipeline RAG de dos pasos, vista hexagonal, pipeline de despliegue).
- [.agents/INDEX.adoc](../slider-plugin/.agents/INDEX.adoc) — EPICs y gobernanza.
- [AGENT.adoc](../slider-plugin/AGENT.adoc) — reglas absolutas.
- [.agents/SESSION_CHECKLIST.adoc](../slider-plugin/.agents/SESSION_CHECKLIST.adoc) — checklist de sesión.
- [doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc](../slider-plugin/doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc) — especificación técnica SLD-1.

## License

Apache License 2.0 — consulte [LICENCE](../LICENCE).

---

_Parte del ecosistema CCCP Education — `groupId: education.cccp`._