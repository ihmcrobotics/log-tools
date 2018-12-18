package us.ihmc.log;

import org.junit.jupiter.api.Test;

public class LogToolsTest
{
   @Test
   public void testBasicLogMessage()
   {
      LogTools.error("Hello there");
      LogTools.warn("Hello there");
      LogTools.info("Hello there");
      LogTools.debug("Hello there");
      LogTools.trace("Hello there");
   }
   
   @Test
   public void testManyLogMessages()
   {
      for (int j = 0; j < 3; j++)
      {
         for (int i = 0; i < 50; i++)
         LogTools.error("Hello there");
         for (int i = 0; i < 50; i++)
         LogTools.warn("Hello there");
         for (int i = 0; i < 50; i++)
         LogTools.info("Hello there");
      }
   }
}
