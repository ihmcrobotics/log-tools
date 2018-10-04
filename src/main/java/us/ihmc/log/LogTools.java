package us.ihmc.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.HashMap;
import java.util.function.Supplier;

public class LogTools
{
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

   /**
    * Boolean value to say that if the user sets the mode explicitly (i.e. -Dlog.granular=*)
    * then don't let a custom level set override that (i.e. -Dlog.level.*=*
    */
   private static boolean GRANULAR_MODE_SET_EXPLICITLY = false;

   // This block runs when the first call to LogTools happens and the class is loaded
   // into the JVM.
   static
   {
      String granular = System.getProperty("log.granular");
      if (granular != null)
      {
         GRANULAR_MODE_SET_EXPLICITLY = true; // the user set this mode, so don't auto switch
         if (granular.trim().toLowerCase().contains("true"))
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
               if (afterLogLevel.isEmpty() || afterLogLevel.equals(".")) // setting log4j root level
               {
                  setLevel(stringKey, LogManager.ROOT_LOGGER_NAME);
               }
               else if (afterLogLevel.equals("." + IHMC_ROOT_LOGGER_NAME)) // setting ihmc root level
               {
                  setLevel(stringKey, IHMC_ROOT_LOGGER_NAME); // don't auto switch to granular
               }
               else if (afterLogLevel.startsWith(".")) // granular level set
               {
                  if (!GRANULAR_MODE_SET_EXPLICITLY) // if the user hasn't explicitly set granular mode
                     GRANULAR_MODE = true;           // auto switch to that mode, otherwise this property would not make sense
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

   /**
    * Keep a list of loggers, so we don't recreate a bunch of formatters.
    */
   private static final HashMap<String, Logger> loggers = GRANULAR_MODE ? new HashMap<>() : null;

   /**
    * Gets or retrieves a logger instance by name.
    */
   private static final Logger getLogger(String loggerName)
   {
      if (!GRANULAR_MODE)
         throw new RuntimeException("getLogger() should never be called when GRANULAR_MODE = false");

      Logger maybeLogger = loggers.get(loggerName);
      if (maybeLogger != null)
      {
         return maybeLogger;
      }
      else
      {
         Logger logger = LogManager.getLogger(loggerName);
         loggers.put(loggerName, logger);
         return logger;
      }
   }

   /**
    * The IHMC root logger instance.
    */
   private static final Logger IHMC_ROOT_LOGGER = GRANULAR_MODE ? getLogger(IHMC_ROOT_LOGGER_NAME) : LogManager.getLogger(IHMC_ROOT_LOGGER_NAME);

   static
   {
      if (GRANULAR_MODE)
      {
         info("Granular logging mode enabled. Not realtime safe.");
      }
   }

   private static StackTraceElement origin()
   {
      return Thread.currentThread().getStackTrace()[4];
   }

   private static String format(StackTraceElement origin, String message)
   {
      return clickableCoordinatePrefix(origin) + message;
   }

   private static String clickableCoordinatePrefix(StackTraceElement origin)
   {
      return "(" + classSimpleName(origin) + ":" + origin.getLineNumber() + "): ";
   }

   private static String classSimpleName(StackTraceElement origin)
   {
      String[] classNameSplit = origin.getClassName().split("\\.");
      return classNameSplit[classNameSplit.length - 1].split("\\$")[0];
   }

   private static String classFromOrigin(StackTraceElement origin)
   {
      return origin.getClassName().replaceAll("\\$", ".");
   }

   // BEGIN BOILERPLATE API

   private static void logIfEnabled(Level level, String message)
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         if (IHMC_ROOT_LOGGER.isEnabled(level)) // simple O(1) boolean check
         {
            StackTraceElement origin = origin(); // here it is OK to start allocating, this log message is enabled
            IHMC_ROOT_LOGGER.log(level, format(origin, message));
         }
      }
      else // granular = true
      {
         StackTraceElement origin = origin(); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         if (logger.isEnabled(level))
         {
            logger.log(level, format(origin, message));
         }
      }
   }

   private static void logIfEnabled(Level level, Supplier<?> msgSupplier)
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         if (IHMC_ROOT_LOGGER.isEnabled(level)) // simple O(1) boolean check
         {
            StackTraceElement origin = origin(); // here it is OK to start allocating, this log message is enabled
            IHMC_ROOT_LOGGER.log(level, format(origin, msgSupplier.get().toString()));
         }
      }
      else // granular = true
      {
         StackTraceElement origin = origin(); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         if (logger.isEnabled(level))
         {
            logger.log(level, format(origin, msgSupplier.get().toString()));
         }
      }
   }

   private static void logIfEnabled(Level level, String message, Supplier<?> msgSupplier)
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         if (IHMC_ROOT_LOGGER.isEnabled(level)) // simple O(1) boolean check
         {
            StackTraceElement origin = origin(); // here it is OK to start allocating, this log message is enabled
            IHMC_ROOT_LOGGER.log(level, format(origin, message), msgSupplier);
         }
      }
      else // granular = true
      {
         StackTraceElement origin = origin(); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         if (logger.isEnabled(level))
         {
            logger.log(level, format(origin, message), msgSupplier);
         }
      }
   }

   private static void logIfEnabled(Level level, String message, Object p0)
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         if (IHMC_ROOT_LOGGER.isEnabled(level)) // simple O(1) boolean check
         {
            StackTraceElement origin = origin(); // here it is OK to start allocating, this log message is enabled
            IHMC_ROOT_LOGGER.log(level, format(origin, message), p0);
         }
      }
      else // granular = true
      {
         StackTraceElement origin = origin(); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         if (logger.isEnabled(level))
         {
            logger.log(level, format(origin, message), p0);
         }
      }
   }

   private static void logIfEnabled(Level level, String message, Object p0, Object p1)
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         if (IHMC_ROOT_LOGGER.isEnabled(level)) // simple O(1) boolean check
         {
            StackTraceElement origin = origin(); // here it is OK to start allocating, this log message is enabled
            IHMC_ROOT_LOGGER.log(level, format(origin, message), p0, p1);
         }
      }
      else // granular = true
      {
         StackTraceElement origin = origin(); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         if (logger.isEnabled(level))
         {
            logger.log(level, format(origin, message), p0, p1);
         }
      }
   }

   private static void logIfEnabled(Level level, String message, Object p0, Object p1, Object p2)
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         if (IHMC_ROOT_LOGGER.isEnabled(level)) // simple O(1) boolean check
         {
            StackTraceElement origin = origin(); // here it is OK to start allocating, this log message is enabled
            IHMC_ROOT_LOGGER.log(level, format(origin, message), p0, p1, p2);
         }
      }
      else // granular = true
      {
         StackTraceElement origin = origin(); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         if (logger.isEnabled(level))
         {
            logger.log(level, format(origin, message), p0, p1, p2);
         }
      }
   }

   public static void fatal(String message)
   {
      logIfEnabled(Level.FATAL, message);
   }

   public static void fatal(Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.FATAL, msgSupplier);
   }

   public static void fatal(String message, Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.FATAL, message, msgSupplier);
   }

   public static void fatal(String message, Object p0)
   {
      logIfEnabled(Level.FATAL, message, p0);
   }

   public static void fatal(String message, Object p0, Object p1)
   {
      logIfEnabled(Level.FATAL, message, p0, p1);
   }

   public static void fatal(String message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(Level.FATAL, message, p0, p1, p2);
   }

   public static void error(String message)
   {
      logIfEnabled(Level.ERROR, message);
   }

   public static void error(Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.ERROR, msgSupplier);
   }

   public static void error(String message, Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.ERROR, message, msgSupplier);
   }

   public static void error(String message, Object p0)
   {
      logIfEnabled(Level.ERROR, message, p0);
   }

   public static void error(String message, Object p0, Object p1)
   {
      logIfEnabled(Level.ERROR, message, p0, p1);
   }

   public static void error(String message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(Level.ERROR, message, p0, p1, p2);
   }

   public static void warn(String message)
   {
      logIfEnabled(Level.WARN, message);
   }

   public static void warn(Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.WARN, msgSupplier);
   }

   public static void warn(String message, Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.WARN, message, msgSupplier);
   }

   public static void warn(String message, Object p0)
   {
      logIfEnabled(Level.WARN, message, p0);
   }

   public static void warn(String message, Object p0, Object p1)
   {
      logIfEnabled(Level.WARN, message, p0, p1);
   }

   public static void warn(String message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(Level.WARN, message, p0, p1, p2);
   }

   public static void info(String message)
   {
      logIfEnabled(Level.INFO, message);
   }

   public static void info(Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.INFO, msgSupplier);
   }

   public static void info(String message, Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.INFO, message, msgSupplier);
   }

   public static void info(String message, Object p0)
   {
      logIfEnabled(Level.INFO, message, p0);
   }

   public static void info(String message, Object p0, Object p1)
   {
      logIfEnabled(Level.INFO, message, p0, p1);
   }

   public static void info(String message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(Level.INFO, message, p0, p1, p2);
   }

   public static void debug(String message)
   {
      logIfEnabled(Level.DEBUG, message);
   }

   public static void debug(Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.DEBUG, msgSupplier);
   }

   public static void debug(String message, Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.DEBUG, message, msgSupplier);
   }

   public static void debug(String message, Object p0)
   {
      logIfEnabled(Level.DEBUG, message, p0);
   }

   public static void debug(String message, Object p0, Object p1)
   {
      logIfEnabled(Level.DEBUG, message, p0, p1);
   }

   public static void debug(String message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(Level.DEBUG, message, p0, p1, p2);
   }

   public static void trace(String message)
   {
      logIfEnabled(Level.TRACE, message);
   }

   public static void trace(Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.TRACE, msgSupplier);
   }

   public static void trace(String message, Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.TRACE, message, msgSupplier);
   }

   public static void trace(String message, Object p0)
   {
      logIfEnabled(Level.TRACE, message, p0);
   }

   public static void trace(String message, Object p0, Object p1)
   {
      logIfEnabled(Level.TRACE, message, p0, p1);
   }

   public static void trace(String message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(Level.TRACE, message, p0, p1, p2);
   }
}
