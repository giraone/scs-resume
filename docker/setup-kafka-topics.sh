#!/bin/bash

for topic in topic-in topic-out-1 topic-out-2
do
  echo "Create $topic"
  docker exec -it kafka-1 kafka-topics \
    --bootstrap-server kafka-1:9092 \
    --create \
    --topic $topic \
    --replication-factor 1 \
    --partitions 2
done
