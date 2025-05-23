plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-kapt")
}

android {
    namespace = "com.braly.ads"
    compileSdk = 35

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.navigation.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    api("com.google.code.gson:gson:2.10.1")
//    api("com.google.firebase:firebase-ads:21.6.0")
    api("io.appmetrica.analytics:analytics:7.9.0")
    api("com.google.firebase:firebase-analytics:21.6.1")
    api("androidx.media3:media3-exoplayer:1.3.1")
    api("com.google.firebase:firebase-config:21.6.3")
    api("com.google.android.gms:play-services-ads:23.0.0")
//    api("io.insert-koin:koin-androidx-viewmodel:3.5.3")
    api("io.insert-koin:koin-android:3.5.3")
    api("com.google.android.ump:user-messaging-platform:3.1.0")
    api("com.airbnb.android:lottie:6.4.0")
    api("androidx.navigation:navigation-fragment-ktx:2.7.7")
    api("androidx.navigation:navigation-ui-ktx:2.7.7")
    api("com.tbuonomo:dotsindicator:4.3")
    api("androidx.recyclerview:recyclerview:1.3.2")
    api("com.github.bumptech.glide:glide:4.16.0")
    api("com.squareup.moshi:moshi:1.15.0")
    api("com.squareup.moshi:moshi-kotlin:1.15.0")
    api("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
    api("com.google.android.gms:play-services-ads:23.0.0")
}