package com.example.influencer.event.processor;

import com.example.influencer.model.Profile.Type;
import com.example.influencer.model.nsq.FacebookMessage;
import com.example.influencer.model.nsq.TwitterMessage;
import com.example.influencer.service.ProfileEnrichmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TwitterMessageEventProcessorTest {

    @Mock
    ObjectMapper objectMapper;

    @Mock
    ProfileEnrichmentService<TwitterMessage> twitterMessageProfileEnrichmentService;

    private TwitterMessageEventProcessor twitterMessageEventProcessor;

    @BeforeEach
    void setup() {
        twitterMessageEventProcessor = new TwitterMessageEventProcessor(
                objectMapper, TwitterMessage.class, twitterMessageProfileEnrichmentService);
    }

    @Test
    public void testProcessMessageSuccessfully() {
        var twitterMessage = TwitterMessage.builder().build();
        twitterMessageEventProcessor.processMessage(twitterMessage);
        verify(twitterMessageProfileEnrichmentService).enrich(Type.TWITTER, twitterMessage);
    }
}
