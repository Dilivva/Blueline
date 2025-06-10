import org.jetbrains.kotlin.gradle.dsl.JvmTarget

//import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.publish)
}

kotlin {
    applyDefaultHierarchyTemplate()
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
    ).forEach {
        it.binaries.framework {
            baseName = "BlueLine"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines)
            implementation(libs.korim)
            api(projects.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {

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
//mavenPublishing {
//    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, true)
//    val versionTxt = "2.0.0"
//    val isDev = findProperty("env")?.equals("dev") ?: false
//    val version = if (isDev) "1.0.1-SNAPSHOT" else versionTxt
//
//
//    coordinates("com.dilivva.blueline", "basic-builder", version)
//
//    pom{
//        name.set("Blueline Basic")
//        description.set("Design print using primitive commands")
//        inceptionYear.set("2024")
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
