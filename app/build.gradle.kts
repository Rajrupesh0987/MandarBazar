plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.rupesh.mandarbazar"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rupesh.mandarbazar"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity:1.9.3")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    // 🔥 FIREBASE SETUP (BOM का सही इस्तेमाल)
    // BOM वर्शन मैनेज करता है, इसलिए नीचे अलग से वर्शन लिखने की ज़रूरत नहीं है
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")

    // 🔥 GOOGLE LOGIN (सिर्फ एक बार, सही वर्शन के साथ)
    implementation("com.google.android.gms:play-services-auth:21.1.1")

    // 🔥 GLIDE (फोटो लोड करने के लिए)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.google.firebase:firebase-messaging")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}