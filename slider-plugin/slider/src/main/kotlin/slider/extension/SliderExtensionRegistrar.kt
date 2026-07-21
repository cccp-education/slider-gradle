package slider.extension

import org.asciidoctor.gradle.jvm.slides.RevealJSExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import slider.Slides.RevealJsSlides.GROUP_TASK_SLIDER

/**
 * Registrar for the slider Gradle extensions.
 *
 * Pure domain orchestration over Gradle's [ExtensionContainer]:
 * - registers the `slider {}` DSL block ([SliderExtension]) so consumers can
 *   configure `configPath`, `language`, `supportedLanguages`;
 * - pins the Reveal.js template to a fixed version (see [RevealJsPin]) on the
 *   AsciidoctorJ `RevealJSExtension`.
 *
 * The registrar replaces the former `SliderManager.Extensions` object. It
 * depends on Gradle types (Project, ExtensionContainer, RevealJSExtension) but
 * keeps no state and has no side effects beyond the extension configuration it
 * is asked to perform.
 */
object SliderExtensionRegistrar {

    /**
     * Registers the `slider {}` DSL extension and pins the Reveal.js template
     * on the supplied [project].
     *
     * The Reveal.js pin defaults to [RevealJsPin] (reveal.js 5.2.1) and can be
     * overridden by passing a custom [pin], which is useful for tests.
     */
    fun configure(project: Project, pin: RevealJsPin = RevealJsPin()) {
        registerSliderExtension(project)
        pinRevealJsTemplate(project, pin)
    }

    /**
     * Registers the `slider {}` DSL extension on [project].
     */
    fun registerSliderExtension(project: Project) {
        project.extensions.create(GROUP_TASK_SLIDER, SliderExtension::class.java)
    }

    /**
     * Pins the Reveal.js template on the AsciidoctorJ RevealJSExtension.
     */
    fun pinRevealJsTemplate(project: Project, pin: RevealJsPin = RevealJsPin()) {
        project.extensions.getByType(RevealJSExtension::class.java).apply {
            version = pin.version
            templateGitHub { gh ->
                gh.setOrganisation(pin.organisation)
                gh.setRepository(pin.repository)
                gh.setTag(pin.tag)
            }
        }
    }
}