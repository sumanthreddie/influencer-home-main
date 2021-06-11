package com.example.influencer.event.publisher;

import com.example.influencer.model.Influencer;
import com.example.influencer.model.elasticsearch.DeleteEvent;
import com.example.influencer.model.elasticsearch.IndexEvent;
import com.example.influencer.model.elasticsearch.IndexEventDTO;
import com.example.influencer.model.elasticsearch.UpdateEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.kafka.core.KafkaTemplate;

public class ElasticsearchEventPublisher {

    private static final IndexEventDTO INDEX_EVENT_DTO = IndexEventDTO.builder()
            .index("influencers")
            .retryOnConflict(3)
            .build();

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final String topicName;

    private final ObjectMapper objectMapper;

    public ElasticsearchEventPublisher(final String topicName,
            final ObjectMapper objectMapper, final KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topicName = topicName;
    }

    public void publishIndexEvent(final Influencer influencer) {
        String message = String.format("%s\n%s\n",
                toJson(IndexEvent.builder().index(INDEX_EVENT_DTO).build()),
                toJson(influencer));
        publishMessage(message);
    }

    public void publishUpdateEvent(final int id, final Influencer influencer) {
        String message = String.format("%s\n%s\n",
                toJson(UpdateEvent.builder().update(INDEX_EVENT_DTO.withId(String.valueOf(id))).build()),
                toJson(influencer));
        publishMessage(message);
    }

    public void publishDeleteEvent(final int id) {
        String message = String.format("%s\n",
                toJson(DeleteEvent.builder().delete(INDEX_EVENT_DTO.withId(String.valueOf(id))).build()));
        publishMessage(message);
    }

    private void publishMessage(final String message) {
        kafkaTemplate.send(topicName, message);
    }

    @SneakyThrows
    private String toJson(final Object object) {
        return objectMapper.writeValueAsString(object);
    }
}
