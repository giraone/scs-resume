package com.giraone.scs.resume.web;

import com.giraone.scs.resume.processor.SwitchOnOff;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class RunningController {

    public static final String ATTRIBUTE_running = "running";
    public static final String ATTRIBUTE_paused = "paused";

    private final SwitchOnOff switchOnOff;

    public RunningController(SwitchOnOff switchOnOff) {
        this.switchOnOff = switchOnOff;
    }

    @GetMapping("/processors/{processorNr}/resume")
    public Mono<ResponseEntity<Map<String, Boolean>>> resume(@PathVariable int processorNr) {

        log.debug("RunningController.start {} called", processorNr);
        return Mono
            .just(Boolean.FALSE)
            .map(value -> switchOnOff.changeStateToPaused(processorNr, value))
            .map(value -> ResponseEntity.ok(Map.of(ATTRIBUTE_paused, value)));
    }

    @GetMapping("/processors/{processorNr}/pause")
    public Mono<ResponseEntity<Map<String, Boolean>>> pause(@PathVariable int processorNr) {

        log.debug("RunningController.stop {} called", processorNr);
        return Mono
            .just(Boolean.TRUE)
            .map(value -> switchOnOff.changeStateToPaused(processorNr, value))
            .map(value -> ResponseEntity.ok(Map.of(ATTRIBUTE_paused, value)));
    }

    @GetMapping("/processors/{processorNr}/status")
    public Mono<ResponseEntity<Map<String, Boolean>>> status(@PathVariable int processorNr) {

        log.debug("RunningController.status {} called", processorNr);
        boolean running = switchOnOff.isRunning(processorNr);
        boolean paused = switchOnOff.isPaused(processorNr);
        return Mono.just(ResponseEntity.ok(
            Map.of(
                ATTRIBUTE_running, running,
                ATTRIBUTE_paused, paused
            )));
    }
}
