package com.objectpartners.kafka.streamjoin.generator

import com.objectpartners.kafka.streamjoin.model.Email
import com.objectpartners.kafka.streamjoin.model.EmailKey
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class EventGenerator implements CommandLineRunner {

    static void main(String[] args) {
        SpringApplication.run(EventGenerator, args);
    }

    @Override
    void run(String... args) throws Exception {
        Producer producer = new KafkaProducer<EmailKey, Email>(buildConfig());

        EmailKey key = EmailKey.newBuilder().setEmailId('email-1').setPersonId('person-1').build()
        Email value = Email.newBuilder().setAddress('tim.drahn@objectpartners.com').setType('work').build()
        ProducerRecord<EmailKey, Email> record = new ProducerRecord<>('email-topic', key, value)
        producer.send(record).get()
    }

    private static Properties buildConfig() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092,localhost:29092,localhost:39092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, SpecificAvroSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SpecificAvroSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        config.put(ProducerConfig.RETRIES_CONFIG, "2147483647");
        config.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");

        return config;
    }
}
