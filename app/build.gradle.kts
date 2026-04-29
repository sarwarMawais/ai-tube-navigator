plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.londontubeai.navigator"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.londontubeai.navigator"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // TfL API base URL
        buildConfigField("String", "TFL_BASE_URL", "\"https://api.tfl.gov.uk/\"")

        // Google Maps API Key (set in local.properties as MAPS_API_KEY=your_key)
        val localProperties = providers.fileContents(rootProject.layout.projectDirectory.file("local.properties"))
        val mapsApiKey = localProperties.asText.map { it.split("\n").find { it.startsWith("MAPS_API_KEY=") }?.substringAfter("=")?.trim() ?: "" }.get()
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey

        // OpenWeatherMap API Key (set in local.properties as WEATHER_API_KEY=your_key)
        val weatherApiKey = localProperties.asText.map { it.split("\n").find { it.startsWith("WEATHER_API_KEY=") }?.substringAfter("=")?.trim() ?: "" }.get()
        buildConfigField("String", "WEATHER_API_KEY", "\"$weatherApiKey\"")

        // TfL API Key (optional, set in local.properties as TFL_APP_KEY=your_key for higher rate limits)
        val tflAppKey = localProperties.asText.map { it.split("\n").find { it.startsWith("TFL_APP_KEY=") }?.substringAfter("=")?.trim() ?: "" }.get()
        buildConfigField("String", "TFL_APP_KEY", "\"$tflAppKey\"")
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
        debug {
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
}

// Workaround for intermittent AGP/Gradle snapshotting failure on debug native strip task:
// :app:stripDebugDebugSymbols -> NoSuchFileException for stripped_native_libs/x86/*.so
// This only affects debug packaging in this project, so we disable that task in debug.
tasks.matching { it.name == "stripDebugDebugSymbols" }.configureEach {
    enabled = false
}

dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-perf")

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)

    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.animation)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Navigation
    implementation(libs.navigation.compose)

    // Lifecycle
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Coroutines
    implementation(libs.coroutines.android)

    // Image Loading
    implementation(libs.coil.compose)

    // Google Maps & Location
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation("com.google.android.gms:play-services-location:21.1.0")

    // TensorFlow Lite (on-device ML)
    implementation(libs.tflite.runtime)
    implementation(libs.tflite.support)

    // DataStore Preferences
    implementation(libs.datastore.preferences)

    // Google Play Billing
    implementation(libs.billing)

    // Google Play In-App Review
    implementation(libs.play.review)

    // Splash Screen
    implementation(libs.splashscreen)

    // WorkManager
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // Testing
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("io.mockk:mockk:1.13.12")
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
}
