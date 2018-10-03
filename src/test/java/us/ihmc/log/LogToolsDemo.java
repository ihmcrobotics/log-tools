package us.ihmc.log;

import static us.ihmc.log.LogTools.*;

public class LogToolsDemo
{
   public static void main(String[] args)
   {
      String demo_there = "Demo there";
      error(demo_there);
      warn(demo_there);
      info(demo_there);
      debug(demo_there);
      trace(demo_there);
   }
}
