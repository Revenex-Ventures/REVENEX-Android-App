import java.util.Properties
import java.net.URI

plugins {
  alias(libs.plugins.android.application)
  id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10"
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
}

// Deliberately do not load .env: it may contain server secrets from legacy integrations.
val supabaseLocal = Properties().apply {
  rootProject.file("supabase.local.properties").takeIf { it.isFile }?.inputStream()?.use(::load)
}
fun publicConfig(name: String): String = supabaseLocal.getProperty(name, "").trim()
check(supabaseLocal.stringPropertyNames().all { it in setOf("SUPABASE_URL", "SUPABASE_PUBLISHABLE_KEY") }) {
  "supabase.local.properties must contain only the two public Supabase configuration values."
}
check(publicConfig("SUPABASE_PUBLISHABLE_KEY").let { it.isBlank() || (it.startsWith("sb_publishable_") && it.none(Char::isWhitespace)) }) {
  "Only an sb_publishable_ key is allowed in Android configuration."
}
check(publicConfig("SUPABASE_URL").let { value -> value.isBlank() || runCatching {
  val uri=URI(value)
  uri.scheme=="https" && !uri.host.isNullOrBlank() && uri.userInfo==null && uri.rawQuery==null && uri.rawFragment==null
}.getOrDefault(false) }) { "SUPABASE_URL must be a public HTTPS project URL, without credentials or query parameters." }
fun javaLiteral(value: String) = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "").replace("\r", "") + "\""

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.example"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
    buildConfigField("String", "SUPABASE_URL", javaLiteral(publicConfig("SUPABASE_URL")))
    buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", javaLiteral(publicConfig("SUPABASE_PUBLISHABLE_KEY")))
    // Retained API compatibility only; demo builds never invoke checkout.
    buildConfigField("String", "RAZORPAY_KEY_ID", "\"\"")

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
  }
  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Keep large native test files off system TEMP. The shared Gradle artifact cache
// also avoids Robolectric's Android 16 native-loader bug with spaces in jar paths.
tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
  val testTemp = layout.buildDirectory.dir("tmp/$name/robolectric").get().asFile
  val testArtifacts = gradle.gradleUserHomeDir.resolve("caches/robolectric-m2")
  systemProperty("java.io.tmpdir", testTemp.absolutePath)
  systemProperty("maven.repo.local", testArtifacts.absolutePath)
  doFirst {
    check(testTemp.mkdirs() || testTemp.isDirectory)
    check(testArtifacts.mkdirs() || testArtifacts.isDirectory)
  }
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
  implementation(platform("io.github.jan-tennert.supabase:bom:3.2.3"))
  implementation("io.github.jan-tennert.supabase:auth-kt")
  implementation("io.github.jan-tennert.supabase:postgrest-kt")
  implementation("io.github.jan-tennert.supabase:storage-kt")
  implementation("io.github.jan-tennert.supabase:realtime-kt")
  implementation("io.github.jan-tennert.supabase:functions-kt")
  implementation("io.ktor:ktor-client-okhttp:3.2.3")
  implementation(platform(libs.androidx.compose.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.androidx.credentials)
  implementation(libs.androidx.credentials.play.services)
  implementation(libs.googleid)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Razorpay checkout (TEST MODE integration). 1.6.39 is pinned because newer
  // Razorpay artifacts (1.6.4x + standard-core) declare the same `com.razorpay`
  // namespace, which AGP 9's unique-namespace check rejects during manifest merge.
  // 1.6.39 exposes the identical checkout API (Checkout, PaymentResultWithDataListener).
  implementation("com.razorpay:checkout:1.6.39")
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

// Prevent a transitive SDK from silently reintroducing the retired backend.
tasks.register("verifyNoFirebaseDependencies") {
  doLast {
    val retired = configurations.getByName("debugRuntimeClasspath")
      .resolvedConfiguration.resolvedArtifacts
      .filter { it.moduleVersion.id.group.startsWith("com.google.firebase") }
    check(retired.isEmpty()) { "Firebase dependencies must remain absent: $retired" }
  }
}
