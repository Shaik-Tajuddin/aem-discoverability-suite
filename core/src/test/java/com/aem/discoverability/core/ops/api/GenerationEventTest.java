package com.aem.discoverability.core.ops.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GenerationEventTest {

    @Test
    void toJsonContainsSchemaFieldsWithoutBody() {
        String json = GenerationEvent.builder()
                .correlationId("req-1")
                .project("site")
                .output("sitemap")
                .productionEnvironment(true)
                .durationMs(42)
                .pagesVisited(10)
                .pagesIncluded(8)
                .pagesExcluded(2)
                .outputBytes(1200)
                .status("success")
                .build()
                .toJson();

        assertTrue(json.contains("\"event\":\"discoverability.generation.completed\""));
        assertTrue(json.contains("\"correlationId\":\"req-1\""));
        assertTrue(json.contains("\"output\":\"sitemap\""));
        assertTrue(json.contains("\"durationMs\":42"));
        assertFalse(json.contains("jcr:content"));
        assertFalse(json.contains("<html"));
    }
}
