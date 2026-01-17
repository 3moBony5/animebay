plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}

android {
    namespace = "io.animebay.stream"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.animebay.stream"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // تحسين الأداء
        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        
        debug {
            isMinifyEnabled = false
        }
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,DEPENDENCIES}"
        }
    }
    
    // تحسين الأداء
    dexOptions {
        javaMaxHeapSize = "4g"
    }
}

dependencies {
    // --- المكتبات الأساسية لأندرويد ---
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.multidex:multidex:2.0.1")

    // --- مكتبات Jetpack Compose ---
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.animation:animation")

    // --- مكتبات سحب البيانات والشبكة ---
    implementation("org.jsoup:jsoup:1.18.3")
    
    // --- مكتبة Coil لتحميل الصور ---
    implementation("io.coil-kt:coil-compose:2.7.0")
    
    // --- مكتبة التنقل ---
    implementation("androidx.navigation:navigation-compose:2.8.5")
    
    // --- مكتبات المشغل ExoPlayer ---
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
    implementation("androidx.media3:media3-session:1.5.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.5.1")
        
    // --- مكتبة OkHttp ---
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // --- مكتبة WebView ---
    implementation("androidx.webkit:webkit:1.12.1")

    // --- مكتبات Firebase ---
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // ✅ --- المكتبة الجديدة والمهمة لتسجيل الدخول عبر جوجل --- ✅
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    
    //مكتبة Shimmer
    implementation("com.valentinilk.shimmer:compose-shimmer:1.3.1")
    
    // --- مكتبات أخرى ---
    implementation("com.cloudinary:cloudinary-android:3.0.2")
    implementation("com.github.dhaval2404:imagepicker:2.1")
    
    // --- مكتبات الاختبار ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    
    // --- مكتبات إضافية للأداء ---
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")
}
