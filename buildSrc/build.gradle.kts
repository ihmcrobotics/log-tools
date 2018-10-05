import com.gradle.publish.MavenCoordinates
import com.gradle.publish.PluginConfig

plugins {
   `java-gradle-plugin`
   kotlin("jvm") version "1.2.61"
   id("com.gradle.plugin-publish") version "0.9.9"
}

group = "us.ihmc"
version = "0.2.2"

repositories {
   mavenCentral()
   jcenter()
}

dependencies {
   compile(gradleApi())
   compile(kotlin("stdlib"))
//   runtimeOnly(kotlin("runtime"))
}

gradlePlugin {
   plugins {
      register("logToolsPlugin") {
         id = project.group as String + "." + project.name
         displayName = "Log Tools"
         implementationClass = "us.ihmc.log.LogToolsPlugin"
         description = "Message logging tools for IHMC Robotics."
      }
   }
}

pluginBundle {
   website = "https://github.com/ihmcrobotics/log-tools"
   vcsUrl = "https://github.com/ihmcrobotics/log-tools"
   description = "Message logging tools for IHMC Robotics."
   tags = listOf("log", "tools", "ihmc", "robotics")

   plugins.register("logToolsPlugin") {
      id = project.group as String + "." + project.name
      version = project.version as String
      displayName = "Log Tools"
   }

   mavenCoordinates(closureOf<MavenCoordinates> {
      groupId = project.group as String
      artifactId = project.name
      version = project.version as String
   })
}