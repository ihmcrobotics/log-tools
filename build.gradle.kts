import org.gradle.api.tasks.testing.logging.TestExceptionFormat.*
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
   id("us.ihmc.ihmc-build") version "0.22.0"
   id("us.ihmc.ihmc-ci") version "6.4"
   id("us.ihmc.ihmc-cd") version "1.8"
   id("us.ihmc.log-tools-plugin")
}

ihmc {
   group = "us.ihmc"
   version = "0.5.0"
   vcsUrl = "https://github.com/ihmcrobotics/log-tools"
   openSource = true
   maintainer = "Duncan Calvert <dcalvert@ihmc.us>"

   configureDependencyResolution()
   configurePublications()
}

dependencies {
   api("org.apache.logging.log4j:log4j-api:2.11.1")
   api("org.apache.logging.log4j:log4j-core:2.11.1")
   api("org.apache.logging.log4j:log4j-slf4j-impl:2.11.1")
   api("com.fasterxml.jackson.core:jackson-databind:2.9.6")
   api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.6")
   api("org.fusesource.jansi:jansi:1.17.1")
}

ihmc.sourceSetProject("test").dependencies {
   api("us.ihmc:ihmc-commons-testing:0.30.2")
}

// test that custom JavaExec tasks receive the log level from Gradle properties
ihmc.sourceSetProject("test").tasks.register("runDemo", JavaExec::class.java) {
   classpath = ihmc.sourceSet("test").runtimeClasspath
   main = "us.ihmc.log.LogToolsDemo"
}

// test that test jvms get the Gradle properties
ihmc.sourceSetProject("test").tasks.withType<Test> {
   useJUnitPlatform()

   ihmc.sourceSetProject("test").configurations.compile.get().files.forEach {
      if (it.name.contains("java-allocation-instrumenter"))
      {
         val jvmArg = "-javaagent:" + it.absolutePath
         println("[ihmc-commons] Passing JVM arg: $jvmArg")
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