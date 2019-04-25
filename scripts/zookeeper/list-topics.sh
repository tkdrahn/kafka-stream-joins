#!/usr/bin/env bash

kafka-topics --zookeeper localhost:2181 --list | sort
