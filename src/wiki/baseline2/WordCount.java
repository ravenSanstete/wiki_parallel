/***
 * Excerpted from "Seven Concurrency Models in Seven Weeks",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/pb7con for more book information.
***/
package wiki.baseline2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class WordCount {
  final static public String option = "size";

  public static void parallel_exp(int parallel_num, int page_num) throws Exception {
    ArrayBlockingQueue<Page> queue = new ArrayBlockingQueue<Page>(1000);
    ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<String, Integer>();
    ExecutorService executor = Executors.newCachedThreadPool();

    for (int i = 0; i < parallel_num; ++i)
      executor.execute(new Counter(queue, counts));
    Thread parser = new Thread(new Parser(queue, page_num));
    long start = System.currentTimeMillis();
    parser.start();
    parser.join();
    for (int i = 0; i < parallel_num; ++i)
      queue.put(new PoisonPill());
    executor.shutdown();
    executor.awaitTermination(10L, TimeUnit.MINUTES);
    long end = System.currentTimeMillis();
    System.out.println("Worker Num: "+parallel_num+" Elapsed time: " + (end - start) + "ms\n");
  }

  public static void main(String[] args) throws Exception {
    if(option.equals("baseline")) {
      for (int n = 1; n <= 12; n += 1) {
        final int p = n;
        System.out.format(" _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_-BASELINE 2 WITH WORKER NUMBER %d _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_-\n    ", n);
        Thread t = new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              parallel_exp(p, 100000);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
        t.start();
        t.join();
        Runtime.getRuntime().gc();
      }
    }else{
      int size = 50000;
      for(int n = 0; n <= 6; n += 1){
        final int p = size;
        System.out.format(" _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_-FEED PAGE SIZE %d _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_-\n", p);
        Thread t = new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              parallel_exp(10, p);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
        t.start();
        t.join();
        Runtime.getRuntime().gc();
        if(n == 0){
          size = 100000;
        }else{
          size += 100000;
        }
      }
    }
  }
}
