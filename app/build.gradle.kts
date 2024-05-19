
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.yunren"
    compileSdk = 34
    buildFeatures {
        buildConfig= true
    }
    defaultConfig {
        applicationId = "com.example.yunren"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "is_pro", "false")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("boolean", "is_pro", "true")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        // 添加 -Xlint:deprecation 选项
        val compilerArgs = mutableListOf("-Xlint:deprecation")
        compilerArgs.addAll(compilerArgs)
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
    implementation("com.squareup.picasso:picasso:2.71828")
}