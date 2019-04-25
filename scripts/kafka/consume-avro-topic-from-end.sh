#!/usr/bin/env bash

if [ "$1" == "" ]; then
  echo "usage: $0 <topic>"
  exit -1
fi

kafka-avro-console-consumer \
--bootstrap-server localhost:19092,localhost:29092,localhost:39092 \
--property schema.registry.url=http://localhost:8081 \
--property print.key=true \
--topic $1
