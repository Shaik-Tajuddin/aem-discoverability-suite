package com.aem.discoverability.core.seo.robots.services.impl;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;

class RobotsGeneratorImplTest {

    private final RobotsGeneratorImpl generator = new RobotsGeneratorImpl();

    @BeforeEach
    void setUp() throws Exception {
        DomainPolicyImpl domainPolicy = new DomainPolicyImpl();
        Field domainField = RobotsGeneratorImpl.class.getDeclaredField("domainPolicy");
        domainField.setAccessible(true);
        domainField.set(generator, domainPolicy);
    }

    @Test
    void disabledReturnsEmpty() {
        ResolvedConfiguration config = baseBuilder().robotsEnabled(false).build();
        Assertions.assertEquals("", generator.generate(config));
    }

    @Test
    void emitsRules() {
        ResolvedConfiguration config = baseBuilder()
                .enabled(true)
                .robotsEnabled(true)
                .robotsAllowRules(new String[] { "/" })
                .robotsDisallowRules(new String[] { "/internal/" })
                .build();
        String body = generator.generate(config);
        Assertions.assertTrue(body.contains("User-agent: *"));
        Assertions.assertTrue(body.contains("Allow: /"));
        Assertions.assertTrue(body.contains("Disallow: /internal/"));
    }

    @Test
    void sitemapUrlsValidateHost() {
        ResolvedConfiguration config = baseBuilder()
                .enabled(true)
                .robotsEnabled(true)
                .robotsManualSitemapUrls(new String[] {
                        "https://www.example.com/sitemap.xml",
                        "https://evil.example/sitemap.xml"
                })
                .build();
        String body = generator.generate(config);
        Assertions.assertTrue(body.contains("Sitemap: https://www.example.com/sitemap.xml"));
        Assertions.assertFalse(body.contains("evil.example"));
    }

    private static ResolvedConfiguration.Builder baseBuilder() {
        return ResolvedConfiguration.builder()
                .projectRoot("/content/site")
                .publicDomain("https://www.example.com");
    }
}
