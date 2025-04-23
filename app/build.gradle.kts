plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  kotlin("plugin.serialization") version(libs.versions.kotlin)
  id("kotlin-kapt")
}

android {
  namespace = "xyz.jdynb.dymovies"
  compileSdk = 34

  defaultConfig {
    applicationId = "xyz.jdynb.dymovies"
    minSdk = 24
    targetSdk = 34
    versionCode = 7
    versionName = "1.0.5"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    signingConfig = signingConfigs.getByName("debug")

    ndk {
      abiFilters.add("arm64-v8a")
      abiFilters.add("x86_64")
    }
  }

  signingConfigs {
    getByName("debug") {
      enableV1Signing = true
      enableV2Signing = true
      enableV3Signing = true
      storeFile = File(properties["APP_SIGN_STORE_FILE"] as String)
      storePassword = properties["APP_SIGN_STORE_PASSWORD"] as String
      keyAlias = properties["APP_SIGN_KEY_ALIAS"] as String
      keyPassword = properties["APP_SIGN_KEY_PASSWORD"] as String
    }
    create("release") {
      enableV1Signing = true
      enableV2Signing = true
      enableV3Signing = true
      storeFile = File(properties["APP_SIGN_STORE_FILE"] as String)
      storePassword = properties["APP_SIGN_STORE_PASSWORD"] as String
      keyAlias = properties["APP_SIGN_KEY_ALIAS"] as String
      keyPassword = properties["APP_SIGN_KEY_PASSWORD"] as String
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
  }
  buildFeatures {
    viewBinding = true
    dataBinding = true
  }
}

dependencies {

  implementation(fileTree("libs"))
  implementation(project(":dyplayer"))
  implementation(project(":video-cache"))

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.material)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.brv)
  implementation(libs.danmakuFlameMaster)
  implementation(libs.okhttp)
  implementation(libs.net)
  implementation(libs.kotlin.serialization.json)
  implementation(libs.glide)
  implementation(libs.litepal)
  implementation(libs.uaoan.dlna)
  implementation(libs.preference)
  implementation(libs.flexbox)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.android)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
}