import org.gradle.api.tasks.testing.logging.TestExceptionFormat.*
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
   `java-library`
   id("us.ihmc.ihmc-build") version "0.15.7"
   id("us.ihmc.ihmc-ci") version "4.23"
   id("us.ihmc.ihmc-cd") version "0.1"
   id("us.ihmc.log-tools")
}

ihmc {
   group = "us.ihmc"
   version = "0.3.2"
   vcsUrl = "https://github.com/ihmcrobotics/log-tools"
   openSource = true
   maintainer = "Duncan Calvert <dcalvert@ihmc.us>"

   configureDependencyResolution()
   configurePublications()
}

dependencies {
//   implementation("org.slf4j:slf4j-api:1.7.25")
   compile("org.apache.logging.log4j:log4j-api:2.11.1")
   compile("org.apache.logging.log4j:log4j-core:2.11.1")
   compile("org.apache.logging.log4j:log4j-slf4j-impl:2.11.1")
   compile("com.fasterxml.jackson.core:jackson-databind:2.9.6")
   compile("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.6")
   compile("org.fusesource.jansi:jansi:1.17.1")
//   compile("com.mihnita:color-loggers:1.0.5")
}

ihmc.sourceSetProject("test").dependencies {
   implementation("org.junit.jupiter:junit-jupiter-api:5.4.0")
   runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.0")
   compile("us.ihmc:ihmc-commons-testing:0.26.3")
}

// test that custom JavaExec tasks receive the log level from Gradle properties
ihmc.sourceSetProject("test").tasks.register("runDemo", JavaExec::class.java) {
   classpath = ihmc.sourceSet("test").runtimeClasspath
   main = "us.ihmc.log.LogToolsDemo"
}

// test that test jvms get the Gradle properties
ihmc.sourceSetProject("test").tasks.withType<Test> {
   useJUnitPlatform()

   ihmc.sourceSetProject("test").configurations.compile.files.forEach {
      if (it.name.contains("java-allocation-instrumenter"))
      {
         val jvmArg = "-javaagent:" + it.getAbsolutePath()
         println("[ihmc-commons] Passing JVM arg: " + jvmArg)
         val tmpArgs = allJvmArgs
         tmpArgs.add(jvmArg)
         allJvmArgs = tmpArgs

      }
   }

   doFirst {
      testLogging {
         events = setOf(PASSED, FAILED, SKIPPED, STANDARD_OUT, STANDARD_ERROR)
         exceptionFormat = FULL
      }
   }
}