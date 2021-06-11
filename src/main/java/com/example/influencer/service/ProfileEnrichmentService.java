package com.example.influencer.service;

import com.example.influencer.model.Profile.Type;
import com.example.influencer.model.nsq.Message;

public interface ProfileEnrichmentService<T extends Message> {

    void enrich(Type type, T message);
}