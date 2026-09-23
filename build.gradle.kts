import java.util.Properties

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.google.ksp) apply false
    alias(libs.plugins.vkid.manifest.placeholders)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

fun getSecret(key: String): String {
    return System.getenv(key) ?: localProperties.getProperty(key) ?: ""
}

vkidManifestPlaceholders {
    init(
        clientId = getSecret("VK_CLIENT_ID"),
        clientSecret = getSecret("VK_CLIENT_SECRET")
    )
}
