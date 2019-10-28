# Recursive Include Plugin
Gradle Plugin for the settings.gradle(.kts) file that automatically includes nested sub builds and sub projects.

## Implementation description

Declares the name of the root directory as project name and recursively searches for 'settings.gradle',
'settings.gradle.kts', 'build.gradle' and 'build.gradle.kts' files in the root directory.

All invisible files (starting with '.') or files contained in a directory called 'build'
are ignored.

If a 'settings.gradle' or 'settings.gradle.kts' file is detected, the relative path to the
root directory of the file is automatically applied as composite build via the composite build api of Gradle.

If no 'settings.gradle' or 'settings.gradle.kts' files are available, but a 'build.gradle'
or 'build.gradle.kts' file was found, the relative path of the root directory of that file
is automatically applied as sub project via the include instruction of Gradle's sub projects api.
The name of the sub project is set to the name of the directory where the 'build.gradle' or 'build.gradle.kts'
file is located.

After one of the four files - 'settings.gradle', 'settings.gradle.kts', 'build.gradle' or
'build.gradle.kts' - was found, the recursive search at the current path is stopped.

## Usage

The plugin with the id 'com.github.ptkltm.development.recursiveinclude' is hosted at the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.github.ptkltm.development.recursiveinclude) and can be applied via the following configurations to either a **settings.gradle.kts** or a **settings.gradle** file.

- Configuration of a **settings.gradle.kts** file:

```kotlin
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath(
            group = "gradle.plugin.com.github.ptkltm.development.recursiveinclude.gradleplugin",
            name = "com.github.ptkltm.development.recursiveinclude.gradleplugin",
            version = "0.2.0"
        )
    }
}

apply(plugin = "com.github.ptkltm.development.recursiveinclude")
```

- Configuration of a **settings.gradle** file:

```groovy
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath 'gradle.plugin.com.github.ptkltm.development.recursiveinclude.gradleplugin:com.github.ptkltm.development.recursiveinclude.gradleplugin:0.2.0'
    }
}

apply plugin: 'com.github.ptkltm.development.recursiveinclude'
```

## Building the source code

- `git clone https://github.com/ptkltm/com.github.ptkltm.development.recursiveinclude.gradleplugin.git`
- `cd com.github.ptkltm.development.recursiveinclude.gradleplugin`
- `./gradlew` (on Linux or macOS) / `gradlew.bat` (on Windows)

## License information

   Copyright [yyyy] [name of copyright owner]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.