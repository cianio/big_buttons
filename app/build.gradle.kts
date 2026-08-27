plugins {
    id("com.android.application")
}

android {
    namespace = "com.bigbuttons.remote"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bigbuttons.remote"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "0.2.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
