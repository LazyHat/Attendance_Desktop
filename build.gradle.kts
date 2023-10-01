import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val ktor_version: String by project
val koin_jvm_version: String by project
val koin_ktor_version: String by project
val koin_compose_version: String by project
val zxing_version: String by project
val zxing_android_version: String by project
val serialization_version: String by project
val datetime_version: String by project


plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
}

group = "ru.lazyhat.novsu.teacher"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$datetime_version")
    //Ktor
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-okhttp:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    //Koin
    implementation("io.insert-koin:koin-core-jvm:$koin_jvm_version")
    implementation("io.insert-koin:koin-ktor:$koin_ktor_version")
    implementation("io.insert-koin:koin-compose:$koin_compose_version")
    //
    //Zxing
    implementation("com.google.zxing:core:$zxing_version")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "TeacherApp"
            packageVersion = "1.0.0"
        }
    }
}
