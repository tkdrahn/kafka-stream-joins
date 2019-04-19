#!/usr/bin/env bash

kafka-avro-console-consumer \
--bootstrap-server localhost:19092,localhost:29092,localhost:39092 \
--property schema.registry.url=http://localhost:8081 \
--property print.key=true \
--from-beginning \
--topic name-topic
