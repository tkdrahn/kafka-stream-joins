#!/usr/bin/env bash

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

kafka-topics \
--zookeeper localhost:2181 \
--partitions 4 \
--replication-factor 3 \
--topic person-topic \
--create