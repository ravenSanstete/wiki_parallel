package wiki.mq;


import org.apache.activemq.ActiveMQConnection;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.function.Consumer;

import wiki.morino.AlphaCounter;


public class Main {
    final static public int BOUND = 500;
    final static public int POOL_SIZE = 3;
    final static public String path = "D:\\enwiki-20190301-pages-articles-multistream.xml\\enwiki-20190301-pages-articles-multistream.xml";
//    for(String s:w){
//        System.out.println(s);
//    }
    public static Thread thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
        return brokerThread;
    }

    public static ArrayList<ConsumerTool> start_consumers(AlphaCounter counter, int parallelThreads){
        ArrayList<ConsumerTool> threads = new ArrayList();
        ConsumerTool consumerTool = new ConsumerTool(counter);
        ;
        for (int threadCount = 1; threadCount <= parallelThreads; threadCount++) {
            consumerTool = new ConsumerTool(counter);
            consumerTool.start();
            threads.add(consumerTool);
        }


        return threads;

    }

    public static void main(String[] args) throws Exception{
        System.out.println(ActiveMQConnection.DEFAULT_BROKER_URL);
        int parallel_num = 2;
        long start = System.currentTimeMillis();

        BlockingQueue<String> line_buffer = new LinkedBlockingDeque<>(BOUND);
        AlphaCounter counter = new AlphaCounter();
        ArrayList<Thread> reader_threads = new ArrayList<>();


        // Start the first level producer
        for(int i = 0; i < 1; i++){
            Thread reader_thread =  thread(new Reader(path, line_buffer), false);
            reader_threads.add(reader_thread);
        }


        ArrayList<ConsumerTool> consumers = start_consumers(counter, parallel_num);


        for(Thread t:reader_threads){
            t.join();
        }

        for(ConsumerTool c:consumers){
            c.join();
        }


        counter.get_hottest();
        long end = System.currentTimeMillis();
        System.out.println("Elapsed time: " + (end - start) + "ms");

        return;

        //start_consumers(counter,2);

        /*  start the first level consumer */


        // Util.log("finished");



    }
}
