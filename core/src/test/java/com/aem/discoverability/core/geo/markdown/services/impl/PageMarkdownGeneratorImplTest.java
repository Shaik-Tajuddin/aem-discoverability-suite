package com.aem.discoverability.core.geo.markdown.services.impl;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.aem.discoverability.core.geo.markdown.extractors.ComponentExtractor;
import com.aem.discoverability.core.geo.markdown.extractors.ComponentExtractorRegistry;
import com.aem.discoverability.core.geo.markdown.extractors.TextExtractor;
import com.aem.discoverability.core.geo.markdown.extractors.TitleExtractor;
import com.aem.discoverability.core.seo.robots.services.impl.DomainPolicyImpl;
import com.aem.discoverability.core.seo.sitemap.api.LanguageRelationshipService;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.seo.sitemap.services.impl.CanonicalUrlServiceImpl;
import com.aem.discoverability.core.seo.sitemap.services.impl.LanguageRelationshipServiceImpl;
import com.aem.discoverability.core.seo.sitemap.services.impl.PageEligibilityServiceImpl;
import com.aem.discoverability.core.testcontext.AppAemContext;
import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class PageMarkdownGeneratorImplTest {

    private final AemContext context = AppAemContext.newAemContext();
    private PageMarkdownGeneratorImpl generator;
    private ResolvedConfiguration config;

    @BeforeEach
    void setUp() throws Exception {
        DomainPolicyImpl domainPolicy = new DomainPolicyImpl();
        CanonicalUrlServiceImpl canonicalUrlService = new CanonicalUrlServiceImpl();
        set(canonicalUrlService, "domainPolicy", domainPolicy);
        PageEligibilityServiceImpl eligibility = new PageEligibilityServiceImpl();
        set(eligibility, "canonicalUrlService", canonicalUrlService);
        LanguageRelationshipService language = new LanguageRelationshipServiceImpl();
        set(language, "canonicalUrlService", canonicalUrlService);
        set(language, "pageEligibilityService", eligibility);
        ComponentExtractorRegistry registry = new ComponentExtractorRegistry();
        registry.bindExtractor(new TitleExtractor());
        registry.bindExtractor(new TextExtractor());
        generator = new PageMarkdownGeneratorImpl();
        set(generator, "pageEligibilityService", eligibility);
        set(generator, "extractorRegistry", registry);
        set(generator, "domainPolicy", domainPolicy);
        set(generator, "canonicalUrlService", canonicalUrlService);
        set(generator, "languageRelationshipService", language);
        config = ResolvedConfiguration.builder()
                .projectRoot("/content/site")
                .publicDomain("https://www.example.com")
                .enabled(true)
                .markdownEnabled(true)
                .languageRoots(new String[] { "/content/site" })
                .locales(new String[] { "en" })
                .build();
    }

    @Test
    void generatesMarkdownFromTitleAndText() {
        Page page = context.create().page("/content/site/about", "about", "About Us");
        context.create().resource(page.getPath() + "/jcr:content/root/title",
                "sling:resourceType", "discoverability/components/title",
                "jcr:title", "Our Mission", "type", "h2");
        context.create().resource(page.getPath() + "/jcr:content/root/text",
                "sling:resourceType", "discoverability/components/text",
                "text", "<p>We build useful products.</p>");
        Optional<String> md = generator.generate(page, config);
        Assertions.assertTrue(md.isPresent());
        Assertions.assertTrue(md.get().startsWith("# About Us"));
        Assertions.assertTrue(md.get().contains("Canonical: https://www.example.com/about"));
        Assertions.assertTrue(md.get().contains("Path: /content/site/about"));
        Assertions.assertTrue(md.get().contains("Locale: en"));
        Assertions.assertTrue(md.get().contains("## Our Mission"));
        Assertions.assertTrue(md.get().contains("We build useful products."));
        Assertions.assertFalse(md.get().contains("sling:resourceType"));
    }

    @Test
    void disabledReturnsEmpty() {
        Page page = context.create().page("/content/site/x");
        ResolvedConfiguration off = ResolvedConfiguration.builder()
                .projectRoot("/content/site")
                .publicDomain("https://www.example.com")
                .enabled(true)
                .markdownEnabled(false)
                .build();
        Assertions.assertFalse(generator.generate(page, off).isPresent());
    }

    @Test
    void ambiguousExtractorsAreSkipped() throws Exception {
        ComponentExtractorRegistry registry = new ComponentExtractorRegistry();
        registry.bindExtractor(new TitleExtractor());
        registry.bindExtractor(new TitleExtractor());
        set(generator, "extractorRegistry", registry);
        Page page = context.create().page("/content/site/amb", "amb", "Amb");
        context.create().resource(page.getPath() + "/jcr:content/root/title",
                "sling:resourceType", "discoverability/components/title",
                "jcr:title", "Should Skip");
        Optional<String> md = generator.generate(page, config);
        Assertions.assertTrue(md.isPresent());
        Assertions.assertFalse(md.get().contains("Should Skip"));
        Assertions.assertTrue(md.get().contains("# Amb"));
        Assertions.assertTrue(md.get().contains("Path: /content/site/amb"));
    }

    private static void set(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
