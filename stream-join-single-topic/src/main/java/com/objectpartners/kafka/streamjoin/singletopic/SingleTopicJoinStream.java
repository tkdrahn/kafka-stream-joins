package com.objectpartners.kafka.streamjoin.singletopic;

import com.google.common.collect.Lists;
import com.objectpartners.kafka.streamjoin.model.Email;
import com.objectpartners.kafka.streamjoin.model.EmailKey;
import com.objectpartners.kafka.streamjoin.model.Person;
import com.objectpartners.kafka.streamjoin.model.PersonKey;
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

import java.util.List;
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

    public static Topology createTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        KStream<EmailKey, Email> emailStream = builder.stream("email-topic");
        emailStream
                .selectKey((k, v) -> PersonKey.newBuilder().setPersonId(k.getPersonId()).build())
                .to("person-aggregate-topic");

        KStream<PersonKey, SpecificRecord> personBuilderStream = builder.stream("person-aggregate-topic");
        personBuilderStream
                .peek((k, v) -> log.info("aggregating record for person: {} of type: {}",
                        k.getPersonId(), v.getClass().getSimpleName()))
                .groupByKey()
                .aggregate(
                        // initializer
                        () -> Person.newBuilder().setEmails(Lists.newArrayList()).build(),
                        // aggregator
                        (personKey, newRecord, aggregate) -> {
                            if (newRecord instanceof Email) {
                                Email newEmail = (Email) newRecord;
                                List<Email> existingEmails = Lists.newArrayList();
                                aggregate.getEmails().stream()
                                        .forEach(email -> {
                                            if (!email.getType().equals(newEmail.getType())) {
                                                existingEmails.add(email);
                                            }
                                        });

                                existingEmails.add(newEmail);
                                aggregate.setEmails(existingEmails);
                            }
                            return aggregate;
                        }
                )
                .toStream()
                .to("person-topic");

        return builder.build();
    }

    private static Properties buildConfig() {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "single-topic-join-stream");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092,localhost:29092,localhost:39092");
        config.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        config.put(StreamsConfig.TOPOLOGY_OPTIMIZATION, "all");
        config.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 3);
        config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0); // disable caching
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        config.put(ProducerConfig.RETRIES_CONFIG, "2147483647");
        return config;
    }

    private void printTopology(Topology topology) {
        log.info("---PRINTING TOPOLOGY---");
        log.info(topology.describe().toString());
        log.info("---END TOPOLOGY---");
    }
}

