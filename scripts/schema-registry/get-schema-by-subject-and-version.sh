#!/usr/bin/env bash

if [ "$1" == "" ]; then
  echo "usage: $0 <subject> <version>"
  exit -1
fi

if [ "$2" == "" ]; then
  echo "usage: $0 <subject> <version>"
  exit -1
fi

curl -s localhost:8081/subjects/$1/versions/$2 | jq
