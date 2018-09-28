package us.ihmc.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
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
      ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
      LoggerContext loggerContext = rootLogger.getLoggerContext();
      // we are not interested in auto-configuration
      loggerContext.reset();

      PatternLayoutEncoder encoder = new PatternLayoutEncoder();
      encoder.setContext(loggerContext);
      encoder.setPattern("[%level] %date{EEE hh:mm a} %message%n");
      encoder.start();

      ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<ILoggingEvent>();
      appender.setContext(loggerContext);
      appender.setEncoder(encoder);
      appender.start();

      rootLogger.addAppender(appender);
      rootLogger.setLevel(Level.WARN);
      rootLogger.setAdditive(false);
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


         //         ((ch.qos.logback.classic.Logger) logger).
         // This line is questionable. It's to disable the default handler
         // Another solution could be to use LogManager.getLogManager().reset();
         //         logger.setUseParentHandlers(false);

         // Set up INFO, CONFIG, FINE, FINER, FINEST to use System.out
         //         StreamHandler infoAndDownHandler = new StreamHandler(System.out, createFormatter());
         //         infoAndDownHandler.setFilter(record -> record.getLevel().intValue() <= Level.INFO.intValue());
         //         logger.addHandler(infoAndDownHandler);

         // Set up error, warn to use System.err
         //         StreamHandler warnAndUpHandler = new StreamHandler(System.err, createFormatter());
         //         warnAndUpHandler.setLevel(Level.warn);
         //         logger.addHandler(warnAndUpHandler);
      }
   }

   //   private static Formatter createFormatter()
   //   {
   //      return new Formatter()
   //      {
   //         @Override
   //         public synchronized String format(LogRecord record)
   //         {
   //            String message = formatMessage(record);
   //            return "[" + record.getLevel().getLocalizedName() + "] " + message + "\n";
   //         }
   //      };
   //   }

   public class MySampleLayout extends LayoutBase<ILoggingEvent>
   {
      public String doLayout(ILoggingEvent event) {
         StringBuffer sbuf = new StringBuffer(128);
         //         sbuf.append(event.getTimeStamp() - event.getLoggingContextVO.getBirthTime());
         //         sbuf.append(" ");
         sbuf.append("[");
         sbuf.append(event.getLevel());
         //         sbuf.append(event.getThreadName());
         sbuf.append("] ");
         //         sbuf.append(event.getLoggerName();
         //         sbuf.append(" - ");
         sbuf.append(event.getFormattedMessage());
         sbuf.append(CoreConstants.LINE_SEPARATOR);
         return sbuf.toString();
      }
   }

   public static void error(String message)
   {
      Throwable throwable = new Throwable();
      getLogger(className(throwable)).error(log(null, message, throwable));
   }

   public static void warn(String message)
   {
      Throwable throwable = new Throwable();
      getLogger(className(throwable)).warn(log(null, message, throwable));
   }

   public static void info(String message)
   {
      Throwable throwable = new Throwable();
      getLogger(className(throwable)).info(log(null, message, throwable));
   }

   public static void debug(String message)
   {
      Throwable throwable = new Throwable();
      getLogger(className(throwable)).debug(log(null, message, throwable));
   }

   public static void error(Logger logger, String message)
   {
      logger.error(log(null, message, new Throwable()));
   }

   public static void warn(Logger logger, String message)
   {
      logger.warn(log(null, message, new Throwable()));
   }

   public static void info(Logger logger, String message)
   {
      warn(log(null, message, new Throwable()));
   }

   public static void debug(Logger logger, String message)
   {
      logger.debug(log(null, message, new Throwable()));
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

      String clickableLocation = "(" + className + ":" + lineNumber + ")";

      return clickableLocation + ": " + message;
   }
}
