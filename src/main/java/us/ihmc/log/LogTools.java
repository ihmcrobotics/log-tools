package us.ihmc.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class LogTools
{
   /** Keep a list of loggers, so we don't recreate a bunch of formatters */
   private static final HashMap<String, Logger> loggers = new HashMap<>();

   /** For keeping track of the class that actually calls the RosieLogTools method. */
   private static final int STACK_TRACE_INDEX = 1;

   static
   {
      String ihmcLogLevel = "ihmc.log.level";
      String ihmcGroup = "us.ihmc";
      if (System.getProperties().containsKey(ihmcLogLevel))
      {
         String level = System.getProperty(ihmcLogLevel).trim().toLowerCase();
         if (level.startsWith("fat") || level.startsWith("err"))
         {
            Configurator.setLevel(ihmcGroup, Level.ERROR);
         }
         else if (level.startsWith("war"))
         {
            Configurator.setLevel(ihmcGroup, Level.WARN);
         }
         else if (level.startsWith("inf"))
         {
            Configurator.setLevel(ihmcGroup, Level.INFO);
         }
         else if (level.startsWith("deb"))
         {
            Configurator.setLevel(ihmcGroup, Level.DEBUG);
         }
         else if (level.startsWith("tra"))
         {
            Configurator.setLevel(ihmcGroup, Level.TRACE);
         }
         else if (level.startsWith("all"))
         {
            Configurator.setLevel(ihmcGroup, Level.ALL);
         }
         else if (level.startsWith("off"))
         {
            Configurator.setLevel(ihmcGroup, Level.OFF);
         }
      }
   }

   public static final Logger getLogger(Class<?> clazz)
   {
      return LoggerFactory.getLogger(clazz);
   }

   public static final Logger getLogger(String className)
   {
      checkLoggerCreated(className);

      return loggers.get(className);
   }

   private static void checkLoggerCreated(String className)
   {
      if (!loggers.containsKey(className))
      {
         Logger logger = LoggerFactory.getLogger(className);

         // Track the loggers that use this class
         loggers.put(className, logger);
      }
   }

   public static void error(String message)
   {
      Throwable throwable = new Throwable();
      getLogger(className(throwable)).error(log(null, message, throwable));
   }

   public static void error(Logger logger, String message)
   {
      logger.error(log(null, message, new Throwable()));
   }

   public static void warn(String message)
   {
      Throwable throwable = new Throwable();
      getLogger(className(throwable)).warn(log(null, message, throwable));
   }

   public static void warn(Logger logger, String message)
   {
      logger.warn(log(null, message, new Throwable()));
   }

   public static void info(String message)
   {
      Throwable throwable = new Throwable();
      getLogger(className(throwable)).info(log(null, message, throwable));
   }

   public static void info(Logger logger, String message)
   {
      warn(log(null, message, new Throwable()));
   }

   public static void debug(String message)
   {
      Throwable throwable = new Throwable();
      getLogger(className(throwable)).debug(log(null, message, throwable));
   }

   public static void debug(Logger logger, String message)
   {
      logger.debug(log(null, message, new Throwable()));
   }

   public static void trace(String message)
   {
      Throwable throwable = new Throwable();
      getLogger(className(throwable)).trace(log(null, message, throwable));
   }

   public static void trace(Logger logger, String message)
   {
      logger.trace(log(null, message, new Throwable()));
   }


   private static String className(Throwable throwable)
   {
      return throwable.getStackTrace()[STACK_TRACE_INDEX].getClassName().split("\\.java")[0];
   }

   private static String log(Object containingObjectOrClass, String message, Throwable throwable)
   {
      int lineNumber = -1;
      String className;
      if (containingObjectOrClass == null)
      {
         String[] classNameSplit = throwable.getStackTrace()[STACK_TRACE_INDEX].getClassName().split("\\.");
         className = classNameSplit[classNameSplit.length - 1].split("\\$")[0];
         lineNumber = throwable.getStackTrace()[STACK_TRACE_INDEX].getLineNumber();
      }
      else
      {
         className = containingObjectOrClass.getClass().getSimpleName();

         for (StackTraceElement stackTraceElement : throwable.getStackTrace())
         {
            if (stackTraceElement.getClassName().endsWith(className))
            {
               lineNumber = stackTraceElement.getLineNumber();
               break;
            }
         }

         if (lineNumber == -1)
            lineNumber = throwable.getStackTrace()[STACK_TRACE_INDEX].getLineNumber();

         if (containingObjectOrClass instanceof Class<?>)
            className = ((Class<?>) containingObjectOrClass).getSimpleName();
      }

      String clickableLocation = "(" + className + ".java:" + lineNumber + ")";

      return clickableLocation + ": " + message;
   }
}
