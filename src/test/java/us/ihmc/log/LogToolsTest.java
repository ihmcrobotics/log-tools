package us.ihmc.log;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;
import us.ihmc.commons.thread.ThreadTools;

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
   public void testShutdown()
   {
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
         LogTools.error("Shutting down");
         LogTools.warn("Shutting down");
         LogTools.info("Shutting down");
         LogTools.debug("Shutting down");
         LogTools.trace("Shutting down");
      }));

      ThreadTools.startAThread(() ->
      {
         for (int i = 0; i < 5; i++)
         {
            LogTools.info("Sleeping...");
            ThreadTools.sleepSeconds(1.0);
         }
      }, "SleepThread");

      ThreadTools.sleepSeconds(2.0);
//      LogManager.shutdown();
      Runtime.getRuntime().exit(0);
      ThreadTools.sleepSeconds(2.0);
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
