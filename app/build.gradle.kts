plugins {
    id("com.android.application")
}

android {
    namespace = "com.dshmobile.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dshmobile.app"
        minSdk = 26
        targetSdk = 28
        versionCode = 20
        versionName = "1.0.19"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // 未提供正式 keystore 时先用调试签名,保证 Actions 开箱即出可安装 APK。
            // 正式发布请改用自己的 keystore(见 README)。
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("org.apache.commons:commons-compress:1.26.2")
    implementation("org.apache.commons:commons-io:2.18.0")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("org.tukaani:xz:1.9")
}