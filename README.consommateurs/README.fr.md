<!-- translated from README.md rev 0.0.1 -->
# slider-gradle — Guide Consommateur

> Plugin Gradle augmenté par RAG qui génère des présentations Reveal.js depuis des sources AsciiDoc.

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-non%20configur%C3%A9-lightgrey?label=CI)](#)
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=Licence)](../LICENCE)

- **Version** : `0.0.1` · **Groupe** : `education.cccp` · **ID plugin** : `education.cccp.slider`
- **Toolchain** : Java 24+ · Kotlin 2.3.20 · Gradle 9.5.1
- **Build** : `./gradlew asciidoctorRevealJs` · **Tests** : `./gradlew test` (JUnit5) + `cucumberTest` (Cucumber) + `functionalTest` (Gradle Test Kit)
- **Couverture** : non mesurée (pas de Kover)

🌐 Langues : [English](README.md) | **Français**

---

## Ce que ça fait

`slider-gradle` compile des sources AsciiDoc en présentations **Reveal.js** HTML
interactives et génère optionnellement des decks complets depuis des sujets en
langage naturel grâce à un pipeline **RAG + LLM** en deux étapes soutenu par un
magasin pgvector. Un LLM propose un `*-deck-context.yml` pour revue humaine,
puis génère le deck AsciiDoc final enrichi par les exemples du projet.

Il fait partie de l'écosystème multi-plugins CCCP Education (borough MIAMI, N2)
et consomme `codebase-gradle` (id plugin `education.cccp.codebase` version `0.0.1`).

```
sujet → generateDeckContext (RAG+LLM) → *-deck-context.yml → revue → generateDeck (RAG+LLM) → <slug>_<lang>-deck.adoc → asciidoctorRevealJs → deck HTML
```

## Démarrage rapide

### 1. Appliquer le plugin

```gradle
plugins {
    id("education.cccp.slider") version "0.0.1"
}
```

### 2. Configurer le DSL

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

Lors de la première exécution, le plugin échafaude le dossier `slides/` (depuis un
`slides.zip` embarqué), un `slides-context.yml` par défaut et un
`slides/misc/example-deck-context.yml` prêt à l'emploi. Le contenu existant n'est
jamais écrasé.

### 3. Compiler les slides

```bash
./gradlew asciidoctorRevealJs
```

La sortie est écrite dans `build/docs/asciidocRevealJs/` (un fichier HTML par deck
plus un tableau de bord `index.html` et un manifeste `slides.json`).

## Tâches disponibles

| Tâche | Groupe | Description |
|------|--------|-------------|
| `asciidoctorRevealJs`     | generate | Compile les sources AsciiDoc en une présentation HTML Reveal.js (dépend de `cleanBuild`). |
| `asciidoctor`             | generate | Conversion HTML Asciidoctor standard (dépend de `asciidoctorRevealJs`). |
| `cleanBuild`              | build    | Supprime les artefacts générés (`slides.json`, `images/`, `.html`). |
| `generateDashboard`       | generate | Génère `index.html` et `slides.json` listant tous les decks (finalise `asciidoctorRevealJs`). |
| `serveSlides`             | info     | Sert le deck généré localement via `npx serve` (dépend de `asciidoctorRevealJs`). |
| `deploySlides`            | deploy   | Déploie les slides générés vers le dépôt Git distant configuré dans `slides-context.yml` (dépend de `asciidoctor`). |
| `generateCapsule`         | generate | Extrait les notes du présentateur des decks AsciiDoc vers un script vidéo dans `build/capsule/`. |
| `installPlaywright`       | setup    | Installe le navigateur Chromium de Playwright pour les tests visuels. |
| `visualTest`              | slider   | Lance les tests de snapshot visuels Playwright sur les slides générés. |
| `reportTests`             | verify   | Lance `check` et ouvre le rapport de tests unitaires dans Firefox. |
| `reportFunctionalTests`   | verify   | Lance `check` et ouvre le rapport de tests fonctionnels dans Firefox. |
| `collectRagIndex`         | collect  | Force une reconstruction complète de l'index d'embeddings RAG. |
| `generateDeckContext`     | generate | Propose un `*-deck-context.yml` pour un sujet via RAG + LLM (étape 1/2). |
| `generateDeck`            | generate | Génère un deck AsciiDoc/Reveal.js complet depuis un `*-deck-context.yml` (étape 2/2). |
| `helloOllama*` / `helloOllamaStream*`       | slider-ai | Smoke tests des modèles Ollama. |
| `helloGemini*` / `helloGeminiStream*`       | slider-ai | Smoke tests des modèles Gemini. |
| `helloMistral*` / `helloMistralStream*`     | slider-ai | Smoke tests des modèles Mistral. |
| `helloHuggingFace*` / `helloHuggingFaceStream*` | slider-ai | Smoke tests des modèles HuggingFace. |

