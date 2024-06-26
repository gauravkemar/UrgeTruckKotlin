pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven  ("https://jitpack.io")
        jcenter()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven  ("https://jitpack.io")
        jcenter()
    }
}

rootProject.name = "UrgeTruckKotlin"
include(":app")
include (":RFIDAPI3Library")
