package com.example.demo.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public void serialize(final LocalDateTime value,
                          final JsonGenerator gen,
                          final SerializerProvider serializers)
            throws IOException {
        final String dateString = value.format(this.formatter);
        gen.writeString(dateString);
    }
}
