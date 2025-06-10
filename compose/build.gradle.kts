import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.publish)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        publishAllLibraryVariants()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    )

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            api(projects.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {}
    }
}

android {
    namespace = "com.dilivva.blueline.compose"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

//mavenPublishing {
//    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, true)
//    val versionTxt = "2.0.0"
//    val isDev = findProperty("env")?.equals("dev") ?: false
//    val version = if (isDev) "1.0.1-SNAPSHOT" else versionTxt
//
//
//    coordinates("com.dilivva.blueline", "compose-builder", version)
//
//    pom{
//        name.set("Blueline Compose Builder")
//        description.set("Create print design using Compose Multiplatform")
//        inceptionYear.set("2025")
//        url.set("https://github.com/Dilivva/Blueline")
//        licenses {
//            license {
//                name.set("MIT License")
//                url.set("https://github.com/Dilivva/Blueline/LICENSE")
//                distribution.set("https://github.com/Dilivva/Blueline/LICENSE")
//            }
//        }
//        developers {
//            developer {
//                name.set("Ayodele Kehinde")
//                url.set("https://github.com/ayodelekehinde")
//                email.set("ayodelekehinde@send24.co")
//                organization.set("Send24")
//            }
//        }
//        scm {
//            url.set("https://github.com/Dilivva/Blueline/")
//            connection.set("scm:git:git://github.com/Dilivva/Blueline.git")
//            developerConnection.set("scm:git:ssh://git@github.com/Dilivva/Blueline.git")
//        }
//    }
//
//    signAllPublications()
//}
