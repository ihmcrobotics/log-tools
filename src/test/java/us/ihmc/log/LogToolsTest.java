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
}