> Les noms de tâches référencés dans la KDoc du projet (`reindexRag`,
> `proposeDeckContext`) sont en réalité enregistrés comme **`collectRagIndex`** et
> **`generateDeckContext`** respectivement. Utilisez les noms enregistrés en ligne
> de commande.

## DSL d'extension

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

Reveal.js est épinglé à la version **5.2.0** (gem `asciidoctor-revealjs:5.2.0@gem`)
sur le tag `5.2.1` de `hakimel/reveal.js` et configuré avec le thème `talaria.css`.
Le DSL n'expose qu'une propriété `configPath` ; les options avancées de deck
(thème, transitions, notes) vivent dans chaque YAML de deck-context, pas dans le DSL.

## deck-context.yml

```yaml
subject: "Kotlin inline functions and reification"
audience: "intermediate Kotlin developers"
duration: 60
language: "en"                          # code ISO 639-1
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

## Workflows typiques

```bash
# Pipeline IA complet — proposer → revue → générer → compiler
./gradlew generateDeckContext \
  -Psubject="Kotlin inline functions and reification" \
  -Planguage=en \
  -Pai.provider=gemini \
  --no-daemon
#   revue de slides/misc/<slug>-deck-context.yml
./gradlew generateDeck \
  -Pdeck.context=slides/misc/<slug>-deck-context.yml \
  -Pai.provider=gemini \
  --no-daemon
./gradlew asciidoctorRevealJs serveSlides

# Aperçu local uniquement
./gradlew serveSlides

# Reconstruction propre
./gradlew cleanBuild asciidoctorRevealJs

# Publier vers le dépôt distant de slides
./gradlew asciidoctorRevealJs deploySlides
```

> Lancez toujours les tâches RAG avec `--no-daemon` : le daemon Gradle réutilise le
> JVM et empêche le rechargement de la bibliothèque native ONNX
> (`libtokenizers.so`), provoquant une `UnsatisfiedLinkError` au deuxième build.

## Sélection du fournisseur

| `-Pai.provider` | Modèle par défaut | Emplacement de la clé API |
|-----------------|------------------|---------------------------|
| `ollama` _(par défaut)_ | `smollm:135m` | aucun — inférence locale |
| `gemini` | `gemini-2.5-flash` | `slides-context.yml` → `ai.gemini[0]` |
| `mistral` | `mistral-small-latest` | `slides-context.yml` → `ai.mistral[0]` |
| `huggingface` | `Llama-3.1-8B-Instruct:sambanova` | `slides-context.yml` → `ai.huggingface[0]` |

Une valeur inconnue ou absente retombe sur `ollama` avec un avertissement journalisé.

## Convention de nommage des fichiers

| Fichier | Motif | Exemple |
|---------|-------|---------|
| Contexte de génération | `<slug>-deck-context.yml` | `kotlin-inline-functions-and-reification-deck-context.yml` |
| Deck AsciiDoc généré | `<slug>_<lang>-deck.adoc` | `kotlin-inline-functions-and-reification_en-deck.adoc` |
| Deck HTML compilé | `<slug>_<lang>-deck.html` | `kotlin-inline-functions-and-reification_en-deck.html` |

`<slug>` est dérivé de `subject` en kebab-case (accents normalisés) ; `<lang>` est
le code ISO 639-1 passé via `-Planguage` (par défaut `fr`).

## Prérequis

- **Java** 24+ (le plugin impose Java 23+ à l'exécution ; toolchain Kotlin 2.3.20)
- **Gradle** 9.5.1+
- **Node.js / npx** pour les tâches `serveSlides` et `visualTest`
- **Docker** pour le conteneur pgvector utilisé par le pipeline RAG
- **Connexion Internet** pour télécharger les dépendances gem de Reveal.js

## Build et tests

```bash
./gradlew asciidoctorRevealJs          # compiler les slides
./gradlew serveSlides                  # aperçu local
./gradlew cleanBuild                   # nettoyer la sortie du build
./gradlew test                         # tests unitaires JUnit5 (exclut Cucumber & fonctionnels)
./gradlew functionalTest               # tests fonctionnels GradleTestKit
./gradlew cucumberTest                 # feature files Cucumber BDD
./gradlew check                         # test + functionalTest + cucumberTest
./gradlew publishToMavenLocal          # publication locale
```

## Dépannage

| Symptôme | Solution |
|----------|----------|
| `UnsatisfiedLinkError: libtokenizers.so` au deuxième run RAG | lancez les tâches RAG avec `--no-daemon` |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| Conteneur pgvector bloqué | `docker rm -f $(docker ps -q -f name=pgvector)` puis réessayer |
| `asciidoctorRevealJs` Configuration Cache en échec | attendu — le Configuration Cache est désactivé (`configurationCache = false` sur le portail) |
| `npx: command not found` | installez Node.js et assurez-vous que `npx` est dans le `PATH` |

Voir [README.adoc](../README.adoc) pour la référence architecturale complète.

## Licence

Apache License 2.0 — voir [LICENCE](../LICENCE).

---

_Partie de l'écosystème CCCP Education — `groupId: education.cccp`._