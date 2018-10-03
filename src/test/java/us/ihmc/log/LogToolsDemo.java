package us.ihmc.log;

import us.ihmc.commons.allocations.AllocationProfiler;

import static us.ihmc.log.LogTools.*;

public class LogToolsDemo
{
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
   }
}
