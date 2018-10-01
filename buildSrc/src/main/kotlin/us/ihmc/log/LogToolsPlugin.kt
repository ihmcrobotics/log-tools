package us.ihmc.log

import org.gradle.api.Plugin
import org.gradle.api.Project

class LogToolsPlugin: Plugin<Project>
{
   override fun apply(target: Project)
   {
      println("Helllo I'm a Kotlin buildSrc plugin!")
   }
}