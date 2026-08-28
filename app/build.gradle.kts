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
        versionCode = 4
        versionName = "0.3.0"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("dev-debug.keystore")
            storePassword = "bigbuttons"
            storeType = "pkcs12"
            keyAlias = "bigbuttons-debug"
            keyPassword = "bigbuttons"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
