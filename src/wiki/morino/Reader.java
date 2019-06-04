package wiki.morino;

import java.io.*;
import java.util.concurrent.BlockingQueue;

public class Reader implements Runnable{
    private BlockingQueue<String> line_queue;
    public BufferedReader reader;
    private String pill;
    private int page_num;
    private boolean verbose;
    private AlphaCounter alpha_counter;
    // constructor
    public Reader(String path, BlockingQueue<String> q, String pill, int page_num, boolean verbose){
        this.line_queue = q;
        this.pill = pill;
        this.page_num = page_num;
        this.verbose = verbose;
        // open an input stream
        try {
            FileInputStream fin = new FileInputStream(path);
            this.reader = new BufferedReader(new InputStreamReader(fin, "UTF-8"), 8192); // getBufferedReaderForCompressedFile(path);
            this.reader = reader;
        }catch(FileNotFoundException e){
            Util.err("File Not Found");
        }catch(IOException e){
            Util.err("IO Exception");
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    // run push one line into the queue
    @Override
    public void run(){
        try{
            this.read_line();
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    private void read_line() throws InterruptedException{
            Iterable<Page> pages = new Pages(page_num,  "D:\\enwiki-20190301-pages-articles-multistream.xml\\enwiki-20190301-pages-articles-multistream.xml");
            for (Page page: pages){
                for(String line: page.getText().split("\n")){
                    line_queue.put(line);
                }
            }
            // finished,
            for(int i = 0; i < Main.BOUND; i += 1) {
                line_queue.put(this.pill); // send termination signal
                // System.out.format("%d ", i);
            }
            Util.log("reader finished");

            Thread.currentThread().interrupt(); // stop thread


    }
}
