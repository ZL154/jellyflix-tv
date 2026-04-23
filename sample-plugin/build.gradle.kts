plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

base { archivesName = "jellyflix-sample-plugin-0.1.0" }

android {
    namespace = "com.jellyflix.sample.trending"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jellyflix.sample.trending"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        create("release") {
            val keystorePath = providers.environmentVariable("JELLYFLIX_KEYSTORE").orNull
            if (keystorePath != null && file(keystorePath).exists()) {
                storeFile = file(keystorePath)
                storePassword = providers.environmentVariable("JELLYFLIX_KEYSTORE_PASSWORD").orNull
                keyAlias = providers.environmentVariable("JELLYFLIX_KEY_ALIAS").orNull
                keyPassword = providers.environmentVariable("JELLYFLIX_KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            val hasKeystore = providers.environmentVariable("JELLYFLIX_KEYSTORE").orNull
                ?.let { file(it).exists() } ?: false
            if (hasKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    compileOnly(project(":plugin-api"))
}
