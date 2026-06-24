<!-- translated from README.md rev 0.0.1 -->
# slider-gradle — Internes du Plugin

> Guide développeur et contributeur pour le plugin Gradle `slider-plugin`.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-non%20configur%C3%A9-lightgrey?label=CI)](#)
[![Coverage](https://img.shields.io/static/v1?label=couverture&message=n%2Fa&color=lightgrey)]()
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=Licence)](../LICENCE)

- **Version** : `0.0.1` · **Groupe** : `education.cccp` · **ID plugin** : `education.cccp.slider`
- **Toolchain** : Java 24 · Kotlin 2.3.20 · Gradle 9.5.1
- **Build** : `./gradlew build -x test` · **Tests** : `./gradlew test` + `functionalTest` + `cucumberTest` (intégrés à `check`)
- **Gate de couverture** : aucune (pas de Kover)

🌐 Langues : [English](README.md) | **Français**

---

## Organisation des modules

```
slider-plugin/
├── settings.gradle.kts          # paramètres nmcp (centralPortal), includeBuild("slider")
└── slider/
    ├── build.gradle.kts          # définition du plugin, publication, signature, setup tests
    └── src/
        ├── main/kotlin/
        │   ├── slider/
        │   │   ├── SliderPlugin.kt        # Point d'entrée du plugin — orchestrateur mince
        │   │   ├── SliderManager.kt       # Prerequisites, Repositories, Plugins, Dependencies,
        │   │   │                            Extensions, Tasks, Git, FileOps (DSL par objets)
        │   │   ├── Slides.kt                # Constantes RevealJsSlides (noms de tâches, layout)
        │   │   └── models.kt                # SlidesConfiguration, DeckContext, AuthorContext…
        │   ├── slider/ai/
        │   │   ├── AssistantManager.kt     # Résolution fournisseurs LLM, catalogues de modèles, tâches chat
        │   │   ├── PgVectorService.kt       # BuildService — cycle de vie pgvector via docker-java
        │   │   ├── RagManager.kt             # Récupération / reindex RAG vers pgvector
        │   │   ├── RagTask.kt                # Classe de base des tâches RAG (injection service)
        │   │   └── RagTasks.kt               # collectRagIndex, generateDeckContext, generateDeck
        │   └── slider/translate/
        │       ├── TranslatorManager.kt      # Orchestration traduction
        │       └── TranslatorPlugin.kt       # Plugin de traduction
        ├── test/                              # Tests unitaires JUnit5 + features/steps Cucumber
        │   ├── features/                      # 4 fichiers .feature (BDD)
        │   └── scenarios/                     # Définitions des steps Cucumber
        └── functionalTest/                    # Tests fonctionnels GradleTestKit
```

## Plugin consommé (dépendance N2 → N2)

- `education.cccp.codebase` version `0.0.1` (alias catalogue
  `libs.plugins.codebase` dans `slider-plugin/gradle/libs.versions.toml`) — appliqué
  comme plugin sibling dans `slider/build.gradle.kts`. Il fournit les primitives
  d'ingestion EAGER/RAG réutilisées par `RagManager`.

## Dépendances clés

`slider/build.gradle.kts` résout les versions depuis `slider-plugin/gradle/libs.versions.toml` :

- **asciidoctor-gradle** `5.0.0-alpha.1` — `asciidoctor-gradle-jvm-slides`,
  `asciidoctor-gradle-jvm-gems`, `asciidoctor-gradle-jvm` (appliqués comme IDs
  de plugins `org.asciidoctor.jvm.revealjs.classic` et `org.asciidoctor.jvm.gems.classic`).
- **gem asciidoctor-revealjs** `5.2.0` épinglée sur le template Reveal.js
  `hakimel/reveal.js` tag `5.2.1`.
- **asciidoctorj-diagram** `3.2.0` + **asciidoctorj-diagram-plantuml** `1.2025.3`.
- **node-gradle** `7.1.0` — `NpxTask` pour `serveSlides`, `installPlaywright`, `visualTest`.
- **langchain4j** `1.14.1` + **langchain4j-beta** `1.14.1-beta24` — `langchain4j`,
  `langchain4j-ollama`, `langchain4j-open-ai`, `langchain4j-google-ai-gemini`,
  `langchain4j-mistral-ai`, `langchain4j-pgvector`, `langchain4j-embeddings-all-minilm-l6-v2`.
- **JGit** `7.5.0.202512021534-r` — `org.eclipse.jgit`, `jgit.ssh.jsch`,
  `jgit.archive` (bundle).
- **docker-java** `3.7.0` + **testcontainers-postgresql** `1.21.4` — cycle de vie pgvector.
- **Jackson** `2.21.1` — `jackson-module-kotlin`, `jackson-dataformat-yaml`.
- **Arrow** `2.2.2` + `arrow-integrations-jackson-module` `0.15.1` — helpers FP typés.
- **kotlinx-coroutines** `1.10.2` — fixtures de tests asynchrones (`core`, `jdk8`, `test`).
- **Playwright** `1.52.0` — tests E2E visuels (`testImplementation`).
- **commons-io** `2.13.0`, **slf4j** `2.0.17`, **logback** `1.5.32`.

Stack de tests : **Kotless** JUnit5 (`kotlin-test-junit5`), **AssertJ** `3.27.7`,
**Mockito Kotlin** `6.2.3`, **Mockito JUnit Jupiter** `5.23.0`, **Cucumber**
`7.34.3` (`cucumber-java`, `cucumber-junit-platform-engine`, `cucumber-picocontainer`,
`junit-platform-suite`).

## Instances Ollama (contrainte globale)

Les ports `11434–11436` sont interdits. Rotation sur `11437–11465` (29 ports).
Modèles autorisés : `gpt-oss:120b-cloud`, `gemma4:31b-cloud`.
Modèles locaux catalogués dans `AssistantManager.localModels` :
`smollm:135m`, `llama3.2:3b-instruct-q8_0`, `smollm:135m-instruct-v0.2-q8_0`,
`gemma3:1b-it-fp16`.

## Matrice de tests

| Tâche | Portée | Notes |
|------|--------|-------|
| `test` | Tests unitaires JUnit5 | exclut `com.cheroliv.slider.scenarios.**` (Cucumber) et `SliderPluginFunctionalTests` |
| `functionalTest` | Tests fonctionnels GradleTestKit | source set dédié, dépend de `test` impl via astuce `extendsFrom` |
| `cucumberTest` | Feature files Cucumber BDD | moteur suite JUnit Platform, exclut `junit-jupiter`, `dependsOn functionalTest.classesTaskName` |
| `check` | agrège | `test + functionalTest + cucumberTest` |

`tasks.withType<Test>` active `useJUnitPlatform()`, le logging complet des
exceptions et `-XX:+EnableDynamicAgentLoading` pour taire les avertissements
d'agents dynamiques. Le classpath de test exclut `logback-classic` pour éviter
les conflits de binding.

Il n'y a **pas de gate Kover** de couverture et **pas de tâche d'audit CVE dédiée**.

## Réglage JVM

- Le plugin impose `jvmToolchain(JavaVersion.VERSION_24.ordinal)` dans
  `slider/build.gradle.kts` et vérifie Java 23+ à l'exécution via
  `SliderManager.Prerequisites.checkJavaVersion()`.
- `gradle.properties` désactive le daemon et le Configuration Cache :
  `org.gradle.daemon=false`, `org.gradle.configuration-cache=false`.
- Un `buildscript.resolutionStrategy` force
  `org.jetbrains:annotations:26.0.2-1` pour contourner un conflit transitif
  koog-utils qui outrepasse le pin strict de `annotations:13.0` par Gradle.
- Les tâches RAG doivent tourner avec `--no-daemon` pour recharger la bibliothèque native ONNX.

## Commandes de build

```bash
./gradlew build                       # build complet (compile + tests)
./gradlew build -x test                # compile seulement (saute test/functionalTest/cucumberTest)
./gradlew test                         # tests unitaires JUnit5
./gradlew functionalTest               # tests fonctionnels GradleTestKit
./gradlew cucumberTest                 # scénarios Cucumber BDD
./gradlew check                         # test + functionalTest + cucumberTest
./gradlew asciidoctorRevealJs           # build des slides depuis les sources embarquées
./gradlew publishToMavenLocal          # publication locale
./gradlew publishAggregationToCentralPortal --no-daemon   # Maven Central
```

## Pipeline CI

Le dépôt contient `.github/workflows/` mais le workflow de tests n'est **pas
configuré** — seuls des helpers de documentation (`readme_plantuml.yml`,
`readme_truth.yml`) et des templates commentés (`generate_deck.yml`,
`website.yml`). Il n'y a actuellement ni job de tests PR, ni badge de couverture,
ni job de publication.

## Publication (NMCP)

Configurée via `com.gradleup.nmcp.settings` (1.5.0) dans
`slider-plugin/settings.gradle.kts`. Les identifiants sont lus depuis
`~/.gradle/gradle.properties` (`ossrhUsername`, `ossrhPassword`), avec
`publishingType = "AUTOMATIC"`.

Le bloc `gradlePlugin` de `slider/build.gradle.kts` câble :
- `gradlePlugin { create("slider") { id = "education.cccp.slider";
  implementationClass = "slider.SliderPlugin" } }` avec les tags `revealjs`,
  `slide-generator`, `slide`, `jgit`, `asciidoc`, `langchain4j`, `ollama`,
  `mistal-ai`, `huggingface`, `gemini`, `kotlin-DSL`.
- `vcsUrl = "https://github.com/cheroliv/slider-gradle.git"`,
  `website = "https://cheroliv.com"`.
- `compatibility { features { configurationCache = false } }` (la tâche
  `asciidoctorRevealJs` tourne `OUT_OF_PROCESS` via JRuby et est incompatible
  avec le Configuration Cache).
- Le POM déclare Apache 2.0, développeur `cccp-education`, SCM pointant vers
  `github.com/cheroliv/slider-gradle`.
- La signature utilise `useGpgCmd()` et signe les publications hors CI, hors SNAPSHOT.

## Statut des EPICs

D'après `slider-plugin/.agents/INDEX.adoc` (dernière session 010) :

| EPIC | Description | Statut |
|------|-------------|--------|
| SLD-0 | Bootstrap gouvernance agent | ✅ TERMINÉ |
| SLD-1 | Upgrade Reveal.js 3.9.1 → 5.2.1 + Auto-Animate + transitions | ✅ TERMINÉ (5/5 US) |
| SLD-2 | Playwright E2E + thème pro + feed capsule | 🔴 EN COURS — 4/5 US faites (US-2.5 en attente) |
| SLD-3 | i18n 10 langues via `i18n-contracts:0.0.1` | ☐ NOUVEAU — cadré session 010 |
| Publication Maven Central 0.0.1 | NMCP, version hardcodée | ✅ 2026-06-11 |

## Contribuer

1. Le build compile : `./gradlew build -x test`
2. Tests verts : `./gradlew check`
3. Respecter le pattern de **responsabilité unique** dans `SliderManager` — chaque
   objet imbriqué possède un seul souci (Prerequisites, Repositories, Plugins,
   Dependencies, Extensions, Tasks, Git, FileOps).
4. Utiliser des imports explicites (pas de wildcards), indentation 4 espaces,
   accolade ouvrante sur la même ligne ; constantes en `SCREAMING_SNAKE_CASE`.
5. Ne pas activer le Configuration Cache — `asciidoctorRevealJs` est déclarée
   explicitement incompatible.

## Docs d'architecture

- [README.adoc](../README.adoc) — référence AsciiDoc complète (diagrammes PlantUML,
  pipeline RAG en deux étapes, vue hexagonale, pipeline de déploiement).
- [.agents/INDEX.adoc](../slider-plugin/.agents/INDEX.adoc) — EPICs & gouvernance.
- [AGENT.adoc](../slider-plugin/AGENT.adoc) — règles absolues.
- [.agents/SESSION_CHECKLIST.adoc](../slider-plugin/.agents/SESSION_CHECKLIST.adoc) — checklist de session.
- [doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc](../slider-plugin/doc/SLD-1_UPGRADE_REVEALJS_AUTO_ANIMATE.adoc) — spec technique SLD-1.

## Licence

Apache License 2.0 — voir [LICENCE](../LICENCE).

---

_Partie de l'écosystème CCCP Education — `groupId: education.cccp`._