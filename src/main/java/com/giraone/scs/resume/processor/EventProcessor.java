package com.giraone.scs.resume.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giraone.scs.resume.common.ObjectMapperBuilder;
import com.giraone.scs.resume.config.ApplicationProperties;
import com.giraone.scs.resume.model.MessageIn;
import com.giraone.scs.resume.model.MessageOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class EventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventProcessor.class);
    private static final ObjectMapper mapper = ObjectMapperBuilder.build(false, false);

    private final ApplicationProperties applicationProperties;
    private final StreamBridge streamBridge;
    private final SwitchOnOff switchOnOff; // only for logging

    public EventProcessor(ApplicationProperties applicationProperties,
                          StreamBridge streamBridge,
                          SwitchOnOff switchOnOff) {

        this.applicationProperties = applicationProperties;
        this.streamBridge = streamBridge;
        this.switchOnOff = switchOnOff;
    }

    @Bean
    public Function<byte[], Message<byte[]>> process1() {
        return in -> processAndSetKey(1, in);
    }

    /*
    @Bean
    public Function<byte[], Message<byte[]>> process2() {
        return in -> processAndSetKey(2, in);
    }
    */

    // Same as above, but using SendBridge
    @Bean
    public Consumer<byte[]> process2() {
        return in -> {
            MessageOut messageOut = process(2, deserialize(in, MessageIn.class));
            sendToDynamicTarget(messageOut, x -> "process2-out-0");
        };
    }

    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    protected <T> T deserialize(byte[] messageInBody, Class<T> cls) {
        T messageIn;
        try {
            messageIn = mapper.readValue(messageInBody, cls);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return messageIn;
    }

    protected Message<byte[]> serialize(MessageOut messageOut) {
        final byte[] messageOutBody;
        try {
            messageOutBody = mapper.writeValueAsBytes(messageOut);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return MessageBuilder.withPayload(messageOutBody)
            .setHeader(KafkaHeaders.MESSAGE_KEY, messageOut.getRequestId())
            .build();
    }

    protected boolean sendToDynamicTarget(MessageOut messageOut, Function<MessageOut,String> dynamicTarget) {

        final String bindingName = dynamicTarget.apply(messageOut);
        if (bindingName == null) {
            LOGGER.warn(">>> No dynamic target for {}!", messageOut);
            return false;
        }
        final Message<MessageOut> message = MessageBuilder.withPayload(messageOut)
            .setHeader(KafkaHeaders.MESSAGE_KEY, messageOut.getRequestId())
            .build();

        boolean ok = streamBridge.send(bindingName, message);
        if (!ok) {
            LOGGER.error(">>> Cannot send event to out binding \"{}\"!", bindingName);
        }
        return false;
    }

    private Message<byte[]> processAndSetKey(int processorNr, byte[] messageInBody) {

        final MessageIn messageIn = deserialize(messageInBody, MessageIn.class);
        final MessageOut messageOut = process(processorNr, messageIn);
        return serialize(messageOut);
    }

    private MessageOut process(int processorNr, MessageIn messageIn) {

        LOGGER.info(">>> process{} {} in mode running={}, paused={}", processorNr, messageIn,
            switchOnOff.isRunning(processorNr), switchOnOff.isPaused(processorNr));
        simulationModeSleep();
        return MessageOut.builder()
            .messageIn(messageIn)
            .requestId(generateRequestId())
            .startTime(LocalDateTime.now())
            .calculatedValue1(messageIn.getName().length())
            .build();
    }

    private static String generateRequestId() {
        return String.format("%012x", System.currentTimeMillis());
    }

    private void simulationModeSleep() {
        try {
            Thread.sleep(applicationProperties.getSleepMsInProcessing());
        } catch (InterruptedException e) {
            LOGGER.warn("simulationModeSleep: Thread.sleep interrupted!", e);
            Thread.currentThread().interrupt();
        }
    }
}
