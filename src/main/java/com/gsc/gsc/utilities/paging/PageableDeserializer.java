package com.gsc.gsc.utilities.paging;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

public class PageableDeserializer extends StdDeserializer<Pageable> {

    public PageableDeserializer() {
        super(Pageable.class);
    }

    @Override
    public Pageable deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.readValueAsTree();

        int pageNumber = node.get("pageNumber").asInt();
        int pageSize = node.get("pageSize").asInt();

        // You can customize this part based on your requirements
        return PageRequest.of(pageNumber, pageSize);
    }
}
