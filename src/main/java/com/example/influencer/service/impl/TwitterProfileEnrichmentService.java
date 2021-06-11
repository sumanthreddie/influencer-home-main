package com.example.influencer.service.impl;

import com.example.influencer.event.publisher.ElasticsearchEventPublisher;
import com.example.influencer.model.InfluencerProfile;
import com.example.influencer.model.nsq.TwitterMessage;
import com.example.influencer.repository.InfluencerRepository;
import java.util.Objects;

public class TwitterProfileEnrichmentService extends ProfileEnrichmentServiceBase<TwitterMessage> {

    public TwitterProfileEnrichmentService(final InfluencerRepository influencerRepository,
            final ElasticsearchEventPublisher eventPublisher) {
        super(influencerRepository, eventPublisher);
    }

    @Override
    InfluencerProfile getInfluencerProfile(TwitterMessage message) {
        Objects.requireNonNull(message);
        return InfluencerProfile.builder()
                .bio(message.getBiography())
                .screenName(message.getScreenName())
                .id(message.getId())
                .location(message.getLocation())
                .image(message.getProfileImage())
                .updatedAt(message.getUpdatedAt())
                .build();
    }
}