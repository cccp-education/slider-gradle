<!-- translated from README.md rev 0.0.1 -->
# slider-gradle — Guia do Consumidor

> Plugin do Gradle aumentado por RAG que gera apresentações Reveal.js a partir de fontes AsciiDoc.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-not%20configured-lightgrey?label=CI)](#)
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.slider`
- **Toolchain**: Java 24+ · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew asciidoctorRevealJs` · **Tests**: `./gradlew test` (JUnit5) + `cucumberTest` (Cucumber) + `functionalTest` (Gradle Test Kit)
- **Coverage**: not measured (no Kover)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | **Português** | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## O que faz

`slider-gradle` compila fontes AsciiDoc em apresentações HTML
interativas de **Reveal.js** e, opcionalmente, gera decks completos a partir de
temas em linguagem natural por meio de um pipeline de dois passos **RAG + LLM**
apoiado por um armazenamento pgvector. Um LLM propõe um
`*-deck-context.yml` para revisão humana, depois gera o deck AsciiDoc final
enriquecido com exemplos do projeto.

Faz parte do ecossistema multi-plugin do CCCP Education (MIAMI borough, N2) e
consome `codebase-gradle` (plugin id `education.cccp.codebase` version `0.0.1`).

```
subject → proposeDeckContext (RAG+LLM) → *-deck-context.yml → review → generateDeck (RAG+LLM) → <slug>_<lang>-deck.adoc → asciidoctorRevealJs → HTML deck
```

## Início rápido

### 1. Aplicar o plugin

```gradle
plugins {
    id("education.cccp.slider") version "0.0.1"
}
```

### 2. Configurar o DSL

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

Na primeira execução, o plugin cria a pasta `slides/` (a partir de um
`slides.zip` embutido), um `slides-context.yml` padrão e um
`slides/misc/example-deck-context.yml` pronto para uso. O conteúdo existente nunca
é sobrescrito.

### 3. Compilar os slides

```bash
./gradlew asciidoctorRevealJs
```

A saída é gravada em `build/docs/asciidocRevealJs/` (um arquivo HTML por deck, além
de um painel `index.html` e um manifesto `slides.json`).

## Tarefas disponíveis

| Task | Group | Description |
|------|-------|-------------|
| `asciidoctorRevealJs`     | generate | Compila fontes AsciiDoc em uma apresentação HTML Reveal.js (depende de `cleanBuild`). |
| `asciidoctor`             | generate | Conversão padrão de Asciidoctor para HTML (depende de `asciidoctorRevealJs`). |
| `cleanBuild`              | build    | Exclui artefatos de apresentação gerados (`slides.json`, `images/`, `.html`). |
| `generateDashboard`       | generate | Gera `index.html` e `slides.json` listando cada deck (finaliza `asciidoctorRevealJs`). |
| `serveSlides`             | info     | Serve o deck gerado localmente via `npx serve` (depende de `asciidoctorRevealJs`). |
| `deploySlides`            | deploy   | Implanta os slides gerados no repositório Git remoto configurado em `slides-context.yml` (depende de `asciidoctor`). |
| `generateCapsule`         | generate | Extrai notas do orador dos decks AsciiDoc para um roteiro de vídeo em `build/capsule/`. |
| `installPlaywright`       | setup    | Instala o navegador Playwright Chromium para testes visuais. |
| `visualTest`              | slider   | Executa testes de instantâneo visual do Playwright nos slides gerados. |
| `reportTests`             | verify   | Executa `check` e abre o relatório de testes de unidade no Firefox. |
| `reportFunctionalTests`   | verify   | Executa `check` e abre o relatório de testes funcionais no Firefox. |
| `collectRagIndex`         | collect  | Força uma reconstrução completa do índice de embeddings RAG. |
| `generateDeckContext`     | generate | Propõe um `*-deck-context.yml` para um tema usando RAG + LLM (passo 1/2). |
| `generateDeck`            | generate | Gera um deck AsciiDoc/Reveal.js completo a partir de um `*-deck-context.yml` (passo 2/2). |
| `helloOllama*` / `helloOllamaStream*`       | slider-ai | Testes de fumaça de modelos Ollama. |
| `helloGemini*` / `helloGeminiStream*`       | slider-ai | Testes de fumaça de modelos Gemini. |
| `helloMistral*` / `helloMistralStream*`     | slider-ai | Testes de fumaça de modelos Mistral. |
| `helloHuggingFace*` / `helloHuggingFaceStream*` | slider-ai | Testes de fumaça de modelos HuggingFace. |

> Os IDs de plugin referenciados no KDoc do projeto (`reindexRag`,
> `proposeDeckContext`) estão na verdade registrados como **`collectRagIndex`** e
> **`generateDeckContext`** respectivamente. Use os nomes registrados na linha de
> comando.

## DSL de extensão

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

Reveal.js está fixado na versão **5.2.0** (gem
`asciidoctor-revealjs:5.2.0@gem`) em relação à tag `hakimel/reveal.js` `5.2.1` e
configurado com o tema `talaria.css`. O DSL expõe uma única propriedade `configPath`;
opções avançadas do deck (tema, transições, notas) residem em cada YAML deck-context,
não no DSL.

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

## Fluxos de trabalho típicos

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

> Sempre execute tarefas RAG com `--no-daemon`: o daemon do Gradle reutiliza a JVM
> e impede o recarregamento da biblioteca nativa ONNX (`libtokenizers.so`), causando
> um `UnsatisfiedLinkError` na segunda compilação.

## Seleção de provider

| `-Pai.provider` | Default model | API key location |
|-----------------|---------------|------------------|
| `ollama` _(default)_ | `smollm:135m` | none — local inference |
| `gemini` | `gemini-2.5-flash` | `slides-context.yml` → `ai.gemini[0]` |
| `mistral` | `mistral-small-latest` | `slides-context.yml` → `ai.mistral[0]` |
| `huggingface` | `Llama-3.1-8B-Instruct:sambanova` | `slides-context.yml` → `ai.huggingface[0]` |

Valores desconhecidos ou ausentes revertem para `ollama` com um aviso registrado.

## Convenção de nomenclatura de arquivos

| File | Pattern | Example |
|------|---------|---------|
| Generation context | `<slug>-deck-context.yml` | `kotlin-inline-functions-and-reification-deck-context.yml` |
| Generated AsciiDoc deck | `<slug>_<lang>-deck.adoc` | `kotlin-inline-functions-and-reification_en-deck.adoc` |
| Compiled HTML deck | `<slug>_<lang>-deck.html` | `kotlin-inline-functions-and-reification_en-deck.html` |

`<slug>` é derivado de `subject` em kebab-case (acentos normalizados); `<lang>` é
o código ISO 639-1 passado via `-Planguage` (padrão `fr`).

## Pré-requisitos

- **Java** 24+ (o plugin exige Java 23+ em runtime; toolchain Kotlin 2.3.20)
- **Gradle** 9.5.1+
- **Node.js / npx** para as tarefas `serveSlides` e `visualTest`
- **Docker** para o contêiner pgvector usado pelo pipeline RAG
- **Internet connection** para baixar dependências gem do Reveal.js

## Build e testes

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

## Solução de problemas

| Symptom | Fix |
|---------|-----|
| `UnsatisfiedLinkError: libtokenizers.so` on second RAG run | run RAG tasks with `--no-daemon` |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| pgvector container stuck | `docker rm -f $(docker ps -q -f name=pgvector)` then retry |
| `asciidoctorRevealJs` Configuration Cache failure | expected — Configuration Cache is disabled (`configurationCache = false` on the portal) |
| `npx: command not found` | install Node.js and ensure `npx` is on `PATH` |

Consulte [README.adoc](../README.adoc) para a referência arquitetural completa.

## License

Apache License 2.0 — consulte [LICENCE](../LICENCE).

---

_Parte do ecossistema CCCP Education — `groupId: education.cccp`._