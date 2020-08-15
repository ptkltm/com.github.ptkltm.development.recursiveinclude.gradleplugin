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

/**
 * Custom configuration for the dependency and version management of the Gradle plugins being used inside the 'plugins'
 * dsl of the build.gradle.kts file.
 */
pluginManagement {
    /**
     * Repositories for the dependencies of the plugins defined inside the 'plugins' dsl of the build.gradle.kts file.
     */
    repositories {
        /**
         * Repository that provides artifacts published to the Gradle Plugin Portal.
         */
        gradlePluginPortal()

        /**
         * Defines Bintray's jcenter as repository for Maven artifacts.
         */
        jcenter()
    }

    /**
     * The prefix of the plugin ids inside the 'com.github.ptkltm.development.fullstackproject.gradleplugin' artifact.
     */
    val fullStackProjectGradlePluginIdPrefix = "com.github.ptkltm.development.fullstackproject"

    /**
     * Custom resolution strategy for the 'plugins' dsl in the build.gradle.kts file.
     */
    resolutionStrategy {
        /**
         * If a plugin id inside the 'plugins' block of the build.gradle.kts file starts with
         * 'com.github.ptkltm.development.fullstackproject' an artifact with the group
         * 'com.github.ptkltm.development.fullstackproject.gradleplugin' and the artifact id
         * 'com.github.ptkltm.development.fullstackproject.gradleplugin' is used explicitly.
         */
        eachPlugin {
            if (requested.id.id.startsWith(
                prefix = fullStackProjectGradlePluginIdPrefix
            )) {
                "$fullStackProjectGradlePluginIdPrefix.gradleplugin".let {
                    useModule("$it:$it:${target.version}")
                }
            }
        }
    }
    /**
     * Centralized management for the versions of the Gradle plugins used inside the 'plugins' dsl in the
     * build.gradle.kts file.
     */
    plugins {
        /**
         * Sets the version of the plugin with the id 'com.github.ptkltm.development.fullstackproject.implementation'
         * to '0.1.0'.
         */
        id("$fullStackProjectGradlePluginIdPrefix.implementation") version "0.1.0"
    }
}

/**
 * Closure for defining the repositories and dependencies of the settings.gradle.kts file.
 */
buildscript {
    /**
     * The repositories of the dependencies of the settings.gradle.kts file.
     */
    repositories {
        /**
         * Defines Bintray's jcenter as repository for Maven artifacts.
         */
        jcenter()
    }
    /**
     * The dependencies of the settings.gradle.kts file.
     */
    dependencies {
        /**
         * Classpath dependency to version '0.4.0' of the artifact that provides the Gradle plugin with the id
         * 'com.github.ptkltm.development.recursiveinclude'.
         */
        "com.github.ptkltm.development.recursiveinclude.gradleplugin".let {
            classpath(
                group = it,
                name = it,
                version = "0.4.0"
            )
        }
    }
}

/**
 * Applies the plugin with the id 'com.github.ptkltm.development.recursiveinclude' for setting the project name to the
 * name of the root directory of the 'Implementation' project and automatically including all sub projects.
 */
apply(plugin = "com.github.ptkltm.development.recursiveinclude")
