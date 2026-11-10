plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.buuzz.docscanar"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.buuzz.docscanar"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // ponytail: APK rilis ditandatangani kunci debug supaya bisa dipasang
            // sendiri. Ganti dengan keystore rilis sebelum dibagikan.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/DEPENDENCIES",
                "/META-INF/LICENSE*",
                "/META-INF/NOTICE*",
            )
        }
    }

    androidResources {
        // PdfBox: cmap CJK dan TTF fallback tidak dipakai; kita hanya memakai
        // Helvetica (standard 14) dengan encoding WinAnsi.
        ignoreAssetsPatterns += listOf("cmap", "LiberationSans-Regular.ttf")
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
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.opencv)
    implementation(libs.mlkit.text)
    implementation(libs.pdfbox) {
        // BouncyCastle hanya dipakai PdfBox untuk PDF terenkripsi; kita tidak pernah membuatnya.
        exclude(group = "org.bouncycastle")
    }
    implementation(libs.coroutines.play.services)

    testImplementation(libs.junit)
}
