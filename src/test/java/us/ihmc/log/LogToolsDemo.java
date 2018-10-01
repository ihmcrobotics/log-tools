package us.ihmc.log;

public class LogToolsDemo
{
   public static void main(String[] args)
   {
      String demo_there = "Demo there";
      LogTools.error(demo_there);
      LogTools.warn(demo_there);
      LogTools.info(demo_there);
      LogTools.debug(demo_there);
      LogTools.trace(demo_there);
   }
}
