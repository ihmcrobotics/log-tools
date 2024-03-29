package us.ihmc.log

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.api.tasks.testing.Test

class LogToolsPlugin : Plugin<Project>
{
   val javaProperties = hashMapOf<String, String>()
   open class LogLevelExtension(val javaProperties: Map<String, String>)

   override fun apply(project: Project)
   {
      for (prop in project.properties) // go through all properties, as they are dynamically named
      {
         if (prop.key is String && prop.value is String)
         {
            if (prop.key.startsWith("log.level") || prop.key.startsWith("log.granular"))
            {
               project.logger.info("[log-tools] Passing to all JVMs: -D${prop.key}=${prop.value}")
               javaProperties.put(prop.key, prop.value as String)
            }
         }
      }

      project.extensions.create("logTools", LogLevelExtension::class.java, javaProperties)

      for (allproject in project.allprojects)
      {
         allproject.tasks.withType(JavaExec::class.java).forEach { javaExec -> // setup properties for all JavaExec tasks
            javaExec.systemProperties.putAll(javaProperties)
            allproject.logger.info("[log-tools] Passing JVM args ${javaExec.systemProperties} to $javaExec")
         }
         allproject.tasks.withType(Test::class.java).forEach { test -> // setup properties for forked test jvms
            test.systemProperties.putAll(javaProperties)
            allproject.logger.info("[log-tools] Passing JVM args ${test.systemProperties} to $test")
         }
         allproject.tasks.withType(CreateStartScripts::class.java).forEach { startScripts -> // setup properties for all start scripts (includes application plugin)
            val list = arrayListOf<String>()
            javaProperties.forEach {
               list.add("-D${it.key}=${it.value}")
            }
            startScripts.defaultJvmOpts = list
            allproject.logger.info("[log-tools] Passing JVM args $list to $startScripts")
         }
      }
   }
}