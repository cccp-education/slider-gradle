package slider.prerequisite

import org.gradle.api.JavaVersion

object SliderPrerequisiteRegistrar {

    fun checkJavaVersion() =
        JavaVersionGuard.requireJava23FromMajor(
            JavaVersion.current().majorVersion,
        )
}
