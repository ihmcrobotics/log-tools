package us.ihmc.log;

import org.apache.logging.log4j.Level;

import java.util.function.Supplier;

public interface LogToolsWriteOnly
{
   void log(Level level, Object message);
   void log(Level level, int additionalStackTraceHeight, Object message);
   void log(Level level, Supplier<?> msgSupplier);
   void log(Level level, Object message, Supplier<?> msgSupplier);
   void log(Level level, Object message, Object p0);
   void log(Level level, Object message, Object p0, Object p1);
   void log(Level level, Object message, Object p0, Object p1, Object p2);
   void fatal(Object message);
   void fatal(int additionalStackTraceHeight, Object message);
   void fatal(Supplier<?> msgSupplier);
   void fatal(Object message, Supplier<?> msgSupplier);
   void fatal(Object message, Object p0);
   void fatal(Object message, Object p0, Object p1);
   void fatal(Object message, Object p0, Object p1, Object p2);
   void error(Object message);
   void error(int additionalStackTraceHeight, Object message);
   void error(Supplier<?> msgSupplier);
   void error(Object message, Supplier<?> msgSupplier);
   void error(Object message, Object p0);
   void error(Object message, Object p0, Object p1);
   void error(Object message, Object p0, Object p1, Object p2);
   void warn(Object message);
   void warn(int additionalStackTraceHeight, Object message);
   void warn(Supplier<?> msgSupplier);
   void warn(Object message, Supplier<?> msgSupplier);
   void warn(Object message, Object p0);
   void warn(Object message, Object p0, Object p1);
   void warn(Object message, Object p0, Object p1, Object p2);
   void info(Object message);
   void info(int additionalStackTraceHeight, Object message);
   void info(Supplier<?> msgSupplier);
   void info(Object message, Supplier<?> msgSupplier);
   void info(Object message, Object p0);
   void info(Object message, Object p0, Object p1);
   void info(Object message, Object p0, Object p1, Object p2);
   void debug(Object message);
   void debug(int additionalStackTraceHeight, Object message);
   void debug(Supplier<?> msgSupplier);
   void debug(Object message, Supplier<?> msgSupplier);
   void debug(Object message, Object p0);
   void debug(Object message, Object p0, Object p1);
   void debug(Object message, Object p0, Object p1, Object p2);
   void trace(Object message);
   void trace(int additionalStackTraceHeight, Object message);
   void trace(Supplier<?> msgSupplier);
   void trace(Object message, Supplier<?> msgSupplier);
   void trace(Object message, Object p0);
   void trace(Object message, Object p0, Object p1);
   void trace(Object message, Object p0, Object p1, Object p2);
}
