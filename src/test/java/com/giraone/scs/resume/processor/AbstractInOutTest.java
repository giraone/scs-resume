package com.giraone.scs.resume.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giraone.scs.resume.common.ObjectMapperBuilder;
import com.giraone.scs.resume.model.MessageIn;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import static com.giraone.scs.resume.config.TestConfig.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Needed, because otherwise setup must be static
public abstract class AbstractInOutTest {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractInOutTest.class);

    protected static final ObjectMapper objectMapper = ObjectMapperBuilder.build(false, false);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    protected EmbeddedKafkaBroker embeddedKafka;

    protected Consumer<String, String> consumer;

    protected void setup() {
        LOGGER.info("TEST AbstractInOutTest setup {}", embeddedKafka.getBrokerAddresses()[0]);
        System.setProperty("spring.kafka.bootstrap-servers", embeddedKafka.getBrokersAsString());
        System.setProperty("spring.cloud.stream.kafka.streams.binder.brokers", embeddedKafka.getBrokersAsString());

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("group", "false", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(
            consumerProps, new StringDeserializer(), new StringDeserializer());
        consumer = consumerFactory.createConsumer();
    }

    @AfterAll
    public void tearDown() {
        LOGGER.info("TEST AbstractInOutTest tearDown");
        consumer.close();
        // Do not use embeddedKafka.destroy(); Use @DirtiesContext on the concrete class.
        System.clearProperty("spring.kafka.bootstrap-servers");
        System.clearProperty("spring.cloud.stream.kafka.streams.binder.brokers");
    }

    protected DefaultKafkaProducerFactory<String, String> buildDefaultKafkaProducerFactory() {
        final Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        // For testing, we use StringSerializer for message key and message body
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(producerProps);
    }

    protected void produce(String topic, DefaultKafkaProducerFactory<String, String> pf) throws JsonProcessingException, InterruptedException {
        KafkaTemplate<String, String> template = new KafkaTemplate<>(pf, true);
        MessageIn messageIn = MessageIn.builder()
            .name("test")
            .build();
        String messageKey = String.format("%08d", System.currentTimeMillis()); // to test StringSerializer
        LOGGER.info("{} SENDING TO TOPIC {}", getClass().getName(), topic);
        template.send(topic, messageKey, objectMapper.writeValueAsString(messageIn));
        Thread.sleep(DEFAULT_SLEEP_AFTER_PRODUCE_TIME.toMillis());
    }

    protected void produceAndCheckEmpty(String topicIn, String topicOut, DefaultKafkaProducerFactory<String, String> pf) throws JsonProcessingException, InterruptedException {

        try {
            produce(topicIn, pf);
            assertThatThrownBy(() -> KafkaTestUtils.getSingleRecord(consumer, topicOut, DEFAULT_CONSUMER_POLL_TIME.toMillis()))
                .hasMessageContaining("No records found for topic");
        } finally {
            pf.destroy();
        }
    }

    protected ConsumerRecord<String, String> pollTopic(String topic) {
        LOGGER.info("{} POLLING TOPIC {}", getClass().getName(), topic);
        ConsumerRecord<String, String> consumerRecord = KafkaTestUtils.getSingleRecord(
            consumer, topic, DEFAULT_CONSUMER_POLL_TIME.toMillis());
        LOGGER.info("{} POLL TOPIC RETURNED key={} value={}",
            getClass().getName(), consumerRecord.key(), consumerRecord.value());
        return consumerRecord;
    }

    protected void awaitOnNewThread(Runnable runnable) {
        awaitOnNewThread(runnable, DEFAULT_THREAD_WAIT_TIME);
    }

    protected void awaitOnNewThread(Runnable runnable, Duration waitAtMost) {
        Thread thread = new Thread(runnable);
        thread.start();
        synchronized (thread) {
            try {
                thread.wait(waitAtMost.toMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void awaitOnNewThread(Callable callable) {
        awaitOnNewThread(callable, DEFAULT_THREAD_WAIT_TIME);
    }

    protected void awaitOnNewThread(Callable callable, Duration waitAtMost) {
        FutureTask<Void> futureTask = new FutureTask<>(callable);
        awaitOnNewThread(futureTask, waitAtMost);
    }
}
