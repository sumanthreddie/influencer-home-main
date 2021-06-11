package com.example.influencer.repository;

import com.example.influencer.model.Influencer;
import com.example.influencer.model.InfluencerProfile;
import com.example.influencer.model.Profile;
import com.example.influencer.model.Profile.Type;
import com.example.influencer.model.elasticsearch.Document;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;
import org.springframework.core.io.ClassPathResource;

public class InfluencerRepository {

    private static final Logger LOGGER = Logger.getLogger(InfluencerRepository.class.getName());

    private final List<Document> INFLUENCER_DB;

    public InfluencerRepository(final ObjectMapper objectMapper) {
        INFLUENCER_DB = initDb(objectMapper);
    }

    private List<Document> initDb(final ObjectMapper objectMapper) {
        ClassPathResource resource = new ClassPathResource("db.json");
        try {
            InputStream inputStream = new FileInputStream(resource.getFile());
            return new ArrayList<>(Arrays.asList(objectMapper.readValue(inputStream, Document[].class)));
        } catch (IOException e) {
            LOGGER.severe(String.format("Failed to initialize the db: %s", e.getMessage()));
        }
        return new ArrayList<>();
    }

    private Optional<Document> findDocumentByScreenName(final String screenName) {
        Objects.requireNonNull(screenName);
        return INFLUENCER_DB.stream()
                .filter(influencer -> {
                    Profile profiles = influencer.getSource().getProfiles();
                    InfluencerProfile facebookProfile = profiles.getFacebook();
                    InfluencerProfile twitterProfile = profiles.getTwitter();
                    return (twitterProfile != null && twitterProfile.getScreenName().equalsIgnoreCase(screenName)) ||
                            (facebookProfile != null && facebookProfile.getScreenName().equalsIgnoreCase(screenName));
                })
                .findFirst();
    }

    public Optional<Influencer> findInfluencerByScreenName(final String screenName) {
        return findDocumentByScreenName(screenName).map(Document::getSource);
    }

    public Optional<Influencer> findInfluencerById(final Type type, final int id) {
        Objects.requireNonNull(type);
        return INFLUENCER_DB.stream()
                .map(Document::getSource)
                .filter(influencer -> {
                    Profile profile = influencer.getProfiles();
                    return (type.equals(Type.FACEBOOK) && profile.getFacebook() != null &&
                            profile.getFacebook().getId() == id) ||
                            (type.equals(Type.TWITTER) && profile.getTwitter() != null &&
                                    profile.getTwitter().getId() == id);
                })
                .findFirst();
    }

    public synchronized void addInfluencer(Influencer influencer) {
        Objects.requireNonNull(influencer);
        Document document = Document.builder()
                .id(UUID.randomUUID().toString())
                .source(influencer)
                .build();
        INFLUENCER_DB.add(document);
    }
}
