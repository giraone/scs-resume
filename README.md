# SCS Start/Resume Sample

A sample to show

- pause/resume with *Spring Cloud Streams* (SCS) using the *Kafka Binder*
- the sample uses two processors *process1* / *process2* running on `topic-in` and write to `topic-out1` / `topic-out2`
- there are HTTP endpoints to perform this for each of the two processors

Hints:

- Serialization/Deserialization of the message payload is done manually within the processor.
  From the perspective of the configuration Kafka keys are Strings and messages are bytes arrays.

## Build / Start / Check

```bash
mvn package
java -jar target/XXXX
curl http://localhost:8070/actuator/health
```

## Usage with docker/podman

- See scripts in [docker](docker)
- Define environment variable **CONTAINER_DATA**, e.g. `export CONTAINER_DATA=./data`
- Run [docker/data-setup.sh](docker/data-setup.sh) to create the data directories for zookeeper and kafka-broker
- Start *Kafka* using *docker-compose*
- Run [docker/setup-kafka-topics.sh](docker/setup-kafka-topics.sh) to create the topics (*auto-creation is switch off*)
- Run [docker/data-cleanup.sh](docker/data-cleanup.sh) if you want to prune the data directories for zookeeper and kafka-broker

## Simple runtime tests

```bash

cd docker
# Write 10 events into topic-in
./create-messages.sh 10
# Read event from topic-out1 and topic-out2
docker exec -i kafka-1 kafka-console-consumer --bootstrap-server kafka-1:9092 --topic topic-out-1 --from-beginning
```

Now pause/resume, while there are still events:

- Status process1: `curl http://localhost:8070/api/processors/1/status`
- Pause process1: `curl http://localhost:8070/api/processors/1/pause`
- Resume process2: `curl http://localhost:8070/api/processors/1/resume`

### Actuator

- `curl http://localhost:8070/actuator/health` 
- `curl http://localhost:8070/actuator/bindings` 

```json
[
    {
        "bindingName": "process1-in-0",
        "name": "topic-in",
        "group": "process1resume",
        "pausable": true,
        "state": "paused",
        "paused": true,
        "extendedInfo": {
            "bindingDestination": "KafkaConsumerDestination{consumerDestinationName='topic-in', partitions=0, dlqName='null'}",
            "ExtendedConsumerProperties": {
                "bindingName": "process1-in-0",
                "autoStartup": true,
                "concurrency": 1,
                "instanceCount": 1,
                "maxAttempts": 3,
                "backOffInitialInterval": 1000,
                "backOffMaxInterval": 10000,
                "backOffMultiplier": 2.0,
                "defaultRetryable": true,
                "extension": {
                    "ackEachRecord": false,
                    "autoRebalanceEnabled": true,
                    "autoCommitOffset": true,
                    "ackMode": null,
                    "autoCommitOnError": null,
                    "startOffset": null,
                    "resetOffsets": false,
                    "enableDlq": false,
                    "dlqName": null,
                    "dlqPartitions": null,
                    "dlqProducerProperties": {
                        "bufferSize": 16384,
                        "compressionType": "none",
                        "sync": false,
                        "sendTimeoutExpression": null,
                        "batchTimeout": 0,
                        "messageKeyExpression": null,
                        "headerPatterns": null,
                        "configuration": {},
                        "topic": {
                            "replicationFactor": null,
                            "replicasAssignments": {},
                            "properties": {}
                        },
                        "useTopicHeader": false,
                        "recordMetadataChannel": null,
                        "transactionManager": null,
                        "closeTimeout": 0,
                        "allowNonTransactional": false
                    },
                    "recoveryInterval": 5000,
                    "trustedPackages": null,
                    "standardHeaders": "none",
                    "converterBeanName": null,
                    "idleEventInterval": 30000,
                    "destinationIsPattern": false,
                    "configuration": {},
                    "topic": {
                        "replicationFactor": null,
                        "replicasAssignments": {},
                        "properties": {}
                    },
                    "pollTimeout": 5000,
                    "transactionManager": null,
                    "txCommitRecovered": true,
                    "commonErrorHandlerBeanName": null
                }
            }
        },
        "input": true
    },
    {
        "bindingName": "process1-out-0",
        "name": "topic-out-1",
        "group": null,
        "pausable": false,
        "state": "running",
        "paused": false,
        "extendedInfo": {
            "bindingDestination": "topic-out-1",
            "ExtendedProducerProperties": {
                "bindingName": "process1-out-0",
                "autoStartup": true,
                "partitionCount": 1,
                "extension": {
                    "bufferSize": 16384,
                    "compressionType": "none",
                    "sync": false,
                    "sendTimeoutExpression": null,
                    "batchTimeout": 0,
                    "messageKeyExpression": null,
                    "headerPatterns": null,
                    "configuration": {},
                    "topic": {
                        "replicationFactor": null,
                        "replicasAssignments": {},
                        "properties": {}
                    },
                    "useTopicHeader": false,
                    "recordMetadataChannel": null,
                    "transactionManager": null,
                    "closeTimeout": 0,
                    "allowNonTransactional": false
                },
                "validPartitionKeyProperty": true,
                "validPartitionSelectorProperty": true
            }
        },
        "input": false
    }
]
```

## Links

- https://stackoverflow.com/questions/73468885/start-binding-in-paused-state