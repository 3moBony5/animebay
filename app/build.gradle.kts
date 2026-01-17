plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}

android {
    namespace = "io.animebay.stream"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.animebay.stream"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // --- المكتبات الأساسية لأندرويد ---
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")

    // --- مكتبات Jetpack Compose ---
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

    // --- مكتبات سحب البيانات والشبكة ---
    implementation("org.jsoup:jsoup:1.17.2")
    
    // --- مكتبة Coil لتحميل الصور ---
    implementation("io.coil-kt:coil-compose:2.6.0")
    
    // --- مكتبة التنقل ---
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // --- مكتبات المشغل ExoPlayer ---
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-session:1.3.1")
        
    // --- مكتبة OkHttp ---
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // --- مكتبة WebView ---
    implementation("androidx.webkit:webkit:1.11.0")

    // --- مكتبات Firebase ---
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // ✅ --- المكتبة الجديدة والمهمة لتسجيل الدخول عبر جوجل --- ✅
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    
    //مكتبة Shimmer
    implementation("com.valentinilk.shimmer:compose-shimmer:1.2.0")
    
    // --- مكتبات أخرى ---
    implementation("com.cloudinary:cloudinary-android:2.4.0")
    implementation("com.github.dhaval2404:imagepicker:2.1")
    
    // --- مكتبات الاختبار ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
