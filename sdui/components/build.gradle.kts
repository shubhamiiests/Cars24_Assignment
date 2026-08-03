plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.cars24.sdui.components"
    compileSdk { version = release(libs.versions.compileSdk.get().toInt()) }
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures { compose = true }
}

dependencies {
    api(project(":sdui:runtime"))

    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
}
