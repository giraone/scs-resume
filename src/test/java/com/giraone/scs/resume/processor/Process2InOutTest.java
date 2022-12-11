package com.giraone.scs.resume.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.giraone.scs.resume.model.MessageOut;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static com.giraone.scs.resume.config.TestConfig.*;
import static org.assertj.core.api.Assertions.assertThat;

// See https://blog.mimacom.com/testing-apache-kafka-with-spring-boot-junit5/
@EmbeddedKafka(
    controlledShutdown = true,
    topics = {
        TOPIC_IN_1,
        TOPIC_OUT_2
    },
    bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
@ActiveProfiles({"test", "process2"})
class Process2InOutTest extends AbstractInOutTest {

    @BeforeAll
    public void setup() {
        super.setup();
        embeddedKafka.consumeFromEmbeddedTopics(consumer, TOPIC_IN_1);
    }

    @Test
    void testProcessWorks() throws JsonProcessingException, InterruptedException {

        LOGGER.info("ProcessSplitTest test");
        produceAndAwaitConsume(TOPIC_IN_1, TOPIC_OUT_2);
    }

    private void produceAndAwaitConsume(String topicIn, String topicOut) throws JsonProcessingException, InterruptedException {

        DefaultKafkaProducerFactory<String, String> pf = buildDefaultKafkaProducerFactory();
        try {
            produce(topicIn, pf);
        } finally {
            pf.destroy();
        }
        awaitOnNewThread(() -> {
            ConsumerRecord<String, String> consumerRecord = pollTopic(topicOut);
            assertThat(consumerRecord.key()).isNotNull();
            assertThat(consumerRecord.value()).isNotNull();
            // Check that Date/Time-as-String works
            assertThat(consumerRecord.value()).contains("\"startTime\":\"");
            MessageOut messageOut = objectMapper.readValue(consumerRecord.value(), MessageOut.class);
            assertThat(messageOut.getMessageIn()).isNotNull();
            assertThat(messageOut.getMessageIn().getName()).isEqualTo("test");
            assertThat(messageOut.getRequestId()).isNotNull();
            assertThat(messageOut.getCalculatedValue1()).isEqualTo(4);
            return true;
        });
    }
}
