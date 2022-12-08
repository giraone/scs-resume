package com.giraone.scs.resume.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Properties specific to application.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * </p>
 */
@Component
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@With
@ToString
// exclude from test coverage
@Generated
public class ApplicationProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationProperties.class);

    private boolean showConfigOnStartup;
    /**
     * Sleep the amount of ms in processing. Do not sleep, when zero
     */
    private long sleepMsInProcessing;
    /**
     * The used topics are configurable
     */
    private Topics topics;
    /**
     * the ids are used as the consumer group ids, so they have to be configurable
     */
    private Id id;

    @PostConstruct
    private void startup() {
        if (this.showConfigOnStartup) {
            LOGGER.info(this.toString());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Generated
    public static class Topics {
        private Topic queueIn;
        private Topic queueOut1;
        private Topic queueOut2;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Generated
    public static class Topic {
        private String topic;
    }

    /**
     * Consumer group ids for Kafka
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Generated
    public static class Id {
        private String process1;
        private String process2;
    }
}
