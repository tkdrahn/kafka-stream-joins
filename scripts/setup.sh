#!/usr/bin/env bash

kafka-topics \
--zookeeper localhost:2181 \
--partitions 4 \
--replication-factor 3 \
--topic email-topic \
--create

kafka-topics \
--zookeeper localhost:2181 \
--partitions 4 \
--replication-factor 3 \
--topic phone-topic \
--create

kafka-topics \
--zookeeper localhost:2181 \
--partitions 4 \
--replication-factor 3 \
--topic name-topic \
--create

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