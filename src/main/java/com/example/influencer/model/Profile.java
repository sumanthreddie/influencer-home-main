package com.example.influencer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_ABSENT)
public class Profile {

    public enum Type {
        FACEBOOK, TWITTER;
    }

    private InfluencerProfile twitter;

    private InfluencerProfile facebook;

    public InfluencerProfile getInfluencerProfile(Type type) {
        if (type.equals(Type.FACEBOOK))
            return facebook;
        return twitter;
    }
}
