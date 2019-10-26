/*
 * Copyright 2019 Patrick Leitermann
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

package com.github.ptkltm.development.recursiveinclude.gradleplugin

import java.io.File
import org.gradle.api.initialization.Settings
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.LoggerFactory


/**
 * Gradle plugin with the id 'com.github.ptkltm.development.recursiveinclude' for recursively applying sub projects
 * and sub builds starting with the sub directories located at the directory returned by [Settings.getRootDir].
 *
 * This plugin can be applied to 'settings.gradle' or 'settings.gradle.kts' files in Gradle builds.
 *
 * For further information check the method-level documentation of the [apply] implementation.
 *
 * @author Patrick Leitermann
 */
class RecursiveIncludeGradlePlugin : Plugin<Settings> {
    /**
     * Singleton-like container for storing static fields, properties and methods
     * reused across all instances of [RecursiveIncludeGradlePlugin].
     */
    private
    companion object {
        /**
         * Logger for debug messages that are shown if Gradle is executed with the '--debug'
         * flag.
         */
        private
        val LOGGER = LoggerFactory.getLogger(RecursiveIncludeGradlePlugin::class.java)
    }

    /**
     * Declares the name of the root directory as project name and recursively searches for 'settings.gradle',
     * 'settings.gradle.kts', 'build.gradle' and 'build.gradle.kts' files in the directory provided by
     * [Settings.getRootDir].
     * All invisible files (starting with '.') or files contained in a directory called 'build'
     * are ignored.
     *
     * If a 'settings.gradle' or 'settings.gradle.kts' file is detected, the relative path to the
     * root directory of the file is automatically applied as composite build via
     * [Settings.includeBuild].
     *
     * If no 'settings.gradle' or 'settings.gradle.kts' files are available, but a 'build.gradle'
     * or 'build.gradle.kts' file was found, the relative path of the root directory of that file
     * is automatically applied as sub project via [Settings.include].
     * The name of the sub project is set to the name of the directory where the 'build.gradle' or 'build.gradle.kts'
     * file is located.
     *
     * After one of the four files - 'settings.gradle', 'settings.gradle.kts', 'build.gradle' or
     * 'build.gradle.kts' - was found, the recursive search at the current path is stopped.
     *
     * @param [settings] The [Settings] that are extended by additional logic for recursively finding Gradle sub builds
     * and Gradle sub projects based on 'settings.gradle(.kts)' and 'build.gradle(.kts)' files. It provides the root
     * directory via [Settings].
     */
    override fun apply(
        settings: Settings
    ) {
        settings.run {
            rootProject.name = settings.rootDir.name
            rootDir.listFiles()?.filter { it.isVisibleDirectory }?.forEach {
                settings.applyDirectoryRecursive(directory = it)
            }
        }
    }

    /**
     * Returns true if the current [File] is neither invisible (by starting with a '.')
     * nor contained in the automatically generated 'build' directory.
     */
    private
    val File.isVisibleDirectory
        get() = isDirectory && !(name.startsWith(prefix = ".") ||
            name == Project.DEFAULT_BUILD_DIR_NAME)

