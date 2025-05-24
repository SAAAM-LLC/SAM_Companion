plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.saaam.companion"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.saaam.companion"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // ViewModel and State
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.compose.runtime:runtime-livedata")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // JSON Processing
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // File I/O
    implementation("androidx.documentfile:documentfile:1.0.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// AndroidManifest.xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="28" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />

    <!-- Optimizations -->
    <uses-feature
        android:name="android.hardware.touchscreen"
        android:required="true" />
    <uses-feature
        android:name="android.software.leanback"
        android:required="false" />

    <application
        android:name=".SAMApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.SAMCompanion"
        android:hardwareAccelerated="true"
        android:largeHeap="true"
        tools:targetApi="31">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop"
            android:theme="@style/Theme.SAMCompanion"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>

            <!-- Support for dynamic shortcuts -->
            <meta-data
                android:name="android.app.shortcuts"
                android:resource="@xml/shortcuts" />
        </activity>

        <!-- Settings Activity -->
        <activity
            android:name=".SettingsActivity"
            android:exported="false"
            android:parentActivityName=".MainActivity"
            android:theme="@style/Theme.SAMCompanion" />

        <!-- File Provider for sharing -->
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="com.saaam.companion.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>

        <!-- Background service for SAM processing -->
        <service
            android:name=".SAMBackgroundService"
            android:enabled="true"
            android:exported="false" />
    </application>
</manifest>

// SAMApplication.kt
package com.saaam.companion

import android.app.Application
import android.util.Log

class SAMApplication : Application() {

    companion object {
        lateinit var instance: SAMApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        Log.i("SAMApp", "SAM Companion Application initialized")

        // Initialize any global components here
        initializeSAMEnvironment()
    }

    private fun initializeSAMEnvironment() {
        // Set up directories for SAM data
        val samDataDir = filesDir.resolve("sam_data")
        if (!samDataDir.exists()) {
            samDataDir.mkdirs()
        }

        val conceptsDir = samDataDir.resolve("concepts")
        if (!conceptsDir.exists()) {
            conceptsDir.mkdirs()
        }

        val experiencesDir = samDataDir.resolve("experiences")
        if (!experiencesDir.exists()) {
            experiencesDir.mkdirs()
        }

        Log.i("SAMApp", "SAM data directories initialized")
    }

    fun getSAMDataDirectory(): String {
        return filesDir.resolve("sam_data").absolutePath
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w("SAMApp", "Low memory warning - SAM may reduce complexity")
        // Trigger memory optimization in SAM engine if needed
    }
}

// strings.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">SAM Companion</string>
    <string name="app_description">Your personal AI that grows and evolves with you</string>

    <!-- UI Strings -->
    <string name="chat_input_hint">Share your thoughts with SAM...</string>
    <string name="sam_thinking">SAM is thinking...</string>
    <string name="sam_evolving">SAM is evolving...</string>
    <string name="menu_title">SAM Control Center</string>

    <!-- Action Strings -->
    <string name="action_send">Send</string>
    <string name="action_menu">Menu</string>
    <string name="action_evolve">Force Evolution</string>
    <string name="action_dream">Dream Cycle</string>
    <string name="action_reset">Reset SAM</string>
    <string name="action_export">Export</string>
    <string name="action_settings">Settings</string>

    <!-- Status Strings -->
    <string name="status_initializing">Initializing neural pathways...</string>
    <string name="status_ready">Ready for interaction</string>
    <string name="status_evolving">Evolving neural architecture...</string>
    <string name="status_dreaming">Entering dream state...</string>

    <!-- Descriptions -->
    <string name="evolve_description">Trigger neural architecture growth</string>
    <string name="dream_description">Process concepts and form connections</string>
    <string name="reset_description">Return to initial state</string>
    <string name="export_description">Save chat history and growth data</string>
    <string name="settings_description">Configure learning parameters</string>

    <!-- Quick Actions -->
    <string name="quick_about">Tell me about yourself</string>
    <string name="quick_learn">Help me learn</string>
    <string name="quick_dream">Dream cycle</string>
    <string name="quick_stats">Show stats</string>

    <!-- Neural Architecture -->
    <string name="neural_concepts">Concepts</string>
    <string name="neural_growth">Growth</string>
    <string name="neural_dreams">Dreams</string>
    <string name="neural_hidden_dim">Hidden Dim</string>
    <string name="neural_layers">Layers</string>
    <string name="neural_thought_depth">Thought Depth</string>
    <string name="neural_resonance">Conceptual Resonance</string>

    <!-- Error Messages -->
    <string name="error_initialization">Failed to initialize SAM - running in basic mode</string>
    <string name="error_processing">Error processing message</string>
    <string name="error_export">Failed to export conversation</string>

    <!-- Success Messages -->
    <string name="success_evolution">Evolution complete - enhanced capabilities</string>
    <string name="success_dream">Dream cycle complete</string>
    <string name="success_export">Conversation exported successfully</string>
    <string name="success_reset">SAM reset complete</string>
</resources>

// themes.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.SAMCompanion" parent="Theme.Material3.DayNight.NoActionBar">
        <!-- Customize your light theme here. -->
        <item name="colorPrimary">@color/sam_primary</item>
        <item name="colorSecondary">@color/sam_secondary</item>
        <item name="colorBackground">@color/sam_background</item>
        <item name="colorSurface">@color/sam_surface</item>
        <item name="android:statusBarColor">@color/sam_background</item>
        <item name="android:navigationBarColor">@color/sam_background</item>
        <item name="android:windowLightStatusBar">false</item>
        <item name="android:windowLightNavigationBar">false</item>
    </style>
</resources>

// colors.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- SAM Theme Colors -->
    <color name="sam_primary">#64FFDA</color>
    <color name="sam_secondary">#80CBC4</color>
    <color name="sam_background">#0A1128</color>
    <color name="sam_surface">#1A1A2E</color>
    <color name="sam_accent">#BA68C8</color>
    <color name="sam_success">#81C784</color>
    <color name="sam_warning">#FFD54F</color>
    <color name="sam_error">#F06292</color>

    <!-- Text Colors -->
    <color name="sam_text_primary">#FFFFFF</color>
    <color name="sam_text_secondary">#80CBC4</color>
    <color name="sam_text_hint">#4A5568</color>

    <!-- Transparent overlays -->
    <color name="sam_overlay_light">#1AFFFFFF</color>
    <color name="sam_overlay_dark">#80000000</color>
</resources>

// shortcuts.xml (for dynamic shortcuts)
<?xml version="1.0" encoding="utf-8"?>
<shortcuts xmlns:android="http://schemas.android.com/apk/res/android">
    <shortcut
        android:shortcutId="quick_chat"
        android:enabled="true"
        android:icon="@drawable/ic_chat"
        android:shortcutShortLabel="@string/app_name"
        android:shortcutLongLabel="Chat with SAM">
        <intent
            android:action="android.intent.action.VIEW"
            android:targetPackage="com.saaam.companion"
            android:targetClass="com.saaam.companion.MainActivity" />
    </shortcut>

    <shortcut
        android:shortcutId="trigger_evolution"
        android:enabled="true"
        android:icon="@drawable/ic_evolution"
        android:shortcutShortLabel="Evolve SAM"
        android:shortcutLongLabel="Trigger SAM Evolution">
        <intent
            android:action="com.saaam.companion.EVOLVE"
            android:targetPackage="com.saaam.companion"
            android:targetClass="com.saaam.companion.MainActivity" />
    </shortcut>
</shortcuts>

// file_paths.xml (for file provider)
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <files-path name="sam_exports" path="exports/" />
    <cache-path name="sam_temp" path="temp/" />
</paths>

// ProGuard rules (proguard-rules.pro)
# Keep SAM core classes
-keep class com.saaam.companion.** { *; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Compose classes
-keep class androidx.compose.** { *; }

# Keep serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
