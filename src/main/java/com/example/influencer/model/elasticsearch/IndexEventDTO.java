package com.example.influencer.model.elasticsearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@With
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class IndexEventDTO {

    @JsonProperty(value = "_index")
    private String index = "influencers";

    @JsonProperty(value = "_retry_on_conflict")
    private int retryOnConflict = 3;

    @JsonProperty(value = "_id")
    private String id;
}