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

import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Applies the Gradle plugins used during the build.
 */
plugins {
    /**
     * Plugin for building custom Gradle plugins.
     */
    `java-gradle-plugin`

    /**
     * Gradle plugin for publishing custom Gradle plugins to
     * the Gradle Plugin Portal.
     */
    id("com.gradle.plugin-publish") version "0.10.1"

    /**
     * Plugin for generating a documentation based on the source
     * code comments at the Kotlin files.
     */
    id("org.jetbrains.dokka") version "0.10.0"

    /**
     * Source quality plugin for checking the code style of
     * Kotlin files.
     */
    id("io.gitlab.arturbosch.detekt") version "1.1.1"

    /**
     * Plugin for checking the coding-conventions of *.kt and *.kts files.
     */
    id("org.gradle.kotlin-dsl.ktlint-convention") version "0.4.1"

    /**
     * License manager for testing and applying the content of a 'LICENSE' file
     * at the headers of various files.
     */
    id("net.minecrell.licenser") version "0.4.1"

    /**
     * Plugin for bootstrapping a JDK.
     * If the Gradle 'wrapper' task is executed, additional code is injected
     * to the generated 'gradlew' file that downloads and sets the 'JAVA_HOME'
     * environment variable if no 'JAVA_HOME' variable is already available.
     */
    id("com.github.rmee.jdk-bootstrap") version "1.0.20190725142159"

    /**
     * Plugin for the configuration of the Kotlin infrastructure
     * for building Kotlin code based on the Java Virtual Machine.
     */
    kotlin(module = "jvm") version "1.3.50"
}

/**
 * Make the name of the project group to the name of the project.
 */
group = name

/**
 * Sets the version of the project.
 */
version = "0.1.0"

/**
 * Make the name of the project to the value of the key 'moduleName'
 * in the project's extra properties.
 */
extra["moduleName"] = name

/**
 * Configure the repositories.
 */
repositories {
    /**
     * Repository that provides artifacts published to the Gradle Plugin Portal.
     */
    gradlePluginPortal()
}

/**
 * Closure of the configurations being used by the 'dependencies' block for the separation
 * of source sets.
 */
configurations {
    /**
     * Extract the value for the key 'junitVersion' from the projects's properties
     * defined in the file 'gradle.properties'.
     */
    val junitVersion: String by project

    /**
     * Changes the version of all dependencies with the group 'org.junit.jupiter' in all
     * configurations to the 'junitVersion' project property defined in the gradle.properties file.
     *
     * That's necessary because the 'test-junit5' module of Kotlin has a transitive dependency to
     * 'org.unit.jupiter' dependencies with the version 5.0.0, but the @TempDir annotation being used
     * in a test class is only supported at version 5.4.0.
     */
    all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.junit.jupiter") {
                useVersion(junitVersion)
                because("@TempDir is only supported since JUnit 5.4.0.")
            }
        }
    }
}

/**
 * Configure the dependencies of the project.
 */
dependencies {
    /**
     * Test dependency to the JUnit 5 Jupiter supported framework for Kotlin.
     */
    testImplementation(dependencyNotation = kotlin(module = "test-junit5"))

    /**
     * Extract the value for the key 'junitVersion' from the project's properties
     * defined in the file 'gradle.properties'.
     */
    val junitVersion: String by project

    /**
     * Runtime dependency to the JUnit 5 Jupiter engine, that's needed for
     * the execution of the 'test' task with the [Test.useJUnitPlatform]
     * configuration.
     */
    testRuntime(
        group = "org.junit.jupiter",
        name = "junit-jupiter-engine",
        version = junitVersion
    )
}

/**
 * If the Gradle 'wrapper' task is executed, the generated 'gradlew' file is extended
 * by additional shell code for the download of openJDK 11.0.2 if no 'JAVA_HOME' is
 * already defined.
 */
jdk {
    version = "11.0.2"
    urlTemplate = "https://download.java.net/java/GA/jdk11/9/GPL/openjdk-\${version}_${
        when {
            Os.isFamily(Os.FAMILY_MAC) -> "osx"
            Os.isFamily(Os.FAMILY_WINDOWS) -> "windows"
            else -> "linux"
        }
    }-x64_bin.${if (Os.isFamily(Os.FAMILY_WINDOWS)) "zip" else "tar.gz"}"
}

/**
 * Configuration of the custom Gradle plugin.
 * Automatically generates a Gradle plugin descriptor for the id
 * 'com.github.pktltm.development.recursiveinclude' and the implementation class
 * 'com.github.pktltm.development.recursiveinclude.gradleplugin.RecursiveIncludeGradlePlugin'
 * at the output jar, so that the Gradle plugin system is able to apply to solve the plugin
 * by it's id after it's applied to a build script.
 */
gradlePlugin {
    plugins {
        create("recursiveIncludePlugin") {
            id = project.name.substringBeforeLast(delimiter = '.')
            implementationClass = "${project.name}.RecursiveIncludeGradlePlugin"
        }
    }
}

