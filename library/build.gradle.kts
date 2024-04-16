import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("com.vanniktech.maven.publish").version("0.25.3")
}

kotlin {
    applyDefaultHierarchyTemplate()
    androidTarget {
        publishAllLibraryVariants()
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "BlueLine"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines)
            implementation(libs.korim)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.startup)
            implementation(libs.androidx.appcompat)
        }
    }
}

android {
    namespace = "com.dilivva.blueline"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
@Suppress("UnstableApiUsage")
mavenPublishing {
    publishToMavenCentral(SonatypeHost.S01, true)
    val versionTxt = "0.0.1"
    val isDev = findProperty("env")?.equals("dev") ?: false
    val version = if (isDev) versionTxt.plus("-SNAPSHOT") else versionTxt


    coordinates("com.dilivva", "blueline", version)

    pom{
        name.set("Blueline")
        description.set("Blueline: Kotlin multiplatform library for Bluetooth printer integration")
        inceptionYear.set("2024")
        url.set("https://github.com/Dilivva/Blueline")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://github.com/Dilivva/Blueline/LICENSE")
                distribution.set("https://github.com/Dilivva/Blueline/LICENSE")
            }
        }
        developers {
            developer {
                name.set("Ayodele Kehinde")
                url.set("https://github.com/ayodelekehinde")
                email.set("ayodelekehinde@send24.co")
                organization.set("Send24")
            }
        }
        scm {
            url.set("https://github.com/Dilivva/Blueline/")
            connection.set("scm:git:git://github.com/Dilivva/Blueline.git")
            developerConnection.set("scm:git:ssh://git@github.com/Dilivva/Blueline.git")
        }
    }

    signAllPublications()
}
