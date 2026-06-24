<!-- master source — other languages are translations of this file -->
# slider-gradle — Consumer Guide

> RAG-augmented Gradle plugin that generates Reveal.js presentations from AsciiDoc sources.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-not%20configured-lightgrey?label=CI)](#)
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.slider`
- **Toolchain**: Java 24+ · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew asciidoctorRevealJs` · **Tests**: `./gradlew test` (JUnit5) + `cucumberTest` (Cucumber) + `functionalTest` (Gradle Test Kit)
- **Coverage**: not measured (no Kover)

🌐 Languages: **EN** | [中文](README.consommateurs/README.zh.md) | [हिन्दी](README.consommateurs/README.hi.md) | [Español](README.consommateurs/README.es.md) | [Français](README.consommateurs/README.fr.md) | [العربية](README.consommateurs/README.ar.md) | [বাংলা](README.consommateurs/README.bn.md) | [Português](README.consommateurs/README.pt.md) | [Русский](README.consommateurs/README.ru.md) | [اردو](README.consommateurs/README.ur.md)

---

## What it does

`slider-gradle` compiles AsciiDoc sources into interactive **Reveal.js** HTML
presentations and optionally generates full decks from natural-language subjects
through a two-step **RAG + LLM** pipeline backed by a pgvector store. An LLM proposes
a `*-deck-context.yml` for human review, then generates the final AsciiDoc deck
enriched by project examples.

It is part of the CCCP Education multi-plugin ecosystem (MIAMI borough, N2) and
consumes `codebase-gradle` (plugin id `education.cccp.codebase` version `0.0.1`).

```
subject → proposeDeckContext (RAG+LLM) → *-deck-context.yml → review → generateDeck (RAG+LLM) → <slug>_<lang>-deck.adoc → asciidoctorRevealJs → HTML deck
```

## Quick Start

### 1. Apply the plugin

```gradle
plugins {
    id("education.cccp.slider") version "0.0.1"
}
```

### 2. Configure the DSL

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

On the first execution the plugin scaffolds the `slides/` folder (from a bundled
`slides.zip`), a default `slides-context.yml` and a ready-to-use
`slides/misc/example-deck-context.yml`. Existing content is never overwritten.

### 3. Compile the slides

```bash
./gradlew asciidoctorRevealJs
```

Output is written to `build/docs/asciidocRevealJs/` (one HTML file per deck plus an
`index.html` dashboard and a `slides.json` manifest).

## Available tasks

| Task | Group | Description |
|------|-------|-------------|
| `asciidoctorRevealJs`     | generate | Compile AsciiDoc sources into a Reveal.js HTML presentation (depends on `cleanBuild`). |
| `asciidoctor`             | generate | Standard Asciidoctor HTML conversion (depends on `asciidoctorRevealJs`). |
| `cleanBuild`              | build    | Delete generated presentation artifacts (`slides.json`, `images/`, `.html`). |
| `generateDashboard`       | generate | Generate `index.html` and `slides.json` listing every deck (finalises `asciidoctorRevealJs`). |
| `serveSlides`             | info     | Serve the generated deck locally via `npx serve` (depends on `asciidoctorRevealJs`). |
| `deploySlides`            | deploy   | Deploy generated slides to the remote Git repository configured in `slides-context.yml` (depends on `asciidoctor`). |
| `generateCapsule`         | generate | Extract speaker notes from AsciiDoc decks into a video script under `build/capsule/`. |
| `installPlaywright`       | setup    | Install Playwright Chromium browser for visual tests. |
| `visualTest`              | slider   | Run Playwright visual snapshot tests on generated slides. |
| `reportTests`             | verify   | Run `check` and open the unit test report in Firefox. |
| `reportFunctionalTests`   | verify   | Run `check` and open the functional test report in Firefox. |
| `collectRagIndex`         | collect  | Force a full rebuild of the RAG embedding index. |
| `generateDeckContext`     | generate | Propose a `*-deck-context.yml` for a subject using RAG + LLM (step 1/2). |
| `generateDeck`            | generate | Generate a complete AsciiDoc/Reveal.js deck from a `*-deck-context.yml` (step 2/2). |
| `helloOllama*` / `helloOllamaStream*`       | slider-ai | Ollama model smoke tests. |
| `helloGemini*` / `helloGeminiStream*`       | slider-ai | Gemini model smoke tests. |
| `helloMistral*` / `helloMistralStream*`     | slider-ai | Mistral model smoke tests. |
| `helloHuggingFace*` / `helloHuggingFaceStream*` | slider-ai | HuggingFace model smoke tests. |

> The plugin IDs referenced in the project KDoc (`reindexRag`, `proposeDeckContext`)
> are actually registered as **`collectRagIndex`** and **`generateDeckContext`** respectively.
> Use the registered names on the command line.

## Extension DSL

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

Reveal.js is pinned to version **5.2.0** (gem `asciidoctor-revealjs:5.2.0@gem`)
against the `hakimel/reveal.js` tag `5.2.1` and configured with the `talaria.css`
theme. The DSL exposes a single `configPath` property; advanced deck options
(theme, transitions, notes) live in each deck-context YAML, not in the DSL.

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

## Typical workflows

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

> Always run RAG tasks with `--no-daemon`: the Gradle daemon reuses the JVM and
> prevents reloading the native ONNX library (`libtokenizers.so`), causing an
> `UnsatisfiedLinkError` on the second build.

## Provider selection

| `-Pai.provider` | Default model | API key location |
|-----------------|---------------|------------------|
| `ollama` _(default)_ | `smollm:135m` | none — local inference |
| `gemini` | `gemini-2.5-flash` | `slides-context.yml` → `ai.gemini[0]` |
| `mistral` | `mistral-small-latest` | `slides-context.yml` → `ai.mistral[0]` |
| `huggingface` | `Llama-3.1-8B-Instruct:sambanova` | `slides-context.yml` → `ai.huggingface[0]` |

Unknown or missing values fall back to `ollama` with a logged warning.

## File naming convention

| File | Pattern | Example |
|------|---------|---------|
| Generation context | `<slug>-deck-context.yml` | `kotlin-inline-functions-and-reification-deck-context.yml` |
| Generated AsciiDoc deck | `<slug>_<lang>-deck.adoc` | `kotlin-inline-functions-and-reification_en-deck.adoc` |
| Compiled HTML deck | `<slug>_<lang>-deck.html` | `kotlin-inline-functions-and-reification_en-deck.html` |

`<slug>` is derived from `subject` in kebab-case (accents normalised); `<lang>` is
the ISO 639-1 code passed via `-Planguage` (default `fr`).

## Prerequisites

- **Java** 24+ (the plugin guards for Java 23+ at runtime; Kotlin 2.3.20 toolchain)
- **Gradle** 9.5.1+
- **Node.js / npx** for the `serveSlides` and `visualTest` tasks
- **Docker** for the pgvector container used by the RAG pipeline
- **Internet connection** to download Reveal.js gem dependencies

## Build & test

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

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `UnsatisfiedLinkError: libtokenizers.so` on second RAG run | run RAG tasks with `--no-daemon` |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| pgvector container stuck | `docker rm -f $(docker ps -q -f name=pgvector)` then retry |
| `asciidoctorRevealJs` Configuration Cache failure | expected — Configuration Cache is disabled (`configurationCache = false` on the portal) |
| `npx: command not found` | install Node.js and ensure `npx` is on `PATH` |

See [README.adoc](../README.adoc) for the full architectural reference.

## License

Apache License 2.0 — see [LICENCE](../LICENCE).

---

_Part of the CCCP Education ecosystem — `groupId: education.cccp`._