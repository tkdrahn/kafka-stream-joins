#!/usr/bin/env bash

# INPUT TOPICS

kafka-topics \
--zookeeper localhost:2181 \
--partitions 1 \
--replication-factor 3 \
--topic email-topic \
--create

kafka-topics \
--zookeeper localhost:2181 \
--partitions 2 \
--replication-factor 3 \
--topic phone-topic \
--create

kafka-topics \
--zookeeper localhost:2181 \
--partitions 4 \
--replication-factor 3 \
--topic name-topic \
--create

kafka-configs --zookeeper localhost:2181 --entity-type topics --entity-name email-topic --alter --add-config cleanup.policy=compact,delete.retention.ms=604800000
kafka-configs --zookeeper localhost:2181 --entity-type topics --entity-name phone-topic --alter --add-config cleanup.policy=compact,delete.retention.ms=604800000
kafka-configs --zookeeper localhost:2181 --entity-type topics --entity-name name-topic --alter --add-config cleanup.policy=compact,delete.retention.ms=604800000

### OUTPUT TOPICS

kafka-topics \
--zookeeper localhost:2181 \
--partitions 4 \
--replication-factor 3 \
--topic person-topic \
--create

kafka-configs --zookeeper localhost:2181 --entity-type topics --entity-name person-topic --alter --add-config cleanup.policy=compact,delete.retention.ms=604800000


# CO-PARTITIONED TOPICS
kafka-topics \
--zookeeper localhost:2181 \
--partitions 4 \
--replication-factor 3 \
--topic office-email-by-person-topic \
--create

kafka-topics \
--zookeeper localhost:2181 \
--partitions 4 \
--replication-factor 3 \
--topic home-email-by-person-topic \
--create

kafka-topics \
--zookeeper localhost:2181 \
--partitions 4 \
--replication-factor 3 \
--topic phone-by-person-topic \
--create

kafka-topics \
--zookeeper localhost:2181 \
--partitions 4 \
--replication-factor 3 \
--topic name-by-person-topic \
--create

# SINGLE TOPIC JOIN

kafka-topics \
--zookeeper localhost:2181 \
--partitions 4 \
--replication-factor 3 \
--topic person-aggregate-topic \
--create
