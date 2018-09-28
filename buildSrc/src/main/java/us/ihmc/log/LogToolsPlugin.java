package us.ihmc.log;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class LogToolsPlugin implements Plugin<Project>
{
   @Override
   public void apply(Project project)
   {
      System.out.println("Hello im a buildsrc!!!!");
   }
}