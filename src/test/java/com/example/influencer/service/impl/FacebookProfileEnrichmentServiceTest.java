package com.example.influencer.service.impl;

import com.example.influencer.event.publisher.ElasticsearchEventPublisher;
import com.example.influencer.model.Influencer;
import com.example.influencer.model.InfluencerProfile;
import com.example.influencer.model.Profile;
import com.example.influencer.model.Profile.Type;
import com.example.influencer.model.nsq.FacebookMessage;
import com.example.influencer.repository.InfluencerRepository;
import com.example.influencer.service.ProfileEnrichmentService;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FacebookProfileEnrichmentServiceTest {

    @Mock
    InfluencerRepository influencerRepository;

    @Mock
    ElasticsearchEventPublisher elasticsearchEventPublisher;

    private ProfileEnrichmentService<FacebookMessage> facebookMessageProfileEnrichmentService;

    @BeforeEach
    void setup() {
        facebookMessageProfileEnrichmentService = new FacebookProfileEnrichmentService(influencerRepository,
                elasticsearchEventPublisher);
    }

    @Test
    public void testCreateNewInfluencer() {
        when(influencerRepository.findInfluencerById(Type.FACEBOOK, 28)).thenReturn(Optional.empty());
        when(influencerRepository.findInfluencerByScreenName(anyString())).thenReturn(Optional.empty());
        var now = Instant.now();
        FacebookMessage facebookMessage = buildMessage(28, "test", now);
        facebookMessageProfileEnrichmentService.enrich(Type.FACEBOOK, facebookMessage);

        ArgumentCaptor<Influencer> influencerArgumentCaptor = ArgumentCaptor.forClass(Influencer.class);

        verify(influencerRepository).addInfluencer(influencerArgumentCaptor.capture());

        Influencer influencer = influencerArgumentCaptor.getValue();

        assertNotNull(influencer);
        assertNotNull(influencer.getProfiles());
        assertNotNull(influencer.getProfiles().getFacebook());
        assertEquals(28, influencer.getProfiles().getFacebook().getId());
        assertEquals("test", influencer.getProfiles().getFacebook().getScreenName());
        assertEquals(now, influencer.getProfiles().getFacebook().getUpdatedAt());
        verify(elasticsearchEventPublisher, atMostOnce()).publishIndexEvent(any(Influencer.class));
    }

    @Test
    public void testAddNewProfileToExistingInfluencer() {
        var profiles = Profile.builder().build();
        var mockInfluencer = Influencer.builder().profiles(profiles).build();
        when(influencerRepository.findInfluencerById(Type.FACEBOOK, 28)).thenReturn(Optional.empty());
        when(influencerRepository.findInfluencerByScreenName(anyString()))
                .thenReturn(Optional.of(mockInfluencer));
        var now = Instant.now();
        FacebookMessage facebookMessage = buildMessage(28, "test", now);
        facebookMessageProfileEnrichmentService.enrich(Type.FACEBOOK, facebookMessage);

        InfluencerProfile influencerProfile = mockInfluencer.getProfiles().getFacebook();

        assertNotNull(influencerProfile);
        assertEquals(28, influencerProfile.getId());
        assertEquals("test", influencerProfile.getScreenName());
        assertEquals(now, influencerProfile.getUpdatedAt());
        verify(elasticsearchEventPublisher, atMostOnce()).publishUpdateEvent(anyInt(), any(Influencer.class));
    }

    @Test
    public void testUpdateExistingInfluencerProfile() {
        var now = Instant.now();
        var influencerProfile = InfluencerProfile.builder()
                .isDeleted(false)
                .id(28)
                .updatedAt(now.minus(Duration.of(2, ChronoUnit.DAYS)))
                .build();
        var profiles = Profile.builder().facebook(influencerProfile).build();
        var mockInfluencer = Influencer.builder().profiles(profiles).build();
        when(influencerRepository.findInfluencerById(Type.FACEBOOK, 28))
                .thenReturn(Optional.of(mockInfluencer));
        FacebookMessage facebookMessage = buildMessage(28, "test", now);
        facebookMessageProfileEnrichmentService.enrich(Type.FACEBOOK, facebookMessage);

        InfluencerProfile updatedInfluencerProfile = mockInfluencer.getProfiles().getFacebook();

        assertNotNull(updatedInfluencerProfile);
        assertEquals(28, updatedInfluencerProfile.getId());
        assertEquals("test", updatedInfluencerProfile.getScreenName());
        assertEquals(now, updatedInfluencerProfile.getUpdatedAt());
        verify(elasticsearchEventPublisher, atMostOnce()).publishUpdateEvent(anyInt(), any(Influencer.class));
    }

    @Test
    public void testIgnoreUpdateForDeletedInfluencerProfile() {
        var now = Instant.now();
        var influencerProfile = InfluencerProfile.builder()
                .isDeleted(true)
                .id(28)
                .screenName("abc")
                .updatedAt(now.minus(Duration.of(2, ChronoUnit.DAYS)))
                .build();
        var profiles = Profile.builder().facebook(influencerProfile).build();
        var mockInfluencer = Influencer.builder().profiles(profiles).build();
        when(influencerRepository.findInfluencerById(Type.FACEBOOK, 28))
                .thenReturn(Optional.of(mockInfluencer));
        FacebookMessage facebookMessage = buildMessage(28, "test", now);
        facebookMessageProfileEnrichmentService.enrich(Type.FACEBOOK, facebookMessage);

        InfluencerProfile updatedInfluencerProfile = mockInfluencer.getProfiles().getFacebook();

        assertNotNull(updatedInfluencerProfile);
        assertEquals(28, updatedInfluencerProfile.getId());
        assertEquals("abc", updatedInfluencerProfile.getScreenName());
        assertEquals(influencerProfile.getUpdatedAt(), updatedInfluencerProfile.getUpdatedAt());
        verify(elasticsearchEventPublisher, never()).publishUpdateEvent(anyInt(), any(Influencer.class));
    }

    @Test
    public void testIgnoreUpdateForInfluencerProfileWithLatestUpdatedAt() {
        var now = Instant.now();
        var influencerProfile = InfluencerProfile.builder()
                .isDeleted(true)
                .id(28)
                .screenName("abc")
                .updatedAt(now.plus(Duration.of(2, ChronoUnit.DAYS)))
                .build();
        var profiles = Profile.builder().facebook(influencerProfile).build();
        var mockInfluencer = Influencer.builder().profiles(profiles).build();
        when(influencerRepository.findInfluencerById(Type.FACEBOOK, 28))
                .thenReturn(Optional.of(mockInfluencer));
        FacebookMessage facebookMessage = buildMessage(28, "test", now);
        facebookMessageProfileEnrichmentService.enrich(Type.FACEBOOK, facebookMessage);

        InfluencerProfile updatedInfluencerProfile = mockInfluencer.getProfiles().getFacebook();

        assertNotNull(updatedInfluencerProfile);
        assertEquals(28, updatedInfluencerProfile.getId());
        assertEquals("abc", updatedInfluencerProfile.getScreenName());
        assertEquals(influencerProfile.getUpdatedAt(), updatedInfluencerProfile.getUpdatedAt());
        verify(elasticsearchEventPublisher, never()).publishUpdateEvent(anyInt(), any(Influencer.class));
    }

    @Test
    public void testDeleteExistingInfluencerProfile() {
        var now = Instant.now();
        var influencerProfile = InfluencerProfile.builder()
                .isDeleted(false)
                .id(28)
                .screenName("abc")
                .updatedAt(now.plus(Duration.of(2, ChronoUnit.DAYS)))
                .build();
        var profiles = Profile.builder().facebook(influencerProfile).build();
        var mockInfluencer = Influencer.builder().profiles(profiles).build();
        when(influencerRepository.findInfluencerById(Type.FACEBOOK, 28))
                .thenReturn(Optional.of(mockInfluencer));
        FacebookMessage facebookMessage = buildMessage(28, "test", now);
        facebookMessage.setDeletedAt(now);
        facebookMessageProfileEnrichmentService.enrich(Type.FACEBOOK, facebookMessage);

        assertTrue(influencerProfile.isDeleted());
        verify(elasticsearchEventPublisher).publishDeleteEvent(anyInt());
    }

    @Test
    public void testPublishUpdateEventWhenOtherProfileExistsForAnInfluencer() {
        var now = Instant.now();
        var influencerProfile = InfluencerProfile.builder()
                .isDeleted(false)
                .id(28)
                .screenName("abc")
                .updatedAt(now.plus(Duration.of(2, ChronoUnit.DAYS)))
                .build();
        var twitterProfile = InfluencerProfile.builder().build();
        var profiles = Profile.builder().facebook(influencerProfile).twitter(twitterProfile).build();
        var mockInfluencer = Influencer.builder().profiles(profiles).build();
        when(influencerRepository.findInfluencerById(Type.FACEBOOK, 28))
                .thenReturn(Optional.of(mockInfluencer));
        FacebookMessage facebookMessage = buildMessage(28, "test", now);
        facebookMessage.setDeletedAt(now);
        facebookMessageProfileEnrichmentService.enrich(Type.FACEBOOK, facebookMessage);

        assertTrue(influencerProfile.isDeleted());
        verify(elasticsearchEventPublisher).publishUpdateEvent(anyInt(), any(Influencer.class));

    }

    @ParameterizedTest
    @MethodSource("paramsForIgnoreMessage")
    public void testIgnoreMessage(FacebookMessage message) {
        facebookMessageProfileEnrichmentService.enrich(Type.FACEBOOK, message);
        verify(influencerRepository, never()).findInfluencerById(any(), anyInt());
        verify(influencerRepository, never()).findInfluencerByScreenName(anyString());
        verify(elasticsearchEventPublisher, never()).publishDeleteEvent(anyInt());
        verify(elasticsearchEventPublisher, never()).publishIndexEvent(any());
        verify(elasticsearchEventPublisher, never()).publishUpdateEvent(anyInt(), any());
    }

    private static Stream<Arguments> paramsForIgnoreMessage() {
        return Stream.of(
                Arguments.of(buildMessage(1, "test", null)),
                Arguments.of(buildMessage(1, null, Instant.now())),
                Arguments.of(buildMessage(null, "test", Instant.now()))
        );
    }

    private static FacebookMessage buildMessage(Integer id, String screenName, Instant updatedAt) {
        return FacebookMessage.builder()
                .id(id)
                .username(screenName)
                .updatedAt(updatedAt)
                .build();
    }

}
