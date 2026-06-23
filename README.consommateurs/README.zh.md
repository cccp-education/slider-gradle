<!-- translated from README.md rev 0.0.1 -->
# slider-gradle — 用户指南

> 基于 RAG 增强的 Gradle 插件，从 AsciiDoc 源文件生成 Reveal.js 演示文稿。

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-not%20configured-lightgrey?label=CI)](#)
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.slider`
- **Toolchain**: Java 24+ · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew asciidoctorRevealJs` · **Tests**: `./gradlew test` (JUnit5) + `cucumberTest` (Cucumber) + `functionalTest` (Gradle Test Kit)
- **Coverage**: not measured (no Kover)

🌐 Languages: [English](README.md) | **中文** | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## 功能介绍

`slider-gradle` 将 AsciiDoc 源文件编译为交互式 **Reveal.js** HTML
演示文稿，并可通过基于 pgvector 存储的两步 **RAG + LLM** 流水线，从自然语言主题生成完整的演示文稿。LLM 首先生成供人工审阅的
`*-deck-context.yml`，随后生成经项目示例增强的最终 AsciiDoc 演示文稿。

它是 CCCP Education 多插件生态系统的一部分（MIAMI borough，N2），并使用
`codebase-gradle`（plugin id `education.cccp.codebase` version `0.0.1`）。

```
subject → proposeDeckContext (RAG+LLM) → *-deck-context.yml → review → generateDeck (RAG+LLM) → <slug>_<lang>-deck.adoc → asciidoctorRevealJs → HTML deck
```

## 快速开始

### 1. 应用插件

```gradle
plugins {
    id("education.cccp.slider") version "0.0.1"
}
```

### 2. 配置 DSL

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

首次执行时，插件会从内置的
`slides.zip` 创建 `slides/` 文件夹、默认的 `slides-context.yml` 以及可直接使用的
`slides/misc/example-deck-context.yml`。现有内容永远不会被覆盖。

### 3. 编译演示文稿

```bash
./gradlew asciidoctorRevealJs
```

输出写入 `build/docs/asciidocRevealJs/`（每个演示文稿对应一个 HTML 文件，外加一个
`index.html` 仪表盘和一个 `slides.json` 清单）。

## 可用任务

| Task | Group | Description |
|------|-------|-------------|
| `asciidoctorRevealJs`     | generate | 将 AsciiDoc 源文件编译为 Reveal.js HTML 演示文稿（依赖于 `cleanBuild`）。 |
| `asciidoctor`             | generate | 标准 Asciidoctor HTML 转换（依赖于 `asciidoctorRevealJs`）。 |
| `cleanBuild`              | build    | 删除生成的演示文稿产物（`slides.json`、`images/`、`.html`）。 |
| `generateDashboard`       | generate | 生成列出每个演示文稿的 `index.html` 和 `slides.json`（完成 `asciidoctorRevealJs`）。 |
| `serveSlides`             | info     | 通过 `npx serve` 本地提供生成的演示文稿（依赖于 `asciidoctorRevealJs`）。 |
| `deploySlides`            | deploy   | 将生成的演示文稿部署到 `slides-context.yml` 中配置的远程 Git 仓库（依赖于 `asciidoctor`）。 |
| `generateCapsule`         | generate | 从 AsciiDoc 演示文稿中提取演讲者备注，生成视频脚本到 `build/capsule/`。 |
| `installPlaywright`       | setup    | 安装 Playwright Chromium 浏览器用于视觉测试。 |
| `visualTest`              | slider   | 对生成的演示文稿运行 Playwright 视觉快照测试。 |
| `reportTests`             | verify   | 运行 `check` 并在 Firefox 中打开单元测试报告。 |
| `reportFunctionalTests`   | verify   | 运行 `check` 并在 Firefox 中打开功能测试报告。 |
| `collectRagIndex`         | collect  | 强制完整重建 RAG 嵌入索引。 |
| `generateDeckContext`     | generate | 使用 RAG + LLM 为某个主题生成 `*-deck-context.yml`（步骤 1/2）。 |
| `generateDeck`            | generate | 从 `*-deck-context.yml` 生成完整的 AsciiDoc/Reveal.js 演示文稿（步骤 2/2）。 |
| `helloOllama*` / `helloOllamaStream*`       | slider-ai | Ollama 模型冒烟测试。 |
| `helloGemini*` / `helloGeminiStream*`       | slider-ai | Gemini 模型冒烟测试。 |
| `helloMistral*` / `helloMistralStream*`     | slider-ai | Mistral 模型冒烟测试。 |
| `helloHuggingFace*` / `helloHuggingFaceStream*` | slider-ai | HuggingFace 模型冒烟测试。 |

> 项目 KDoc 中引用的插件 ID（`reindexRag`、`proposeDeckContext`）实际注册为
> **`collectRagIndex`** 和 **`generateDeckContext`**。请在命令行中使用注册的名称。

## 扩展 DSL

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

Reveal.js 固定为版本 **5.2.0**（gem `asciidoctor-revealjs:5.2.0@gem`），
对应 `hakimel/reveal.js` 标签 `5.2.1`，并配置 `talaria.css` 主题。DSL 仅暴露一个 `configPath` 属性；高级演示文稿选项
（主题、过渡、备注）位于每个 deck-context YAML 中，而非 DSL 中。

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

## 典型工作流

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

> 始终使用 `--no-daemon` 运行 RAG 任务：Gradle 守护进程会复用 JVM 并
> 阻止重新加载原生 ONNX 库（`libtokenizers.so`），导致第二次构建时出现
> `UnsatisfiedLinkError`。

## Provider 选择

| `-Pai.provider` | Default model | API key location |
|-----------------|---------------|------------------|
| `ollama` _(default)_ | `smollm:135m` | none — local inference |
| `gemini` | `gemini-2.5-flash` | `slides-context.yml` → `ai.gemini[0]` |
| `mistral` | `mistral-small-latest` | `slides-context.yml` → `ai.mistral[0]` |
| `huggingface` | `Llama-3.1-8B-Instruct:sambanova` | `slides-context.yml` → `ai.huggingface[0]` |

未知或缺失的值会回退到 `ollama` 并记录警告日志。

## 文件命名约定

| File | Pattern | Example |
|------|---------|---------|
| Generation context | `<slug>-deck-context.yml` | `kotlin-inline-functions-and-reification-deck-context.yml` |
| Generated AsciiDoc deck | `<slug>_<lang>-deck.adoc` | `kotlin-inline-functions-and-reification_en-deck.adoc` |
| Compiled HTML deck | `<slug>_<lang>-deck.html` | `kotlin-inline-functions-and-reification_en-deck.html` |

`<slug>` 由 `subject` 经 kebab-case 转换得到（重音符号已规范化）；`<lang>` 是
通过 `-Planguage` 传入的 ISO 639-1 代码（默认为 `fr`）。

## 前置条件

- **Java** 24+（插件在运行时要求 Java 23+；Kotlin 2.3.20 工具链）
- **Gradle** 9.5.1+
- **Node.js / npx**，用于 `serveSlides` 和 `visualTest` 任务
- **Docker**，用于 RAG 流水线所需的 pgvector 容器
- **Internet connection**，以下载 Reveal.js gem 依赖

## 构建与测试

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

## 故障排除

| Symptom | Fix |
|---------|-----|
| `UnsatisfiedLinkError: libtokenizers.so` on second RAG run | run RAG tasks with `--no-daemon` |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| pgvector container stuck | `docker rm -f $(docker ps -q -f name=pgvector)` then retry |
| `asciidoctorRevealJs` Configuration Cache failure | expected — Configuration Cache is disabled (`configurationCache = false` on the portal) |
| `npx: command not found` | install Node.js and ensure `npx` is on `PATH` |

完整的架构参考请见 [README.adoc](../README.adoc)。

## License

Apache License 2.0 — 见 [LICENCE](../LICENCE)。

---

_CCCP Education 生态系统的一部分 — `groupId: education.cccp`。_