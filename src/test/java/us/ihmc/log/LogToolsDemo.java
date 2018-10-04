package us.ihmc.log;

import static us.ihmc.log.LogTools.*;

public class LogToolsDemo
{
   public LogToolsDemo()
   {
      new SubclassOne();
      LogTools.debug("Hello LogTools constructor");
   }

   public class SubclassOne
   {
      public SubclassOne()
      {
         LogTools.debug("Hello subsclass level 1");
         new SubSubClassOne();
      }

      class SubSubClassOne
      {
         public SubSubClassOne()
         {
            LogTools.debug("Hi I'm subclass 2");
         }
      }
   }

   public static void main(String[] args)
   {
      String demo_there = "Demo there";
      error(demo_there);
      warn(demo_there);
      info(demo_there);
      debug(demo_there);
      trace(demo_there);

      new LogToolsDemo();
   }
}
