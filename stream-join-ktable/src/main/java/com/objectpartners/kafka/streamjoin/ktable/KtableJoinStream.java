package com.objectpartners.kafka.streamjoin.ktable;

import com.objectpartners.kafka.streamjoin.model.input.Email;
import com.objectpartners.kafka.streamjoin.model.input.EmailKey;
import com.objectpartners.kafka.streamjoin.model.input.EmailType;
import com.objectpartners.kafka.streamjoin.model.input.PersonName;
import com.objectpartners.kafka.streamjoin.model.input.PersonNameKey;
import com.objectpartners.kafka.streamjoin.model.input.Phone;
import com.objectpartners.kafka.streamjoin.model.input.PhoneKey;
import com.objectpartners.kafka.streamjoin.model.input.PhoneType;
import com.objectpartners.kafka.streamjoin.model.intermediate.EmailAggregate;
import com.objectpartners.kafka.streamjoin.model.intermediate.EmailPhoneAggregate;
import com.objectpartners.kafka.streamjoin.model.intermediate.PhoneAggregate;
import com.objectpartners.kafka.streamjoin.model.output.Person;
import com.objectpartners.kafka.streamjoin.model.output.PersonKey;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Properties;

@SpringBootApplication
public class KtableJoinStream implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(KtableJoinStream.class);

    public static void main(String[] args) {
        SpringApplication.run(KtableJoinStream.class, args);
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
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "ktable-join-stream");
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
        return config;
    }

    public static Topology createTopology() {
        // ===========================================================================================
        // STREAM RAW TOPICS
        // ===========================================================================================

        StreamsBuilder builder = new StreamsBuilder();
        KStream<EmailKey, Email> emailStream = builder.stream("email-topic");
        KStream<PhoneKey, Phone> phoneStream = builder.stream("phone-topic");
        KStream<PersonNameKey, PersonName> nameStream = builder.stream("name-topic");

        // ===========================================================================================
        // CO-PARTITION INPUT TOPICS
        // ===========================================================================================

        // 1-to-1 co-partitioning
        nameStream
                .selectKey((k, v) -> buildPersonKey(k.getPersonId()))
                .to("name-by-person-topic");

        // 1-to-many co-partitioning: branching
        KStream<EmailKey, Email>[] emailByTypeStreams = emailStream.branch(
                (k, v) -> v.getType() == EmailType.HOME,
                (k, v) -> v.getType() == EmailType.OFFICE
        );
        emailByTypeStreams[0]
                .selectKey((k, v) -> buildPersonKey(k.getPersonId()))
                .to("home-email-by-person-topic");
        emailByTypeStreams[1]
                .selectKey((k, v) -> buildPersonKey(k.getPersonId()))
                .to("office-email-by-person-topic");

        // 1-to-many co-partitioning: aggregation
        phoneStream
                .groupBy((k, v) -> buildPersonKey(k.getPersonId()))
                .aggregate(
                        // initializer
                        () -> PhoneAggregate.newBuilder().build(),
                        // aggregator
                        (personKey, incomingRecord, aggregate) -> {
                            if (incomingRecord.getType() == PhoneType.CELL) {
                                aggregate.setCellNumber(incomingRecord.getPhoneNumber());
                            }
                            if (incomingRecord.getType() == PhoneType.LANDLINE) {
                                aggregate.setLandlineNumber(incomingRecord.getPhoneNumber());
                            }
                            return aggregate;
                        }
                )
                .toStream()
                .to("phone-by-person-topic");


        // ===========================================================================================
        // BUILD K-TABLES
        // ===========================================================================================

        KTable<PersonKey, Email> homeEmailTable = builder.table("home-email-by-person-topic");
        KTable<PersonKey, Email> officeEmailTable = builder.table("office-email-by-person-topic");
        KTable<PersonKey, PhoneAggregate> phoneTable = builder.table("phone-by-person-topic");
        KTable<PersonKey, PersonName> nameTable = builder.table("name-by-person-topic");

        // ===========================================================================================
        // APPLY JOINS
        // ===========================================================================================

        KTable<PersonKey, EmailAggregate> emailAggregateTable = officeEmailTable.outerJoin(
                homeEmailTable,
                // aggregator
                (officeEmail, homeEmail) ->
                        EmailAggregate.newBuilder()
                                .setOfficeEmail(officeEmail == null ? null : officeEmail.getAddress())
                                .setHomeEmail(homeEmail == null ? null : homeEmail.getAddress())
                                .build()
        );

        KTable<PersonKey, EmailPhoneAggregate> emailPhoneAggregateTable = emailAggregateTable.outerJoin(
                phoneTable,
                // aggregator
                (emailAggregate, phoneAggregate) ->
                        EmailPhoneAggregate.newBuilder()
                                .setEmail(emailAggregate)
                                .setPhone(phoneAggregate)
                                .build()
        );

        KTable<PersonKey, Person> personTable = emailPhoneAggregateTable.outerJoin(
                nameTable,
                // aggregator
                (emailPhoneAggregate, personName) ->
                        Person.newBuilder()
                                .setHomeEmail(emailPhoneAggregate == null || emailPhoneAggregate.getEmail() == null ? null : emailPhoneAggregate.getEmail().getHomeEmail())
                                .setOfficeEmail(emailPhoneAggregate == null || emailPhoneAggregate.getEmail() == null ? null : emailPhoneAggregate.getEmail().getOfficeEmail())
                                .setCellPhoneNumber(emailPhoneAggregate == null || emailPhoneAggregate.getPhone() == null ? null : emailPhoneAggregate.getPhone().getCellNumber())
                                .setFirstName(personName == null ? null : personName.getFirstName())
                                .setLastName(personName == null ? null : personName.getLastName())
                                .build()
        );


        personTable.toStream().to("person-topic");
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

