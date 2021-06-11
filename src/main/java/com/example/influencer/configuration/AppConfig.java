package com.example.influencer.configuration;

import com.example.influencer.event.publisher.ElasticsearchEventPublisher;
import com.example.influencer.model.nsq.FacebookMessage;
import com.example.influencer.model.nsq.TwitterMessage;
import com.example.influencer.repository.InfluencerRepository;
import com.example.influencer.service.ProfileEnrichmentService;
import com.example.influencer.service.impl.FacebookProfileEnrichmentService;
import com.example.influencer.service.impl.ProfileEnrichmentServiceBase;
import com.example.influencer.service.impl.TwitterProfileEnrichmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class AppConfig {

    @Bean
    ProfileEnrichmentService<TwitterMessage> configureTwitterEnrichmentService(
            final InfluencerRepository influencerRepository,
            final ElasticsearchEventPublisher eventPublisher) {
        return new TwitterProfileEnrichmentService(influencerRepository, eventPublisher);
    }

    @Bean
    ProfileEnrichmentServiceBase<FacebookMessage> configureFacebookEnrichmentService(
            final InfluencerRepository influencerRepository,
            final ElasticsearchEventPublisher eventPublisher) {
        return new FacebookProfileEnrichmentService(influencerRepository, eventPublisher);
    }

    @Bean
    ElasticsearchEventPublisher configureElasticSearchEventPublisher(final KafkaTemplate<String, String> kafkaTemplate,
            final ObjectMapper objectMapper, @Value("${kafka.topic-name}") final String topicName) {
        return new ElasticsearchEventPublisher(topicName, objectMapper, kafkaTemplate);
    }

    @Bean
    InfluencerRepository configureInfluencerRepository(final ObjectMapper objectMapper) {
        return new InfluencerRepository(objectMapper);
    }
}
