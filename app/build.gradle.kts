/*
 * Copyright 2024 David Takač
 *
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.davidtakac.bura"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.davidtakac.bura"
        minSdk = 28
        targetSdk = 35
        versionCode = 17
        versionName = "1.8.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resourceConfigurations.addAll(
            listOf(
                "en",
                "es",
                "fr",
                "hr",
                "vi",
                "zh-rCN",
                "de",
                "ru",
                "pl",
                "sv",
                "nl",
                "ar",
                "zh-rTW",
                "cs",
                "pt-rBR"
            )
        )
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
        // Source: https://developer.android.com/jetpack/compose/performance/stability/fix#configuration-file
        // Replaced project.absolutePath with rootDir.
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:stabilityConfigurationPath=$rootDir/compose-stability-conf.txt"
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
