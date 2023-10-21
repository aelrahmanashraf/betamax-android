import java.util.Properties

plugins {
    Libraries.plugins.forEach(::id)
}

val localProperties = loadProperties(rootProject.file("local.properties"))
val keystoreProperties = loadProperties(rootProject.file("keystore.properties"))

@Suppress("UnstableApiUsage")
android {
    defaultConfig {
        applicationId = "app.betamax.android"
        minSdk = 24
        compileSdk = 33
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SECRET_API_KEY", "\"${localProperties.getProperty("SECRET_API_KEY")}\"")
        buildConfigField("String", "BASE_URL", "\"${localProperties.getProperty("BASE_URL")}\"")
        buildConfigField("String", "ONESIGNAL_APP_ID", "\"${localProperties.getProperty("ONESIGNAL_APP_ID")}\"")
    }

    signingConfigs {
        create("release") {
            if (properties["STORE_FILE"] != null) {
                storeFile = rootProject.file(properties["STORE_FILE"] as String)
                storePassword = properties["STORE_PASSWORD"] as String
                keyAlias = properties["KEY_ALIAS"] as String
                keyPassword = properties["KEY_PASSWORD"] as String
            }
        }
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "PAYPAL_CLIENT_ID", "\"${localProperties.getProperty("PAYPAL_SANDBOX_CLIENT_ID")}\"")
            buildConfigField("String", "PAYPAL_SECRET_KEY", "\"${localProperties.getProperty("PAYPAL_SANDBOX_SECRET_KEY")}\"")
        }
        getByName("release") {
            buildConfigField("String", "PAYPAL_CLIENT_ID", "\"${localProperties.getProperty("PAYPAL_CLIENT_ID")}\"")
            buildConfigField("String", "PAYPAL_SECRET_KEY", "\"${localProperties.getProperty("PAYPAL_SECRET_KEY")}\"")

            isMinifyEnabled = true
            signingConfig = signingConfigs["release"]
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    Libraries.implementations.forEach(::implementation)
    Libraries.kapts.forEach(::kapt)
    Libraries.testImplementations.forEach(::testImplementation)
    Libraries.androidTestImplementations.forEach(::androidTestImplementation)
    // firebase bom
    val firebaseVersion = "30.4.1"
    implementation(platform("com.google.firebase:firebase-bom:${firebaseVersion}"))
    // external libs
    implementation(fileTree(mapOf(
        "dir" to "libs",
        "include" to listOf("*.aar", "*.jar"),
        "exclude" to listOf("mock.jar")
    )))
}

fun loadProperties(file: File): Properties {
    val properties = Properties()
    try {
        file.inputStream().use { inputStream ->
            properties.load(inputStream)
        }
    } catch (e: java.io.FileNotFoundException) {
        println("Warning: ${file.name} file not found.")
    } catch (e: java.io.IOException) {
        println("Warning: Error loading ${file.name} file.")
    }
    return properties
}