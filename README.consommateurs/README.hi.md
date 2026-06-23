<!-- translated from README.md rev 0.0.1 -->
# slider-gradle — उपभोक्ता गाइड

> RAG-संवर्धित Gradle प्लगइन जो AsciiDoc स्रोतों से Reveal.js प्रस्तुतियाँ बनाता है।

[![Maven Central](https://img.shields.io/maven-central/v/education.cccp/slider-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/education.cccp/slider-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/education.cccp.slider.svg?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/education.cccp.slider)
[![CI](https://img.shields.io/badge/CI-not%20configured-lightgrey?label=CI)](#)
[![License](https://img.shields.io/github/license/cheroliv/slider-gradle?label=License)](../LICENCE)

- **Version**: `0.0.1` · **Group**: `education.cccp` · **Plugin ID**: `education.cccp.slider`
- **Toolchain**: Java 24+ · Kotlin 2.3.20 · Gradle 9.5.1
- **Build**: `./gradlew asciidoctorRevealJs` · **Tests**: `./gradlew test` (JUnit5) + `cucumberTest` (Cucumber) + `functionalTest` (Gradle Test Kit)
- **Coverage**: not measured (no Kover)

🌐 Languages: [English](README.md) | [中文](README.zh.md) | **हिन्दी** | [Español](README.es.md) | [Français](README.fr.md) | [العربية](README.ar.md) | [বাংলা](README.bn.md) | [Português](README.pt.md) | [Русский](README.ru.md) | [اردو](README.ur.md)

---

## यह क्या करता है

`slider-gradle` AsciiDoc स्रोतों को इंटरैक्टिव **Reveal.js** HTML
प्रस्तुतियों में संकलित करता है, और pgvector स्टोर पर आधारित दो-चरणीय **RAG + LLM** पाइपलाइन के माध्यम से प्राकृतिक-भाषा विषयों से वैकल्पिक रूप से पूर्ण डेक बनाता है। एक LLM मानव समीक्षा हेतु
`*-deck-context.yml` प्रस्तावित करता है, फिर परियोजना उदाहरणों द्वारा संवर्धित अंतिम AsciiDoc डेक
बनाता है।

यह CCCP Education मल्टी-प्लगइन पारिस्थितिकी का हिस्सा है (MIAMI borough, N2) और
`codebase-gradle` (plugin id `education.cccp.codebase` version `0.0.1`) का उपभोग करता है।

```
subject → proposeDeckContext (RAG+LLM) → *-deck-context.yml → review → generateDeck (RAG+LLM) → <slug>_<lang>-deck.adoc → asciidoctorRevealJs → HTML deck
```

## त्वरित प्रारंभ

### 1. प्लगइन लागू करें

```gradle
plugins {
    id("education.cccp.slider") version "0.0.1"
}
```

### 2. DSL कॉन्फ़िगर करें

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

पहले निष्पादन पर प्लगइन बंडल किए गए
`slides.zip` से `slides/` फ़ोल्डर, एक डिफ़ॉल्ट `slides-context.yml` और एक उपयोग-मुहूर्त
`slides/misc/example-deck-context.yml` का निर्माण करता है। मौजूदा सामग्री कभी अधिलेखित नहीं होती।

### 3. स्लाइड्स संकलित करें

```bash
./gradlew asciidoctorRevealJs
```

आउटपुट `build/docs/asciidocRevealJs/` में लिखा जाता है (प्रत्येक डेक हेतु एक HTML फ़ाइल, साथ ही एक
`index.html` डैशबोर्ड और एक `slides.json` मेनिफ़ेस्ट)।

## उपलब्ध कार्य

| Task | Group | Description |
|------|-------|-------------|
| `asciidoctorRevealJs`     | generate | AsciiDoc स्रोतों को Reveal.js HTML प्रस्तुति में संकलित करता है (`cleanBuild` पर निर्भर)। |
| `asciidoctor`             | generate | मानक Asciidoctor HTML रूपांतरण (`asciidoctorRevealJs` पर निर्भर)। |
| `cleanBuild`              | build    | बनाए गए प्रस्तुति कलाकृतियों को हटाता है (`slides.json`, `images/`, `.html`)। |
| `generateDashboard`       | generate | हर डेक को सूचीबद्ध करते हुए `index.html` और `slides.json` बनाता है (`asciidoctorRevealJs` को अंतिम रूप देता है)। |
| `serveSlides`             | info     | `npx serve` द्वारा बनाए गए डेक को स्थानीय रूप से प्रस्तुत करता है (`asciidoctorRevealJs` पर निर्भर)। |
| `deploySlides`            | deploy   | बनाए गए स्लाइड्स को `slides-context.yml` में कॉन्फ़िगर किए गए रिमोट Git रिपॉज़िटरी में तैनात करता है (`asciidoctor` पर निर्भर)। |
| `generateCapsule`         | generate | AsciiDoc डेक से वक्ता नोट्स निकालकर `build/capsule/` में वीडियो स्क्रिप्ट बनाता है। |
| `installPlaywright`       | setup    | विज़ुअल परीक्षणों हेतु Playwright Chromium ब्राउज़र स्थापित करता है। |
| `visualTest`              | slider   | बनाए गए स्लाइड्स पर Playwright विज़ुअल स्नैपशॉट परीक्षण चलाता है। |
| `reportTests`             | verify   | `check` चलाता है और Firefox में यूनिट परीक्षण रिपोर्ट खोलता है। |
| `reportFunctionalTests`   | verify   | `check` चलाता है और Firefox में कार्यात्मक परीक्षण रिपोर्ट खोलता है। |
| `collectRagIndex`         | collect  | RAG एम्बेडिंग इंडेक्स का पूर्ण पुनर्निर्माण बाध्य करता है। |
| `generateDeckContext`     | generate | RAG + LLM का उपयोग कर किसी विषय हेतु `*-deck-context.yml` प्रस्तावित करता है (चरण 1/2)। |
| `generateDeck`            | generate | `*-deck-context.yml` से पूर्ण AsciiDoc/Reveal.js डेक बनाता है (चरण 2/2)। |
| `helloOllama*` / `helloOllamaStream*`       | slider-ai | Ollama मॉडल स्मोक परीक्षण। |
| `helloGemini*` / `helloGeminiStream*`       | slider-ai | Gemini मॉडल स्मोक परीक्षण। |
| `helloMistral*` / `helloMistralStream*`     | slider-ai | Mistral मॉडल स्मोक परीक्षण। |
| `helloHuggingFace*` / `helloHuggingFaceStream*` | slider-ai | HuggingFace मॉडल स्मोक परीक्षण। |

> परियोजना KDoc में संदर्भित प्लगइन ID (`reindexRag`, `proposeDeckContext`)
> वास्तव में **`collectRagIndex`** और **`generateDeckContext`** के रूप में पंजीकृत हैं।
> कमांड लाइन पर पंजीकृत नाम का उपयोग करें।

## एक्सटेंशन DSL

```gradle
slider {
    configPath = file("slides-context.yml").absolutePath
}
```

Reveal.js संस्करण **5.2.0** पर नियत है (gem `asciidoctor-revealjs:5.2.0@gem`)，
`hakimel/reveal.js` टैग `5.2.1` के विरुद्ध，और `talaria.css` 主题 से कॉन्फ़िगर किया गया। DSL एकमात्र `configPath` गुण प्रस्तुत करता है; उन्नत डेक विकल्प
(थीम, संक्रमण, नोट्स) प्रत्येक deck-context YAML में रहते हैं, DSL में नहीं।

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

## विशिष्ट कार्यप्रवाह

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

> RAG कार्य हमेशा `--no-daemon` से चलाएँ: Gradle डेमन JVM का पुनः उपयोग करता है और
> नेटिव ONNX लाइब्रेरी (`libtokenizers.so`) को पुनः लोड होने से रोकता है, जिससे दूसरे बिल्ड पर
> `UnsatisfiedLinkError` होता है।

## Provider चयन

| `-Pai.provider` | Default model | API key location |
|-----------------|---------------|------------------|
| `ollama` _(default)_ | `smollm:135m` | none — local inference |
| `gemini` | `gemini-2.5-flash` | `slides-context.yml` → `ai.gemini[0]` |
| `mistral` | `mistral-small-latest` | `slides-context.yml` → `ai.mistral[0]` |
| `huggingface` | `Llama-3.1-8B-Instruct:sambanova` | `slides-context.yml` → `ai.huggingface[0]` |

अज्ञात या अनुपस्थित मान `ollama` पर एक लॉग की गई चेतावनी के साथ लौटते हैं।

## फ़ाइल नामकरण सम्मेलन

| File | Pattern | Example |
|------|---------|---------|
| Generation context | `<slug>-deck-context.yml` | `kotlin-inline-functions-and-reification-deck-context.yml` |
| Generated AsciiDoc deck | `<slug>_<lang>-deck.adoc` | `kotlin-inline-functions-and-reification_en-deck.adoc` |
| Compiled HTML deck | `<slug>_<lang>-deck.html` | `kotlin-inline-functions-and-reification_en-deck.html` |

`<slug>` को `subject` से kebab-case में व्युत्पन्न किया जाता है (उच्चारण चिह्न सामान्यीकृत); `<lang>`
`-Planguage` द्वारा पास किया गया ISO 639-1 कोड है (डिफ़ॉल्ट `fr`)।

## पूर्वापेक्षाएँ

- **Java** 24+ (प्लगइन रनटाइम पर Java 23+ की जाँच करता है; Kotlin 2.3.20 टूलचेन)
- **Gradle** 9.5.1+
- **Node.js / npx** `serveSlides` और `visualTest` कार्यों हेतु
- **Docker** RAG पाइपलाइन द्वारा उपयोग किए जाने वाले pgvector कंटेनर हेतु
- **Internet connection** Reveal.js gem निर्भरताएँ डाउनलोड करने हेतु

## बिल्ड एवं परीक्षण

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

## समस्या निवारण

| Symptom | Fix |
|---------|-----|
| `UnsatisfiedLinkError: libtokenizers.so` on second RAG run | run RAG tasks with `--no-daemon` |
| `Java heap space` | `export GRADLE_OPTS="-Xmx2g"` |
| pgvector container stuck | `docker rm -f $(docker ps -q -f name=pgvector)` then retry |
| `asciidoctorRevealJs` Configuration Cache failure | expected — Configuration Cache is disabled (`configurationCache = false` on the portal) |
| `npx: command not found` | install Node.js and ensure `npx` is on `PATH` |

पूर्ण वास्तुकला संदर्भ हेतु [README.adoc](../README.adoc) देखें।

## License

Apache License 2.0 — [LICENCE](../LICENCE) देखें।

---

_CCCP Education पारिस्थितिकी का हिस्सा — `groupId: education.cccp`。_