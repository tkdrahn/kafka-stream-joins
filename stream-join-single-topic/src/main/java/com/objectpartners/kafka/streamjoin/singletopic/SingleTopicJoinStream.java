package com.objectpartners.kafka.streamjoin.singletopic;

import com.objectpartners.kafka.streamjoin.model.input.Email;
import com.objectpartners.kafka.streamjoin.model.input.EmailKey;
import com.objectpartners.kafka.streamjoin.model.input.EmailType;
import com.objectpartners.kafka.streamjoin.model.input.PersonName;
import com.objectpartners.kafka.streamjoin.model.input.PersonNameKey;
import com.objectpartners.kafka.streamjoin.model.input.PhoneType;
import com.objectpartners.kafka.streamjoin.model.input.Phone;
import com.objectpartners.kafka.streamjoin.model.input.PhoneKey;
import com.objectpartners.kafka.streamjoin.model.output.Person;
import com.objectpartners.kafka.streamjoin.model.output.PersonKey;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Properties;

@SpringBootApplication
public class SingleTopicJoinStream implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SingleTopicJoinStream.class);

    public static void main(String[] args) {
        SpringApplication.run(SingleTopicJoinStream.class, args);
    }

    @Override
    public void run(String... args) {
        // define topology
        Topology topology = createTopology();
        printTopology(topology);

        // start stream
        KafkaStreams streams = new KafkaStreams(topology, buildConfig());
        streams.cleanUp();
        streams.start();

        // shutdown hook to cleanly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private static Properties buildConfig() {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "single-topic-join-stream");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092,localhost:29092,localhost:39092");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        config.put(StreamsConfig.TOPOLOGY_OPTIMIZATION, "all");
        config.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 3);
        config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0); // disable caching
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        config.put(ProducerConfig.RETRIES_CONFIG, "2147483647");

        config.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        config.put(AbstractKafkaAvroSerDeConfig.KEY_SUBJECT_NAME_STRATEGY, "io.confluent.kafka.serializers.subject.TopicRecordNameStrategy");
        config.put(AbstractKafkaAvroSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY, "io.confluent.kafka.serializers.subject.TopicRecordNameStrategy");

        return config;
    }

    public static Topology createTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        // rekey input streams by personId
        KStream<EmailKey, Email> emailStream = builder.stream("email-topic");
        KStream<PhoneKey, Phone> phoneStream = builder.stream("phone-topic");
        KStream<PersonNameKey, PersonName> nameStream = builder.stream("name-topic");

        emailStream
                .selectKey((k, v) -> buildPersonKey(k.getPersonId()))
                .to("person-aggregate-topic");

        phoneStream
                .selectKey((k, v) -> buildPersonKey(k.getPersonId()))
                .to("person-aggregate-topic");

        nameStream
                .selectKey((k, v) -> buildPersonKey(k.getPersonId()))
                .to("person-aggregate-topic");


        // create Person aggregation
        KStream<PersonKey, SpecificRecord> personAggregationStream = builder.stream("person-aggregate-topic");
        personAggregationStream
                .groupByKey()
                .aggregate(
                        // initializer
                        () -> Person.newBuilder().build(),
                        // aggregator
                        (personKey, incomingRecord, aggregate) -> {

                            if (incomingRecord instanceof Email) {
                                Email newEmail = (Email) incomingRecord;
                                if (newEmail.getType() == EmailType.OFFICE) {
                                    aggregate.setOfficeEmail(newEmail.getAddress());
                                }
                                if (newEmail.getType() == EmailType.HOME) {
                                    aggregate.setHomeEmail(newEmail.getAddress());
                                }
                            }

                            if (incomingRecord instanceof Phone) {
                                Phone newPhone = (Phone) incomingRecord;
                                if (newPhone.getType() == PhoneType.CELL) {
                                    aggregate.setCellPhoneNumber(newPhone.getPhoneNumber());
                                }
                            }

                            if (incomingRecord instanceof PersonName) {
                                PersonName personName = (PersonName) incomingRecord;
                                aggregate.setFirstName(personName.getFirstName());
                                aggregate.setLastName(personName.getLastName());
                            }

                            return aggregate;
                        }
                )
                .toStream()
                .to("person-topic");

        return builder.build();
    }


    private static void printTopology(Topology topology) {
        log.info("---PRINTING TOPOLOGY---");
        log.info(topology.describe().toString());
        log.info("---END TOPOLOGY---");
    }

    private static PersonKey buildPersonKey(String personId) {
        return PersonKey.newBuilder().setPersonId(personId).build();
    }
}

