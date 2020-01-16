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

import com.jfrog.bintray.gradle.BintrayExtension.GpgConfig
import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig
import java.net.URL
import java.util.Date
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
     * Plugin for publishing Maven artifacts.
     */
    `maven-publish`

    /**
     * Gradle plugin for publishing Maven artifacts to
     * the Bintray repository.
     */
    id("com.jfrog.bintray") version "1.8.4"

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
     * License manager for testing and applying the content of a 'LICENSE-HEADER' file
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
 * Sets the name of the project group to the name of the project.
 */
group = name

/**
 * Sets the description of the project.
 */
description = "Plugin for recursively applying sub projects and sub builds."

/**
 * Sets the version of the project.
 */
version = "0.4.0"

/**
 * The labels of the project.
 */
val projectLabels = listOf("automation", "recursiveinclude", "subprojects", "subbuilds", "compositebuilds", "build")

/**
 * The name of the publication for the ´maven-publish´ plugin that is referenced by the 'bintray' configuration
 * of the 'com.jfrog.bintray' plugin.
 */
val publicationName = "pluginMaven"

/**
 * Configures the repositories.
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
     * Extracts the value for the key 'junitVersion' from the projects's properties
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
 * Configures the dependencies of the project.
 */
dependencies {
    /**
     * Test dependency to the JUnit 5 Jupiter supported framework for Kotlin.
     */
    testImplementation(dependencyNotation = kotlin(module = "test-junit5"))

    /**
     * Extracts the value for the key 'junitVersion' from the project's properties
     * defined in the file 'gradle.properties'.
     */
    val junitVersion: String by project

    /**
     * Runtime dependency to the JUnit 5 Jupiter engine, that's needed for
     * the execution of the 'test' task with the [Test.useJUnitPlatform]
     * configuration.
     */
    testRuntimeOnly(
        group = "org.junit.jupiter",
        name = "junit-jupiter-engine",
        version = junitVersion
    )
}

/**
 * If the Gradle 'wrapper' task is executed, the generated 'gradlew' file is extended
 * by additional shell code for the download of AdoptOpenJDK 8u202-b08 if no 'JAVA_HOME' is
 * already defined.
 */
jdk {
    useAdoptOpenJdk8("jdk8u202-b08")
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
    /**
     * Extracts the value for the key 'repositoryUrl' from the projects's properties
     * defined in the file 'gradle.properties'.
     */
    val repositoryUrl: String by project
    website = repositoryUrl
    vcsUrl = repositoryUrl
    description = project.description
    tags = projectLabels

    plugins {
        maybeCreate("recursiveIncludePlugin").apply {
            displayName = project.findProperty("projectDisplayName").toString()
        }
    }

    /**
     * Removes the 'gradle.plugin' prefix from the group of the Maven artifact
     * of the plugin.
     */
    mavenCoordinates {
        groupId = project.group.toString()
        artifactId = project.name
        version = project.version.toString()
    }
}

/**
 * Configures the metadata of the pom.xml for the Maven publication.
 */
publishing {
    publications {
        create(publicationName, MavenPublication::class) {
            pom {
                /**
                 * Extracts the value for the key 'projectDisplayName' from the projects's properties
                 * defined in the file 'gradle.properties'.
                 */
                val projectDisplayName: String by project

                /**
                 * Extracts the value for the key 'repositoryUrl' from the projects's properties
                 * defined in the file 'gradle.properties'.
                 */
                val repositoryUrl: String by project

                /**
                 * Extracts the value for the key 'developerName' from the projects's properties
                 * defined in the file 'gradle.properties'.
                 */
                val developerName: String by project

                /**
                 * Extracts the value for the key 'developerEmail' from the projects's properties
                 * defined in the file 'gradle.properties'.
                 */
                val developerEmail: String by project

                name.set(projectDisplayName)
                description.set(project.description)
                inceptionYear.set("2019")
                organization {
                    name.set(developerName)
                    url.set("https://github.com/ptkltm")
                }
                developers {
                    developer {
                        name.set(developerName)
                        email.set(developerEmail)
                    }
                }
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("$repositoryUrl/issues")
                }
                scm {
                    url.set(repositoryUrl)
                    tag.set(project.version.toString())
                }
            }

            afterEvaluate {
                val publishPluginJar by tasks.getting(Jar::class)
                val publishPluginJavaDocsJar by tasks.getting(Jar::class)

                artifact(publishPluginJar)
                artifact(publishPluginJavaDocsJar)
            }
        }
    }
}

/**
 * Configuration of the plugin publication to the Bintray repository.
 * If the plugin should be published to Bintray the Gradle task 'bintrayUpload' must be executed with the
 * command line parameters
 * '-PisReleasePublication=true -PbintrayUser=<username> -PbintrayKey=<apikey> -PgpgPassphrase=<passphrase>'.
 *
 */
