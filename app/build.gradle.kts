plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
//    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.app.base"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.app.base"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    flavorDimensions += "env"

    productFlavors {
        create("dev") {
            dimension = "env"
            applicationId = "dev.app.base"
        }
        create("prod") {
            dimension = "env"
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
        buildConfig = true
    }

    bundle {
        language {
            enableSplit = false
        }
    }
}

dependencies {
    implementation(project(":base"))
    implementation(project(":ads"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}