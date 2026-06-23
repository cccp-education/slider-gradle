<!-- translated from README.md rev 0.0.1 -->
# slider-gradle — Руководство потребителя

> Плагин Gradle с усилением RAG, генерирующий презентации Reveal.js из источников AsciiDoc.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-not%20configured-lightgrey?label=CI)](#)
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.slider`
- **Toolchain**: Java 24+ · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew asciidoctorRevealJs` · **Tests**: `./gradlew test` (JUnit5) + `cucumberTest` (Cucumber) + `functionalTest` (Gradle Test Kit)
- **Coverage**: not measured (no Kover)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | **Русский** | [اردو](README.ur.md)

---

## Что делает

`slider-gradle` компилирует источники AsciiDoc в интерактивные
HTML-презентации **Reveal.js** и опционально генерирует полные колоды из тем на
естественном языке через двухшаговый конвейер **RAG + LLM** на базе хранилища
pgvector. LLM предлагает
`*-deck-context.yml` для проверки человеком, затем генерирует финальную колоду AsciiDoc,
обогащённую примерами проекта.

Он является частью мультиплагинной экосистемы CCCP Education (MIAMI borough, N2) и
использует `codebase-gradle` (plugin id `education.cccp.codebase` version `0.0.1`).

```
subject → proposeDeckContext (RAG+LLM) → *-deck-context.yml → review → generateDeck (RAG+LLM) → <slug>_<lang>-deck.adoc → asciidoctorRevealJs → HTML deck
```

## Быстрый старт

### 1. Применить плагин

```gradle
plugins {
    id("education.cccp.slider") version "0.0.1"
}
```

### 2. Настроить DSL

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

При первом запуске плагин создаёт папку `slides/` (из встроенного
`slides.zip`), стандартный `slides-context.yml` и готовый к использованию
`slides/misc/example-deck-context.yml`. Существующее содержимое никогда не
перезаписывается.

### 3. Компиляция слайдов

```bash
./gradlew asciidoctorRevealJs
```

Результат записывается в `build/docs/asciidocRevealJs/` (по одному HTML-файлу на
колоду, плюс панель `index.html` и манифест `slides.json`).

## Доступные задачи

| Task | Group | Description |
|------|-------|-------------|
| `asciidoctorRevealJs`     | generate | Компилирует источники AsciiDoc в HTML-презентацию Reveal.js (зависит от `cleanBuild`). |
| `asciidoctor`             | generate | Стандартное HTML-преобразование Asciidoctor (зависит от `asciidoctorRevealJs`). |
| `cleanBuild`              | build    | Удаляет созданные артефакты презентации (`slides.json`, `images/`, `.html`). |
| `generateDashboard`       | generate | Генерирует `index.html` и `slides.json`, перечисляющие каждую колоду (завершает `asciidoctorRevealJs`). |
| `serveSlides`             | info     | Обслуживает созданную колоду локально через `npx serve` (зависит от `asciidoctorRevealJs`). |
| `deploySlides`            | deploy   | Развёртывание созданных слайдов в удалённый Git-репозиторий, настроенный в `slides-context.yml` (зависит от `asciidoctor`). |
| `generateCapsule`         | generate | Извлекает заметки докладчика из колод AsciiDoc в видеосценарий в `build/capsule/`. |
| `installPlaywright`       | setup    | Устанавливает браузер Playwright Chromium для визуальных тестов. |
| `visualTest`              | slider   | Запускает визуальные тесты снимков Playwright на созданных слайдах. |
| `reportTests`             | verify   | Запускает `check` и открывает отчёт по модульным тестам в Firefox. |
| `reportFunctionalTests`   | verify   | Запускает `check` и открывает отчёт по функциональным тестам в Firefox. |
| `collectRagIndex`         | collect  | Принудительно полностью перестраивает индекс эмбеддингов RAG. |
| `generateDeckContext`     | generate | Предлагает `*-deck-context.yml` для темы с использованием RAG + LLM (шаг 1/2). |
| `generateDeck`            | generate | Генерирует полную колоду AsciiDoc/Reveal.js из `*-deck-context.yml` (шаг 2/2). |
| `helloOllama*` / `helloOllamaStream*`       | slider-ai | Дымовые тесты моделей Ollama. |
| `helloGemini*` / `helloGeminiStream*`       | slider-ai | Дымовые тесты моделей Gemini. |
| `helloMistral*` / `helloMistralStream*`     | slider-ai | Дымовые тесты моделей Mistral. |
| `helloHuggingFace*` / `helloHuggingFaceStream*` | slider-ai | Дымовые тесты моделей HuggingFace. |

> Идентификаторы плагинов, указанные в KDoc проекта (`reindexRag`,
> `proposeDeckContext`), фактически зарегистрированы как **`collectRagIndex`** и
> **`generateDeckContext`** соответственно. Используйте зарегистрированные имена в
> командной строке.

## DSL расширения

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

Reveal.js зафиксирован на версии **5.2.0** (gem
`asciidoctor-revealjs:5.2.0@gem`) относительно тега `hakimel/reveal.js` `5.2.1` и
настроен с темой `talaria.css`. DSL предоставляет единственное свойство `configPath`;
расширенные опции колоды (тема, переходы, заметки) находятся в каждом YAML
deck-context, а не в DSL.

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

## Типичные рабочие процессы

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

> Всегда запускайте задачи RAG с `--no-daemon`: демон Gradle повторно использует JVM
> и препятствует перезагрузке нативной библиотеки ONNX (`libtokenizers.so`), вызывая
> `UnsatisfiedLinkError` при второй сборке.

## Выбор провайдера

| `-Pai.provider` | Default model | API key location |
|-----------------|---------------|------------------|
| `ollama` _(default)_ | `smollm:135m` | none — local inference |
| `gemini` | `gemini-2.5-flash` | `slides-context.yml` → `ai.gemini[0]` |
| `mistral` | `mistral-small-latest` | `slides-context.yml` → `ai.mistral[0]` |
| `huggingface` | `Llama-3.1-8B-Instruct:sambanova` | `slides-context.yml` → `ai.huggingface[0]` |

Неизвестные или отсутствующие значения возвращаются к `ollama` с записанным
предупреждением.

## Соглашение об именовании файлов

| File | Pattern | Example |
|------|---------|---------|
| Generation context | `<slug>-deck-context.yml` | `kotlin-inline-functions-and-reification-deck-context.yml` |
| Generated AsciiDoc deck | `<slug>_<lang>-deck.adoc` | `kotlin-inline-functions-and-reification_en-deck.adoc` |
| Compiled HTML deck | `<slug>_<lang>-deck.html` | `kotlin-inline-functions-and-reification_en-deck.html` |

`<slug>` выводится из `subject` в kebab-case (акценты нормализованы); `<lang>` —
код ISO 639-1, передаваемый через `-Planguage` (по умолчанию `fr`).

## Предварительные требования

- **Java** 24+ (плагин требует Java 23+ во время выполнения; toolchain Kotlin 2.3.20)
- **Gradle** 9.5.1+
- **Node.js / npx** для задач `serveSlides` и `visualTest`
- **Docker** для контейнера pgvector, используемого конвейером RAG
- **Internet connection** для загрузки gem-зависимостей Reveal.js

## Сборка и тестирование

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

## Устранение неполадок

| Symptom | Fix |
|---------|-----|
| `UnsatisfiedLinkError: libtokenizers.so` on second RAG run | run RAG tasks with `--no-daemon` |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| pgvector container stuck | `docker rm -f $(docker ps -q -f name=pgvector)` then retry |
| `asciidoctorRevealJs` Configuration Cache failure | expected — Configuration Cache is disabled (`configurationCache = false` on the portal) |
| `npx: command not found` | install Node.js and ensure `npx` is on `PATH` |

Полный архитектурный справочник см. в [README.adoc](../README.adoc).

## License

Apache License 2.0 — см. [LICENCE](../LICENCE).

---

_Часть экосистемы CCCP Education — `groupId: education.cccp`._