bintray {
    /**
     * Extracts a bintray value from either the property in the gradle.properties.kts file
     * or the environment variable.
     *
     * For the properties in the gradle.properties.kts file camel-case is used (e. g. 'bintrayUser' for 'user')
     * and for environment variables upper snake-case is used (e. g. 'BINTRAY_USER' for 'user').
     *
     * @receiver The key being prefixed with either 'bintray' for the build.gradle.kts properties
     * or 'BINTRAY_' for the environment variable approach.
     */
    fun String.extractValue(): String? {
        val propertyName = "bintray${capitalize()}"
        return if (hasProperty(propertyName))
            property(propertyName)?.toString()
        else
            System.getenv("BINTRAY_${toUpperCase()}")
    }

    user = "user".extractValue()
    key = "key".extractValue()
    setPublications(publicationName)

    /**
     * Extracts the value for the key 'isReleasePublication' from the project's properties
     * defined in the file 'gradle.properties'.
     */
    val isReleasePublication: String by project
    publish = "true" == isReleasePublication

    pkg(closureOf<PackageConfig> {
        name = project.name
        repo = name.substringBeforeLast(delimiter = '.')
                .substringBeforeLast(delimiter = '.')
        version(closureOf<VersionConfig> {
            name = project.version.toString()
            desc = project.description
            released = Date().toString()
            vcsTag = name
            gpg(closureOf<GpgConfig> {
                if (publish) {
                    /**
                     * Extracts the value for the key 'gpgPassphrase' from the project's properties
                     * defined in the file 'gradle.properties'.
                     */
                    val gpgPassphrase: String by project
                    passphrase = gpgPassphrase
                }
                sign = true
            })
        })
        setLabels(*listOf("gradle", "gradleplugin").union(other = projectLabels).toTypedArray())
        setLicenses("Apache-2.0")

        /**
         * Extracts the value for the key 'repositoryUrl' from the projects's properties
         * defined in the file 'gradle.properties'.
         */
        val repositoryUrl: String by project

        vcsUrl = "$repositoryUrl.git"
        githubRepo = repositoryUrl
        githubReleaseNotesFile = "README.md"
        websiteUrl = repositoryUrl
        issueTrackerUrl = "$repositoryUrl/issues"
        publicDownloadNumbers = true
    })
}

/**
 * Checks that the content of the 'LICENSE-HEADER' file is applied to all
 * recursive files with the *.java and *.kt extensions and the files
 * build.gradle.kts, settings.gradle.kts and gradle.properties at the
 * root level of the project.
 */
license {
    header = file("LICENSE-HEADER")
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
 * Configures the tasks of the project.
 */
tasks {
    /**
     * Configuration of all Kotlin compile tasks to use the jvmTarget 1.8.
     */
    withType(KotlinCompile::class) {
        kotlinOptions {
            /**
             * Extracts the value for the key 'kotlinJvmTarget' from the project's properties
             * defined in the file 'gradle.properties'.
             */
            val kotlinJvmTarget: String by project
            jvmTarget = kotlinJvmTarget
        }
    }

    /**
     * Configures all Java compile tasks to use Java 1.8.
     */
    withType(JavaCompile::class) {
        val javaVersion = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = javaVersion
        sourceCompatibility = javaVersion
    }

    /**
     * Enables Junit 5 Jupiter during the test runtime.
     */
    "test"(Test::class) {
        useJUnitPlatform()
    }

    /**
     * Configures the Gradle wrapper with the version '6.1'.
     */
    "wrapper"(Wrapper::class) {
        gradleVersion = "6.1"
    }
}

/**
 * Closure for the initialization of the Gradle project after it's fully
 * evaluated.
 */
afterEvaluate {
    /**
     * Configures the tasks of the project.
     */
    tasks {
        /**
         * Disables the 'javadoc' task.
         */
        val javadoc by getting(Javadoc::class) {
            enabled = false
        }

        /**
         * Produces the javadoc documentation for the Kotlin files inside the
         * output directory of the disabled 'javadoc' task.
         */
        val dokka by getting(DokkaTask::class) {
            outputFormat = "javadoc"
            outputDirectory = javadoc.destinationDir?.absolutePath ?: ""
            configuration {
                externalDocumentationLink {
                    val externalGradleJavaDocUrl = "https://docs.gradle.org/current/javadoc/"
                    url = URL(externalGradleJavaDocUrl)
                    packageListUrl = URL("${externalGradleJavaDocUrl}package-list")
                }
                skipDeprecated = true
                skipEmptyPackages = true
                reportUndocumented = true
            }
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