    /**
     * Recursively searches for 'settings.gradle', 'settings.gradle.kts', 'build.gradle' and
     * 'build.gradle.kts' files in the directory provided by the [directory] parameter.
     * All invisible files (starting with '.') or files contained in a directory called 'build'
     * are ignored.
     *
     * If a 'settings.gradle' or 'settings.gradle.kts' file is detected, the relative path to the
     * root directory of the file is automatically applied as composite build via
     * [Settings.includeBuild].
     *
     * If no 'settings.gradle' or 'settings.gradle.kts' files are available, but a 'build.gradle'
     * or 'build.gradle.kts' file was found, the relative path of the root directory of that file
     * is automatically applied as sub project via [Settings.include].
     *
     * After one of the four files - 'settings.gradle', 'settings.gradle.kts', 'build.gradle' or
     * 'build.gradle.kts' - was found, the recursive search at the current path is stopped.
     *
     * @receiver The Gradle settings represented by the file 'settings.gradle' or 'settings.gradle.kts'.
     * @param [directory] The directory that's recursively searched for
     * 'settings.gradle', 'settings.gradle.kts', 'build.gradle' or 'build.gradle.kts' files.
     */
    private
    fun Settings.applyDirectoryRecursive(
        directory: File
    ) {
        val subDirectories = mutableListOf<File>()
        var gradleBuildFile: File? = null
        val subFiles = directory.listFiles()
        if (null != subFiles) {
            for (subFile in subFiles)
                when {
                    subFile.isVisibleDirectory -> subDirectories.add(element = subFile)
                    subFile.isGradleFile(buildFileName = Settings.DEFAULT_SETTINGS_FILE) -> {
                        LOGGER.debug("Settings.DEFAULT_SETTINGS_FILE: ${subFile.absolutePath}.")
                        includeBuild(buildRelativePath(file = subFile))
                        return
                    }
                    subFile.isGradleFile(buildFileName = Project.DEFAULT_BUILD_FILE) -> {
                        LOGGER.debug("Project.DEFAULT_BUILD_FILE: ${subFile.absolutePath}.")
                        gradleBuildFile = subFile
                    }
                }
        }
        if (null == gradleBuildFile) {
            LOGGER.debug("Sub Directories of ${directory.absoluteFile}: $subDirectories.")
            subDirectories.forEach { applyDirectoryRecursive(directory = it) }
        } else {
            LOGGER.debug("Project.DEFAULT_BUILD_FILE: ${gradleBuildFile.absolutePath}.")
            val subProjectPath = "${Project.PATH_SEPARATOR}${gradleBuildFile.parentFile.name}"
            include(subProjectPath)
            project(subProjectPath).projectDir = File(buildRelativePath(file = gradleBuildFile))
        }
    }

    /**
     * Checks if the name of a [File] is equals the name of the [buildFileName]
     * or equals the name of the file name with the '.kts' extension.
     *
     * @receiver The file that should be checked.
     * @param [buildFileName] The name of the Gradle build file.
     * @return true if the [buildFileName] has the name of a Gradle build file with or without the '.kts' extension.
     * @sample [buildFileName] The [buildFileName] may be 'build.gradle' (or 'settings.gradle')
     * and the method returns true if the name of the file is either 'build.gradle' or
     * 'build.gradle.kts' (or 'settings.gradle' / 'settings.gradle.kts'.)
     */
    private
    fun File.isGradleFile(
        buildFileName: String
    ): Boolean = buildFileName == name || "$buildFileName.kts" == name

    /**
     * Get the relative location of the root directory's settings.gradle(.kts) file (where the
     * 'com.github.ptkltm.development.recursiveinclude' plugin is applied) to the
     * sub root directory of the found 'build.gradle', 'build.gradle.kts', 'settings.gradle' or
     * 'settings.gradle.kts' file.
     *
     * @receiver The Gradle settings represented by the file 'settings.gradle' or 'settings.gradle.kts'.
     * @param [file] The found 'build.gradle', 'build.gradle.kts', 'settings.gradle' or
     * 'settings.gradle.kts' file in a sub folder.
     * @return The relative path.
     * @sample [file] If the file may be a 'build.gradle.kts' located at the path
     * 'com.example.exampleplatform/javaapi/com.example.exampleplatform.javaapi/build.gradle.kts' and
     * the settings.gradle(.kts) file where the plugin 'com.github.ptkltm.development.recursiveinclude'
     * was applied is located at the path 'com.example.exampleplatform/settings.gradle.kts', the relative
     * path would be './javaapi/com.example.exampleplatform.javaapi'.
     */
    private
    fun Settings.buildRelativePath(
        file: File
    ) = file.parentFile.absolutePath
        .replaceFirst(oldValue = "${rootDir.absoluteFile}${File.separatorChar}", newValue = "")
        .replace(oldChar = File.separatorChar, newChar = '/')
}
