import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.openapi.generator)
}

val openApiBuildPath = "${layout.buildDirectory.asFile.get().path}/openapi-generator"

openApiGenerate {
    generatorName = "kotlin"
    generateApiDocumentation = true
    generateModelDocumentation = true

    val secrets = Properties().apply {
        load(rootProject.file("secrets.properties").reader())
    }
    remoteInputSpec = secrets["REST_URI"].toString()
    outputDir = openApiBuildPath
}

tasks.withType<KotlinCompile> {
    dependsOn(tasks.openApiGenerate)
}

android {
    namespace = "com.vladislaviliev.meet"
    compileSdk = 36

    sourceSets.getByName("main") {
        java.setSrcDirs(listOf("src/main/java", "$openApiBuildPath/src/main/kotlin"))
    }

    defaultConfig {
        applicationId = "com.vladislaviliev.meet"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.moshi)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
}