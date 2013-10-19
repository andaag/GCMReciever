package no.codebox.gcmreciever.helpers;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Object parseBlock(String data) throws IOException {
        return objectMapper.reader(Object.class).readValue(data);
    }
}
