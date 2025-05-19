plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.uhufor.inspector"

    compileSdk = 35
    defaultConfig { minSdk = 28 }
}
