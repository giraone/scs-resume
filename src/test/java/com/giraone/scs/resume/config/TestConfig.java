package com.giraone.scs.resume.config;

import java.time.Duration;

public class TestConfig {

    public static final String TOPIC_IN_1 = "topic-in";
    public static final String TOPIC_OUT_1 = "topic-out-1";
    public static final String TOPIC_OUT_2 = "topic-out-2";

    public static final Duration DEFAULT_SLEEP_AFTER_PRODUCE_TIME = Duration.ofSeconds(1);
    public static final Duration DEFAULT_CONSUMER_POLL_TIME = Duration.ofSeconds(3);
}
