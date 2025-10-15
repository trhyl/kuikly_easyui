pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        maven {
            url =
                uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven {
            url =
                uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
        }
    }
}

rootProject.name = "kuiklyeasyui"

val buildFileName = "build.ohos.gradle.kts"
rootProject.buildFileName = buildFileName

include(":androidApp")
include(":easyui")
project(":easyui").buildFileName = buildFileName