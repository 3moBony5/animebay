# AnimeBay - دليل التركيب والبناء

دليل شامل لتركيب وتشغيل تطبيق AnimeBay على Android Studio

---

## 📋 المتطلبات

### الأدوات المطلوبة:

1. **Android Studio**
   - الإصدار: Hedgehog (2023.1.1) أو أحدث
   - رابط التحميل: [https://developer.android.com/studio](https://developer.android.com/studio)

2. **Java Development Kit (JDK)**
   - الإصدار: 17 أو أحدث
   - رابط التحميل: [https://www.oracle.com/java/technologies/downloads/](https://www.oracle.com/java/technologies/downloads/)

3. **Android SDK**
   - الإصدار: 34 (Android 14)
   - يتم تثبيته مع Android Studio

4. **Git (اختياري)**
   - للحصول على أحدث التحديثات
   - رابط التحميل: [https://git-scm.com/downloads](https://git-scm.com/downloads)

---

## 🚀 خطوات التركيب

### الخطوة 1: تثبيت Android Studio

1. **تحميل Android Studio**
   ```bash
   # Windows
   - اذهب إلى الموقع الرسمي
   - حمل المثبت (exe)
   - شغل المثبت واتبع التعليمات
   
   # macOS
   - حمل ملف dmg
   - اسحب Android Studio إلى Applications
   
   # Linux
   - حمل ملف tar.gz
   - فك الضغط وشغل studio.sh
   ```

2. **أول تشغيل**
   - شغل Android Studio
   - اختر "Do not import settings"
   - انتظر تحميل المكونات

### الخطوة 2: فتح المشروع

1. **فتح المشروع**
   - في شاشة البداية، اختر "Open"
   - اذهب إلى مجلد المشروع
   - اختر مجلد `animebay-main`
   - اضغط "OK"

2. **انتظار المزامنة**
   - سيعرض Android Studio رسالة "Loading"
   - انتظر حتى تنتهي المزامنة (قد تستغرق 2-5 دقائق)
   - ستظهر رسالة "Gradle sync finished"

### الخطوة 3: تثبيت المكتبات

1. **تحقق من build.gradle.kts**
   - افتح ملف `app/build.gradle.kts`
   - تأكد من وجود جميع المكتبات المطلوبة:

```kotlin
dependencies {
    // المكتبات الأساسية
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

    // مكتبات الشبكة
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // تحميل الصور
    implementation("io.coil-kt:coil-compose:2.6.0")

    // التنقل
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // مشغل الفيديو
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-session:1.3.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // تسجيل الدخول عبر Google
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // مؤثرات بصرية
    implementation("com.valentinilk.shimmer:compose-shimmer:1.2.0")
}
```

2. **مزامنة Gradle**
   - اضغط "Sync Now" إذا ظهرت رسالة
   - أو اضغط Ctrl+Shift+O (File → Sync Project with Gradle Files)
   - انتظر حتى تنتهي المزامنة

### الخطوة 4: إعداد الجهاز

#### الطريقة 1: محاكي Android Studio

1. **إنشاء محاكي**
   - افتح AVD Manager (Tools → Device Manager)
   - اضغط "Create device"
   - اختر جهاز (مثلاً: Pixel 7)
   - اختر إصدار Android (يفضل Android 13 أو 14)
   - اضغط "Finish"

2. **تشغيل المحاكي**
   - اضغط على زر التشغيل الأخضر بجانب الجهاز
   - انتظر حتى يبدأ المحاكي

#### الطريقة 2: جهاز حقيقي

1. **تفعيل وضع المطور**
   - اذهب إلى إعدادات الجهاز
   - حول الهاتف → اضغط على "رقم الإصدار" 7 مرات
   - سيتم تفعيل "خيارات المطور"

2. **تفعيل تصحيح USB**
   - افتح إعدادات الجهاز
   - خيارات المطور → تصحيح USB
   - فعّل الخيار

3. **توصيل الجهاز**
   - وصل الجهاز بالكمبيوتر بكابل USB
   - اختر "نقل الملفات" عند ظهور الرسالة
   - اضغط "موافقة" على رسالة تصحيح USB

### الخطوة 5: البناء والتشغيل

1. **اختيار الجهاز**
   - من قائمة الأجهزة أعلى الشاشة
   - اختر المحاكي أو الجهاز الحقيقي

2. **بناء المشروع**
   - اضغط Ctrl+F9 (Build → Make Project)
   - أو اضغط على زر "Run" الأخضر
   - انتظر حتى ينتهي البناء

3. **تشغيل التطبيق**
   - اضغط Shift+F10 (Run → Run 'app')
   - أو اضغط على زر "Run" الأخضر
   - انتظر حتى يتم تثبيت التطبيق على الجهاز

---

## 🔧 حل المشاكل الشائعة

### مشكلة 1: Gradle sync failed

**الأعراض:**
- رسالة خطأ "Gradle sync failed"
- المكتبات لا تتحمل

**الحل:**

```bash
# 1. تحقق من اتصال الإنترنت
# 2. امسح الكاش
File → Invalidate Caches / Restart → Invalidate and Restart

# 3. تحقق من ملف gradle.properties
# تأكد من وجود هذا السطر:
android.useAndroidX=true
android.enableJetifier=true

# 4. أعد بناء المشروع
Build → Clean Project
Build → Rebuild Project
```

### مشكلة 2: Could not find com.android.tools.build:gradle

**الحل:**

```kotlin
// في build.gradle.kts (Project level)
buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.0")
    }
}
```

### مشكلة 3: Minimum supported Gradle version

**الحل:**

```bash
# 1. تحديث Gradle Wrapper
# افتح gradle/wrapper/gradle-wrapper.properties
# غير هذا السطر:
distributionUrl=https\://services.gradle.org/distributions/gradle-8.2-bin.zip

# 2. تحديث Android Gradle Plugin
# في build.gradle.kts (Project level)
classpath("com.android.tools.build:gradle:8.2.0")
```

### مشكلة 4: Out of memory

**الحل:**

```bash
# 1. زيادة الذاكرة
# افتح gradle.properties
# أضف هذه السطور:
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.configureondemand=true

# 2. أعد تشغيل Android Studio
```

### مشكلة 5: Device not found

**الحل:**

```bash
# للجهاز الحقيقي:
# 1. تأكد من تفعيل تصحيح USB
# 2. جرب كابل USB آخر
# 3. أعد تشغيل الجهاز

# للمحاكي:
# 1. أعد تشغيل المحاكي
# 2. حذف المحاكي وإنشاء واحد جديد
# 3. افحص ذاكرة الكمبيوتر
```

### مشكلة 6: Build failed (Kotlin errors)

**الحل:**

```bash
# 1. تحديث Kotlin
# في build.gradle.kts (Project level)
classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")

# 2. تحقق من صياغة الكود
# تأكد من عدم وجود أخطاء في الكود

# 3. Clean و Rebuild
Build → Clean Project
Build → Rebuild Project
```

### مشكلة 7: Dependencies not found

**الحل:**

```bash
# 1. تحقق من اتصال الإنترنت
# 2. أضف المستودعات في build.gradle.kts
allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

# 3. امسح الكاش
./gradlew clean
./gradlew build
```

### مشكلة 8: App crashes on startup

**الحل:**

```bash
# 1. افحص Logcat للأخطاء
# 2. تحقق من أذونات التطبيق
# 3. أعد تثبيت التطبيق
# 4. جرب محاكي/جهاز آخر
```

---

## 📊 إعدادات البناء المتقدمة

### Gradle Configuration:

```kotlin
// build.gradle.kts (Module: app)
android {
    compileSdk = 34
    
    defaultConfig {
        applicationId = "io.animebay.stream"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "2.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
            isDebuggable = true
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}
```

### ProGuard Rules:

```proguard
# Keep important classes
-keep class io.animebay.stream.** { *; }
-keep class com.google.android.exoplayer2.** { *; }
-keep class org.jsoup.** { *; }
-keep class okhttp3.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
```

---

## 🎯 بناء نسخة Release

### الخطوات:

1. **تغيير Build Variant**
   - اضغط على "Build Variants" في الجهة اليسرى
   - اختر "release" بدلاً من "debug"

2. **توقيع التطبيق**
   - اذهب إلى Build → Generate Signed Bundle/APK
   - اختر "APK"
   - أنشئ keystore جديد أو استخدم موجود
   - املأ المعلومات المطلوبة
   - اختر "release" كـ Build Type
   - اضغط "Finish"

3. **الحصول على APK**
   - سيتم إنشاء ملف APK في:
   ```
   app/build/outputs/apk/release/app-release.apk
   ```

4. **تثبيت APK**
   - انقل الملف إلى الجهاز
   - اضغط على الملف للتثبيت
   - فعّل "مصادر غير معروفة" إذا طلب

---

## 📦 حجم التطبيق

### تحسين الحجم:

```kotlin
// تقليل حجم APK
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### النتيجة:
- **Debug APK**: ~15-20 MB
- **Release APK**: ~8-12 MB

---

## 🚀 النشر على Google Play

### الخطوات:

1. **إنشاء حساب مطور**
   - اذهب إلى [Google Play Console](https://play.google.com/console)
   - أنشئ حساب مطور ($25 رسوم لمرة واحدة)

2. **تحضير المتجر**
   - أضف صور من التطبيق
   - اكتب وصفاً للتطبيق
   - اختر التصنيف

3. **رفع APK**
   - ارفع ملف APK
   - املأ المعلومات المطلوبة
   - اضغط "نشر"

4. **الانتظار**
   - المراجعة قد تستغرق 1-3 أيام
   - سيتم إشعارك عند الموافقة

---

## 🔍 التصحيح والتتبع

### استخدام Logcat:

```kotlin
// في الكود
import android.util.Log

class MyClass {
    companion object {
        private const val TAG = "MyClass"
    }
    
    fun myFunction() {
        Log.d(TAG, "Debug message")
        Log.e(TAG, "Error message", exception)
    }
}
```

### تصفية الرسائل:

1. افتح Logcat (View → Tool Windows → Logcat)
2. اختر الجهاز والتطبيق
3. استخدم الفلاتر:
   - `tag:AnimeRepository` للبحث في Repository
   - `tag:ServersViewModel` للبحث في Servers
   - `level:E` للأخطاء فقط

---

## 📝 ملاحظات مهمة

### قبل البناء:
- تأكد من وجود جميع الملفات
- تحقق من صحة المكتبات
- افحص اتصال الإنترنت

### أثناء البناء:
- انتظر حتى تنتهي المزامنة
- لا تقم بتعديل الملفات أثناء البناء
- راقب Logcat للتحذيرات

### بعد البناء:
- اختبر التطبيق جيداً
- افحص جميع الميزات
- راجع الأداء

---

## 📊 معلومات إضافية

### الإصدارات المدعومة:
- **Android**: 7.0 - 14.0
- **Kotlin**: 1.9.10
- **Compose**: 1.5.4
- **Gradle**: 8.2

### الحد الأدنى للموارد:
- **RAM**: 4 GB
- **Storage**: 100 MB free
- **Internet**: 2 Mbps (للمشاهدة)

---

## 🎉 بنجاح!

تهانينا! لقد قمت بتركيب وتشغيل تطبيق AnimeBay بنجاح.

### الخطوات التالية:
1. اختبر جميع الميزات
2. شارك التطبيق مع الأصدقاء
3. أبلغ عن الأخطاء إن وجدت
4. تابع التحديثات

---

**تم التحديث:** 2026-01-18  
**الإصدار:** 2.0  
**الحالة:** جاهز للبناء والنشر

---

## 📞 الدعم

للمساعدة في التركيب:
1. راجع قسم "حل المشاكل الشائعة"
2. افحص ملفات README
3. راجع الكود جيداً
4. استخدم أدوات التصحيح

**بالتوفيق!** 🚀
