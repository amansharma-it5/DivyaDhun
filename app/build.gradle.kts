import java.util.Properties

plugins { alias(libs.plugins.android.application); alias(libs.plugins.compose.compiler) }
val configuredRemoteCatalogUrl = providers.gradleProperty("divyadhunCatalogUrl").orNull.orEmpty()
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
 if (keystorePropertiesFile.exists()) {
  keystorePropertiesFile.inputStream().use(::load)
 }
}
val releaseSigningKeys = listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
val hasReleaseSigning = releaseSigningKeys.all { keystoreProperties.getProperty(it).orEmpty().isNotBlank() }
if (keystorePropertiesFile.exists() && !hasReleaseSigning) {
 error("keystore.properties exists but is incomplete; provide storeFile, storePassword, keyAlias, and keyPassword")
}
android {
 namespace = "com.divyadhun.app"
 compileSdk = 37
 defaultConfig {
  applicationId = "com.divyadhun.app"
  minSdk = 26
  targetSdk = 37
  versionCode = 1
  versionName = "1.0.0"
  testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  buildConfigField("String", "REMOTE_CATALOG_URL", "\"${configuredRemoteCatalogUrl.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
 }
 signingConfigs {
  if (hasReleaseSigning) {
   create("release") {
    storeFile = file(keystoreProperties.getProperty("storeFile"))
    storePassword = keystoreProperties.getProperty("storePassword")
    keyAlias = keystoreProperties.getProperty("keyAlias")
    keyPassword = keystoreProperties.getProperty("keyPassword")
   }
  }
 }
 buildTypes {
 release {
   if (hasReleaseSigning) signingConfig = signingConfigs.getByName("release")
  }
  create("releaseQa") {
   initWith(getByName("release"))
   matchingFallbacks += listOf("release")
  }
 }
 testOptions { testBuildType = "releaseQa" }
 buildFeatures { compose = true; buildConfig = true }
 bundle { language { enableSplit = false } }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
dependencies {
 implementation(platform(libs.compose.bom))
 implementation(libs.compose.ui)
 implementation(libs.compose.material3)
 implementation(libs.compose.icons)
 implementation(libs.compose.icons.extended)
 implementation(libs.compose.preview)
 implementation(libs.activity.compose)
 implementation(libs.navigation.compose)
 implementation(libs.datastore)
 implementation(libs.media3.exoplayer)
 implementation(libs.media3.session)
 implementation(libs.serialization.json)
 implementation(libs.lifecycle)
 implementation(libs.viewmodel)
 debugImplementation(libs.compose.tooling)
 debugImplementation(libs.compose.test.manifest)
 testImplementation(libs.junit)
 androidTestImplementation(platform(libs.compose.bom))
 androidTestImplementation(libs.compose.test)
 add("releaseQaImplementation", libs.compose.test.manifest)
 androidTestImplementation(libs.android.test)
 androidTestImplementation(libs.test.runner)
}
