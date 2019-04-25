package com.objectpartners.kafka.streamjoin.generator

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class EventGenerator {

    static void main(String[] args) {
        SpringApplication.run(EventGenerator, args);
    }

//    @Override
//    void run(String... args) throws Exception {
//        Producer producer = new KafkaProducer<EmailKey, Email>(buildConfig());
//
//        EmailKey emailKey = EmailKey.newBuilder().setEmailId('email-1').setPersonId('person-1').build()
//        Email emailValue = Email.newBuilder().setAddress('tim.drahn@objectpartners.com').setType('office').build()
//
//        EmailKey emailKey2 = EmailKey.newBuilder().setEmailId('email-2').setPersonId('person-1').build()
//        Email emailValue2 = Email.newBuilder().setAddress('tim.drahn@personal.com').setType('home').build()
//
//        TelephoneKey phoneKey = TelephoneKey.newBuilder().setPersonId('person-1').setTelephoneId('phone-1').build()
//        Telephone phoneValue = Telephone.newBuilder().setType('cell').setPhoneNumber('123-456-7890').build()
//
//        PersonNameKey nameKey = PersonNameKey.newBuilder().setPersonId('person-1').build()
//        PersonName nameValue = PersonName.newBuilder().setFirstName('Test').setLastName('Person').build()
//
//        ProducerRecord<EmailKey, Email> emailRecord = new ProducerRecord<>('email-topic', emailKey, emailValue)
//        ProducerRecord<EmailKey, Email> emailRecord2 = new ProducerRecord<>('email-topic', emailKey2, emailValue2)
//        ProducerRecord<TelephoneKey, Telephone> phoneRecord = new ProducerRecord<>('phone-topic', phoneKey, phoneValue)
//        ProducerRecord<PersonNameKey, PersonName> nameRecord = new ProducerRecord<>('name-topic', nameKey, nameValue)
//
//        producer.send(emailRecord).get()
//        producer.send(emailRecord2).get()
//        producer.send(phoneRecord).get()
//        producer.send(nameRecord).get()
//    }

    @Bean
    Producer producer() {
        return new KafkaProducer<SpecificRecord, SpecificRecord>(buildConfig());
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
