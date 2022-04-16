import org.gradle.api.tasks.testing.logging.TestExceptionFormat.*
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
   id("us.ihmc.ihmc-build")
   id("us.ihmc.ihmc-ci") version "7.6"
   id("us.ihmc.ihmc-cd") version "1.23"
   id("us.ihmc.log-tools-plugin")
}

ihmc {
   group = "us.ihmc"
   version = "0.6.3"
   vcsUrl = "https://github.com/ihmcrobotics/log-tools"
   openSource = true
   maintainer = "Duncan Calvert <dcalvert@ihmc.us>"

   configureDependencyResolution()
   configurePublications()
}

dependencies {
   api("org.apache.logging.log4j:log4j-api:2.17.0")
   api("org.apache.logging.log4j:log4j-core:2.17.0")
   api("org.apache.logging.log4j:log4j-slf4j-impl:2.17.0")
   api("com.fasterxml.jackson.core:jackson-databind:2.13.0")
   api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")
   api("org.fusesource.jansi:jansi:2.4.0")
}

ihmc.sourceSetProject("test").dependencies {
   api("us.ihmc:ihmc-commons-testing:0.30.5")
}

app.entrypoint(ihmc.sourceSetProject("test"), "LogToolsDemo", "us.ihmc.log.LogToolsDemo")

val hostname: String by project
val username: String by project
val distFolder by lazy { ihmc.sourceSetProject("test").tasks.named<Sync>("installDist").get().destinationDir.toString() }

tasks.create("deploy") {
   dependsOn("log-tools-test:installDist")

   doLast {
      remote.session(hostname, username) {
         exec("mkdir -p /home/$username/.ihmc")
         exec("mkdir -p /home/$username/.ihmc/log-tools-test")
         exec("rm -rf /home/$username/.ihmc/log-tools-test/bin")
         exec("rm -rf /home/$username/.ihmc/log-tools-test/lib")
         put(file("$distFolder/bin").path, "/home/$username/.ihmc/log-tools-test/bin")
         put(file("$distFolder/lib").path, "/home/$username/.ihmc/log-tools-test/lib")
         exec("find /home/$username/.ihmc/log-tools-test/bin -type f -exec chmod +x {} \\;")
      }
   }
}

// test that custom JavaExec tasks receive the log level from Gradle properties
ihmc.sourceSetProject("test").tasks.register("runDemo", JavaExec::class.java) {
   classpath = ihmc.sourceSet("test").runtimeClasspath
   main = "us.ihmc.log.LogToolsDemo"
}

// test that test jvms get the Gradle properties
ihmc.sourceSetProject("test").tasks.withType<Test> {
   useJUnitPlatform()

   ihmc.sourceSetProject("test").configurations.runtimeClasspath.get().files.forEach {
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