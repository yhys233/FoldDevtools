import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

fun runGitCommand(vararg args: String): String? {
    return try {
        val command = listOf("git") + args
        println("Run $command")

        val process = ProcessBuilder(command)
            .directory(project.rootDir)
            .redirectErrorStream(true)
            .start()

        val result = process.inputStream
            .bufferedReader()
            .use { it.readText().trim() }

        println(result)

        val exitCode = process.waitFor()

        if (exitCode == 0 && result.isNotBlank()) result else null
    } catch (_: Throwable) {
        null
    }
}

fun getGitCommitCount(): Int =
    runGitCommand("rev-list", "--count", "HEAD")?.toIntOrNull() ?: 0

fun getGitShortHash(): String =
    runGitCommand("rev-parse", "--short", "HEAD") ?: "unknown"


android {
    namespace = "io.github.achyuki.folddevtools"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.achyuki.folddevtools"

        minSdk = 26
        targetSdk = 36

        versionCode = getGitCommitCount()
        versionName = getGitShortHash()
    }


    signingConfigs {

        create("release") {

            val keystorePath =
                System.getenv("KEYSTORE_PATH") ?: "keystore.jks"

            storeFile = rootProject.file(keystorePath)

            storePassword =
                System.getenv("KEYSTORE_PASSWORD")

            keyAlias =
                System.getenv("KEY_ALIAS")

            keyPassword =
                System.getenv("KEY_PASSWORD")
        }
    }


    buildTypes {

        release {

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )

            signingConfig =
                signingConfigs["release"]
        }


        debug {

            isMinifyEnabled = false

            signingConfig =
                signingConfigs["release"]
        }
    }


    compileOptions {

        sourceCompatibility =
            JavaVersion.VERSION_21

        targetCompatibility =
            JavaVersion.VERSION_21
    }


    kotlin {

        compilerOptions {

            jvmTarget.set(
                JvmTarget.JVM_21
            )
        }
    }


    buildFeatures {

        compose = true
        aidl = true
        buildConfig = true
    }
}


dependencies {

    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.lifecycle.runtime.ktx)


    implementation(libs.androidx.navigation)

    implementation(libs.compose.preference)


    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.ui)

    implementation(libs.androidx.material3)

    implementation(libs.androidx.material)

    implementation(libs.androidx.material.icons.extended)


    implementation(libs.libsu.core)

    implementation(libs.libsu.service)


    implementation(libs.coil.compose)

    implementation(libs.coil.okhttp)


    compileOnly(libs.xposed.api)
}