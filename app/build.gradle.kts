plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

//buildscript {
//    dependencies {
//        classpath("com.google.android.libraries.mapsplatform.secrets-gradleplugin:secrets-gradle-plugin:2.0.1")
//    }
//}


android {
    namespace = "com.momcilo.postme"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.momcilo.postme"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.0"
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation ("com.google.firebase:firebase-firestore-ktx:24.10.0")
    implementation ("com.firebase:geofire-android-common:3.2.0")
    implementation ("com.firebase:geofire-android:3.1.0")


    // Navigation
    val nav_version = "2.9.3"
    implementation("androidx.navigation:navigation-compose:$nav_version")

    // Location
    implementation("com.google.android.gms:play-services-location:21.3.0")

    //Google Maps
    implementation("com.google.maps.android:maps-compose:4.4.1")
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    //implementation ("com.google.android.gms:play-serviceslocation:21.2.0")

}
