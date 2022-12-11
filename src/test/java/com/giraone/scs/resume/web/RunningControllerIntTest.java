package com.giraone.scs.resume.web;

import com.giraone.scs.resume.processor.SwitchOnOff;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.giraone.scs.resume.config.TestConfig.*;
import static com.giraone.scs.resume.web.RunningController.ATTRIBUTE_paused;
import static com.giraone.scs.resume.web.RunningController.ATTRIBUTE_running;

/**
 * Test class for the ErrorStatusController REST controller.
 * To show whether the "dump" works.
 *
 * @see RunningController
 */
@EmbeddedKafka(
    controlledShutdown = true,
    bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@SpringBootTest
@DirtiesContext
@AutoConfigureWebTestClient
class RunningControllerIntTest {

    @Autowired
    private SwitchOnOff switchOnOff;
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void pause_works() {

        // arrange
        switchOnOff.changeStateToPaused(1, false);

        // act/assert
        webTestClient.get().uri("/api/processors/1/pause")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$." + ATTRIBUTE_paused).isEqualTo(true);
    }

    @Test
    void resume_works() {

        // arrange
        switchOnOff.changeStateToPaused(1, true);

        // act/assert
        webTestClient.get().uri("/api/processors/1/resume")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$." + ATTRIBUTE_paused).isEqualTo(false);
    }

    @ParameterizedTest
    @CsvSource({
        "false",
        "true"
    })
    void status_works(boolean value) {

        // arrange
        switchOnOff.changeStateToPaused(1, value);

        // act/assert
        webTestClient.get().uri("/api/processors/1/status")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$." + ATTRIBUTE_paused).isEqualTo(value)
            .jsonPath("$." + ATTRIBUTE_running).isEqualTo(true);
    }

}
