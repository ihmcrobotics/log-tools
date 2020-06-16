package us.ihmc.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

      LogTools.log(Level.ERROR, "Hello there");
      LogTools.log(Level.WARN, "Hello there");
      LogTools.log(Level.INFO, "Hello there");
      LogTools.log(Level.DEBUG, "Hello there");
      LogTools.log(Level.TRACE, "Hello there");

      LogTools.error(1, "Hello there");
      LogTools.warn(1, "Hello there");
      LogTools.info(1, "Hello there");
      LogTools.debug(1, "Hello there");
      LogTools.trace(1, "Hello there");
   }

   @Test
   public void testBracketReplacement()
   {
      System.out.println(String.format("One: {}, Two: {}", 1, 2));
      System.out.println(new ParameterizedMessage("One: {}, Two: {}", 1, 2).getFormattedMessage());
      System.out.println(new ParameterizedMessageFactory().newMessage("One: {}, Two: {}", 1, 2).getFormattedMessage());
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

   @Test
   public void testPassingNull()
   {
      assertDoesNotThrow(() -> {
         LogTools.error((String) null);
         LogTools.warn((String) null);
         LogTools.info((String) null);
         LogTools.debug((String) null);
         LogTools.trace((String) null);
      });
   }
}
