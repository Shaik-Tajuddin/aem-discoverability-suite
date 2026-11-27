package com.aem.discoverability.core.seo.sitemap.services.impl;

import com.aem.discoverability.core.seo.robots.services.impl.DomainPolicyImpl;
import com.aem.discoverability.core.seo.robots.services.impl.EnvironmentPolicyImpl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.aem.discoverability.core.seo.sitemap.api.DiscoverabilityPageProperties;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.testcontext.AppAemContext;
import com.day.cq.commons.Externalizer;
import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class CanonicalUrlServiceImplTest {

    private final AemContext context = AppAemContext.newAemContext();
    private CanonicalUrlServiceImpl service;
    private ResolvedConfiguration config;

    @BeforeEach
    void setUp() throws Exception {
        DomainPolicyImpl domainPolicy = new DomainPolicyImpl();

        service = new CanonicalUrlServiceImpl();
        var domainField = CanonicalUrlServiceImpl.class.getDeclaredField("domainPolicy");
        domainField.setAccessible(true);
        domainField.set(service, domainPolicy);

        config = ResolvedConfiguration.builder()
                .projectRoot("/content/site")
                .publicDomain("https://www.example.com")
                .build();
    }

    @Test
    void buildsAbsoluteUrlFromProjectRootRelativePath() {
        Page page = context.create().page("/content/site/en/about");
        assertEquals("https://www.example.com/en/about",
                service.resolve(page, config).orElse(null));
    }

    @Test
    void acceptsAllowlistedCanonicalOverride() {
        Page page = context.create().page("/content/site/en/about");
        context.resourceResolver().getResource(page.getPath() + "/jcr:content")
                .adaptTo(org.apache.sling.api.resource.ModifiableValueMap.class)
                .put(DiscoverabilityPageProperties.CANONICAL_URL, "https://www.example.com/about-us");

        assertEquals("https://www.example.com/about-us",
                service.resolve(page, config).orElse(null));
    }

    @Test
    void rejectsUntrustedHostOverride() {
        Page page = context.create().page("/content/site/en/about");
        context.resourceResolver().getResource(page.getPath() + "/jcr:content")
                .adaptTo(org.apache.sling.api.resource.ModifiableValueMap.class)
                .put(DiscoverabilityPageProperties.CANONICAL_URL, "https://attacker.test/about");

        assertFalse(service.resolve(page, config).isPresent());
    }

    @Test
    void failsClosedWithoutPublicDomain() {
        Page page = context.create().page("/content/site/en/about");
        ResolvedConfiguration noDomain = ResolvedConfiguration.builder()
                .projectRoot("/content/site")
                .publicDomain("")
                .build();
        assertTrue(service.resolve(page, noDomain).isEmpty());
    }

    @Test
    void fallsBackWhenExternalizerHostDoesNotMatchPublicDomain() throws Exception {
        Page page = context.create().page("/content/site/en/about");
        Externalizer externalizer = mock(Externalizer.class);
        when(externalizer.publishLink(any(), anyString())).thenReturn("http://localhost:4503/content/site/en/about");
        var field = CanonicalUrlServiceImpl.class.getDeclaredField("externalizer");
        field.setAccessible(true);
        field.set(service, externalizer);

        assertEquals("https://www.example.com/en/about",
                service.resolve(page, config).orElse(null));
    }
}
