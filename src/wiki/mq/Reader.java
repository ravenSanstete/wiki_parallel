package wiki.mq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.*;
import java.util.concurrent.BlockingQueue;

public class Reader extends Thread{
    private Destination destination;
    private long timeToLive;
    private String user = ActiveMQConnection.DEFAULT_USER;
    private String password = ActiveMQConnection.DEFAULT_PASSWORD;
    private String url = Util.queue_address;// ActiveMQConnection.DEFAULT_BROKER_URL;
    private String subject = "TOOL.DEFAULT";
    private int batch_size = 1000;

    public BufferedReader reader;

    // constructor
    public Reader(String path, BlockingQueue<String> q){
        // open an input stream
        try {
            FileInputStream fin = new FileInputStream(path);
            this.reader = new BufferedReader(new InputStreamReader(fin, "UTF-8"), 8196*256); // getBufferedReaderForCompressedFile(path);
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
        Connection connection = null;
        try {
            // Create the connection.
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
            connectionFactory.setAlwaysSessionAsync(false);
            connectionFactory.setUseAsyncSend(true);
            connectionFactory.setOptimizeAcknowledge(true);


            connection = connectionFactory.createConnection();
            connection.start();

            // Create the session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            destination = session.createQueue(subject);


            // Create the producer.
            MessageProducer producer = session.createProducer(destination);


            // non persisten to make the producer faster than ever
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            if (timeToLive != 0) {
                producer.setTimeToLive(timeToLive);
            }

            // Start sending messages
            sendLoop(session, producer);
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (Throwable ignore) {
            }
        }
    }

    private void sendLoop(Session session, MessageProducer producer) throws JMSException{
            Iterable<Page> pages = new Pages(100000,  "D:\\enwiki-20190301-pages-articles-multistream.xml\\enwiki-20190301-pages-articles-multistream.xml");
            for (Page page: pages){
                for(String line: page.getText().split("\n")){
                    producer.send(session.createTextMessage(line));
                }
            }
            // finished,
            for(int i = 0; i < Main.BOUND; i += 1) {
                producer.send(session.createTextMessage(Util.termination)); // send termination signal
            }
            Util.log("reader finished");

            Thread.currentThread().interrupt(); // stop thread


    }
}
