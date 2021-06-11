package com.example.influencer.configuration;

import com.example.influencer.event.processor.FacebookMessageEventProcessor;
import com.example.influencer.event.processor.TwitterMessageEventProcessor;
import com.example.influencer.model.nsq.FacebookMessage;
import com.example.influencer.model.nsq.TwitterMessage;
import com.example.influencer.service.ProfileEnrichmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.brainlag.nsq.NSQConsumer;
import com.github.brainlag.nsq.lookup.DefaultNSQLookup;
import com.github.brainlag.nsq.lookup.NSQLookup;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NSQConsumerConfig {

    @Value("${nsq.host}")
    private String nsqHost;

    @Value("${nsq.port}")
    private int nsqPort;

    @Value("${nsq.topic.twitter}")
    private String twitterTopic;

    @Value("${nsq.topic.facebook}")
    private String facebookTopic;

    @Bean
    NSQLookup configureNSQLookup() {
        NSQLookup lookup = new DefaultNSQLookup();
        lookup.addLookupAddress(nsqHost, nsqPort);
        return lookup;
    }

    @Bean
    @Qualifier("twitter-nsq-consumer")
    NSQConsumer configureTwitterNSQConsumer(NSQLookup nsqLookup, ObjectMapper objectMapper,
            ProfileEnrichmentService<TwitterMessage> twitterMessageProfileEnrichmentService) {
        NSQConsumer consumer = new NSQConsumer(nsqLookup, twitterTopic, twitterTopic,
                new TwitterMessageEventProcessor(objectMapper, TwitterMessage.class,
                        twitterMessageProfileEnrichmentService));
        consumer.start();
        return consumer;
    }

    @Bean
    @Qualifier("facebook-nsq-consumer")
    NSQConsumer configureFacebookNSQConsumer(NSQLookup nsqLookup, ObjectMapper objectMapper,
            ProfileEnrichmentService<FacebookMessage> facebookMessageProfileEnrichmentService) {
        NSQConsumer consumer = new NSQConsumer(nsqLookup, facebookTopic, facebookTopic,
                new FacebookMessageEventProcessor(objectMapper, FacebookMessage.class,
                        facebookMessageProfileEnrichmentService));
        consumer.start();
        return consumer;
    }
}
