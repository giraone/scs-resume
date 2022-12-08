package com.giraone.scs.resume.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.Binding;
import org.springframework.cloud.stream.binding.BindingsLifecycleController;
import org.springframework.cloud.stream.endpoint.BindingsEndpoint;
import org.springframework.stereotype.Service;

@Service
public class SwitchOnOff {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwitchOnOff.class);

    private final BindingsEndpoint bindingsEndpoint;

    public SwitchOnOff(BindingsEndpoint bindingsEndpoint) {
        this.bindingsEndpoint = bindingsEndpoint;
    }

    public boolean changeStateToPaused(int processorNr, boolean paused) {

        final String bindingNameConsumer = "process" + processorNr + "-in-0";
        final Binding<?> state = bindingsEndpoint.queryState(bindingNameConsumer);
        if (state == null) {
            throw new IllegalArgumentException("bindingNameConsumer \"" + bindingNameConsumer + "\" + wrong. No state!");
        }
        if (paused) {
            if (!state.isPaused()) {
                LOGGER.info(">>> PAUSING {} - - - PAUSING - - - PAUSING - - -", bindingNameConsumer);
                bindingsEndpoint.changeState(bindingNameConsumer, BindingsLifecycleController.State.PAUSED);

            } else {
                LOGGER.warn(">>> Attempt to PAUSE {}, but is running={}, paused={}", bindingNameConsumer, state.isRunning(), state.isPaused());
            }

        } else {
            if (state.isPaused()) {
                LOGGER.info(">>> RESUMING {} - - - RESUMING - - - RESUMING - - -", bindingNameConsumer);
                bindingsEndpoint.changeState(bindingNameConsumer, BindingsLifecycleController.State.RESUMED);
            } else {
                LOGGER.warn(">>> Attempt to RESUME {}, but is running={}, paused={}", bindingNameConsumer, state.isRunning(), state.isPaused());
            }
        }
        final Binding<?> newState = bindingsEndpoint.queryState(bindingNameConsumer);
        LOGGER.info(">>> NEW STATE = running={}, paused={}", newState.isRunning(), newState.isPaused());
        return newState.isPaused();
    }

    public boolean isRunning(int processorNr) {

        final String bindingNameConsumer = "process" + processorNr + "-in-0";
        LOGGER.debug(">>> IS RUNNING? {}", bindingNameConsumer);
        Binding<?> state = bindingsEndpoint.queryState(bindingNameConsumer);
        return state.isRunning();
    }

    public boolean isPaused(int processorNr) {

        final String bindingNameConsumer = "process" + processorNr + "-in-0";
        LOGGER.debug(">>> IS PAUSED? {}", bindingNameConsumer);
        Binding<?> state = bindingsEndpoint.queryState(bindingNameConsumer);
        return state.isPaused();
    }
}
