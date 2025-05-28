plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

group = "io.github.uhufor"
version = (project.findProperty("maven_publish_version") as String? ?: "0.0.1-SNAPSHOT")

android {
    namespace = "com.uhufor.inspector"

    compileSdk = 35
    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.material3)
    implementation(libs.google.ui.material)
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                from(components["release"])
                artifactId = project.name

                pom {
                    name.set("Android UI Inspection - ${project.name}")
                    description.set("Android UI inspection tool - ${project.name}.")
                    url.set("https://github.com/uhufor/ui_inspection_sample")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("uhufor")
                            name.set("김해중")
                            email.set("haejung83@gmail.com")
                        }
                    }

                    scm {
                        url.set("https://github.com/uhufor/ui_inspection_sample")
                        connection.set("scm:git:https://github.com/uhufor/ui_inspection_sample.git")
                        developerConnection.set("scm:git:git@github.com:uhufor/ui_inspection_sample.git")
                    }
                }
            }
        }

        repositories {
            maven {
                name = "Sonatype"
                url = uri(
                    if (version.toString().endsWith("SNAPSHOT"))
                        "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                    else
                        "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                )
                credentials {
                    username = project.findProperty("maven_publish_username") as String? ?: ""
                    password = project.findProperty("maven_publish_password") as String? ?: ""
                }
            }
        }
    }
}
