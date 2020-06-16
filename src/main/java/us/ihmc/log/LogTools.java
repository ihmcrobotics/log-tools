package us.ihmc.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.HashMap;
import java.util.TreeSet;
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

      TreeSet<Object> sortedKeys = new TreeSet<>();  // sort the keys so the behavior is independent of property order
      sortedKeys.addAll(System.getProperties().keySet());   // this also fixes a bug where levels should always be set breadth first
      for (Object key : sortedKeys)
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
                  setLevel(stringKey, IHMC_ROOT_LOGGER_NAME); // also set the ihmc level, there are two loggers present, root and ihmc
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
   private static Logger getLogger(String loggerName)
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

   private static StackTraceElement origin(int additionalStackTraceHeight)
   {
      return Thread.currentThread().getStackTrace()[4 + additionalStackTraceHeight];
   }

   private static StackTraceElement origin()
   {
      return Thread.currentThread().getStackTrace()[4];
   }

   private static String format(StackTraceElement origin, Object message)
   {
      return clickableCoordinatePrefix(origin) + (message == null ? null : message.toString());
   }

   private static String clickableCoordinatePrefix(StackTraceElement origin)
   {
      return "(" + classSimpleName(origin) + ".java:" + origin.getLineNumber() + "): ";
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

   private static void logIfEnabled(Level level, Object message)
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

   private static void logIfEnabled(Level level, int additionalStackTraceHeight, Object message)
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         if (IHMC_ROOT_LOGGER.isEnabled(level)) // simple O(1) boolean check
         {
            StackTraceElement origin = origin(additionalStackTraceHeight); // here it is OK to start allocating, this log message is enabled
            IHMC_ROOT_LOGGER.log(level, format(origin, message));
         }
      }
      else // granular = true
      {
         StackTraceElement origin = origin(additionalStackTraceHeight); // allocate throwable even if level is disabled
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
            IHMC_ROOT_LOGGER.log(level, format(origin, msgSupplier.get()));
         }
      }
      else // granular = true
      {
         StackTraceElement origin = origin(); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         if (logger.isEnabled(level))
         {
            logger.log(level, format(origin, msgSupplier.get()));
         }
      }
   }

   private static void logIfEnabled(Level level, Object message, Supplier<?> msgSupplier)
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         if (IHMC_ROOT_LOGGER.isEnabled(level)) // simple O(1) boolean check
         {
            StackTraceElement origin = origin(); // here it is OK to start allocating, this log message is enabled
            IHMC_ROOT_LOGGER.log(level, format(origin, message), msgSupplier.get());
         }
      }
      else // granular = true
      {
         StackTraceElement origin = origin(); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         if (logger.isEnabled(level))
         {
            logger.log(level, format(origin, message), msgSupplier.get());
         }
      }
   }

   private static void logIfEnabled(Level level, Object message, Object p0)
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

   private static void logIfEnabled(Level level, Object message, Object p0, Object p1)
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

   private static void logIfEnabled(Level level, Object message, Object p0, Object p1, Object p2)
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

   public static boolean isEnabled(Level level)
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         return IHMC_ROOT_LOGGER.isEnabled(level); // simple O(1) boolean check
      }
      else // granular = true
      {
         StackTraceElement origin = origin(-1); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         return logger.isEnabled(level);
      }
   }

   public static boolean isEnabled(Level level, int additionalStackTraceHeight)
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         return IHMC_ROOT_LOGGER.isEnabled(level); // simple O(1) boolean check
      }
      else // granular = true
      {
         StackTraceElement origin = origin(-1 + additionalStackTraceHeight); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         return logger.isEnabled(level);
      }
   }

   public static boolean isFatalEnabled()
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         return IHMC_ROOT_LOGGER.isEnabled(Level.FATAL); // simple O(1) boolean check
      }
      else // granular = true
      {
         StackTraceElement origin = origin(-1); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         return logger.isEnabled(Level.FATAL);
      }
   }

   public static boolean isFatalEnabled(int additionalStackTraceHeight)
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         return IHMC_ROOT_LOGGER.isEnabled(Level.FATAL); // simple O(1) boolean check
      }
      else // granular = true
      {
         StackTraceElement origin = origin(-1 + additionalStackTraceHeight); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         return logger.isEnabled(Level.FATAL);
      }
   }

   public static boolean isErrorEnabled()
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         return IHMC_ROOT_LOGGER.isEnabled(Level.ERROR); // simple O(1) boolean check
      }
      else // granular = true
      {
         StackTraceElement origin = origin(-1); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         return logger.isEnabled(Level.ERROR);
      }
   }

   public static boolean isErrorEnabled(int additionalStackTraceHeight)
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         return IHMC_ROOT_LOGGER.isEnabled(Level.ERROR); // simple O(1) boolean check
      }
      else // granular = true
      {
         StackTraceElement origin = origin(-1 + additionalStackTraceHeight); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         return logger.isEnabled(Level.ERROR);
      }
   }

   public static boolean isWarnEnabled()
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         return IHMC_ROOT_LOGGER.isEnabled(Level.WARN); // simple O(1) boolean check
      }
      else // granular = true
      {
         StackTraceElement origin = origin(-1); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         return logger.isEnabled(Level.WARN);
      }
   }

   public static boolean isWarnEnabled(int additionalStackTraceHeight)
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         return IHMC_ROOT_LOGGER.isEnabled(Level.WARN); // simple O(1) boolean check
      }
      else // granular = true
      {
         StackTraceElement origin = origin(-1 + additionalStackTraceHeight); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         return logger.isEnabled(Level.WARN);
      }
   }

   public static boolean isInfoEnabled()
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         return IHMC_ROOT_LOGGER.isEnabled(Level.INFO); // simple O(1) boolean check
      }
      else // granular = true
      {
         StackTraceElement origin = origin(-1); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         return logger.isEnabled(Level.INFO);
      }
   }

   public static boolean isInfoEnabled(int additionalStackTraceHeight)
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         return IHMC_ROOT_LOGGER.isEnabled(Level.INFO); // simple O(1) boolean check
      }
      else // granular = true
      {
         StackTraceElement origin = origin(-1 + additionalStackTraceHeight); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         return logger.isEnabled(Level.INFO);
      }
   }

   public static boolean isDebugEnabled()
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         return IHMC_ROOT_LOGGER.isEnabled(Level.DEBUG); // simple O(1) boolean check
      }
      else // granular = true
      {
         StackTraceElement origin = origin(-1); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         return logger.isEnabled(Level.DEBUG);
      }
   }

   public static boolean isDebugEnabled(int additionalStackTraceHeight)
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         return IHMC_ROOT_LOGGER.isEnabled(Level.DEBUG); // simple O(1) boolean check
      }
      else // granular = true
      {
         StackTraceElement origin = origin(-1 + additionalStackTraceHeight); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         return logger.isEnabled(Level.DEBUG);
      }
   }

   public static boolean isTraceEnabled()
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         return IHMC_ROOT_LOGGER.isEnabled(Level.TRACE); // simple O(1) boolean check
      }
      else // granular = true
      {
         StackTraceElement origin = origin(-1); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         return logger.isEnabled(Level.TRACE);
      }
   }

   public static boolean isTraceEnabled(int additionalStackTraceHeight)
   {
      if (!GRANULAR_MODE) // default, realtime safe mode
      {
         return IHMC_ROOT_LOGGER.isEnabled(Level.TRACE); // simple O(1) boolean check
      }
      else // granular = true
      {
         StackTraceElement origin = origin(-1 + additionalStackTraceHeight); // allocate throwable even if level is disabled
         Logger logger = getLogger(classFromOrigin(origin)); // get logger based on class name
         return logger.isEnabled(Level.TRACE);
      }
   }

   public static void log(Level level, Object message)
   {
      logIfEnabled(level, message);
   }

   public static void log(Level level, int additionalStackTraceHeight, Object message)
   {
      logIfEnabled(level, additionalStackTraceHeight, message);
   }

   public static void log(Level level, Supplier<?> msgSupplier)
   {
      logIfEnabled(level, msgSupplier);
   }

   public static void log(Level level, Object message, Supplier<?> msgSupplier)
   {
      logIfEnabled(level, message, msgSupplier);
   }

   public static void log(Level level, Object message, Object p0)
   {
      logIfEnabled(level, message, p0);
   }

   public static void log(Level level, Object message, Object p0, Object p1)
   {
      logIfEnabled(level, message, p0, p1);
   }

   public static void log(Level level, Object message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(level, message, p0, p1, p2);
   }

   public static void log(Level level, String message)
   {
      logIfEnabled(level, message);
   }

   public static void log(Level level, String message, Supplier<?> msgSupplier)
   {
      logIfEnabled(level, message, msgSupplier);
   }

   public static void log(Level level, String message, Object p0)
   {
      logIfEnabled(level, message, p0);
   }

   public static void log(Level level, String message, Object p0, Object p1)
   {
      logIfEnabled(level, message, p0, p1);
   }

   public static void log(Level level, String message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(level, message, p0, p1, p2);
   }

   public static void fatal(Object message)
   {
      logIfEnabled(Level.FATAL, message);
   }

   public static void fatal(int additionalStackTraceHeight, Object message)
   {
      logIfEnabled(Level.FATAL, additionalStackTraceHeight, message);
   }

   public static void fatal(Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.FATAL, msgSupplier);
   }

   public static void fatal(Object message, Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.FATAL, message, msgSupplier);
   }

   public static void fatal(Object message, Object p0)
   {
      logIfEnabled(Level.FATAL, message, p0);
   }

   public static void fatal(Object message, Object p0, Object p1)
   {
      logIfEnabled(Level.FATAL, message, p0, p1);
   }

   public static void fatal(Object message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(Level.FATAL, message, p0, p1, p2);
   }

   public static void fatal(String message)
   {
      logIfEnabled(Level.FATAL, message);
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

   public static void error(Object message)
   {
      logIfEnabled(Level.ERROR, message);
   }

   public static void error(int additionalStackTraceHeight, Object message)
   {
      logIfEnabled(Level.ERROR, additionalStackTraceHeight, message);
   }

   public static void error(Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.ERROR, msgSupplier);
   }

   public static void error(Object message, Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.ERROR, message, msgSupplier);
   }

   public static void error(Object message, Object p0)
   {
      logIfEnabled(Level.ERROR, message, p0);
   }

   public static void error(Object message, Object p0, Object p1)
   {
      logIfEnabled(Level.ERROR, message, p0, p1);
   }

   public static void error(Object message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(Level.ERROR, message, p0, p1, p2);
   }

   public static void error(String message)
   {
      logIfEnabled(Level.ERROR, message);
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

   public static void warn(Object message)
   {
      logIfEnabled(Level.WARN, message);
   }

   public static void warn(int additionalStackTraceHeight, Object message)
   {
      logIfEnabled(Level.WARN, additionalStackTraceHeight, message);
   }

   public static void warn(Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.WARN, msgSupplier);
   }

   public static void warn(Object message, Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.WARN, message, msgSupplier);
   }

   public static void warn(Object message, Object p0)
   {
      logIfEnabled(Level.WARN, message, p0);
   }

   public static void warn(Object message, Object p0, Object p1)
   {
      logIfEnabled(Level.WARN, message, p0, p1);
   }

   public static void warn(Object message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(Level.WARN, message, p0, p1, p2);
   }

   public static void warn(String message)
   {
      logIfEnabled(Level.WARN, message);
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

   public static void info(Object message)
   {
      logIfEnabled(Level.INFO, message);
   }

   public static void info(int additionalStackTraceHeight, Object message)
   {
      logIfEnabled(Level.INFO, additionalStackTraceHeight, message);
   }

   public static void info(Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.INFO, msgSupplier);
   }

   public static void info(Object message, Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.INFO, message, msgSupplier);
   }

   public static void info(Object message, Object p0)
   {
      logIfEnabled(Level.INFO, message, p0);
   }

   public static void info(Object message, Object p0, Object p1)
   {
      logIfEnabled(Level.INFO, message, p0, p1);
   }

   public static void info(Object message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(Level.INFO, message, p0, p1, p2);
   }

   public static void info(String message)
   {
      logIfEnabled(Level.INFO, message);
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

   public static void debug(Object message)
   {
      logIfEnabled(Level.DEBUG, message);
   }

   public static void debug(int additionalStackTraceHeight, Object message)
   {
      logIfEnabled(Level.DEBUG, additionalStackTraceHeight, message);
   }

   public static void debug(Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.DEBUG, msgSupplier);
   }

   public static void debug(Object message, Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.DEBUG, message, msgSupplier);
   }

   public static void debug(Object message, Object p0)
   {
      logIfEnabled(Level.DEBUG, message, p0);
   }

   public static void debug(Object message, Object p0, Object p1)
   {
      logIfEnabled(Level.DEBUG, message, p0, p1);
   }

   public static void debug(Object message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(Level.DEBUG, message, p0, p1, p2);
   }

   public static void debug(String message)
   {
      logIfEnabled(Level.DEBUG, message);
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

   public static void trace(Object message)
   {
      logIfEnabled(Level.TRACE, message);
   }

   public static void trace(int additionalStackTraceHeight, Object message)
   {
      logIfEnabled(Level.TRACE, additionalStackTraceHeight, message);
   }

   public static void trace(Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.TRACE, msgSupplier);
   }

   public static void trace(Object message, Supplier<?> msgSupplier)
   {
      logIfEnabled(Level.TRACE, message, msgSupplier);
   }

   public static void trace(Object message, Object p0)
   {
      logIfEnabled(Level.TRACE, message, p0);
   }

   public static void trace(Object message, Object p0, Object p1)
   {
      logIfEnabled(Level.TRACE, message, p0, p1);
   }

   public static void trace(Object message, Object p0, Object p1, Object p2)
   {
      logIfEnabled(Level.TRACE, message, p0, p1, p2);
   }

   public static void trace(String message)
   {
      logIfEnabled(Level.TRACE, message);
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
