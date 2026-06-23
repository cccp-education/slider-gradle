<!-- translated from README.md rev 0.0.1 -->
# slider-gradle — Internais do Plugin

> Guia para desenvolvedores e contribuidores do plugin Gradle `slider-plugin`.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-not%20configured-lightgrey?label=CI)](#)
[![Coverage](https://img.shields.io/static/v1?label=coverage&message=n%2Fa&color=lightgrey)]()
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.slider`
- **Toolchain**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew build -x test` · **Tests**: `./gradlew test` + `functionalTest` + `cucumberTest` (wired into `check`)
- **Coverage gate**: none (no Kover)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | **Português** | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## Layout do módulo

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

## Plugin consumido (dependência N2 → N2)

- `education.cccp.codebase` version `0.0.1` (catalog alias
  `libs.plugins.codebase` em `slider-plugin/gradle/libs.versions.toml`) — aplicado
  como plugin irmão em `slider/build.gradle.kts`. Fornece as primitivas de
  ingestão EAGER/RAG reutilizadas por `RagManager`.

## Dependências principais

`slider/build.gradle.kts` resolve versões de `slider-plugin/gradle/libs.versions.toml`:

- **asciidoctor-gradle** `5.0.0-alpha.1` — `asciidoctor-gradle-jvm-slides`,
  `asciidoctor-gradle-jvm-gems`, `asciidoctor-gradle-jvm` (aplicados como plugin IDs
  `org.asciidoctor.jvm.revealjs.classic` e `org.asciidoctor.jvm.gems.classic`).
- **asciidoctor-revealjs gem** `5.2.0` fixada contra o template Reveal.js
  `hakimel/reveal.js` tag `5.2.1`.
- **asciidoctorj-diagram** `3.2.0` + **asciidoctorj-diagram-plantuml** `1.2025.3`.
- **node-gradle** `7.1.0` — `NpxTask` para `serveSlides`, `installPlaywright`, `visualTest`.
- **langchain4j** `1.14.1` + **langchain4j-beta** `1.14.1-beta24` — `langchain4j`,
  `langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
  `langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`.
- **JGit** `7.5.0.202512021534-r` — `org.eclipse.jgit`, `jgit.ssh.jsch`,
  `jgit.archive` (bundle).
- **docker-java** `3.7.0` + **testcontainers-postgresql** `1.21.4` — ciclo de vida do pgvector.
- **Jackson** `2.21.1` — `jackson-module-kotlin`, `jackson-dataformat-yaml`.
- **Arrow** `2.2.2` + `arrow-integrations-jackson-module` `0.15.1` — helpers FP tipados.
- **kotlinx-coroutines** `1.10.2` — fixtures de teste assíncronos (`core`, `jdk8`, `test`).
- **Playwright** `1.52.0` — testes E2E visuais (`testImplementation`).
- **commons-io** `2.13.0`, **slf4j** `2.0.17`, **logback** `1.5.32`.

Stack de testes: **Kotless** JUnit5 (`kotlin-test-junit5`), **AssertJ** `3.27.7`,
**Mockito Kotlin** `6.2.3`, **Mockito JUnit Jupiter** `5.23.0`, **Cucumber**
`7.34.3` (`cucumber-java`, `cucumber-junit-platform-engine`, `cucumber-picocontainer`,
`junit-platform-suite`).

## Instâncias Ollama (restrição global)

Portas `11434–11436` são proibidas. Rotacione sobre `11437–11465` (29 portas).
Modelos autorizados: `gpt-oss:120b-cloud`, `gemma4:31b-cloud`.
Modelos disponíveis localmente catalogados em `AssistantManager.localModels`:
`smollm:135m`, `llama3.2:3b-instruct-q8_0`, `smollm:135m-instruct-v0.2-q8_0`,
`gemma3:1b-it-fp16`.

## Matriz de testes

| Task | Scope | Notes |
|------|-------|-------|
| `test` | JUnit5 unit tests | excludes `com.cheroliv.slider.scenarios.**` (Cucumber) and `SliderPluginFunctionalTests` |
| `functionalTest` | GradleTestKit functional tests | own source set, depends on `test` impl via `extendsFrom` trick |
| `cucumberTest` | Cucumber BDD feature files | uses JUnit Platform suite engine, excludes `junit-jupiter`, `dependsOn functionalTest.classesTaskName` |
| `check` | aggregates | `test + functionalTest + cucumberTest` |

`tasks.withType<Test>` habilita `useJUnitPlatform()`, registro completo de
exceções e `-XX:+EnableDynamicAgentLoading` para silenciar avisos de agentes
dinâmicos. O classpath de testes exclui `logback-classic` para evitar conflitos
de binding.

Não há **porta de cobertura Kover** nem **tarefa dedicada de auditoria CVE**.

## Ajuste da JVM

- O plugin impõe `jvmToolchain(JavaVersion.VERSION_24.ordinal)` em
  `slider/build.gradle.kts` e exige Java 23+ em runtime via
  `SliderManager.Prerequisites.checkJavaVersion()`.
- `gradle.properties` desativa o daemon e o Configuration Cache:
  `org.gradle.daemon=false`, `org.gradle.configuration-cache=false`.
- Um `buildscript.resolutionStrategy` força
  `org.jetbrains:annotations:26.0.2-1` para contornar um conflito transitivo do
  koog-utils que contorna o pin estrito do Gradle de `annotations:13.0`.
- Tarefas RAG devem executar com `--no-daemon` para recarregar a biblioteca
  nativa ONNX.

## Comandos de build

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

O repositório inclui `.github/workflows/` mas o workflow de testes **não está
configurado** — apenas helpers de documentação (`readme_plantuml.yml`,
`readme_truth.yml`) e templates comentados (`generate_deck.yml`,
`website.yml`). Atualmente não há job de testes de PR, nem badge de cobertura,
nem job de publicação.

## Publicação (NMCP)

Configurado via `com.gradleup.nmcp.settings` (1.5.0) em
`slider-plugin/settings.gradle.kts`. As credenciais são lidas de
`~/.gradle/gradle.properties` (`ossrhUsername`, `ossrhPassword`), com
`publishingType = "AUTOMATIC"`.

O bloco de plugin em `slider/build.gradle.kts` conecta:
- `gradlePlugin { create("slider") { id = "education.cccp.slider";
  implementationClass = "slider.SliderPlugin" } }` com tags `revealjs`,
  `slide-generator`, `slide`, `jgit`, `asciidoc`, `langchain4j`, `ollama`,
  `mistal-ai`, `huggingface`, `gemini`, `kotlin-DSL`.
- `vcsUrl = "https://github.com/cheroliv/slider-gradle.git"`,
  `website = "https://cheroliv.com"`.
- `compatibility { features { configurationCache = false } }` (a tarefa
  `asciidoctorRevealJs` executa `OUT_OF_PROCESS` via JRuby e é incompatível
  com o Configuration Cache).
- O POM declara Apache 2.0, desenvolvedor `cccp-education`, SCM apontando para
  `github.com/cheroliv/slider-gradle`.
- A assinatura usa `useGpgCmd()` e assina publicações em builds non-CI, non-SNAPSHOT.

## Status dos EPICs

De `slider-plugin/.agents/INDEX.adoc` (última sessão 010):

| EPIC | Description | Status |
|------|-------------|--------|
| SLD-0 | Bootstrap governance agent | ✅ DONE |
| SLD-1 | Upgrade Reveal.js 3.9.1 → 5.2.1 + Auto-Animate + transitions | ✅ DONE (5/5 US) |
| SLD-2 | Playwright E2E + Pro theme + Capsule feed | 🔴 IN PROGRESS — 4/5 US done (US-2.5 pending) |
| SLD-3 | i18n 10 languages via `i18n-contracts:0.0.1` | ☐ NEW — framed session 010 |
| Publication Maven Central 0.0.1 | NMCP, hardcoded version | ✅ 2026-06-11 |

## Contribuindo

1. O build compila: `./gradlew build -x test`
2. Testes verdes: `./gradlew check`
3. Respeite o padrão de **responsabilidade única** em `SliderManager` — cada object
   aninhado possui uma preocupação (Prerequisites, Repositories, Plugins, Dependencies,
   Extensions, Tasks, Git, FileOps).
4. Use imports explícitos (sem wildcards), indentação de 4 espaços, chave de
   abertura na mesma linha; constantes são `SCREAMING_SNAKE_CASE`.
5. Não habilite o Configuration Cache — `asciidoctorRevealJs` está declarado
   explicitamente como incompatível.

## Documentos de arquitetura

- [README.adoc](../README.adoc) — referência completa em AsciiDoc (diagramas PlantUML,
  pipeline RAG de dois passos, visão hexagonal, pipeline de implantação).
- [.agents/INDEX.adoc](../slider-plugin/.agents/INDEX.adoc) — EPICs e governança.
- [AGENT.adoc](../slider-plugin/AGENT.adoc) — regras absolutas.
- [.agents/SESSION_CHECKLIST.adoc](../slider-plugin/.agents/SESSION_CHECKLIST.adoc) — checklist de sessão.
- [doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc](../slider-plugin/doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc) — especificação técnica SLD-1.

## License

Apache License 2.0 — consulte [LICENCE](../LICENCE).

---

_Parte do ecossistema CCCP Education — `groupId: education.cccp`._