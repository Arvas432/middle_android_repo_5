package com.yandex.practicum.middle_homework_5

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

abstract class FindUntranslatedStringsTask : DefaultTask() {
    companion object {
        const val RES_DIR = "src/main/res"
        const val STRINGS_DIR = "values/strings.xml"
    }
    @TaskAction
    fun findUntranslatedStrings() {
        val resDir = File(project.projectDir, RES_DIR)
        val defaultStringsFile = File(resDir, STRINGS_DIR)
        val defaultStrings = parseStringNames(defaultStringsFile)
        val valueDirs = resDir.listFiles { file ->
            file.isDirectory && file.name.startsWith("values-") && file.name != "values"
        } ?: emptyArray()
        val missingStrings = mutableMapOf<String, List<String>>()
        valueDirs.forEach { dir ->
            val locale = dir.name.substringAfter("values-")
            val stringsFile = File(dir, "strings.xml")
            val localeStrings = parseStringNames(stringsFile)
            val missing = defaultStrings.filter { it !in localeStrings }
            if (missing.isNotEmpty()) {
                missingStrings[locale] = missing
            }
        }
        generateGradleError(missingStrings)


    }

    private fun generateGradleError(missingStrings: Map<String, List<String>>) {
        if (missingStrings.isNotEmpty()) {
            val stringBuilderErrorText = StringBuilder("Missing translations").append(System.lineSeparator())
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
        if (file.exists()) {
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val doc = dBuilder.parse(file)
            doc.documentElement.normalize()
            val stringNodes = doc.getElementsByTagName("string")
            return (0 until stringNodes.length).mapNotNull { i ->
                val node = stringNodes.item(i)
                node.attributes?.getNamedItem("name")?.nodeValue
            }
        } else {
            return emptyList()
        }

    }

}