package org.example;

import com.example.java.schema.Customer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;

public class KafkaAvroRecordProducer {

    public static void main(String[] args) {
       Properties properties = new Properties();
        // normal producer
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");
        properties.setProperty("acks", "all");
        properties.setProperty("retries", "10");
        // avro part
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer", KafkaAvroSerializer.class.getName());
        properties.setProperty("schema.registry.url", "http://127.0.0.1:8081");

        Producer<String, Customer> producer = new KafkaProducer<String, Customer>(properties);

        String topic = "customer-avro";

        String line = "";
        String splitBy = ",";
        try {
            BufferedReader br = new BufferedReader(new FileReader("dataFiles/customer.csv"));
            while ((line = br.readLine()) != null)   //returns a Boolean value
            {
                String[] custObj = line.split(splitBy);    // use comma as separator
                 Customer customer = Customer.newBuilder()
                        .setFirstName(custObj[0])
                         .setLastName(custObj[1])
                         .setAge(Integer.parseInt(custObj[2]))
                         .setHeight(Integer.parseInt(custObj[3]))
                         .setWeight(Integer.parseInt(custObj[4]))
                         .setAutomatedEmail(Boolean.getBoolean(custObj[5]))
                         .build();
                ProducerRecord<String, Customer> producerRecord = new ProducerRecord<String, Customer>(
                        topic, customer
                );

                System.out.println(customer);
                producer.send(producerRecord, new Callback() {
                    public void onCompletion(RecordMetadata metadata, Exception exception) {
                        if (exception == null) {
                            System.out.println(metadata);
                        } else {
                            exception.printStackTrace();
                        }
                    }
                });



            }
            producer.flush();
            producer.close();

        }
        catch (Exception ignore) {
            ignore.printStackTrace();
            //System.out.print("HARSH PRATEEK SINGH");
            }



    }
}
