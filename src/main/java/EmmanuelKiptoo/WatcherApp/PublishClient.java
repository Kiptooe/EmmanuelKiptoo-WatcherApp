package EmmanuelKiptoo.WatcherApp;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class PublishClient {
    public static void connectGateway(HashMap credentials) throws MqttException {

        MqttClient client = new MqttClient((String)credentials.get("broker"),(String)credentials.get("clientid"), new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setConnectionTimeout(40);
        options.setKeepAliveInterval(40);
        int qos = Integer.parseInt((String) credentials.get("qos"));
        String topic = (String) credentials.get("topic");

        Integer retries ;
        String time;
        String netStatistics;

        MqttMessage message = new MqttMessage();
        message.setQos(qos);

        try{
            client.setCallback(new MqttCallback() {
                public void connectionLost(Throwable cause) {

                    try {
                        client.setTimeToWait(5000);
                        client.reconnect();
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                }
                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

                }
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("deliveryComplete---------" + token.isComplete());
                }
            });

            client.connect(options);
            time = String.valueOf(LocalDateTime.now());
            retries = 0;

            if(!client.isConnected()){
                System.out.print("No Connection at: " + time+" "+"...retrying connection..."+" "+"Number of retries:"+ retries+"\n");
                netStatistics =  "No connection at: " + time+" "+"...retrying connection..."+" "+"Number of retries:"+ retries+"\n";
                client.publish(topic, message);
                client.setTimeToWait(5000);
                ++retries;
                connectGateway(credentials);
            }else{
                System.out.print("Connection made at:" + time+" "+"Number of retries:"+ retries+"\n");
                netStatistics =  "Connected at: " + time+" "+"Number of retries:"+ retries+"\n";
                message.setPayload(netStatistics.getBytes());
            }
        }catch(MqttException e){
            throw new RuntimeException(e);
        }
            client.publish(topic, message);
            System.out.println("Message published");
            System.out.println("topic:" + "  " + topic);
            System.out.println("Message:" + " " + netStatistics);
    }

    public static void main(String[] args) throws MqttException {
        Map <String,String> credentials = new HashMap<String, String>();

        credentials.put("broker","tcp://broker.emqx.io:1883");
        credentials.put("clientid","publish_client");
        credentials.put("topic","mqtt/NetworkStats");
        credentials.put("qos","0");

        connectGateway((HashMap) credentials);

    }

}
