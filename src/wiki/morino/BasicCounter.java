package wiki.morino;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BasicCounter implements  Counter{
    private ConcurrentHashMap<String , Integer> bucket;
    public BasicCounter(){
        bucket = new ConcurrentHashMap<>();
    }

    public void count(String word){
        if(word.length() == 0) return;
        char st = word.toLowerCase().charAt(0);
        if(st >= 'a' && st <= 'z'){
            while(true){
                Integer current_count = bucket.get(word);
                if(current_count == null){
                    if(bucket.putIfAbsent(word, 1) == null) break;
                }else if(bucket.replace(word, current_count, current_count + 1)) {
                    break;
                }
            }
        }
        word = null;
    }

    public void get_hottest(){
        String max_word = null;
        int max_count = 0;
        for(String w:bucket.keySet()){
            if(max_count <= bucket.get(w)){
                max_count = bucket.get(w);
                max_word = w;
            }
        }

        System.out.format("The Hottest Word: %s (Count %d)\n", max_word, max_count);
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

    public void dump_freq(ArrayList<String> stopwords){
        List<CountEntry> count_entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry: bucket.entrySet()){
            count_entries.add(new CountEntry(entry.getKey(), entry.getValue()));
        }

        Collections.sort(count_entries, Comparator.comparing(CountEntry::getCount).reversed());
        System.out.format("Vocabulary Size %d\n", count_entries.size());
        int top = 1;
        for (CountEntry entry: count_entries){
            if(stopwords.contains(entry.getWord().toLowerCase())) continue;

            System.out.format("%s %d\n", entry.getWord(), entry.getCount());
            if(top > 1000) break;
            top += 1;
        }
    }


}
