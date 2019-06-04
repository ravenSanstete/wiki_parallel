package wiki.morino;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.*;


public class Main {
    static public int BOUND = 100;
    final static public int POOL_SIZE = 3;
    // final static public String path = "/Users/morino/Downloads/wiki_data/enwiki-20190520-pages-articles-multistream1.xml-p10p30302";
    final static public String path = "D:\\enwiki-20190301-pages-articles-multistream.xml\\enwiki-20190301-pages-articles-multistream.xml";
    final static public String[] paths = {"aa", "ab"};
    final static public String[] pills = {"23948023942"};
    final static public String option = "visual";
    final static public int PAGE_NUM = 100000;
    final static public String stop_path = "C:\\Users\\morin\\Desktop\\onix.txt";

    static public ArrayList<String> stop_words;

    public static void init(){
        stop_words = new ArrayList<>();
        try {
            FileReader f = new FileReader(stop_path);
            BufferedReader lines = new BufferedReader(f);
            String line = lines.readLine();
            while(line != null){
                stop_words.add(line);
                line = lines.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }

    }
//    for(String s:w){
//        System.out.println(s);
//    }
    public static Thread thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
        return brokerThread;
    }

    public static void start_consumers(AlphaCounter counter, int parallelThreads){
        ArrayList<ConsumerTool> threads = new ArrayList();
        ConsumerTool consumerTool = new ConsumerTool(counter);
        ;
        for (int threadCount = 1; threadCount <= parallelThreads; threadCount++) {
            consumerTool = new ConsumerTool(counter);
            consumerTool.start();
            threads.add(consumerTool);
        }

        while (true) {
            Iterator<ConsumerTool> itr = threads.iterator();
            int running = 0;
            while (itr.hasNext()) {
                ConsumerTool thread = itr.next();
                if (thread.isAlive()) {
                    running++;
                }
            }

            if (running <= 0) {
                System.out.println("Consumer. All threads completed their work");
                break;
            }
//
//            try {
//                Thread.sleep(1000);
//            } catch (Exception e) {
//            }
        }
        Iterator<ConsumerTool> itr = threads.iterator();
        while (itr.hasNext()) {
            ConsumerTool thread = itr.next();
        }
    }

    public static void parallel_exp(int parallel_num, int bound,int page_num, boolean verbose) throws Exception{
        long start = System.currentTimeMillis();
        // ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);
        BlockingQueue<String> line_buffer = new ArrayBlockingQueue<String>(bound, false);
        BasicCounter counter = new BasicCounter();
        // Start the first level producer
        //Thread reader_thread =  thread(new Reader(path, line_buffer), false);

        ArrayList<Thread> reader_threads = new ArrayList<>();

        // Start the first level producer
        for(int i = 0; i < 1; i++){
            Thread reader_thread =  thread(new Reader(path, line_buffer, pills[i], page_num, verbose), false);
            // System.out.format("reader thread id %d\n", reader_thread.getId());
            reader_threads.add(reader_thread);
        }


        ArrayList<Thread> producer_thread = new ArrayList<>();
        for(int i = 0; i < parallel_num; i ++){
            producer_thread.add(new ProducerTool(line_buffer, counter, pills));
            producer_thread.get(i).start();
           // System.out.format("producer thread id %d\n", producer_thread.get(i).getId());
        }


        for(Thread t:reader_threads){
            t.join();
        }

        for(int i = 0; i < parallel_num; i++){
            producer_thread.get(i).join();
        }
       // counter.get_hottest();
        counter.dump_freq(stop_words);
        long end = System.currentTimeMillis();
        // System.out.println("Worker Num: "+parallel_num+" Elapsed time: " + (end - start) + "ms\n");
        System.out.print(end-start + "\n");

        // try let the process sleep, then let the consumer get the information from the producer


        //start_consumers(counter,2);

        /*  start the first level consumer */


        // Util.log("finished");


    }

    public static void main(String[] args) throws Exception{
        if(option.equals("baseline")) {
            for (int n = 1; n <= 12; n += 1) {
                final int p = n;
                System.out.format(" _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_-WITH WORKER NUMBER %d _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_-\n    ", n);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            parallel_exp(p, BOUND, PAGE_NUM,true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                t.start();
                t.join();
                Runtime.getRuntime().gc();
            }
        }else if(option.equals("buffer")){
            int bound = 50;

            for(int n = 0; n <= 20; n+=1){
                final int b = bound;
                BOUND = b;
                System.out.format(" _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_-WITH BUFFER SIZE %d _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_-\n    ", bound);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            parallel_exp(10, b, PAGE_NUM,true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                t.start();
                t.join();

                Runtime.getRuntime().gc();
                if(n == 0){
                    bound = 100;
                }else{
                    bound += 100;
                }

            }
        }else if(option.equals("size")){
            int size = 50000;
            for(int n = 0; n <= 6; n += 1){
                final int p = size;
                System.out.format(" _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_-FEED PAGE SIZE %d _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_- _-_-_-\n", p);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            parallel_exp(10, 1000, p,true);
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
        }else if(option.equals("two_dim")){
            for(int n = 1; n <= 10; n+= 1){
                int bound = 100;
                for(int k = 1; k <= 10; k += 1){
                    final int b = bound;
                    BOUND = b;
                    final int p = n;
                    System.out.format("%d %d\n", p, b);
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                parallel_exp(p, b, 50000,true);
                            } catch (Exception e) {
                                // e.printStackTrace();
                            }
                        }
                    });
                    t.start();
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        /* if somebody interrupts us he knows what he is doing */
                    }
//doing                    if (t.isAlive()) {
//                        t.interrupt();
//                        System.out.println("timeout");
//                        // throw new TimeoutException();
//                    }
                    Runtime.getRuntime().gc();
                    bound += 200;
                }
            }


        }else if(option.equals("visual")){
            init();
            parallel_exp(10, 1000, 500000, true);

        }

    }
}
