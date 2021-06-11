package com.example.influencer.service.impl;

import com.example.influencer.event.publisher.ElasticsearchEventPublisher;
import com.example.influencer.model.Influencer;
import com.example.influencer.model.InfluencerProfile;
import com.example.influencer.model.Profile;
import com.example.influencer.model.Profile.Type;
import com.example.influencer.model.nsq.Message;
import com.example.influencer.repository.InfluencerRepository;
import com.example.influencer.service.ProfileEnrichmentService;
import java.time.Instant;
import java.util.Optional;
import java.util.logging.Logger;

public abstract class ProfileEnrichmentServiceBase<T extends Message> implements ProfileEnrichmentService<T> {

    private static final Logger LOGGER = Logger.getLogger(ProfileEnrichmentServiceBase.class.getName());

    private final InfluencerRepository influencerRepository;

    private final ElasticsearchEventPublisher elasticsearchEventPublisher;

    public ProfileEnrichmentServiceBase(final InfluencerRepository influencerRepository,
            final ElasticsearchEventPublisher eventPublisher) {
        this.influencerRepository = influencerRepository;
        this.elasticsearchEventPublisher = eventPublisher;
    }

    abstract InfluencerProfile getInfluencerProfile(T message);

    @Override
    public void enrich(Type type, T message) {
        LOGGER.info(String.format("Enriching message %s %s.", type, message));

        InfluencerProfile newProfile = getInfluencerProfile(message);

        if (newProfile.getId() == null || newProfile.getScreenName() == null ||
                (newProfile.getUpdatedAt() == null && message.getDeletedAt() == null)) {
            // missing one of the required attributes, ignore this message
            LOGGER.info("Ignoring this message as it doesn't contain the required attributes.");
            return;
        }

        boolean shouldDelete = message.getDeletedAt() != null;

        Instant updatedAt = newProfile.getUpdatedAt();

        Optional<Influencer> influencerById = influencerRepository.findInfluencerById(
                type, newProfile.getId());

        if (shouldDelete) {
            if (influencerById.isEmpty()) {
                // influencer not found, abort silently
                return;
            }

            getInfluencerProfile(influencerById.get(), type).setDeleted(true);

            if (isSocialProfileExists(influencerById.get())) {
                elasticsearchEventPublisher.publishUpdateEvent(newProfile.getId(), createInfluencer(type,
                        InfluencerProfile.builder().build(), updatedAt));
            } else {
                elasticsearchEventPublisher.publishDeleteEvent(newProfile.getId());
            }
            return;
        }

        if (influencerById.isPresent()) {
            // check if updated_at is before the new profile, if yes update the influencer profile.
            InfluencerProfile existingInfluencerProfile = influencerById.get().getProfiles().getInfluencerProfile(type);

            if (existingInfluencerProfile.isDeleted()) {
                // already deleted, don't perform any updates
                return;
            }

            // update the profile only if it's the latest data
            if (existingInfluencerProfile.getUpdatedAt().isBefore(newProfile.getUpdatedAt())) {
                updateInfluencerProfile(existingInfluencerProfile, newProfile);
                elasticsearchEventPublisher.publishUpdateEvent(newProfile.getId(),
                        createInfluencer(type, newProfile, updatedAt));
            }
        } else {
            Optional<Influencer> influencerByScreenName = influencerRepository.findInfluencerByScreenName(
                    newProfile.getScreenName());
            if (influencerByScreenName.isPresent()) {
                // update the other profile of the influencer
                addSocialProfile(influencerByScreenName.get(), type, newProfile);
                elasticsearchEventPublisher.publishUpdateEvent(newProfile.getId(),
                        createInfluencer(type, newProfile, updatedAt));
            } else {
                // create a new profile for the influencer
                Influencer influencer = createInfluencer(type, newProfile, updatedAt);
                influencerRepository.addInfluencer(influencer);
                elasticsearchEventPublisher.publishIndexEvent(influencer);
            }
        }
    }

    private InfluencerProfile getInfluencerProfile(Influencer influencer, Type type) {
        return type.equals(Type.FACEBOOK) ? influencer.getProfiles().getFacebook() : influencer.getProfiles()
                .getTwitter();
    }

    private Influencer createInfluencer(Type type, InfluencerProfile newProfile, Instant updatedAt) {
        Profile profile = Profile.builder().build();
        if (type.equals(Type.FACEBOOK)) {
            profile.setFacebook(newProfile);
        } else {
            profile.setTwitter(newProfile);
        }

        return Influencer.builder()
                .profiles(profile)
                .updatedAt(updatedAt)
                .build();

    }

    private void addSocialProfile(Influencer influencer, Type type, InfluencerProfile newProfile) {
        if (type.equals(Type.FACEBOOK)) {
            influencer.getProfiles().setFacebook(newProfile);
        } else {
            influencer.getProfiles().setTwitter(newProfile);
        }
    }

    private void updateInfluencerProfile(InfluencerProfile existingProfile, InfluencerProfile newProfile) {
        existingProfile.setBio(newProfile.getBio());
        existingProfile.setImage(newProfile.getImage());
        existingProfile.setLocation(newProfile.getLocation());
        existingProfile.setScreenName(newProfile.getScreenName());
        existingProfile.setUpdatedAt(newProfile.getUpdatedAt());
    }

    private boolean isSocialProfileExists(Influencer influencer) {
        Profile profile = influencer.getProfiles();
        return (profile.getFacebook() != null && !profile.getFacebook().isDeleted())
                || (profile.getTwitter() != null && !profile.getTwitter().isDeleted());
    }
}
