package com.example.influencer.event.processor;

import com.example.influencer.event.decoder.NSQJsonMessageDecoder;
import com.example.influencer.model.Profile.Type;
import com.example.influencer.model.nsq.TwitterMessage;
import com.example.influencer.service.ProfileEnrichmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Logger;

public class TwitterMessageEventProcessor extends NSQJsonMessageDecoder<TwitterMessage> {

    private static final Logger LOGGER = Logger.getLogger(TwitterMessageEventProcessor.class.getName());

    private final ProfileEnrichmentService<TwitterMessage> twitterMessageProfileEnrichmentService;

    public TwitterMessageEventProcessor(ObjectMapper objectMapper, Class<TwitterMessage> klass,
            ProfileEnrichmentService<TwitterMessage> twitterMessageProfileEnrichmentService) {
        super(objectMapper, klass);
        this.twitterMessageProfileEnrichmentService = twitterMessageProfileEnrichmentService;
    }

    @Override
    public void processMessage(TwitterMessage message) {
        LOGGER.info(String.format("Recieved message %s", message));
        twitterMessageProfileEnrichmentService.enrich(Type.TWITTER, message);
    }
}