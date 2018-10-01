package us.ihmc.log

import org.gradle.api.Plugin
import org.gradle.api.Project
import kotlin.reflect.KClass

class LogToolsPlugin : Plugin<Project>
{
   // meta class
   data class PropertyDefinition<T : Any>(val name: String, val klass: KClass<T>)

   // property definitions
   val LOG_LEVEL = PropertyDefinition("logLevel", LogLevel::class)

   // property value holders
   class NotSet

   data class LogLevel(val value: String)

   override fun apply(project: Project)
   {
      val logLevel = loadProperty(project, "logLevel")
      if (logLevel is String)
      {

      }
   }

   fun loadProperty(project: Project, name: String): Any
   {
      if (project.properties.containsKey(name))
      {
         val property = project.properties[name]
         if (property != null && property is String)
         {
            return property.trim().toLowerCase()
         }
      }

      return NotSet()
   }
}