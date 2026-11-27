package com.aem.discoverability.core.seo.sitemap.services.impl;

import com.aem.discoverability.core.seo.robots.services.impl.DomainPolicyImpl;
import com.aem.discoverability.core.seo.robots.services.impl.EnvironmentPolicyImpl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.aem.discoverability.core.seo.sitemap.api.DiscoverabilityPageProperties;
import com.aem.discoverability.core.seo.sitemap.api.LanguageAlternate;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.testcontext.AppAemContext;
import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class LanguageRelationshipServiceImplTest {

    private final AemContext context = AppAemContext.newAemContext();
    private LanguageRelationshipServiceImpl service;
    private ResolvedConfiguration config;

    @BeforeEach
    void setUp() throws Exception {
        DomainPolicyImpl domainPolicy = new DomainPolicyImpl();

        CanonicalUrlServiceImpl canonicalUrlService = new CanonicalUrlServiceImpl();
        set(canonicalUrlService, "domainPolicy", domainPolicy);

        PageEligibilityServiceImpl eligibility = new PageEligibilityServiceImpl();
        set(eligibility, "canonicalUrlService", canonicalUrlService);

        service = new LanguageRelationshipServiceImpl();
        set(service, "pageEligibilityService", eligibility);
        set(service, "canonicalUrlService", canonicalUrlService);

        context.create().page("/content/site/en");
        context.create().page("/content/site/de");
        context.create().page("/content/site/en/about");
        context.create().page("/content/site/de/about");

        config = ResolvedConfiguration.builder()
                .projectRoot("/content/site")
                .publicDomain("https://www.example.com")
                .enabled(true)
                .languageRoots(new String[] { "/content/site/en", "/content/site/de" })
                .locales(new String[] { "en", "de" })
                .sameRelativePathEnabled(true)
                .xDefaultPath("/content/site/en")
                .build();
    }

    @Test
    void sameRelativePathResolvesAlternatesWithSelf() {
        Page en = context.pageManager().getPage("/content/site/en/about");
        List<LanguageAlternate> alts = service.resolveAlternates(en, config);

        assertEquals(2, alts.size());
        assertTrue(hreflangs(alts).contains("en"));
        assertTrue(hreflangs(alts).contains("de"));
        assertTrue(alts.stream().anyMatch(a -> a.getAbsoluteUrl().contains("/en/about")));
        assertTrue(alts.stream().anyMatch(a -> a.getAbsoluteUrl().contains("/de/about")));
    }

    @Test
    void reciprocalIncludesSelfAndXDefault() {
        Page en = context.pageManager().getPage("/content/site/en/about");
        List<LanguageAlternate> alts = service.resolveReciprocalAlternates(en, config);

        assertTrue(hreflangs(alts).contains("en"));
        assertTrue(hreflangs(alts).contains("de"));
        assertTrue(hreflangs(alts).contains(LanguageAlternate.HREFLANG_X_DEFAULT));
        assertEquals("https://www.example.com/en",
                alts.stream()
                        .filter(LanguageAlternate::isXDefault)
                        .map(LanguageAlternate::getAbsoluteUrl)
                        .findFirst()
                        .orElse(null));
    }

    @Test
    void omitsIneligibleTarget() {
        context.resourceResolver().getResource("/content/site/de/about/jcr:content")
                .adaptTo(org.apache.sling.api.resource.ModifiableValueMap.class)
                .put(DiscoverabilityPageProperties.EXCLUDE, true);

        Page en = context.pageManager().getPage("/content/site/en/about");
        List<LanguageAlternate> alts = service.resolveAlternates(en, config);

        assertEquals(1, alts.size());
        assertEquals("en", alts.get(0).getHreflang());
    }

    @Test
    void explicitMappingWorksWhenSameRelativeDisabled() {
        context.create().page("/content/site/fr");
        context.create().page("/content/site/fr/a-propos");

        ResolvedConfiguration mapped = ResolvedConfiguration.builder()
                .projectRoot("/content/site")
                .publicDomain("https://www.example.com")
                .enabled(true)
                .languageRoots(new String[] { "/content/site/en", "/content/site/fr" })
                .locales(new String[] { "en", "fr" })
                .sameRelativePathEnabled(false)
                .explicitMappings(new String[] {
                        "/content/site/en/about=/content/site/fr/a-propos"
                })
                .build();

        Page en = context.pageManager().getPage("/content/site/en/about");
        List<LanguageAlternate> alts = service.resolveAlternates(en, mapped);

        assertTrue(hreflangs(alts).contains("en"));
        assertTrue(hreflangs(alts).contains("fr"));
    }

    @Test
    void omitsInvalidXDefault() {
        ResolvedConfiguration badXDefault = ResolvedConfiguration.builder()
                .projectRoot("/content/site")
                .publicDomain("https://www.example.com")
                .enabled(true)
                .languageRoots(new String[] { "/content/site/en", "/content/site/de" })
                .locales(new String[] { "en", "de" })
                .sameRelativePathEnabled(true)
                .xDefaultPath("/content/site/missing")
                .build();

        Page en = context.pageManager().getPage("/content/site/en/about");
        List<LanguageAlternate> alts = service.resolveReciprocalAlternates(en, badXDefault);
        assertFalse(hreflangs(alts).contains(LanguageAlternate.HREFLANG_X_DEFAULT));
    }

    @Test
    void resolveLocaleFromConfiguredRoots() {
        Page de = context.pageManager().getPage("/content/site/de/about");
        assertEquals("de", service.resolveLocale(de, config).orElse(null));
    }

    private static List<String> hreflangs(List<LanguageAlternate> alts) {
        return alts.stream().map(LanguageAlternate::getHreflang).collect(Collectors.toList());
    }

    private static void set(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
