package com.aem.discoverability.core.seo.sitemap.services.impl;

import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.seo.sitemap.services.impl.CanonicalUrlServiceImpl;
import com.aem.discoverability.core.seo.robots.services.impl.DomainPolicyImpl;
import com.aem.discoverability.core.seo.robots.services.impl.EnvironmentPolicyImpl;
import com.aem.discoverability.core.seo.sitemap.services.impl.LanguageRelationshipServiceImpl;
import com.aem.discoverability.core.seo.sitemap.services.impl.PageDiscoveryServiceImpl;
import com.aem.discoverability.core.seo.sitemap.services.impl.PageEligibilityServiceImpl;
import com.aem.discoverability.core.seo.sitemap.partition.SitemapPartitionPlanner;
import com.aem.discoverability.core.seo.sitemap.services.impl.SitemapGeneratorImpl;
import com.aem.discoverability.core.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(value={AemContextExtension.class})
class SitemapGeneratorImplTest {
    private final AemContext context = AppAemContext.newAemContext();
    private SitemapGeneratorImpl generator;
    private ResolvedConfiguration config;

    SitemapGeneratorImplTest() {
    }

    @BeforeEach
    void setUp() throws Exception {
        DomainPolicyImpl domainPolicy = new DomainPolicyImpl();
        CanonicalUrlServiceImpl canonicalUrlService = new CanonicalUrlServiceImpl();
        SitemapGeneratorImplTest.set(canonicalUrlService, "domainPolicy", domainPolicy);
        PageEligibilityServiceImpl eligibility = new PageEligibilityServiceImpl();
        SitemapGeneratorImplTest.set(eligibility, "canonicalUrlService", canonicalUrlService);
        LanguageRelationshipServiceImpl language = new LanguageRelationshipServiceImpl();
        SitemapGeneratorImplTest.set(language, "pageEligibilityService", eligibility);
        SitemapGeneratorImplTest.set(language, "canonicalUrlService", canonicalUrlService);
        SitemapPartitionPlanner planner = new SitemapPartitionPlanner();
        SitemapGeneratorImplTest.set(planner, "pageDiscoveryService", new PageDiscoveryServiceImpl());
        SitemapGeneratorImplTest.set(planner, "pageEligibilityService", eligibility);
        SitemapGeneratorImplTest.set(planner, "canonicalUrlService", canonicalUrlService);
        SitemapGeneratorImplTest.set(planner, "languageRelationshipService", language);
        this.generator = new SitemapGeneratorImpl();
        SitemapGeneratorImplTest.set(this.generator, "partitionPlanner", planner);
        SitemapGeneratorImplTest.set(this.generator, "domainPolicy", domainPolicy);
        this.context.create().page("/content/site");
        this.context.create().page("/content/site/a");
        this.context.create().page("/content/site/b");
        this.context.create().page("/content/site/c");
        this.config = ResolvedConfiguration.builder().projectRoot("/content/site").publicDomain("https://www.example.com").enabled(true).sitemapEnabled(true).sitemapMaxUrlsPerFile(2).sitemapIncludeLastModified(true).sitemapIncludeHreflang(false).languageRoots(new String[]{"/content/site"}).locales(new String[]{"en"}).build();
    }

    @Test
    void multiPartitionIndexListsShards() {
        Optional index = this.generator.generateIndex(this.context.resourceResolver(), this.config);
        Assertions.assertTrue((boolean)index.isPresent());
        Assertions.assertTrue((boolean)((String)index.get()).contains("<sitemapindex"));
        Assertions.assertTrue((boolean)((String)index.get()).contains("/sitemap-0.xml"));
        Assertions.assertTrue((boolean)((String)index.get()).contains("/sitemap-1.xml"));
    }

    @Test
    void partitionZeroContainsStableFirstUrls() {
        Optional part0 = this.generator.generatePartition(this.context.resourceResolver(), this.config, 0);
        Assertions.assertTrue((boolean)part0.isPresent());
        String xml = (String)part0.get();
        Assertions.assertTrue((boolean)xml.contains("<urlset"));
        Assertions.assertTrue((boolean)xml.contains("<loc>https://www.example.com/</loc>"));
        Assertions.assertTrue((boolean)xml.contains("<loc>https://www.example.com/a</loc>"));
        Assertions.assertFalse((boolean)xml.contains("<loc>https://www.example.com/b</loc>"));
    }

    @Test
    void disabledReturnsEmpty() {
        ResolvedConfiguration off = ResolvedConfiguration.builder().projectRoot("/content/site").publicDomain("https://www.example.com").enabled(true).sitemapEnabled(false).build();
        Assertions.assertFalse((boolean)this.generator.generateIndex(this.context.resourceResolver(), off).isPresent());
    }

    @Test
    void singlePartitionReturnsUrlsetFromIndexEndpoint() {
        ResolvedConfiguration single = ResolvedConfiguration.builder().projectRoot("/content/site").publicDomain("https://www.example.com").enabled(true).sitemapEnabled(true).sitemapMaxUrlsPerFile(50000).sitemapIncludeHreflang(false).build();
        Optional index = this.generator.generateIndex(this.context.resourceResolver(), single);
        Assertions.assertTrue((boolean)index.isPresent());
        Assertions.assertTrue((boolean)((String)index.get()).contains("<urlset"));
        Assertions.assertFalse((boolean)((String)index.get()).contains("<sitemapindex"));
        Assertions.assertEquals((Object)true, (Object)((String)index.get()).contains("https://www.example.com/a"));
    }

    private static void set(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

