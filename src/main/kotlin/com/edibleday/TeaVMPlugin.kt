/**
 * Copyright 2015 SIA "Edible Day"
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.edibleday

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

const val TASK_NAME = "teavmc"

class TeaVMPlugin : Plugin<Project> {

    val version = "0.5.1"

    override fun apply(project: Project) {

        val extension = project.extensions.create(
            TASK_NAME,
            TeaVMPluginExtension::class.java)

        project.apply(mapOf(
                "plugin" to "java"
        ))

        project.configurations.create("teavmsources")

        project.repositories.mavenCentral()

        project.dependencies.let {
            it.add("compile", "org.teavm:teavm-classlib:$version")
            it.add("compile", "org.teavm:teavm-jso:$version")
            it.add("compile", "org.teavm:teavm-jso-apis:$version")
            it.add("compile", "org.teavm:teavm-metaprogramming-impl:$version")
            it.add("teavmsources", "org.teavm:teavm-platform:$version:sources")
            it.add("teavmsources", "org.teavm:teavm-classlib:$version:sources")
            it.add("teavmsources", "org.teavm:teavm-jso:$version:sources")
            it.add("teavmsources", "org.teavm:teavm-jso-apis:$version:sources")
        }


        val task = project.tasks.create(TASK_NAME,
            TeaVMTask::class.java,
            Action {
                task: TeaVMTask ->
                    task.dependsOn("classes")
                    task.description = "TeaVM Compile"
                    task.group = "build"
            })

        project.afterEvaluate {

            if (extension.flavourVersion != null) {
                project.dependencies.let {
                    it.add("compile", "org.teavm.flavour:teavm-flavour-widgets:${extension.flavourVersion}")
                    it.add("compile", "org.teavm.flavour:teavm-flavour-rest:${extension.flavourVersion}")
                }
            }

            task.mainClass = extension.mainClass ?: task.mainClass
            task.installDirectory = extension.installDirectory ?: task.installDirectory
            task.targetFileName = extension.targetFileName ?: task.targetFileName
            task.copySources = extension.copySources ?: task.copySources
            task.generateSourceMap = extension.generateSourceMap ?: task.generateSourceMap
            task.minified = extension.minified ?: task.minified
            task.runtime = extension.runtime ?: task.runtime
        }
    }

}
