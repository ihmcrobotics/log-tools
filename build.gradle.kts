import com.gradle.publish.MavenCoordinates

plugins {
   `java-gradle-plugin`
   id("us.ihmc.ihmc-build") version "0.15.1"
   id("com.gradle.plugin-publish") version "0.9.9"
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
   compile(gradleApi())
}

ihmc.sourceSetProject("api").dependencies {

}

gradlePlugin {
   plugins {
      register("logToolsPlugin") {
         id = ihmc.group + "." + project.name
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

   mavenCoordinates(closureOf<MavenCoordinates> {
      groupId = ihmc.group
      artifactId = project.name
      version = ihmc.version
   })
}