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

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.teavm.tooling.RuntimeCopyOperation
import org.teavm.tooling.TeaVMTool
import org.teavm.tooling.sources.DirectorySourceFileProvider
import org.teavm.tooling.sources.JarSourceFileProvider
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URLClassLoader

open class TeaVMTask : DefaultTask() {

    var mainClass: String = ""
    var installDirectory: String = File(project.buildDir, "teavm").absolutePath
    var targetFileName: String = "app.js"
    var mainPageIncluded: Boolean = true
    var copySources: Boolean = false
    var generateSourceMap: Boolean = false
    var minified: Boolean = true
    var runtime: RuntimeCopyOperation = RuntimeCopyOperation.SEPARATE

    val gradleLog = Logging.getLogger(TeaVMTask::class.java)
    val log by lazy { TeaVMLoggerGlue(project.logger) }

    @get:OutputDirectory
    protected val outputDir by lazy {
        File(installDirectory)
    }

    @get:InputFiles
    protected val inputFiles by lazy {
        if (copySources) {
            sourceFiles + jarsAndClassDirectories
        }
        else {
            jarsAndClassDirectories
        }
    }

    private val sourceFiles by lazy {
        val convention = project.convention.getPlugin(JavaPluginConvention::class.java)
        val sources = convention
                .sourceSets
                .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
                .allSource
                .srcDirs
        val teaVmSources = project
                .configurations
                .getByName("teavmsources")
                .files
        sources + teaVmSources
    }

    private val jarsAndClassDirectories by lazy {
        project.configurations.getByName("runtime").run {
            val mainSourceSet = project.convention.getPlugin(JavaPluginConvention::class.java)
                .sourceSets
                .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
            val classesDir = mainSourceSet.java.outputDir
            val resourceDir = mainSourceSet.output.resourcesDir
            files + allArtifacts.files + classesDir + resourceDir
        }
    }

    @TaskAction fun compTeaVM() {
        val tool = TeaVMTool()
        val project = project

        tool.targetDirectory = File(installDirectory)
        tool.targetFileName = targetFileName

        if (mainClass.isNotBlank()) {
            tool.mainClass = mainClass
        } else {
            throw TeaVMException("mainClass not specified!")
        }

        for (f in sourceFiles) {
            if (f.isFile) {
                if (f.absolutePath.endsWith(".jar")) {
                    tool.addSourceFileProvider(JarSourceFileProvider(f))
                } else {
                    tool.addSourceFileProvider(DirectorySourceFileProvider(f))
                }
            } else {
                tool.addSourceFileProvider(DirectorySourceFileProvider(f))
            }

        }

        val cacheDirectory = File(project.buildDir, "teavm-cache")
        cacheDirectory.mkdirs()
        tool.cacheDirectory = cacheDirectory
        tool.setIncremental(true)
        tool.runtime = runtime
        tool.isMinifying = minified
        tool.log = log
        tool.isSourceFilesCopied = copySources
        tool.isSourceMapsFileGenerated = generateSourceMap

        val classLoader = prepareClassLoader()
        classLoader.use {
            tool.classLoader = it
            tool.generate()
        }
    }


    private fun prepareClassLoader(): URLClassLoader {
        try {
            val urls = jarsAndClassDirectories.map { it.toURI().toURL() }
            gradleLog.info("Using classpath URLs: {}", urls)

            return URLClassLoader(urls.toTypedArray(), javaClass.classLoader)
        } catch (e: MalformedURLException) {
            throw GradleException("Error gathering classpath information", e)
        }
    }


}
