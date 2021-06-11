package com.example.influencer.event.publisher;

import com.example.influencer.TestUtil;
import com.example.influencer.model.Influencer;
import com.example.influencer.model.InfluencerProfile;
import com.example.influencer.model.Profile;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ElasticsearchEventPublisherTest {

    @Mock
    KafkaTemplate<String, String> kafkaTemplate;

    private ElasticsearchEventPublisher elasticsearchEventPublisher;

    private String topicName = "topic";

    @BeforeEach
    void setup() {
        elasticsearchEventPublisher = new ElasticsearchEventPublisher(
                topicName, TestUtil.objectMapper(), kafkaTemplate);
    }

    @Test
    public void testPublishDeleteEvent() {
        elasticsearchEventPublisher.publishDeleteEvent(11);
        verify(kafkaTemplate).send(topicName, "{\"delete\":{\"_index\":\"influencers\",\"_retry_on_conflict\":3,"
                + "\"_id\":\"11\"}}\n");
    }

    @Test
    public void testPublishUpdateEvent() {
        var expectedMessage = "{\"update\":{\"_index\":\"influencers\",\"_retry_on_conflict\":3,\"_id\":\"42\"}}\n"
                + "{\"profiles\":{\"facebook\":{\"id\":42,\"bio\":\"this is test 4\",\"screen_name\":\"test4\","
                + "\"updated_at\":\"2021-01-01T21:13:42Z\"}},\"updated_at\":\"2021-01-01T21:13:42Z\"}\n";
        var updatedAt = Instant.parse("2021-01-01T21:13:42Z");
        InfluencerProfile influencerProfile = InfluencerProfile.builder()
                .id(42)
                .bio("this is test 4")
                .screenName("test4")
                .updatedAt(updatedAt)
                .build();
        Profile profile = Profile.builder()
                .facebook(influencerProfile)
                .build();
        Influencer influencer = Influencer.builder()
                .profiles(profile)
                .updatedAt(updatedAt)
                .build();
        elasticsearchEventPublisher.publishUpdateEvent(42, influencer);
        verify(kafkaTemplate).send(topicName, expectedMessage);
    }

    @Test
    public void testPublishCreateEvent() {
        var expectedMessage = "{\"index\":{\"_index\":\"influencers\",\"_retry_on_conflict\":3}}\n"
                + "{\"profiles\":{\"twitter\":{\"id\":81,\"image\":\"www.twitter.com/img/test81\","
                + "\"bio\":\"test81 bio\",\"screen_name\":\"test81\",\"updated_at\":\"2021-02-05T21:13:42Z\"}},"
                + "\"updated_at\":\"2021-02-05T21:13:42Z\"}\n";
        var updatedAt = Instant.parse("2021-02-05T21:13:42Z");
        InfluencerProfile influencerProfile = InfluencerProfile.builder()
                .id(81)
                .image("www.twitter.com/img/test81")
                .bio("test81 bio")
                .screenName("test81")
                .updatedAt(updatedAt)
                .build();
        Profile profile = Profile.builder()
                .twitter(influencerProfile)
                .build();
        Influencer influencer = Influencer.builder()
                .profiles(profile)
                .updatedAt(updatedAt)
                .build();
        elasticsearchEventPublisher.publishIndexEvent(influencer);
        verify(kafkaTemplate).send(topicName, expectedMessage);
    }

}
