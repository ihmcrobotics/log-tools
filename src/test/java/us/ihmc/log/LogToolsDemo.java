package us.ihmc.log;

import java.util.function.Supplier;

import static us.ihmc.log.LogTools.*;

public class LogToolsDemo
{
   public LogToolsDemo()
   {
      new SubclassOne();
      LogTools.debug("Hello LogTools constructor");

      Supplier<String> supplier = () -> "string" + "builder";
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
      error(demo_there);
      warn("var1: {}, var2: {}", 9, false);
      warn("var1: {}, var2: {}", 9, false);
      info(demo_there);
      info(demo_there);
      error(demo_there);
      error(demo_there);
      warn("var1: {}, var2: {}", 9, false);
      warn("var1: {}, var2: {}", 9, false);
      debug(demo_there);
      debug(demo_there);
      trace(demo_there);
      trace(demo_there);

      new LogToolsDemo();
   }
}
