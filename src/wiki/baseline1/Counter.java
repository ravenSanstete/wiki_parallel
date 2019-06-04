/***
 * Excerpted from "Seven Concurrency Models in Seven Weeks",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/pb7con for more book information.
***/
package wiki.baseline1;



import java.util.*;
import java.util.concurrent.BlockingQueue;

class Counter implements Runnable {
  private BlockingQueue<Page> queue;
  private Map<String, Integer> counts;
  
  public Counter(BlockingQueue<Page> queue,
                 Map<String, Integer> counts) {
    this.queue = queue;
    this.counts = counts;
  }

  public void run() {
    try {
      while(true) {
        Page page = queue.take();
        if (page.isPoisonPill())
          break;

        Iterable<String> words = new Words(page.getText());
        for (String word: words)
          countWord(word);
      }

    } catch (Exception e) { e.printStackTrace(); }
  }

  private void countWord(String word) {
    Integer currentCount = counts.get(word);
    if (currentCount == null)
      counts.put(word, 1);
    else
      counts.put(word, currentCount + 1);
  }

  class CountEntry{
    private String word;
    private int count;
    public CountEntry(String _word, int _count){
      this.word = _word;
      this.count = _count;
    }

    public String getWord(){
      return word;
    }

    public int getCount(){
      return count;
    }
  }

  public void get_hottest(){
    System.out.println("heeere");
    List<CountEntry> count_entries = new ArrayList<>();
    for (Map.Entry<String, Integer> entry: counts.entrySet()){
      count_entries.add(new CountEntry(entry.getKey(), entry.getValue()));
    }

    Collections.sort(count_entries, Comparator.comparing(CountEntry::getCount).reversed());
    System.out.format("Vocabulary Size %d\n", count_entries.size());
    int top = 1;

    for (CountEntry entry: count_entries){
      System.out.format("Word: %s\t Count: %d\n", entry.getWord(), entry.getCount());
      if(top > 10) break;
      top += 1;
    }




  }
}
