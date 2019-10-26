# com.github.ptkltm.development.recursiveinclude.gradleplugin
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

## Building the source code

- `git clone https://github.com/ptkltm/com.github.ptkltm.development.recursiveinclude.gradleplugin.git`
- `cd com.github.ptkltm.development.recursiveinclude.gradleplugin`
- `./gradlew` (on Linux or macOS) / `gradlew.bat` (on Windows)