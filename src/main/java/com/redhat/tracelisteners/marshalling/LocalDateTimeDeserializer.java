package com.redhat.tracelisteners.marshalling;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;

public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    public LocalDateTimeDeserializer(Class vc) {
        super(vc);
    }

    public LocalDateTimeDeserializer() {
        this(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext ctxt)
            throws IOException {
        return LocalDateTime.parse(parser.readValueAs(String.class));
    }


}

