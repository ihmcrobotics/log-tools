import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.*
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
   id("us.ihmc.ihmc-build") version "0.15.1"
   id("us.ihmc.log-tools")
}

ihmc {
   group = "us.ihmc"
   version = "0.0.1"
   vcsUrl = "https://github.com/ihmcrobotics/log-tools"
   openSource = true
   maintainer = "Duncan Calvert <dcalvert@ihmc.us>"

   configureDependencyResolution()
   configurePublications()
}

dependencies {
   compile("org.slf4j:slf4j-api:1.7.25")
   runtimeOnly("org.apache.logging.log4j:log4j-api:2.11.1")
   runtimeOnly("org.apache.logging.log4j:log4j-core:2.11.1")
   runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.11.1")
   runtimeOnly("com.fasterxml.jackson.core:jackson-databind:2.9.6")
   runtimeOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.6")
}

ihmc.sourceSetProject("test").dependencies {
   compile("org.junit.jupiter:junit-jupiter-api:5.3.1")
   runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}

ihmc.sourceSetProject("test").tasks.withType<Test> {
   useJUnitPlatform()

   doFirst {
      testLogging {
         events = setOf(PASSED, FAILED, SKIPPED, STANDARD_OUT, STANDARD_ERROR)
         exceptionFormat = FULL
      }
   }
}