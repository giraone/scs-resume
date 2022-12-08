#!/bin/bash

if [[ $# != 1 ]]; then
  echo "Usage: $0 <amount-of-messages>"
  echo "  e.g. $0 10"
  exit 1
fi

typeset -i amount=$1

while (( i < amount )); do

  jq -rc . ../src/test/resources/testdata/process1-in.json | \
  sed -e "s/DATA/$(date --iso-8601=ns)/" | \
  docker exec -i kafka-1 kafka-console-producer --bootstrap-server localhost:9092 --topic topic-in
  echo -n '.'
  (( i+=1 ))
done

echo