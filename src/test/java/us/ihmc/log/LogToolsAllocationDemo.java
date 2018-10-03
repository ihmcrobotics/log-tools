package us.ihmc.log;

import us.ihmc.commons.allocations.AllocationProfiler;

import java.util.function.Supplier;

import static us.ihmc.log.LogTools.*;

public class LogToolsAllocationDemo
{
   public LogToolsAllocationDemo()
   {
      new SubclassOne();
   }

   public class SubclassOne
   {
      public SubclassOne()
      {
         LogTools.debug("Hello subsclass level 1");
         new SubSubClassOne();
      }

      class SubSubClassOne
      {
         public SubSubClassOne()
         {
            LogTools.debug("Hi I'm subclass 2");
         }
      }
   }

   /**
    * Run this demo with the -javaagent and -Dlog.level=off JVM properties
    * (run `gradle` to see a printout of the full argument)
    */
   public static void main(String[] args)
   {
      String demo_there = "Demo there";
      error(demo_there);
      warn(demo_there);
      info(demo_there);
      debug(demo_there);
      trace(demo_there);

      AllocationProfiler profiler = new AllocationProfiler();
      profiler.setRecordClassLoader(true);
      profiler.setRecordStaticMemberInitialization(true);

      System.out.println("Wrapped integer:\n" + profiler.recordAllocations(() -> {
         info(demo_there, new Integer(9));
      }));

      System.out.println("Inline consts:\n" + profiler.recordAllocations(() -> {
         info(demo_there, 9, true);
      }));

      Supplier<Object> stringSupplier = () -> {
         StringBuilder builder = new StringBuilder();
         builder.append("hello");
         builder.append("there");
         return builder.toString();
      };
      System.out.println("String supplier:\n" + profiler.recordAllocations(() -> {
         info(demo_there, stringSupplier);
      }));

      System.out.println("New class with subclasses:\n" + profiler.recordAllocations(() -> {
         new LogToolsAllocationDemo();
      }));
   }
}
