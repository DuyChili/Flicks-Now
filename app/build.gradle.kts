plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.flicks_now"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.flicks_now"
        minSdk = 30
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures{
        viewBinding= true;
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.google.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("com.google.android.exoplayer:exoplayer:2.17.1")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("com.google.firebase:firebase-auth:21.0.1")
    implementation ("com.google.firebase:firebase-database:20.0.0")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    //exoplayer
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.4.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation ("com.google.firebase:firebase-messaging:23.0.0")
    implementation ("com.google.android.material:material:1.8.0")
}