package com.example.influencer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TestUtil {

    private TestUtil() {

    }

    public static ObjectMapper objectMapper() {
        var objectMapper = new ObjectMapper();
        var timeModule = new JavaTimeModule();
        timeModule.addSerializer(Instant.class, new JsonSerializer<Instant>() {
            @Override
            public void serialize(Instant instant, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                    throws IOException {
                var out = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        .withZone(ZoneId.from(ZoneOffset.UTC))
                        .format(instant);
                jsonGenerator.writeString(out + "Z");
            }
        });
        objectMapper.registerModule(timeModule);
        return objectMapper;
    }
}
