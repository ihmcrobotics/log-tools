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
   compile(gradleApi())
   compile("ch.qos.logback:logback-core:1.2.3")
   compile("ch.qos.logback:logback-classic:1.2.3")
}

ihmc.sourceSetProject("test").dependencies {
   compile("org.junit.jupiter:junit-jupiter-api:5.3.1")
   runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}