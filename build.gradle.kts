import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "io.techcode.fluxy"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "4.4.2"
val junitJupiterVersion = "5.9.1"
val guavaVersion = "31.1-jre"
val jcToolsVersion = "4.0.1"
val configVersion = "1.4.2"
val picocliVersion = "4.7.3"

val launcherClassName = "io.techcode.fluxy.Fluxy"

application {
  mainClass.set(launcherClassName)
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-core")
  implementation("com.google.guava:guava:$guavaVersion")
  implementation("com.typesafe:config:$configVersion")
  implementation("org.jctools:jctools-core:$jcToolsVersion")
  implementation("info.picocli:picocli:$picocliVersion")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}
