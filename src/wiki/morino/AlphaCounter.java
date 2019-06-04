package wiki.morino;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AlphaCounter implements  Counter{
// this counter implements a concurrent Bucket of <x\in{alphabeta}, Hashmap>
    private ConcurrentHashMap<Character, ConcurrentHashMap<String, Integer>> bucket;
    final private String alphabeta = "abcdefghijklmnopqrstuvwxyz";
    public AlphaCounter(){
        bucket = new ConcurrentHashMap<>();
        for(char c:alphabeta.toCharArray()){
            bucket.put(c, new ConcurrentHashMap<>());
        }
    }

    public synchronized void thin() {
        int previous_size = 0;
        int current_size = 0;
        for (char c : alphabeta.toCharArray()) {
            previous_size += bucket.get(c).keySet().size();
            for (String w : bucket.get(c).keySet()) {
                if (bucket.get(c).get(w) <= 1) {
                    bucket.get(c).remove(w);
                }
            }
            current_size += bucket.get(c).keySet().size();

        }

        System.out.format("Previous %d Current %d Ratio %.3f", previous_size, current_size, current_size/(previous_size * 1.0));
    }

    public void count(String word){
        if(word.length() == 0) return;
        char st = word.toLowerCase().charAt(0);
        if(st >= 'a' && st <= 'z'){
            while(true){
                Integer current_count = bucket.get(st).get(word);
                if(current_count == null){
                    if(bucket.get(st).putIfAbsent(word, 1) == null) break;
                }else if(bucket.get(st).replace(word, current_count, current_count + 1)) {
                    break;
                }
            }
        }
        word = null;
    }

    public void get_hottest(){
        String max_word = null;
        int max_count = 0;
        for(char c:alphabeta.toCharArray()){
            ConcurrentHashMap<String, Integer> counter = bucket.get(c);
            for(String w:counter.keySet()){
                if(max_count <= counter.get(w)){
                    max_count = counter.get(w);
                    max_word = w;
                }
            }
        }
        System.out.format("The Hottest Word: %s (Count %d)\n", max_word, max_count);
    }


}
