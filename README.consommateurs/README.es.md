<!-- translated from README.md rev 0.0.1 -->
# slider-gradle — Guía del Consumidor

> Plugin de Gradle aumentado con RAG que genera presentaciones Reveal.js a partir de fuentes AsciiDoc.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-not%20configured-lightgrey?label=CI)](#)
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.slider`
- **Toolchain**: Java 24+ · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew asciidoctorRevealJs` · **Tests**: `./gradlew test` (JUnit5) + `cucumberTest` (Cucumber) + `functionalTest` (Gradle Test Kit)
- **Coverage**: not measured (no Kover)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | **Español** | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## Qué hace

`slider-gradle` compila fuentes AsciiDoc en presentaciones HTML
interactivas de **Reveal.js** y, opcionalmente, genera decks completos a partir de
temas en lenguaje natural mediante un pipeline de dos pasos **RAG + LLM** respaldado
por un almacén pgvector. Un LLM propone un
`*-deck-context.yml` para revisión humana, luego genera el deck AsciiDoc final
enriquecido con ejemplos del proyecto.

Forma parte del ecosistema multi-plugin de CCCP Education (MIAMI borough, N2) y
consume `codebase-gradle` (plugin id `education.cccp.codebase` version `0.0.1`).

```
subject → proposeDeckContext (RAG+LLM) → *-deck-context.yml → review → generateDeck (RAG+LLM) → <slug>_<lang>-deck.adoc → asciidoctorRevealJs → HTML deck
```

## Inicio rápido

### 1. Aplicar el plugin

```gradle
plugins {
    id("education.cccp.slider") version "0.0.1"
}
```

### 2. Configurar el DSL

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

En la primera ejecución, el plugin crea la carpeta `slides/` (a partir de un
`slides.zip` incluido), un `slides-context.yml` predeterminado y un
`slides/misc/example-deck-context.yml` listo para usar. El contenido existente nunca
se sobrescribe.

### 3. Compilar las diapositivas

```bash
./gradlew asciidoctorRevealJs
```

La salida se escribe en `build/docs/asciidocRevealJs/` (un archivo HTML por deck, más
un panel `index.html` y un manifiesto `slides.json`).

## Tareas disponibles

| Task | Group | Description |
|------|-------|-------------|
| `asciidoctorRevealJs`     | generate | Compila fuentes AsciiDoc en una presentación HTML Reveal.js (depende de `cleanBuild`). |
| `asciidoctor`             | generate | Conversión estándar HTML de Asciidoctor (depende de `asciidoctorRevealJs`). |
| `cleanBuild`              | build    | Elimina los artefactos de presentación generados (`slides.json`, `images/`, `.html`). |
| `generateDashboard`       | generate | Genera `index.html` y `slides.json` que listan cada deck (finaliza `asciidoctorRevealJs`). |
| `serveSlides`             | info     | Sirve el deck generado localmente vía `npx serve` (depende de `asciidoctorRevealJs`). |
| `deploySlides`            | deploy   | Despliega las diapositivas generadas al repositorio Git remoto configurado en `slides-context.yml` (depende de `asciidoctor`). |
| `generateCapsule`         | generate | Extrae las notas del orador de los decks AsciiDoc a un guion de vídeo en `build/capsule/`. |
| `installPlaywright`       | setup    | Instala el navegador Playwright Chromium para pruebas visuales. |
| `visualTest`              | slider   | Ejecuta pruebas de instantáneas visuales de Playwright sobre las diapositivas generadas. |
| `reportTests`             | verify   | Ejecuta `check` y abre el informe de pruebas unitarias en Firefox. |
| `reportFunctionalTests`   | verify   | Ejecuta `check` y abre el informe de pruebas funcionales en Firefox. |
| `collectRagIndex`         | collect  | Fuerza una reconstrucción completa del índice de embeddings RAG. |
| `generateDeckContext`     | generate | Propone un `*-deck-context.yml` para un tema usando RAG + LLM (paso 1/2). |
| `generateDeck`            | generate | Genera un deck AsciiDoc/Reveal.js completo a partir de un `*-deck-context.yml` (paso 2/2). |
| `helloOllama*` / `helloOllamaStream*`       | slider-ai | Pruebas de humo de modelos Ollama. |
| `helloGemini*` / `helloGeminiStream*`       | slider-ai | Pruebas de humo de modelos Gemini. |
| `helloMistral*` / `helloMistralStream*`     | slider-ai | Pruebas de humo de modelos Mistral. |
| `helloHuggingFace*` / `helloHuggingFaceStream*` | slider-ai | Pruebas de humo de modelos HuggingFace. |

> Los IDs de plugin referenciados en el KDoc del proyecto (`reindexRag`,
> `proposeDeckContext`) están registrados en realidad como **`collectRagIndex`** y
> **`generateDeckContext`** respectivamente. Use los nombres registrados en la línea
> de comandos.

## DSL de extensión

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

Reveal.js está fijado a la versión **5.2.0** (gem
`asciidoctor-revealjs:5.2.0@gem`) frente a la etiqueta `hakimel/reveal.js` `5.2.1` y
configurado con el tema `talaria.css`. El DSL expone una sola propiedad `configPath`;
las opciones avanzadas del deck (tema, transiciones, notas) viven en cada YAML
deck-context, no en el DSL.

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

## Flujos de trabajo típicos

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

> Ejecute siempre las tareas RAG con `--no-daemon`: el daemon de Gradle reutiliza la
> JVM y evita recargar la biblioteca nativa ONNX (`libtokenizers.so`), provocando un
> `UnsatisfiedLinkError` en la segunda compilación.

## Selección de provider

| `-Pai.provider` | Default model | API key location |
|-----------------|---------------|------------------|
| `ollama` _(default)_ | `smollm:135m` | none — local inference |
| `gemini` | `gemini-2.5-flash` | `slides-context.yml` → `ai.gemini[0]` |
| `mistral` | `mistral-small-latest` | `slides-context.yml` → `ai.mistral[0]` |
| `huggingface` | `Llama-3.1-8B-Instruct:sambanova` | `slides-context.yml` → `ai.huggingface[0]` |

Los valores desconocidos o ausentes revierten a `ollama` con una advertencia registrada.

## Convención de nombres de archivo

| File | Pattern | Example |
|------|---------|---------|
| Generation context | `<slug>-deck-context.yml` | `kotlin-inline-functions-and-reification-deck-context.yml` |
| Generated AsciiDoc deck | `<slug>_<lang>-deck.adoc` | `kotlin-inline-functions-and-reification_en-deck.adoc` |
| Compiled HTML deck | `<slug>_<lang>-deck.html` | `kotlin-inline-functions-and-reification_en-deck.html` |

`<slug>` se deriva de `subject` en kebab-case (acentos normalizados); `<lang>` es
el código ISO 639-1 pasado vía `-Planguage` (predeterminado `fr`).

## Requisitos previos

- **Java** 24+ (el plugin requiere Java 23+ en tiempo de ejecución; toolchain Kotlin 2.3.20)
- **Gradle** 9.5.1+
- **Node.js / npx** para las tareas `serveSlides` y `visualTest`
- **Docker** para el contenedor pgvector usado por el pipeline RAG
- **Internet connection** para descargar las dependencias gem de Reveal.js

## Construcción y pruebas

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

## Solución de problemas

| Symptom | Fix |
|---------|-----|
| `UnsatisfiedLinkError: libtokenizers.so` on second RAG run | run RAG tasks with `--no-daemon` |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| pgvector container stuck | `docker rm -f $(docker ps -q -f name=pgvector)` then retry |
| `asciidoctorRevealJs` Configuration Cache failure | expected — Configuration Cache is disabled (`configurationCache = false` on the portal) |
| `npx: command not found` | install Node.js and ensure `npx` is on `PATH` |

Consulte [README.adoc](../README.adoc) para la referencia arquitectónica completa.

## License

Apache License 2.0 — consulte [LICENCE](../LICENCE).

---

_Parte del ecosistema CCCP Education — `groupId: education.cccp`._