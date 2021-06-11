package com.example.influencer.event.processor;

import com.example.influencer.event.decoder.NSQJsonMessageDecoder;
import com.example.influencer.model.Profile.Type;
import com.example.influencer.model.nsq.FacebookMessage;
import com.example.influencer.service.ProfileEnrichmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Logger;

public class FacebookMessageEventProcessor extends NSQJsonMessageDecoder<FacebookMessage> {
    private static final Logger LOGGER = Logger.getLogger(FacebookMessageEventProcessor.class.getName());

    private final ProfileEnrichmentService<FacebookMessage> facebookMessageProfileEnrichmentService;

    public FacebookMessageEventProcessor(ObjectMapper objectMapper, Class<FacebookMessage> klass,
            ProfileEnrichmentService<FacebookMessage> facebookMessageProfileEnrichmentService) {
        super(objectMapper, klass);
        this.facebookMessageProfileEnrichmentService = facebookMessageProfileEnrichmentService;
    }


    @Override
    public void processMessage(FacebookMessage message) {
        LOGGER.info(String.format("Recieved facebook profile: %s", message));
        facebookMessageProfileEnrichmentService.enrich(Type.FACEBOOK, message);
    }
}
