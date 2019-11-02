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
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.Test
import org.gradle.api.Project
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.io.TempDir


/**
 * Tests for the [RecursiveIncludeGradlePlugin].
 *
 * @author Patrick Leitermann
 */
class RecursiveIncludeGradlePluginTest {
    /**
     * Singleton-like container for storing static fields, properties and methods
     * reused across all instances of [RecursiveIncludeGradlePluginTest].
     */
    private
    companion object {
        /**
         * The name of the 'build' task.
         */
        private
        const val BUILD_TASK_NAME = "build"

        /**
         * The name of the 'clean' task.
         */
        private
        const val CLEAN_TASK_NAME = "clean"

        /**
         * The name of a Gradle build script with the Groovy syntax.
         */
        private
        const val BUILD_GRADLE_FILE_NAME = "build.gradle"

        /**
         * The content of a 'build.gradle' script in the Groovy syntax.
         *
         * Defines two tasks:
         * 1. 'build' for creating a file with the name of the project inside the 'build' folder.
         * 2. 'clean' for deleting the 'build' folder.
         */
        private
        val BUILD_GRADLE_FILE_CONTENT = """
import java.io.File

task $BUILD_TASK_NAME {
    doFirst {
        def targetFile = file(
            "${'$'}{Project.DEFAULT_BUILD_DIR_NAME}${'$'}{File.separatorChar}${'$'}{project.name}.txt"
        )
        targetFile.mkdirs()
        targetFile.createNewFile()
    }
}

task $CLEAN_TASK_NAME(type: Delete) {
    delete(Project.DEFAULT_BUILD_DIR_NAME)
}
        """.trimIndent()

        /**
         * The name of a Gradle build script with the Kotlin syntax.
         */
        private
        const val BUILD_GRADLE_KTS_FILE_NAME = "$BUILD_GRADLE_FILE_NAME.kts"

        /**
         * The content of a 'build.gradle.kts' script in the Kotlin syntax.
         *
         * Defines two tasks:
         * 1. 'build' for creating a file with the name of the project inside the 'build' folder.
         * 2. 'clean' for deleting the 'build' folder.
         */
        private
        val BUILD_GRADLE_KTS_FILE_CONTENT = """
import java.io.File

tasks {
    register("$BUILD_TASK_NAME") {
        doFirst {
            file("build${'$'}{File.separatorChar}${'$'}{project.name}.txt").apply {
                mkdirs()
                createNewFile()
            }
        }
    }
    register("$CLEAN_TASK_NAME", Delete::class) {
        delete(Project.DEFAULT_BUILD_DIR_NAME)
    }
}
        """.trimIndent()

        /**
         * The name of the Gradle settings file with the Groovy syntax.
         */
        private
        const val SETTINGS_GRADLE_FILE_NAME = "settings.gradle"

        /**
         * The name of the Gradle settings file with the Kotlin syntax.
         */
        private
        const val SETTINGS_GRADLE_KTS_FILE_NAME = "$SETTINGS_GRADLE_FILE_NAME.kts"
    }

    /**
     * Tests the recursive include logic for sub projects if the 'com.github.ptkltm.development.recursiveinclude'
     * plugin is applied to a 'settings.gradle.kts' file in the root and the file 'build.gradle.kts' contained inside a
     * nested folder folder.
     * The test also checks that projects inside hidden sub directories starting with '.' or inside a directory called
     * 'build' are ignored.
     *
     * For more information about the test logic take a look at the method-level documentation of the
     * method [verifyRecursiveSubProjectInclude].
     *
     * @param [temporaryFolder] The temporary folder created and deleted by JUnit Jupiter.
     */
    @Test
    fun testRecursiveSubProjectIncludeWithKotlinSubProjectSetup(
        @TempDir temporaryFolder: Path
    ) {
        temporaryFolder.verifyRecursiveSubProjectInclude(
            subProjectBuildGradleName = BUILD_GRADLE_KTS_FILE_NAME,
            subProjectBuildGradleContent = BUILD_GRADLE_KTS_FILE_CONTENT
        )
    }

