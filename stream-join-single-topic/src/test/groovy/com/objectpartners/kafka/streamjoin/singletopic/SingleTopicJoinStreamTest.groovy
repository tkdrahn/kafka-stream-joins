package com.objectpartners.kafka.streamjoin.singletopic

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import spock.lang.Ignore
import spock.lang.Specification

class SingleTopicJoinStreamTest extends Specification {

    TopologyTestDriver testDriver
    StringSerializer stringSerializer = new StringSerializer()
    ConsumerRecordFactory<String, String> recordFactory = new ConsumerRecordFactory<>(stringSerializer, stringSerializer)

    void setup() {
        Properties config = new Properties()
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, 'test')
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, 'dummy:1234')
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass())
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass())

        Topology topology = SingleTopicJoinStream.createTopology()
        testDriver = new TopologyTestDriver(topology, config)
    }

    void cleanup() {
        testDriver.close()
    }

    @Ignore
    void 'test counts are correct'() {
        when:
        publishRecord('tim,red')
        publishRecord('john,blue')

        then:
        compareKeyValue(consumeRecord(), 'red', 1L) == true
        compareKeyValue(consumeRecord(), 'blue', 1L) == true
        consumeRecord() == null
    }

    private void publishRecord(String value) {
        testDriver.pipeInput(recordFactory.create('favorite-color-input-topic', null, value))
    }

    private ProducerRecord<String, Long> consumeRecord() {
        return testDriver.readOutput('favorite-color-output-topic', new StringDeserializer(), new LongDeserializer())
    }

    private boolean compareKeyValue(ProducerRecord<String, Long> record, expectedKey, expectedValue) {
        return record.key() == expectedKey && record.value() == expectedValue
    }
}
