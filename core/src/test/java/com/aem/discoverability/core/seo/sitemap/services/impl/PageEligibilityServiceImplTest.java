package com.aem.discoverability.core.seo.sitemap.services.impl;

import com.aem.discoverability.core.seo.robots.services.impl.DomainPolicyImpl;
import com.aem.discoverability.core.seo.robots.services.impl.EnvironmentPolicyImpl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.aem.discoverability.core.seo.sitemap.api.DiscoverabilityPageProperties;
import com.aem.discoverability.core.seo.sitemap.api.EligibilityResult;
import com.aem.discoverability.core.seo.sitemap.api.ExclusionReason;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.testcontext.AppAemContext;
import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class PageEligibilityServiceImplTest {

    private final AemContext context = AppAemContext.newAemContext();
    private PageEligibilityServiceImpl eligibility;
    private ResolvedConfiguration config;

    @BeforeEach
    void setUp() throws Exception {
        DomainPolicyImpl domainPolicy = new DomainPolicyImpl();

        CanonicalUrlServiceImpl canonicalUrlService = new CanonicalUrlServiceImpl();
        var domainField = CanonicalUrlServiceImpl.class.getDeclaredField("domainPolicy");
        domainField.setAccessible(true);
        domainField.set(canonicalUrlService, domainPolicy);

        eligibility = new PageEligibilityServiceImpl();
        var canonicalField = PageEligibilityServiceImpl.class.getDeclaredField("canonicalUrlService");
        canonicalField.setAccessible(true);
        canonicalField.set(eligibility, canonicalUrlService);

        config = ResolvedConfiguration.builder()
                .projectRoot("/content/site")
                .publicDomain("https://www.example.com")
                .enabled(true)
                .build();
    }

    @Test
    void eligiblePage() {
        Page page = context.create().page("/content/site/en/about");
        EligibilityResult result = eligibility.evaluate(page, config);
        assertTrue(result.isEligible());
        assertEquals(ExclusionReason.NONE, result.getReason());
    }

    @Test
    void outsideRoot() {
        Page page = context.create().page("/content/other/page");
        EligibilityResult result = eligibility.evaluate(page, config);
        assertEquals(ExclusionReason.OUTSIDE_ROOT, result.getReason());
    }

    @Test
    void excludedFlag() {
        Page page = context.create().page("/content/site/hidden");
        context.resourceResolver().getResource(page.getPath() + "/jcr:content").adaptTo(org.apache.sling.api.resource.ModifiableValueMap.class)
                .put(DiscoverabilityPageProperties.EXCLUDE, true);
        assertEquals(ExclusionReason.EXCLUDED, eligibility.evaluate(page, config).getReason());
    }

    @Test
    void noIndexFlag() {
        Page page = context.create().page("/content/site/private");
        context.resourceResolver().getResource(page.getPath() + "/jcr:content").adaptTo(org.apache.sling.api.resource.ModifiableValueMap.class)
                .put(DiscoverabilityPageProperties.NO_INDEX, true);
        assertEquals(ExclusionReason.NOINDEX, eligibility.evaluate(page, config).getReason());
    }

    @Test
    void redirect() {
        Page page = context.create().page("/content/site/old");
        context.resourceResolver().getResource(page.getPath() + "/jcr:content").adaptTo(org.apache.sling.api.resource.ModifiableValueMap.class)
                .put(DiscoverabilityPageProperties.REDIRECT_TARGET, "/content/site/new");
        assertEquals(ExclusionReason.REDIRECT, eligibility.evaluate(page, config).getReason());
    }

    @Test
    void cugProtected() {
        Page page = context.create().page("/content/site/members");
        context.resourceResolver().getResource(page.getPath() + "/jcr:content").adaptTo(org.apache.sling.api.resource.ModifiableValueMap.class)
                .put(DiscoverabilityPageProperties.CUG_ENABLED, true);
        assertEquals(ExclusionReason.CUG_PROTECTED, eligibility.evaluate(page, config).getReason());
    }

    @Test
    void deactivated() {
        Page page = context.create().page("/content/site/gone");
        context.resourceResolver().getResource(page.getPath() + "/jcr:content").adaptTo(org.apache.sling.api.resource.ModifiableValueMap.class)
                .put("cq:lastReplicationAction", "Deactivate");
        assertEquals(ExclusionReason.UNPUBLISHED, eligibility.evaluate(page, config).getReason());
    }

    @Test
    void badCanonicalOverride() {
        Page page = context.create().page("/content/site/bad");
        context.resourceResolver().getResource(page.getPath() + "/jcr:content").adaptTo(org.apache.sling.api.resource.ModifiableValueMap.class)
                .put(DiscoverabilityPageProperties.CANONICAL_URL, "https://evil.example/x");
        EligibilityResult result = eligibility.evaluate(page, config);
        assertFalse(result.isEligible());
        assertEquals(ExclusionReason.BAD_CANONICAL, result.getReason());
    }
}
