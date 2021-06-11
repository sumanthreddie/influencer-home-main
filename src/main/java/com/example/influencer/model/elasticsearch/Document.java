package com.example.influencer.model.elasticsearch;

import com.example.influencer.model.Influencer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Document {

    @JsonProperty(value = "_index", defaultValue = "influencers")
    private String index;

    @JsonProperty(value = "_type", defaultValue = "_doc")
    private String doc;

    @JsonProperty(value = "_id")
    private String id;

    @JsonProperty(value = "_source")
    private Influencer source;
}
