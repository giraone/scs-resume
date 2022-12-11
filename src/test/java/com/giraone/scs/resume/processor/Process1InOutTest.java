package com.giraone.scs.resume.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.giraone.scs.resume.model.MessageIn;
import com.giraone.scs.resume.model.MessageOut;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static com.giraone.scs.resume.config.TestConfig.*;
import static com.giraone.scs.resume.processor.Process1InOutTest.*;
import static org.assertj.core.api.Assertions.assertThat;

// See https://blog.mimacom.com/testing-apache-kafka-with-spring-boot-junit5/
@EmbeddedKafka(
    controlledShutdown = true,
    topics = {
        TOPIC_IN,
        TOPIC_OUT_1
    },
    bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
@ActiveProfiles({"test", "process1"})
class Process1InOutTest extends AbstractInOutTest {

    @Autowired
    private SwitchOnOff switchOnOff;

    @BeforeAll
    public void setup() {
        super.setup();
        embeddedKafka.consumeFromEmbeddedTopics(consumer, TOPIC_OUT_1);
    }

    @Test
    void testProcessWorksWhenPaused() throws JsonProcessingException, InterruptedException {

        LOGGER.info("ProcessInOutTest testProcessWorksWhenStopped");
        switchOnOff.changeStateToPaused(1, true);
        MessageIn messageIn = MessageIn.builder()
            .name("test")
            .build();
        produceAndCheckEmpty(messageIn, TOPIC_IN, TOPIC_OUT_1);
    }

    @Test
    void testProcessWorksWhenResumed() throws JsonProcessingException, InterruptedException {

        LOGGER.info("ProcessInOutTest testProcessWorksWhenResumed");
        switchOnOff.changeStateToPaused(1, false);
        MessageIn messageIn = MessageIn.builder()
            .name("test")
            .build();
        ConsumerRecord<String, String> consumerRecord = produceAndAwaitConsume(messageIn, TOPIC_IN, TOPIC_OUT_1);
        // Check that Date/Time-as-String works
        assertThat(consumerRecord.value()).contains("\"startTime\":\"");
        MessageOut messageOut = objectMapper.readValue(consumerRecord.value(), MessageOut.class);
        assertThat(messageOut.getMessageIn()).isNotNull();
        assertThat(messageOut.getMessageIn().getName()).isEqualTo("test");
        assertThat(messageOut.getRequestId()).isNotNull();
        assertThat(messageOut.getCalculatedValue1()).isEqualTo(4);
    }
}
