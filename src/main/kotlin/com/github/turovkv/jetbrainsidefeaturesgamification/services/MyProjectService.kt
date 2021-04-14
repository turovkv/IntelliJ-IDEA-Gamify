package com.github.turovkv.jetbrainsidefeaturesgamification.services

import com.github.turovkv.jetbrainsidefeaturesgamification.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
