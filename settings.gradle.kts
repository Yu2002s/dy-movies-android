pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
    maven { setUrl("https://maven.aliyun.com/repository/public/") }
    maven {
      setUrl("http://4thline.org/m2")
      isAllowInsecureProtocol = true
    }
  }
}

rootProject.name = "DyMovies"
include(":app")
include(":dyplayer")
include(":video-cache")
