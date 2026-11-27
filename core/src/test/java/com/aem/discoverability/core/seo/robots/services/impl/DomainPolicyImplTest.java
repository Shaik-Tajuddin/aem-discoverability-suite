package com.aem.discoverability.core.seo.robots.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;

class DomainPolicyImplTest {

    private final DomainPolicyImpl policy = new DomainPolicyImpl();

    @Test
    void publicOriginFromTrustedConfigOnly() {
        ResolvedConfiguration config = ResolvedConfiguration.builder()
                .projectRoot("/content/site")
                .publicDomain("https://WWW.Example.com/path")
                .build();

        assertEquals("https://www.example.com", policy.publicOrigin(config).orElse(null));
    }

    @Test
    void rejectsBlankOrInvalidDomain() {
        assertFalse(policy.publicOrigin(ResolvedConfiguration.builder()
                .projectRoot("/content/site")
                .publicDomain(" ")
                .build()).isPresent());
        assertFalse(policy.publicOrigin(ResolvedConfiguration.builder()
                .projectRoot("/content/site")
                .publicDomain("not-a-uri")
                .build()).isPresent());
    }

    @Test
    void crawlAllowedWhenConfigurationPresent() {
        assertTrue(policy.isCrawlAllowed(ResolvedConfiguration.builder()
                .projectRoot("/content/site")
                .publicDomain("https://www.example.com")
                .build()));
        assertFalse(policy.isCrawlAllowed(null));
    }
}
