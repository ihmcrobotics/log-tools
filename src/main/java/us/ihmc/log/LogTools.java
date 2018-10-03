package us.ihmc.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.HashMap;
import java.util.function.Supplier;

public class LogTools
{
   /** Keep a list of loggers, so we don't recreate a bunch of formatters */
   private static final HashMap<String, Logger> loggers = new HashMap<>();

   /**
    * Unless granular mode is enabled, all log messages go through the IHMC root logger,
    * named "us.ihmc" after the common package containing all IHMC code.
    *
    * Note: Even if code is not in the package "us.ihmc", it will still use the
    * IHMC root logger unless granular mode enabled.
    */
   private static final String IHMC_ROOT_LOGGER_NAME = "us.ihmc";

   /**
    * Granular mode is enabled with -Dlog.granular=true and allows the user to set
    * levels based on package and class name.
    *
    * This mode is disabled by default to remove the need to allocate Throwables
    * to find out which logger to check is enabled. This both provides realtime
    * safety and removes cost of having many low level log statements (i.e. DEBUG,
    * TRACE).
    */
   private static boolean GRANULAR_MODE = false;

   static
   {
      String granular = System.getProperty("log.granular");
      if (granular != null && granular instanceof String)
      {
         String granularString = (String) granular;
         if (granularString.trim().toLowerCase().contains("true"))
         {
            GRANULAR_MODE = true;
         }
      }

      for (Object key : System.getProperties().keySet())
      {
         if (key instanceof String)
         {
            String stringKey = (String) key;
            if (stringKey.startsWith("log.level"))
            {
               String afterLogLevel = stringKey.substring(9);
               if (afterLogLevel.isEmpty() || afterLogLevel.equals(".")) // root level
               {
                  setLevel(stringKey, LogManager.ROOT_LOGGER_NAME);
               }
               else if (afterLogLevel.startsWith(".")) // custom level
               {
                  setLevel(stringKey, afterLogLevel.substring(1));
               }
            }
         }
      }
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

   private static final Logger getLog4J2Logger(String loggerName)
   {
      checkLoggerCreated(loggerName);

      return loggers.get(loggerName);
   }

   private static void checkLoggerCreated(String loggerName)
   {
      if (!loggers.containsKey(loggerName))
      {
         StackTraceElement origin = origin();

         Logger logger = LogManager.getLogger(loggerName);

         // Track the loggers that use this class
         loggers.put(loggerName, logger);
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
      Logger logger = getLog4J2Logger(loggerName);
      if (logger.isEnabled(level))
      {
         StackTraceElement origin = origin();
         logger.log(level, format(origin, message));
      }
   }

   private static void logIfEnabled(String loggerName, Level level, Supplier<?> msgSupplier)
   {
      Logger logger = getLog4J2Logger(loggerName);
      if (logger.isEnabled(level))
      {
         StackTraceElement origin = origin();
         logger.log(level, format(origin, msgSupplier.get().toString()));
      }
   }

   private static void logIfEnabled(String loggerName, Level level, String message, Supplier<?> msgSupplier)
   {
      Logger logger = getLog4J2Logger(loggerName);
      if (logger.isEnabled(level))
      {
         StackTraceElement origin = origin();
         logger.log(level, format(origin, message), msgSupplier);
      }
   }

   private static void logIfEnabled(String loggerName, Level level, String message, Object p0)
   {
      Logger logger = getLog4J2Logger(loggerName);
      if (logger.isEnabled(level))
      {
         StackTraceElement origin = origin();
         logger.log(level, format(origin, message), p0);
      }
   }

   private static void logIfEnabled(String loggerName, Level level, String message, Object p0, Object p1)
   {
      Logger logger = getLog4J2Logger(loggerName);
      if (logger.isEnabled(level))
      {
         StackTraceElement origin = origin();
         logger.log(level, format(origin, message), p0, p1);
      }
   }

   private static void logIfEnabled(String loggerName, Level level, String message, Object p0, Object p1, Object p2)
   {
      Logger logger = getLog4J2Logger(loggerName);
      if (logger.isEnabled(level))
      {
         StackTraceElement origin = origin();
         logger.log(level, format(origin, message), p0, p1, p2);
      }
   }

   public static void fatal(String message)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.FATAL, message);
   }

   public static void fatal(Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.FATAL, msgSupplier);
   }

   public static void fatal(String message, Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.FATAL, message, msgSupplier);
   }

   public static void fatal(String message, Object p0)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.FATAL, message, p0);
   }

   public static void fatal(String message, Object p0, Object p1)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.FATAL, message, p0, p1);
   }

   public static void fatal(String message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.FATAL, message, p0, p1, p2);
   }

   public static void error(String message)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.ERROR, message);
   }

   public static void error(Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.ERROR, msgSupplier);
   }

   public static void error(String message, Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.ERROR, message, msgSupplier);
   }

   public static void error(String message, Object p0)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.ERROR, message, p0);
   }

   public static void error(String message, Object p0, Object p1)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.ERROR, message, p0, p1);
   }

   public static void error(String message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.ERROR, message, p0, p1, p2);
   }

   public static void warn(String message)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.WARN, message);
   }

   public static void warn(Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.WARN, msgSupplier);
   }

   public static void warn(String message, Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.WARN, message, msgSupplier);
   }

   public static void warn(String message, Object p0)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.WARN, message, p0);
   }

   public static void warn(String message, Object p0, Object p1)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.WARN, message, p0, p1);
   }

   public static void warn(String message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.WARN, message, p0, p1, p2);
   }

   public static void info(String message)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.INFO, message);
   }

   public static void info(Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.INFO, msgSupplier);
   }

   public static void info(String message, Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.INFO, message, msgSupplier);
   }

   public static void info(String message, Object p0)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.INFO, message, p0);
   }

   public static void info(String message, Object p0, Object p1)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.INFO, message, p0, p1);
   }

   public static void info(String message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.INFO, message, p0, p1, p2);
   }

   public static void debug(String message)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.DEBUG, message);
   }

   public static void debug(Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.DEBUG, msgSupplier);
   }

   public static void debug(String message, Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.DEBUG, message, msgSupplier);
   }

   public static void debug(String message, Object p0)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.DEBUG, message, p0);
   }

   public static void debug(String message, Object p0, Object p1)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.DEBUG, message, p0, p1);
   }

   public static void debug(String message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.DEBUG, message, p0, p1, p2);
   }

   public static void trace(String message)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.TRACE, message);
   }

   public static void trace(Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.TRACE, msgSupplier);
   }

   public static void trace(String message, Supplier<?> msgSupplier)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.TRACE, message, msgSupplier);
   }

   public static void trace(String message, Object p0)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.TRACE, message, p0);
   }

   public static void trace(String message, Object p0, Object p1)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.TRACE, message, p0, p1);
   }

   public static void trace(String message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(IHMC_ROOT_LOGGER_NAME, Level.TRACE, message, p0, p1, p2);
   }
}
