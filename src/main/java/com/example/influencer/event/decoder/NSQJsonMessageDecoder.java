package com.example.influencer.event.decoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.brainlag.nsq.NSQMessage;
import com.github.brainlag.nsq.callbacks.NSQMessageCallback;
import java.util.logging.Logger;

public abstract class NSQJsonMessageDecoder<T> implements NSQMessageCallback {

    private static final Logger LOGGER = Logger.getLogger(NSQJsonMessageDecoder.class.getName());

    private final ObjectMapper objectMapper;

    private final Class<T> klass;

    public NSQJsonMessageDecoder(final ObjectMapper objectMapper, final Class<T> klass) {
        this.objectMapper = objectMapper;
        this.klass = klass;
    }

    @Override
    public void message(NSQMessage nsqMessage) {
        String message = new String(nsqMessage.getMessage());
        try {
            processMessage(objectMapper.readValue(message, klass));
        } catch (JsonProcessingException e) {
            LOGGER.severe(String.format("Failed to deserialize %s", message));
        } finally {
            nsqMessage.finished();
        }
    }

    public abstract void processMessage(T message);
}
