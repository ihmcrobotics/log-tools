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
   void log(Level level, String message);
   void log(Level level, String message, Supplier<?> msgSupplier);
   void log(Level level, String message, Object p0);
   void log(Level level, String message, Object p0, Object p1);
   void log(Level level, String message, Object p0, Object p1, Object p2);
   void fatal(Object message);
   void fatal(int additionalStackTraceHeight, Object message);
   void fatal(Supplier<?> msgSupplier);
   void fatal(Object message, Supplier<?> msgSupplier);
   void fatal(Object message, Object p0);
   void fatal(Object message, Object p0, Object p1);
   void fatal(Object message, Object p0, Object p1, Object p2);
   void fatal(String message);
   void fatal(String message, Supplier<?> msgSupplier);
   void fatal(String message, Object p0);
   void fatal(String message, Object p0, Object p1);
   void fatal(String message, Object p0, Object p1, Object p2);
   void error(Object message);
   
}
