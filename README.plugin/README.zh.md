<!-- translated from README.md rev 0.0.1 -->
# slider-gradle — 插件内部机制

> `slider-plugin` Gradle 插件的开发者与贡献者指南。

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-not%20configured-lightgrey?label=CI)](#)
[![Coverage](https://img.shields.io/static/v1?label=coverage&message=n%2Fa&color=lightgrey)]()
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.slider`
- **Toolchain**: Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew build -x test` · **Tests**: `./gradlew test` + `functionalTest` + `cucumberTest` (wired into `check`)
- **Coverage gate**: none (no Kover)

🌐 Languages: [English](README.md) | **中文** | [हिन्दी](README.hi.md) | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## 模块布局

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

## 被消费的插件（N2 → N2 依赖）

- `education.cccp.codebase` version `0.0.1`（catalog alias
  `libs.plugins.codebase` 在 `slider-plugin/gradle/libs.versions.toml` 中）——
  在 `slider/build.gradle.kts` 中作为兄弟插件应用。它提供被 `RagManager` 复用的
  EAGER/RAG 摄取原语。

## 关键依赖

`slider/build.gradle.kts` 从 `slider-plugin/gradle/libs.versions.toml` 解析版本：

- **asciidoctor-gradle** `5.0.0-alpha.1` — `asciidoctor-gradle-jvm-slides`,
  `asciidoctor-gradle-jvm-gems`, `asciidoctor-gradle-jvm`（以 plugin ID
  `org.asciidoctor.jvm.revealjs.classic` 和 `org.asciidoctor.jvm.gems.classic` 应用）。
- **asciidoctor-revealjs gem** `5.2.0` 固定对应 Reveal.js 模板
  `hakimel/reveal.js` 标签 `5.2.1`。
- **asciidoctorj-diagram** `3.2.0` + **asciidoctorj-diagram-plantuml** `1.2025.3`。
- **node-gradle** `7.1.0` — 用于 `serveSlides`、`installPlaywright`、`visualTest` 的 `NpxTask`。
- **langchain4j** `1.14.1` + **langchain4j-beta** `1.14.1-beta24` — `langchain4j`,
  `langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
  `langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`。
- **JGit** `7.5.0.202512021534-r` — `org.eclipse.jgit`, `jgit.ssh.jsch`,
  `jgit.archive` (bundle)。
- **docker-java** `3.7.0` + **testcontainers-postgresql** `1.21.4` — pgvector 生命周期。
- **Jackson** `2.21.1` — `jackson-module-kotlin`, `jackson-dataformat-yaml`。
- **Arrow** `2.2.2` + `arrow-integrations-jackson-module` `0.15.1` — 类型化 FP 辅助工具。
- **kotlinx-coroutines** `1.10.2` — 异步测试夹具（`core`、`jdk8`、`test`）。
- **Playwright** `1.52.0` — 视觉 E2E 测试（`testImplementation`）。
- **commons-io** `2.13.0`, **slf4j** `2.0.17`, **logback** `1.5.32`。

测试栈：**Kotless** JUnit5 (`kotlin-test-junit5`)、**AssertJ** `3.27.7`、
**Mockito Kotlin** `6.2.3`、**Mockito JUnit Jupiter** `5.23.0`、**Cucumber**
`7.34.3`（`cucumber-java`、`cucumber-junit-platform-engine`、`cucumber-picocontainer`、
`junit-platform-suite`）。

## Ollama 实例（全局约束）

端口 `11434–11436` 被禁止。在 `11437–11465`（29 个端口）上轮换。
授权模型：`gpt-oss:120b-cloud`、`gemma4:31b-cloud`。
本地可用模型记录于 `AssistantManager.localModels`：
`smollm:135m`、`llama3.2:3b-instruct-q8_0`、`smollm:135m-instruct-v0.2-q8_0`、
`gemma3:1b-it-fp16`。

## 测试矩阵

| Task | Scope | Notes |
|------|-------|-------|
| `test` | JUnit5 unit tests | excludes `com.cheroliv.slider.scenarios.**` (Cucumber) and `SliderPluginFunctionalTests` |
| `functionalTest` | GradleTestKit functional tests | own source set, depends on `test` impl via `extendsFrom` trick |
| `cucumberTest` | Cucumber BDD feature files | uses JUnit Platform suite engine, excludes `junit-jupiter`, `dependsOn functionalTest.classesTaskName` |
| `check` | aggregates | `test + functionalTest + cucumberTest` |

`tasks.withType<Test>` 启用 `useJUnitPlatform()`、完整异常日志和
`-XX:+EnableDynamicAgentLoading` 以静默动态代理警告。测试
classpath 排除 `logback-classic` 以避免绑定冲突。

没有 **Kover** 覆盖率门槛，也**没有专门的 CVE 审计任务**。

## JVM 调优

- 插件在
  `slider/build.gradle.kts` 中强制 `jvmToolchain(JavaVersion.VERSION_24.ordinal)`，并通过
  `SliderManager.Prerequisites.checkJavaVersion()` 在运行时断言 Java 23+。
- `gradle.properties` 禁用 daemon 和 Configuration Cache：
  `org.gradle.daemon=false`、`org.gradle.configuration-cache=false`。
- `buildscript.resolutionStrategy` 强制
  `org.jetbrains:annotations:26.0.2-1` 以绕过 koog-utils 传递
  冲突，该冲突绕过 Gradle 对 `annotations:13.0` 的严格锁定。
- RAG 任务必须以 `--no-daemon` 运行以重新加载原生 ONNX 库。

## 构建命令

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

## CI 流水线

仓库附带 `.github/workflows/`，但测试工作流**未
配置**——只有文档助手（`readme_plantuml.yml`、
`readme_truth.yml`）和被注释掉的模板（`generate_deck.yml`、
`website.yml`）。目前没有 PR 测试任务、没有覆盖率徽章，也没有
发布任务。

## 发布（NMCP）

通过 `slider-plugin/settings.gradle.kts` 中的
`com.gradleup.nmcp.settings` (1.5.0) 配置。凭据从
`~/.gradle/gradle.properties`（`ossrhUsername`、`ossrhPassword`）读取，并设置
`publishingType = "AUTOMATIC"`。

`slider/build.gradle.kts` 插件块配置：
- `gradlePlugin { create("slider") { id = "education.cccp.slider";
  implementationClass = "slider.SliderPlugin" } }`，标签为 `revealjs`、
  `slide-generator`、`slide`、`jgit`、`asciidoc`、`langchain4j`、`ollama`、
  `mistal-ai`、`huggingface`、`gemini`、`kotlin-DSL`。
- `vcsUrl = "https://github.com/cheroliv/slider-gradle.git"`、
  `website = "https://cheroliv.com"`。
- `compatibility { features { configurationCache = false } }`（
  `asciidoctorRevealJs` 任务通过 JRuby 以 `OUT_OF_PROCESS` 运行，与
  Configuration Cache 不兼容）。
- POM 声明 Apache 2.0、开发者 `cccp-education`、指向
  `github.com/cheroliv/slider-gradle` 的 SCM。
- 签名使用 `useGpgCmd()`，并在非 CI、非 SNAPSHOT 构建上签署发布。

## EPIC 状态

来自 `slider-plugin/.agents/INDEX.adoc`（最后一次会话 010）：

| EPIC | Description | Status |
|------|-------------|--------|
| SLD-0 | Bootstrap governance agent | ✅ DONE |
| SLD-1 | Upgrade Reveal.js 3.9.1 → 5.2.1 + Auto-Animate + transitions | ✅ DONE (5/5 US) |
| SLD-2 | Playwright E2E + Pro theme + Capsule feed | 🔴 IN PROGRESS — 4/5 US done (US-2.5 pending) |
| SLD-3 | i18n 10 languages via `i18n-contracts:0.0.1` | ☐ NEW — framed session 010 |
| Publication Maven Central 0.0.1 | NMCP, hardcoded version | ✅ 2026-06-11 |

## 贡献

1. 构建可编译：`./gradlew build -x test`
2. 测试通过：`./gradlew check`
3. 遵守 `SliderManager` 中的**单一职责**模式——每个嵌套
   object 负责一个关注点（Prerequisites、Repositories、Plugins、Dependencies、
   Extensions、Tasks、Git、FileOps）。
4. 使用显式导入（无通配符）、4 空格缩进、左大括号在
   同一行；常量使用 `SCREAMING_SNAKE_CASE`。
5. 不要启用 Configuration Cache——`asciidoctorRevealJs` 已明确
   声明为不兼容。

## 架构文档

- [README.adoc](../README.adoc) — 完整的 AsciiDoc 参考（PlantUML 图、
  两步 RAG 流水线、六边形视图、部署流水线）。
- [.agents/INDEX.adoc](../slider-plugin/.agents/INDEX.adoc) — EPICs 与治理。
- [AGENT.adoc](../slider-plugin/AGENT.adoc) — 绝对规则。
- [.agents/SESSION_CHECKLIST.adoc](../slider-plugin/.agents/SESSION_CHECKLIST.adoc) — 会话清单。
- [doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc](../slider-plugin/doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc) — SLD-1 技术规范。

## License

Apache License 2.0 — 见 [LICENCE](../LICENCE)。

---

_CCCP Education 生态系统的一部分 — `groupId: education.cccp`。_