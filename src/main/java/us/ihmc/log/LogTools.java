package us.ihmc.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.util.Supplier;

import java.util.HashMap;

public class LogTools
{
   /** Keep a list of loggers, so we don't recreate a bunch of formatters */
   private static final HashMap<String, Logger> loggers = new HashMap<>();

   private static final String IHMC_DEFAULT_LOGGER_NAME = "us.ihmc";

   static
   {
      setLevel("log.level.root", LogManager.ROOT_LOGGER_NAME);
      setLevel("log.level.us.ihmc", IHMC_DEFAULT_LOGGER_NAME);
   }

   private static void setLevel(String propertyName, String group)
   {
      if (System.getProperties().containsKey(propertyName))
      {
         String level = System.getProperty(propertyName).trim().toLowerCase();
         if (level.startsWith("fat") || level.startsWith("err"))
         {
            Configurator.setLevel(group, Level.ERROR);
         }
         else if (level.startsWith("war"))
         {
            Configurator.setLevel(group, Level.WARN);
         }
         else if (level.startsWith("inf"))
         {
            Configurator.setLevel(group, Level.INFO);
         }
         else if (level.startsWith("deb"))
         {
            Configurator.setLevel(group, Level.DEBUG);
         }
         else if (level.startsWith("tra"))
         {
            Configurator.setLevel(group, Level.TRACE);
         }
         else if (level.startsWith("all"))
         {
            Configurator.setLevel(group, Level.ALL);
         }
         else if (level.startsWith("off"))
         {
            Configurator.setLevel(group, Level.OFF);
         }
      }
   }

   public static final Logger getLogger(Class<?> clazz)
   {
      return LogManager.getLogger(clazz);
   }

   public static final Logger getLogger(String loggerName)
   {
      checkLoggerCreated(loggerName);

      return loggers.get(loggerName);
   }

   private static void checkLoggerCreated(String className)
   {
      if (!loggers.containsKey(className))
      {
         Logger logger = LogManager.getLogger(className);

         // Track the loggers that use this class
         loggers.put(className, logger);
      }
   }

   private static StackTraceElement origin()
   {
      return Thread.currentThread().getStackTrace()[2];
   }

   private static String format(StackTraceElement origin, String message)
   {
      return clickableCoordinatePrefix(origin) + message;
   }

   private static String clickableCoordinatePrefix(StackTraceElement origin)
   {
      return "(" + className(origin) + ":" + origin.getLineNumber() + "): ";
   }

   private static String className(StackTraceElement origin)
   {
      String[] classNameSplit = origin.getClassName().split("\\.");
      return classNameSplit[classNameSplit.length - 1].split("\\$")[0];
   }

   // BEGIN BOILERPLATE API

   private static void logIfEnabled(String loggerName, Level level, String message)
   {
      Logger logger = getLogger(loggerName);
      if (logger.isEnabled(level))
      {
         StackTraceElement origin = origin();
         logger.log(level, format(origin, message));
      }
   }

   private static void logIfEnabled(String loggerName, Level level, Supplier<?> msgSupplier)
   {
      Logger logger = getLogger(loggerName);
      if (logger.isEnabled(level))
      {
         StackTraceElement origin = origin();
         logger.log(level, format(origin, msgSupplier.get().toString()));
      }
   }

   private static void logIfEnabled(String loggerName, Level level, String message, Supplier<?> msgSupplier)
   {
      Logger logger = getLogger(loggerName);
      if (logger.isEnabled(level))
      {
         StackTraceElement origin = origin();
         logger.log(level, format(origin, message), msgSupplier);
      }
   }

   private static void logIfEnabled(String loggerName, Level level, String message, Object p0)
   {
      Logger logger = getLogger(loggerName);
      if (logger.isEnabled(level))
      {
         StackTraceElement origin = origin();
         logger.log(level, format(origin, message), p0);
      }
   }

   private static void logIfEnabled(String loggerName, Level level, String message, Object p0, Object p1)
   {
      Logger logger = getLogger(loggerName);
      if (logger.isEnabled(level))
      {
         StackTraceElement origin = origin();
         logger.log(level, format(origin, message), p0, p1);
      }
   }

   private static void logIfEnabled(String loggerName, Level level, String message, Object p0, Object p1, Object p2)
   {
      Logger logger = getLogger(loggerName);
      if (logger.isEnabled(level))
      {
         StackTraceElement origin = origin();
         logger.log(level, format(origin, message), p0, p1, p2);
      }
   }

   public static void fatal(String message)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.FATAL, message);
   }

   public static void fatal(Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.FATAL, msgSupplier);
   }

   public static void fatal(String message, Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.FATAL, message, msgSupplier);
   }

   public static void fatal(String message, Object p0)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.FATAL, message, p0);
   }

   public static void fatal(String message, Object p0, Object p1)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.FATAL, message, p0, p1);
   }

   public static void fatal(String message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.FATAL, message, p0, p1, p2);
   }

   public static void error(String message)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.ERROR, message);
   }

   public static void error(Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.ERROR, msgSupplier);
   }

   public static void error(String message, Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.ERROR, message, msgSupplier);
   }

   public static void error(String message, Object p0)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.ERROR, message, p0);
   }

   public static void error(String message, Object p0, Object p1)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.ERROR, message, p0, p1);
   }

   public static void error(String message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.ERROR, message, p0, p1, p2);
   }

   public static void warn(String message)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.WARN, message);
   }

   public static void warn(Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.WARN, msgSupplier);
   }

   public static void warn(String message, Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.WARN, message, msgSupplier);
   }

   public static void warn(String message, Object p0)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.WARN, message, p0);
   }

   public static void warn(String message, Object p0, Object p1)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.WARN, message, p0, p1);
   }

   public static void warn(String message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.WARN, message, p0, p1, p2);
   }

   public static void info(String message)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.INFO, message);
   }

   public static void info(Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.INFO, msgSupplier);
   }

   public static void info(String message, Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.INFO, message, msgSupplier);
   }

   public static void info(String message, Object p0)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.INFO, message, p0);
   }

   public static void info(String message, Object p0, Object p1)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.INFO, message, p0, p1);
   }

   public static void info(String message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.INFO, message, p0, p1, p2);
   }

   public static void debug(String message)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.DEBUG, message);
   }

   public static void debug(Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.DEBUG, msgSupplier);
   }

   public static void debug(String message, Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.DEBUG, message, msgSupplier);
   }

   public static void debug(String message, Object p0)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.DEBUG, message, p0);
   }

   public static void debug(String message, Object p0, Object p1)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.DEBUG, message, p0, p1);
   }

   public static void debug(String message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.DEBUG, message, p0, p1, p2);
   }

   public static void trace(String message)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.TRACE, message);
   }

   public static void trace(Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.TRACE, msgSupplier);
   }

   public static void trace(String message, Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.TRACE, message, msgSupplier);
   }

   public static void trace(String message, Object p0)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.TRACE, message, p0);
   }

   public static void trace(String message, Object p0, Object p1)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.TRACE, message, p0, p1);
   }

   public static void trace(String message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(IHMC_DEFAULT_LOGGER_NAME, Level.TRACE, message, p0, p1, p2);
   }
}
