/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wiki.morino;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.console.command.store.amq.CommandLineSupport;
import org.apache.activemq.util.IndentPrinter;



/**
 * A simple tool for publishing messages
 * 
 * 
 */
public class ProducerTool extends Thread {

    private Destination destination;
    private int messageCount = 10;
    private long sleepTime;
    private boolean verbose = true;
    private static int parallelThreads = 1;
    private long timeToLive;
    private String user = ActiveMQConnection.DEFAULT_USER;
    private String password = ActiveMQConnection.DEFAULT_PASSWORD;
    private String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    private String subject = "TOOL.DEFAULT";
    private boolean topic;
    private boolean transacted;
    private boolean persistent;
    private long batch = 10;
    private static Object lockResults = new Object();
    private HashMap<String, Boolean> pills;
    private BlockingQueue<String> queue;
    private Counter counter;

    public ProducerTool(BlockingQueue<String> q, Counter c, String[] pills){
        this.queue = q; // for reading line from q
        this.counter = c;
        this.pills = new HashMap<String, Boolean>();
        for(String pill:pills){
            this.pills.put(pill, false);
        }
    }


    public void run() {
        try{
            sendLoop();
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    protected void sendLoop() throws InterruptedException {
        int count = 0;
        while(true){
            count += 1;
            String line = queue.take();
            for(String w:Util.morph(line)){
                counter.count(w);

            }

            if(pills.containsKey(line)){
                pills.put(line, true);
            }

            line = null;
            if(!pills.values().contains(false)){
                break;
            }

            if(count % 5000000 == 0){
                System.gc();
            }

        }
       // Util.log("consumer finished");


    }


    public void setPersistent(boolean durable) {
        this.persistent = durable;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }


    public void setPassword(String pwd) {
        this.password = pwd;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    public void setParallelThreads(int parallelThreads) {
        if (parallelThreads < 1) {
            parallelThreads = 1;
        }
        this.parallelThreads = parallelThreads;
    }

    public void setTopic(boolean topic) {
        this.topic = topic;
    }

    public void setQueue(boolean queue) {
        this.topic = !queue;
    }

    public void setTransacted(boolean transacted) {
        this.transacted = transacted;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setBatch(long batch) {
        this.batch = batch;
    }
}