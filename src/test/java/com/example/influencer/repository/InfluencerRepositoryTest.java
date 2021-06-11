package com.example.influencer.repository;

import com.example.influencer.TestUtil;
import com.example.influencer.model.Influencer;
import com.example.influencer.model.InfluencerProfile;
import com.example.influencer.model.Profile;
import com.example.influencer.model.Profile.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class InfluencerRepositoryTest {

    private InfluencerRepository influencerRepository;

    @BeforeEach
    void setup() {
        influencerRepository = new InfluencerRepository(TestUtil.objectMapper());
    }

    @Test
    public void testFindInfluencerByScreenName() {
        assertNotNull(influencerRepository.findInfluencerByScreenName("test1"));
    }

    @Test
    public void testFindInfluencerById() {
        assertNotNull(influencerRepository.findInfluencerById(Type.FACEBOOK, 22));
    }

    @Test
    public void testAddInfluencer() {
        InfluencerProfile profile = InfluencerProfile.builder()
                .id(25)
                .build();
        Profile profile1 = Profile.builder().facebook(profile).build();
        Influencer influencer = Influencer.builder().profiles(profile1).build();
        influencerRepository.addInfluencer(influencer);
        assertNotNull(influencerRepository.findInfluencerById(Type.FACEBOOK, 25));
    }
}