import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
}

val localePropsFile = rootProject.file("local.properties")
val localeProperties = Properties().apply {
    load(FileInputStream(localePropsFile))
}
android {
    namespace = "com.example.gmapapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gmapapplication"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Makes MAP_API_KEY available for manifest merging
        manifestPlaceholders += mapOf(
            "MAP_API_KEY" to localeProperties.getProperty("MAP_API_KEY")
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.maps.android:android-maps-utils:3.8.0")
    implementation("androidx.core:core-ktx:1.16.0")

}