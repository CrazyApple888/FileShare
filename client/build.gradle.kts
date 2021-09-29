import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
}

group = "me.idrew"
version = "1.0-SNAPSHOT"

dependencies {
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")
    implementation(project(":protocol"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}