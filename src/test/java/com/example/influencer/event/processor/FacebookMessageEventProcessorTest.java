package com.example.influencer.event.processor;

import com.example.influencer.model.Profile.Type;
import com.example.influencer.model.nsq.FacebookMessage;
import com.example.influencer.service.ProfileEnrichmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class FacebookMessageEventProcessorTest {

    @Mock
    ObjectMapper objectMapper;

    @Mock
    ProfileEnrichmentService<FacebookMessage> facebookMessageProfileEnrichmentService;

    private FacebookMessageEventProcessor facebookMessageEventProcessor;

    @BeforeEach
    void setup() {
        facebookMessageEventProcessor = new FacebookMessageEventProcessor(
                objectMapper, FacebookMessage.class, facebookMessageProfileEnrichmentService);
    }

    @Test
    public void testProcessMessageSuccessfully() {
        var facebookMessage = FacebookMessage.builder().build();
        facebookMessageEventProcessor.processMessage(facebookMessage);
        verify(facebookMessageProfileEnrichmentService).enrich(Type.FACEBOOK, facebookMessage);
    }
}
