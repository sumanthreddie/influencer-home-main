package com.example.influencer.configuration;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.bootstrap-address}")
    private String bootstrapAddress;

    @Value("${kafka.topic-name}")
    private String influencersTopicName;

    @Bean
    public KafkaAdmin configureKafkaAdmin() {
        Map<String, Object> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(config);
    }

    @Bean
    public NewTopic createInfluncersTopic() {
        return new NewTopic(influencersTopicName, 1, (short) 1);
    }
}