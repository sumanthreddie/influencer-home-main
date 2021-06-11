package com.example.influencer.service.impl;

import com.example.influencer.event.publisher.ElasticsearchEventPublisher;
import com.example.influencer.model.InfluencerProfile;
import com.example.influencer.model.nsq.FacebookMessage;
import com.example.influencer.repository.InfluencerRepository;
import java.util.Objects;

public class FacebookProfileEnrichmentService extends ProfileEnrichmentServiceBase<FacebookMessage> {

    public FacebookProfileEnrichmentService(final InfluencerRepository influencerRepository,
            final ElasticsearchEventPublisher eventPublisher) {
        super(influencerRepository, eventPublisher);
    }

    @Override
    InfluencerProfile getInfluencerProfile(FacebookMessage message) {
        Objects.requireNonNull(message);
        return InfluencerProfile.builder()
                .updatedAt(message.getUpdatedAt())
                .id(message.getId())
                .bio(message.getAbout())
                .image(message.getImage())
                .location(message.getLocation())
                .screenName(message.getUsername())
                .build();
    }
}