/**
 * Configuration of the meta data for publishing the Gradle plugin to the
 * Gradle Plugin portal.
 */
pluginBundle {
    val githubProjectUrl = "https://github.com/ptkltm/com.github.ptkltm.development.recursiveinclude.gradleplugin"
    website = githubProjectUrl
    vcsUrl = githubProjectUrl
    description = "Plugin for recursively applying sub projects and composite builds."
    tags = listOf("automation", "recursiveinclude", "subprojects", "subbuilds", "compositebuilds", "build", "gradle")

    plugins {
        maybeCreate("recursiveIncludePlugin").apply {
            displayName = "Recursive Include Plugin"
        }
    }
}

/**
 * Check that the content of the 'LICENSE' file is applied to all
 * recursive files with the *.java and *.kt extensions and the files
 * build.gradle.kts, settings.gradle.kts and gradle.properties at the
 * root level of the project.
 */
license {
    include("**/*.java", "**/*.kt")
    tasks {
        create("gradle") {
            files = project.files(
                "build.gradle.kts",
                "settings.gradle.kts",
                "gradle.properties"
            )
        }
    }
}

/**
 * Configure the tasks of the project.
 */
tasks {
    /**
     * Configure the Gradle wrapper with the version '5.6.3'.
     */
    "wrapper"(Wrapper::class) {
        gradleVersion = "5.6.3"
    }
}

/**
 * Closure for the initialization of the Gradle project after it's fully
 * evaluated.
 */
afterEvaluate {
    /**
     * Obtain the value for the key 'moduleName' from the
     * extra properties of the project.
     */
    val moduleName: String by extra

    /**
     * Configure the task of the project.
     */
    tasks {
        /**
         * Compile Kotlin based on the jvmTarget version.
         */
        withType(KotlinCompile::class.java) {
            kotlinOptions {
                /**
                 * Extract the value for the key 'kotlinJvmTarget' from the project's properties
                 * defined in the file 'gradle.properties'.
                 */
                val kotlinJvmTarget: String by project

                jvmTarget = kotlinJvmTarget
            }
        }
        /**
         * Compile Java using the Jigsaw module path.
         */
        "compileJava"(JavaCompile::class) {
            inputs.property("moduleName", moduleName)
            doFirst {
                options.compilerArgs = listOf(
                    "--module-path", classpath.asPath,
                    "--module-version", version.toString()
                )
                classpath = files()
            }
        }

        /**
         * Enable Junit 5 Jupiter during the test runtime.
         */
        "test"(Test::class) {
            inputs.property("moduleName", moduleName)
            useJUnitPlatform()
        }

        /**
         * Disable the 'javadoc' task.
         */
        val javadoc by getting(Javadoc::class) {
            enabled = false
        }

        /**
         * Produce the html documentation for the Kotlin files inside the
         * output directory of the disabled 'javadoc' task
         */
        val dokka by getting(DokkaTask::class) {
            inputs.property("moduleName", moduleName)
            outputFormat = "html"
            outputDirectory = javadoc.destinationDir?.absolutePath ?: ""
        }

        /**
         * Instead of the output of the disabled 'javadoc' task, the JavaDoc jar
         * now uses the 'dokka' output by depending on the task.
         */
        "publishPluginJavaDocsJar" {
            dependsOn(dokka)
        }
    }
}

/**
 * Declares 'clean' and 'build' as the default task.
 * If './gradlew' is executed on Unix-based systems or 'gradlew' is executed on
 * the Windows operating system, these tasks are executed automatically.
 */
defaultTasks("clean", "build")

/**
 * Configure the destination directory of the 'compileKotlin' task to the
 * destination directory of the 'compileJava' task.
 */
configureKotlinDestinationDir(
    javaCompileTaskName = "compileJava",
    kotlinCompileTaskName = "compileKotlin"
)

/**
 * Configure the destination directory of the 'compileTestKotlin' task to the
 * destination directory of the 'compileTestJava' task.
 */
configureKotlinDestinationDir(
    javaCompileTaskName = "compileTestJava",
    kotlinCompileTaskName = "compileTestKotlin"
)

/**
 * Reusable function for making the output directory of the Java compilation
 * to the output directory of the corresponding Kotlin compile task.
 *
 * Needed for making the Jigsaw module system able to find the .class files
 * based on the *.kt files.
 *
 * @param [javaCompileTaskName] The name of the Java compile task.
 * @param [kotlinCompileTaskName] The name of the Kotlin compile task.
 */
fun configureKotlinDestinationDir(
    javaCompileTaskName: String,
    kotlinCompileTaskName: String
) {
    val compileJava = tasks.getByName(javaCompileTaskName) as JavaCompile

    (tasks.getByName(kotlinCompileTaskName) as KotlinCompile).apply {
        destinationDir = compileJava.destinationDir
    }
}