    /**
     * Tests the recursive include logic for sub projects if the 'com.github.ptkltm.development.recursiveinclude'
     * plugin is applied to a 'settings.gradle.kts' file in the root and the file 'build.gradle' contained inside a
     * nested folder folder.
     * The test also checks that projects inside hidden sub directories starting with '.' or inside a directory called
     * 'build' are ignored.
     *
     * For more information about the test logic take a look at the method-level documentation of the
     * method [verifyRecursiveSubProjectInclude].
     *
     * @param [temporaryFolder] The temporary folder created and deleted by JUnit Jupiter.
     */
    @Test
    fun testRecursiveSubProjectIncludeWithGroovySubProjectSetup(
        @TempDir temporaryFolder: Path
    ) {
        temporaryFolder.verifyRecursiveSubProjectInclude(
            subProjectBuildGradleName = BUILD_GRADLE_FILE_NAME,
            subProjectBuildGradleContent = BUILD_GRADLE_FILE_CONTENT
        )
    }

    /**
     * Tests the recursive include logic for sub builds if the 'com.github.ptkltm.development.recursiveinclude'
     * plugin is applied to a 'settings.gradle.kts' file in the root and the files 'settings.gradle.kts' and
     * 'build.gradle.kts' contained inside a nested folder folder.
     * The test also checks that projects inside hidden sub directories starting with '.' or inside a directory called
     * 'build' are ignored.
     *
     * For more information about the test logic take a look at the method-level documentation of the
     * method [verifyRecursiveSubBuildInclude].
     *
     * @param [temporaryFolder] The temporary folder created and deleted by JUnit Jupiter.
     */
    @Test
    fun testRecursiveSubBuildIncludeWithKotlinSubProjectSetup(
        @TempDir temporaryFolder: Path
    ) {
        temporaryFolder.verifyRecursiveSubBuildInclude(
            subProjectBuildGradleName = BUILD_GRADLE_KTS_FILE_NAME,
            subProjectBuildGradleContent = BUILD_GRADLE_KTS_FILE_CONTENT,
            subProjectSettingsGradleName = SETTINGS_GRADLE_KTS_FILE_NAME
        )
    }

    /**
     * Tests the recursive include logic for sub builds if the 'com.github.ptkltm.development.recursiveinclude'
     * plugin is applied to a 'settings.gradle.kts' file in the root and the files 'settings.gradle' and
     * 'build.gradle' contained inside a nested folder folder.
     * The test also checks that projects inside hidden sub directories starting with '.' or inside a directory called
     * 'build' are ignored.
     *
     * For more information about the test logic take a look at the method-level documentation of the
     * method [verifyRecursiveSubBuildInclude].
     *
     * @param [temporaryFolder] The temporary folder created and deleted by JUnit Jupiter.
     */
    @Test
    fun testRecursiveSubBuildIncludeWithGroovySubProjectSetup(
        @TempDir temporaryFolder: Path
    ) {
        temporaryFolder.verifyRecursiveSubBuildInclude(
            subProjectBuildGradleName = BUILD_GRADLE_FILE_NAME,
            subProjectBuildGradleContent = BUILD_GRADLE_FILE_CONTENT,
            subProjectSettingsGradleName = SETTINGS_GRADLE_FILE_NAME
        )
    }

