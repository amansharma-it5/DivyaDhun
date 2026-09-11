plugins { alias(libs.plugins.android.application); alias(libs.plugins.compose.compiler) }
android {
 namespace = "com.divyadhun.app"
 compileSdk = 37
 defaultConfig {
  applicationId = "com.divyadhun.app"
  minSdk = 26
  targetSdk = 37
  versionCode = 1
  versionName = "0.1.0"
  testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
 }
 buildFeatures { compose = true }
 bundle { language { enableSplit = false } }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
dependencies {
 implementation(platform(libs.compose.bom))
 implementation(libs.compose.ui)
 implementation(libs.compose.material3)
 implementation(libs.compose.icons)
 implementation(libs.compose.preview)
 implementation(libs.activity.compose)
 implementation(libs.navigation.compose)
 implementation(libs.datastore)
 implementation(libs.lifecycle)
 implementation(libs.viewmodel)
 debugImplementation(libs.compose.tooling)
 debugImplementation(libs.compose.test.manifest)
 testImplementation(libs.junit)
 androidTestImplementation(platform(libs.compose.bom))
 androidTestImplementation(libs.compose.test)
 androidTestImplementation(libs.android.test)
 androidTestImplementation(libs.test.runner)
}
