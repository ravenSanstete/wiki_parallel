package wiki.mq;

import org.apache.activemq.ActiveMQConnection;

public class Util {
    final public static String termination = "234232351234";
    final public static String queue_address = "failover://tcp://localhost:61616";
    public static void err(String s){
        System.err.println(s);
        return;
    }
    public static void log(String s){
        System.out.println(s);
    }

    public static Words morph(String line){
        Words w = new Words(line);
        return w;
    }

}
