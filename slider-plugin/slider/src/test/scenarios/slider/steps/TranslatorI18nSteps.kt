package slider.steps

import contracts.i18n.LanguageCatalog
import io.cucumber.java8.En
import org.assertj.core.api.Assertions.assertThat
import slider.translate.TranslatorManager

class TranslatorI18nSteps : En {

    private var supportedLanguages: Set<String> = emptySet()
    private var translationTasks: Set<Pair<String, Pair<String, String>>> = emptySet()
    private var prompt: String = ""

    init {

        When("the translator supported languages are queried") {
            supportedLanguages = TranslatorManager.supportedLanguages
        }

        Then("they should contain all LanguageCatalog supported codes") {
            LanguageCatalog.supportedCodes().forEach { code ->
                assertThat(supportedLanguages)
                    .withFailMessage("supportedLanguages should contain ISO code '$code'")
                    .contains(code)
            }
        }

        Then("they should not contain {string} or {string}") { first: String, second: String ->
            assertThat(supportedLanguages).doesNotContain(first, second)
        }

        When("translation tasks are generated from supported languages") {
            translationTasks = TranslatorManager.run {
                supportedLanguages.translationTasks()
            }
        }

        Then("no task should translate a language to itself") {
            translationTasks.forEach { entry ->
                val (from, to) = entry.second
                assertThat(from)
                    .withFailMessage("Task '${entry.first}' should not translate a language to itself")
                    .isNotEqualTo(to)
            }
        }

        Then("the number of tasks should be {int} times {int}") { n: Int, m: Int ->
            assertThat(translationTasks).hasSize(n * m)
        }

        When("a translation prompt is generated from {string} to {string} for text {string}") { from: String, to: String, text: String ->
            prompt = TranslatorManager.PromptManager.run {
                (from to to).getTranslatePromptMessage(text)
            }
        }

        Then("the prompt should contain the native name of {string}") { code: String ->
            val nativeName = LanguageCatalog.findByCode(code)?.nativeName
            assertThat(nativeName).isNotNull()
            assertThat(prompt)
                .withFailMessage("Prompt should contain native name '$nativeName' for code '$code'")
                .contains(nativeName!!)
        }

        Then("the prompt should contain {string}") { expected: String ->
            assertThat(prompt).contains(expected)
        }
    }
}