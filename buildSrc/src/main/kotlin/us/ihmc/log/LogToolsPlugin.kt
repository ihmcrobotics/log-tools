package us.ihmc.log

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test

class LogToolsPlugin : Plugin<Project>
{
   class NotSet
   val javaProperties = hashMapOf<String, String>()
   open class LogLevelExtension(val javaProperties: Map<String, String>)

   override fun apply(project: Project)
   {
      val ihmcLogLevel = acceptStringProperty(project, "ihmcLogLevel")
      if (ihmcLogLevel is String)
      {
         javaProperties.put("ihmc.log.level", ihmcLogLevel)
      }

      project.extensions.create("logTools", LogLevelExtension::class.java, javaProperties)

      for (allproject in project.allprojects)
      {
         allproject.tasks.withType(JavaExec::class.java) { javaExec -> // setup properties for all JavaExec tasks
            javaExec.systemProperties.putAll(javaProperties)
            allproject.logger.info("[log-tools] ${allproject.name}: $javaExec: properties: ${javaExec.systemProperties}")
         }
         allproject.tasks.withType(Test::class.java) { test -> // setup properties for forked test jvms

            test.systemProperties.putAll(javaProperties)
            allproject.logger.info("[log-tools] ${allproject.name}: $test: systemProperties: ${test.systemProperties}")
         }
         val application = allproject.extensions.findByType(JavaApplication::class.java)
         if (application != null)
         {
            val list = arrayListOf<String>()
            javaProperties.forEach {
               list.add("-D${it.key}=${it.value}")
               allproject.logger.info("[log-tools] ${allproject.name}: $application: jvmArg: -D${it.key}=${it.value}")
            }
            application.setApplicationDefaultJvmArgs(list)
         }
      }
   }

   fun acceptStringProperty(project: Project, name: String): Any
   {
      if (project.properties.containsKey(name))
      {
         val property = project.properties[name]
         if (property != null && property is String)
         {
            val toLowerCase = property.trim().toLowerCase()
            project.logger.info("[log-tools] Loaded $name = $toLowerCase")
            return toLowerCase
         }
      }

      project.logger.info("[log-tools] Could not find property: $name")
      return NotSet()
   }
}