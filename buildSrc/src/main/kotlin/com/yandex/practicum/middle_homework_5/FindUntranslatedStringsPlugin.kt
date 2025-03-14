package com.yandex.practicum.middle_homework_5

import org.gradle.api.Plugin
import org.gradle.api.Project

class FindUntranslatedStringsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val taskCustom =
            project.tasks.register("untranslatedStrings", FindUntranslatedStringsTask::class.java)
        taskCustom.configure {
            group = "UntranslatedTask"
        }
    }
}
