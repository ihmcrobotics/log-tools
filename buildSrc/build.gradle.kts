import com.gradle.publish.MavenCoordinates

plugins {
   `kotlin-dsl`
   `java-gradle-plugin`
   `maven-publish`
   id("com.gradle.plugin-publish") version "0.10.0"
}

group = "us.ihmc"
version = "0.4.2"

repositories {
   mavenCentral()
   jcenter()
}

dependencies {

}

val pluginDisplayName = "Log Tools"
val pluginDescription = "Message logging tools for IHMC Robotics."
val pluginVcsUrl = "https://github.com/ihmcrobotics/log-tools"
val pluginTags = listOf("log", "tools", "ihmc", "robotics")

gradlePlugin {
   plugins.register("logToolsPlugin") {
      id = project.group as String + "." + project.name
      implementationClass = "us.ihmc.log.LogToolsPlugin"
      displayName = pluginDisplayName
      description = pluginDescription
   }
}

pluginBundle {
   website = pluginVcsUrl
   vcsUrl = pluginVcsUrl
   description = pluginDescription
   tags = pluginTags

   plugins.getByName("logToolsPlugin") {
      id = project.group as String + "." + project.name
      version = project.version as String
      displayName = pluginDisplayName
      description = pluginDescription
      tags = pluginTags
   }

   mavenCoordinates(closureOf<MavenCoordinates> {
      groupId = project.group as String
      artifactId = project.name
      version = project.version as String
   })
}