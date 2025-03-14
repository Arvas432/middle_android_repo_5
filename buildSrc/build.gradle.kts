
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(gradleApi())
}
allprojects {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
abstract class FindUntranslatedStringsTask : DefaultTask() {
    @TaskAction
    fun findUntranslatedStrings() {
        val resDir = File(project.projectDir, "src/main/res")
        val defaultStringsFile = File(resDir, "values/strings.xml")
        val defaultStrings = parseStringNames(defaultStringsFile)
        val valueDirs = resDir.listFiles { file ->
            file.isDirectory && file.name.startsWith("values-") && file.name != "values"
        } ?: emptyArray()
        val missingStrings = mutableMapOf<String, List<String>>()
        valueDirs.forEach { dir ->
            val locale = dir.name.substringAfter("values-")
            val stringsFile = File(dir, "strings.xml")
            val localeStrings = if (stringsFile.exists()) {
                parseStringNames(stringsFile)
            } else {
                emptyList()
            }
            val missing = defaultStrings.filter { it !in localeStrings }
            if (missing.isNotEmpty()) {
                missingStrings[locale] = missing
            }
        }

        if (missingStrings.isNotEmpty()) {
            val stringBuilderErrorText =
                StringBuilder("Missing translations").append(System.lineSeparator())
            missingStrings.forEach { missing ->
                stringBuilderErrorText
                    .append("=== ${missing.key} ===")
                    .append(System.lineSeparator())
                    .append(missing.value.joinToString(separator = System.lineSeparator()))
                    .append(System.lineSeparator())

            }
            throw GradleException(stringBuilderErrorText.toString())
        }

    }

    private fun parseStringNames(file: File): List<String> {
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(file)
        doc.documentElement.normalize()
        val stringNodes = doc.getElementsByTagName("string")
        return (0 until stringNodes.length).mapNotNull { i ->
            val node = stringNodes.item(i)
            node.attributes?.getNamedItem("name")?.nodeValue
        }
    }

}

class FindUntranslatedStringsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("untranslatedStrings", FindUntranslatedStringsTask::class.java)
    }
}
apply<FindUntranslatedStringsPlugin>()
