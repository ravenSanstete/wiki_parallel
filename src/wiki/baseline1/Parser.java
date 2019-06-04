/***
 * Excerpted from "Seven Concurrency Models in Seven Weeks",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/pb7con for more book information.
***/
package wiki.baseline1;
import java.util.concurrent.BlockingQueue;
class Parser implements Runnable {
  private BlockingQueue<Page> queue;

  public Parser(BlockingQueue<Page> queue) {
    this.queue = queue;
  }
  
  public void run() {
    try {
      Iterable<Page> pages = new Pages(100000,  "D:\\enwiki-20190301-pages-articles-multistream.xml\\enwiki-20190301-pages-articles-multistream.xml");
      for (Page page: pages)
        queue.put(page);
    } catch (Exception e) { e.printStackTrace(); }
  }
}
