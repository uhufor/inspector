import com.vanniktech.maven.publish.AndroidMultiVariantLibrary
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.vanniktech.maven.publish") version "0.32.0"
}

group = "io.github.uhufor"
version = (project.findProperty("mavenPublishVersion") as String? ?: "SNAPSHOT")

android {
    namespace = "com.uhufor.inspector"

    compileSdk = 35
    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.android)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.material3)
    implementation(libs.google.ui.material)
}

mavenPublishing {
    coordinates(
        groupId = "io.github.uhufor",
        artifactId = project.name,
        version = version.toString()
    )

    configure(
        AndroidMultiVariantLibrary(
            sourcesJar = true,
            publishJavadocJar = true,
        )
    )

    pom {
        name.set("Android UI Inspector - ${project.name}")
        description.set("A powerful Android UI inspection tool - ${project.name}")
        inceptionYear.set("2025")
        url.set("https://github.com/uhufor/inspector")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("uhufor")
                name.set("Hae Jung, Kim")
                url.set("https://github.com/uhufor/")
            }
        }
        scm {
            url.set("https://github.com/uhufor/inspector")
            connection.set("scm:git:https://github.com/uhufor/inspector.git")
            developerConnection.set("scm:git:git@github.com:uhufor/inspector.git")
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}