    /**
     * Reusable function for the test of recursively included sub projects.
     * Generates a temporary project structure and tests the logic of the
     * 'com.github.ptkltm.development.recursiveinclue' plugin during a Gradle build.
     * For more information see the method-level documentation of [verifyRecursiveBuildInclude].
     *
     * @receiver The path of the parent directory.
     * @param [subProjectBuildGradleName] 'build.gradle' or 'build.gradle.kts'.
     * @param [subProjectBuildGradleContent] The content of the build.gradle(.kts) file of the sub projects.
     */
    private
    fun Path.verifyRecursiveSubProjectInclude(
        subProjectBuildGradleName: String,
        subProjectBuildGradleContent: String
    ) {
        verifyRecursiveBuildInclude(
            includedCollectionProperty = "subprojects",
            includedTaskExpression = { taskName -> """"${'$'}{it.path}${'$'}{Project.PATH_SEPARATOR}$taskName"""" },
            subProjectBuildGradleName = subProjectBuildGradleName,
            subProjectBuildGradleContent = subProjectBuildGradleContent,
            additionalSubProjectInitialization = { }
        )
    }

    /**
     * Reusable function for the test of recursively included composite builds.
     * Generates a temporary project structure and tests the logic of the
     * 'com.github.ptkltm.development.recursiveinclue' plugin during a Gradle build.
     * For more information see the method-level documentation of [verifyRecursiveBuildInclude].
     *
     * @receiver The path of the parent directory.
     * @param [subProjectBuildGradleName] 'build.gradle' or 'build.gradle.kts'.
     * @param [subProjectBuildGradleContent] The content of the build.gradle(.kts) file of the sub projects.
     * @param [subProjectSettingsGradleName] settings.gradle (for Groovy syntax) or settings.gradle.kts
     * (for Kotlin syntax).
     */
    private
    fun Path.verifyRecursiveSubBuildInclude(
        subProjectBuildGradleName: String,
        subProjectBuildGradleContent: String,
        subProjectSettingsGradleName: String
    ) {
        verifyRecursiveBuildInclude(
            includedCollectionProperty = "gradle.includedBuilds",
            includedTaskExpression = { taskName -> """it.task("${'$'}{Project.PATH_SEPARATOR}$taskName")""" },
            subProjectBuildGradleName = subProjectBuildGradleName,
            subProjectBuildGradleContent = subProjectBuildGradleContent,
            additionalSubProjectInitialization = { projectPath ->
                Files.write(
                    projectPath.resolve(subProjectSettingsGradleName),
                    """rootProject.name = "${projectPath.last().fileName}"""".toByteArray()
                )
            }
        )
    }

    /**
     * Reusable generic method for the logic of the test for the automatic inclusion of sub builds / sub projects in
     * the root project that applied the 'com.github.ptkltm.development.recursiveinclude' Gradle plugin.
     *
     * The test code is based on multiple steps:
     *
     * 1. Generate a temporary Gradle project with three other Gradle builds in nested folders based on the following
     * structure:
     *
     * /com.example
     *      - build.gradle.kts
     *          Contains two tasks:
     *              1. 'build' task
     *                  Uses the Gradle composite build / sub projects api for the execution of the 'build' task of
     *                  all included builds / sub projects.
     *              2. 'clean' task
     *                  Uses the Gradle composite build / sub projects api for the execution of the 'clean' task of all
     *                  included builds / sub projects.
     *      - settings.gradle.kts
     *          Applies the 'com.github.ptkltm.development.recursiveinclude' plugin.
     *
     * /com.example/exampleplatform
     *      - no non-directory files
     *
     * /com.example/exampleplatform/com.example.exampleplatform.javaapi
     *      - build.gradle(.kts)
     *          Contains two tasks:
     *              1. 'build' task
     *                  Creates a file named 'com.example.exampleplatform.javaapi.txt' inside the root path
     *                  './com.example/exampleplatform/com.example.exampleplatform.javaapi/build'
     *              2. 'clean' task
     *                  Deletes the folder './com.example/exampleplatform/com.example.exampleplatform.javaapi/build'.
     *      - settings.gradle(.kts)
     *          Sets the rootProject.name to 'com.example.exampleplatform.javaapi'.
     *          This file is only created if the method [verifyRecursiveBuildInclude] is executed by the method
     *          [verifyRecursiveSubBuildInclude] to simulate a sub build.
     *
     * /com.example/exampleplatform/.hidden
     *      - build.gradle(.kts)
     *              1. 'build' task
     *                  Creates a file named '.hidden.txt' inside the root path
     *                  './com.example/exampleplatform/.hidden/build'
     *              2. 'clean' task
     *                  Deletes the folder './com.example/exampleplatform/.hidden/build'.
     *              The test expects that none of the two tasks is executed, because the 'build.gradle(.kts)' file
     *              is located at a hidden folder starting with a '.'.
     *      - settings.gradle(.kts)
     *          Sets the rootProject.name to '.hidden'.
     *          This file is only created if the method [verifyRecursiveBuildInclude] is executed by the method
     *          [verifyRecursiveSubBuildInclude] to simulate a sub build.
     *
     * /com.example/exampleplatform/build
     *      - build.gradle(.kts)
     *              1. 'build' task
     *                  Creates a file named '.build.txt' inside the root path
     *                  './com.example/exampleplatform/build/build'
     *              2. 'clean' task
     *                  Deletes the folder './com.example/exampleplatform/.hidden/build'.
     *              The test expects that none of the two tasks is executed, because the 'build.gradle(.kts)' file
     *              is located at an a parent folder with the name 'build'
     *      - settings.gradle(.kts)
     *          Sets the rootProject.name to 'build'.
     *          This file is only created if the method [verifyRecursiveBuildInclude] is executed by the method
     *          [verifyRecursiveSubBuildInclude] to simulate a sub build.
     *
     * 2. Executes the 'build' task at the root directory.
     *
     * 3. Check that the execution of the 'build' task was successful.
     *
     * 4. Verifies that a file 'com.example.exampleplatform.javaapi.txt' was generated in the root path
     * './com.example/exampleplatform/com.example.exampleplatform.javaapi/build'.
     *
     * 5. Checks that no folders './com.example/exampleplatform/.hidden/build' and
     * './com.example/exampleplatform/build/build' were generated
     * by the 'build' tasks in the files './com.example/exampleplatform/.hidden/build.gradle(.kts)' and
     * './com.example/exampleplatform/build/build.gradle(.kts)', because one project is located in a hidden folder
     * starting with '.' and the other one is located in a folder called 'build'.
     *
     * 6. Executes the 'clean' task at the root directory.
     *
     * 7. Verify that the execution of the 'clean' task was successful.
     *
     * 8. Verifies that the directory './com.example/exampleplatform/com.example.exampleplatform.javaapi/build' was
     * deleted.
     *
     * @receiver The path of the parent directory.
     * @param [includedCollectionProperty] The content of the 'build.gradle.kts' file at the root project.
     * @param [includedTaskExpression] The expression for defining the task dependencies.
     * @param [subProjectBuildGradleName] 'build.gradle' or 'build.gradle.kts'.
     * @param [subProjectBuildGradleContent] The content of the build.gradle(.kts) file of the sub projects.
     * @param [additionalSubProjectInitialization] Closure for additional initialization of the sub projects.
     * @sample [includedCollectionProperty] 'gradle.includedBuilds' or included sub builds and 'subprojects' for
     * included sub projects.
     */
    private
    fun Path.verifyRecursiveBuildInclude(
        includedCollectionProperty: String,
        includedTaskExpression: (String) -> String,
        subProjectBuildGradleName: String,
        subProjectBuildGradleContent: String,
        additionalSubProjectInitialization: (Path) -> Unit
    ) {
        // 1. Generate the file structure described in the documentation of this method.

        // Initializes the root project './com.example'.
        val rootProjectDirectoryPath = initializeGradleProject(
            projectDirectoryName = "com.example",
            buildGradleFileName = BUILD_GRADLE_KTS_FILE_NAME,
            buildGradleContent = """
tasks {
    ${setOf(BUILD_TASK_NAME, CLEAN_TASK_NAME).joinToString(separator = "\n") {
                """
    create("$it") {
        dependsOn(
            $includedCollectionProperty.map { 
                ${includedTaskExpression(it)}
            }
        )
    }
        """.trimIndent()
            }}
}
            """.trimIndent(),
            additionalProjectInitialization = { projectPath ->
                Files.write(
                    projectPath.resolve(SETTINGS_GRADLE_KTS_FILE_NAME),
                    """
buildscript {
    repositories { 
        flatDir {
            dirs = setOf(file("${File("").absoluteFile}/${'$'}{Project.DEFAULT_BUILD_DIR_NAME}/libs"))
        }
    }
    dependencies { 
        classpath(
            group = "com.github.ptkltm.development.recursiveinclude.gradleplugin", 
            name = "com.github.ptkltm.development.recursiveinclude.gradleplugin",
            version = "0.4.0"
        )
    }
}

apply {
    plugin("com.github.ptkltm.development.recursiveinclude")
}
                    """.trimIndent().toByteArray()
                )
            }
        )

        // /exampleplatform
        val examplePlatformSubDirectory = rootProjectDirectoryPath.resolve("exampleplatform")

        val validSubProjectName = "com.example.exampleplatform.javaapi"

        // Initializes a valid sub project './com.example/exampleplatform/com.example.exampleplatform.javaapi'.
        // => Build should be included.
        val validSubBuildDirectoryPath = examplePlatformSubDirectory.initializeGradleProject(
            projectDirectoryName = validSubProjectName,
            buildGradleFileName = subProjectBuildGradleName,
            buildGradleContent = subProjectBuildGradleContent,
            additionalProjectInitialization = additionalSubProjectInitialization
        )

        // Initializes a sub project inside of an hidden folder '/com.example/exampleplatform/.hidden'.
        // => Build should be ignored.
        val hiddenSubBuildDirectoryPath = examplePlatformSubDirectory.initializeGradleProject(
            projectDirectoryName = ".hidden",
            buildGradleFileName = subProjectBuildGradleName,
            buildGradleContent = subProjectBuildGradleContent,
            additionalProjectInitialization = additionalSubProjectInitialization
        )

        // Initialize a sub project inside a directory called 'build' ('/com.example/exampleplatform/build').
        // => Build should be ignored.
        val subBuildInBuildDirectoryPath = examplePlatformSubDirectory.initializeGradleProject(
            projectDirectoryName = Project.DEFAULT_BUILD_DIR_NAME,
            buildGradleFileName = subProjectBuildGradleName,
            buildGradleContent = subProjectBuildGradleContent,
            additionalProjectInitialization = additionalSubProjectInitialization
        )

        // Initialize the GradleRunner with the root project's directory './com.example'.
        val gradleRunner = GradleRunner.create().withProjectDir(rootProjectDirectoryPath.toFile())

        // 2. Executes the 'build' task at the root directory.
        val buildResult = gradleRunner.withArguments(BUILD_TASK_NAME).build()

        // 3. Check that the execution of the 'build' task was successful.
        assertEquals(
            expected = TaskOutcome.SUCCESS,
            actual = buildResult.task("${Project.PATH_SEPARATOR}$BUILD_TASK_NAME")?.outcome,
            message = "The execution of the '$BUILD_TASK_NAME' task was not successful."
        )

        // 4. Verifies that a file 'com.example.exampleplatform.javaapi.txt' was generated in the root path
        // './com.example/exampleplatform/com.example.exampleplatform.javaapi/build'.
        assertTrue(
            actual = File("${validSubBuildDirectoryPath.toAbsolutePath()}${File
                    .separatorChar}build${File.separatorChar}$validSubProjectName.txt").exists(),
            message = "The file '$validSubProjectName.txt' doesn't exist."
        )

        // 5. Checks that no folders './com.example/exampleplatform/.hidden/build' and
        // './com.example/exampleplatform/build/build' were
        // generated by the 'build' tasks in the files './com.example/exampleplatform/.hidden/build.gradle(.kts)' and
        // './com.example/exampleplatform/build/build.gradle(.kts)', because one project is located in a hidden folder
        // starting with '.' and the other one is located in a folder called 'build'.
        listOf(hiddenSubBuildDirectoryPath, subBuildInBuildDirectoryPath).forEach {
            assertFalse(
                actual = File("${it.toAbsolutePath()}${File
                        .separatorChar}${Project.DEFAULT_BUILD_DIR_NAME}").exists(),
                message = "The task '$BUILD_TASK_NAME' of the folder '${it
                        .toAbsolutePath()}' was executed."
            )
        }

        // 6. Executes the 'clean' task at the root directory.
        val cleanResult = gradleRunner.withArguments(CLEAN_TASK_NAME).build()

        // 7. Verify that the execution of the 'clean' task was successful.
        assertEquals(
            expected = TaskOutcome.SUCCESS,
            actual = cleanResult.task("${Project.PATH_SEPARATOR}$CLEAN_TASK_NAME")?.outcome,
            message = "The execution of the '$CLEAN_TASK_NAME' task was not successful."
        )

        // 8. Verifies that the directory './com.example/exampleplatform/com.example.exampleplatform.javaapi/build' was
        // deleted.
        assertFalse(
            actual = File("${validSubBuildDirectoryPath.toAbsolutePath()}${File
                    .separatorChar}${Project.DEFAULT_BUILD_DIR_NAME}").exists(),
            message = "The directory '${Project.DEFAULT_BUILD_DIR_NAME}' was not deleted."
        )
    }

    /**
     * Initializes a sub project with a settings.gradle(.kts) and a build.gradle(.kts) file.
     *
     * @receiver The path of the parent directory.
     * @param [projectDirectoryName] The name of the project directory and the project.
     * @param [buildGradleFileName] build.gradle (for Groovy syntax) or settings.gradle.kts (for Kotlin syntax).
     * @param [buildGradleContent] The content of the build.gradle(.kts) file.
     * @param [additionalProjectInitialization] Closure for additional initialization of the sub projects.
     * @return The path to the initialized project.
     */
    private
    fun Path.initializeGradleProject(
        projectDirectoryName: String,
        buildGradleFileName: String,
        buildGradleContent: String,
        additionalProjectInitialization: (Path) -> Unit
    ): Path {
        // /${parentDirectoryPath}/${projectDirectoryName}
        val projectPath = resolve(projectDirectoryName)
        Files.createDirectories(projectPath)

        // /${parentDirectoryPath}/${projectDirectoryName}/build.gradle(.kts)
        Files.write(
            projectPath.resolve(buildGradleFileName),
            buildGradleContent.toByteArray()
        )

        // Additional initialization of the project
        additionalProjectInitialization(projectPath)

        return projectPath
    }
}