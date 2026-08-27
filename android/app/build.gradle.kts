plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "ing.ipcheck.netscope"
    compileSdk = 35

    defaultConfig {
        applicationId = "ing.ipcheck.netscope"
        minSdk = 26
        targetSdk = 35
        // v1.0.18 adds editable connectivity targets, ASN/MAC lookup and official status summaries.
        versionCode = providers.gradleProperty("versionCode").orNull?.toIntOrNull() ?: 1018
        versionName = providers.gradleProperty("versionName").orNull ?: "1.0.18"
    }

    val releaseStorePath = providers.gradleProperty("signingStoreFile").orNull
    signingConfigs {
        create("release") {
            if (releaseStorePath != null) {
                storeFile = file(releaseStorePath)
                storePassword = providers.gradleProperty("signingStorePassword").orNull
                keyAlias = providers.gradleProperty("signingKeyAlias").orNull
                keyPassword = providers.gradleProperty("signingKeyPassword").orNull
            }
        }
    }

    buildTypes {
        getByName("release") {
            if (releaseStorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
