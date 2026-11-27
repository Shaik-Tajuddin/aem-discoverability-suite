package com.aem.discoverability.core.geo.llm.services.impl;

import com.aem.discoverability.core.geo.llm.services.impl.LlmsTxtGeneratorImpl;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.seo.sitemap.services.impl.CanonicalUrlServiceImpl;
import com.aem.discoverability.core.seo.robots.services.impl.DomainPolicyImpl;
import com.aem.discoverability.core.seo.robots.services.impl.EnvironmentPolicyImpl;
import com.aem.discoverability.core.seo.sitemap.services.impl.PageDiscoveryServiceImpl;
import com.aem.discoverability.core.seo.sitemap.services.impl.PageEligibilityServiceImpl;
import com.aem.discoverability.core.testcontext.AppAemContext;
import com.day.cq.wcm.api.Page;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.lang.reflect.Field;
import java.util.Optional;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(value={AemContextExtension.class})
class LlmsTxtGeneratorImplTest {
    private final AemContext context = AppAemContext.newAemContext();
    private LlmsTxtGeneratorImpl generator;

    LlmsTxtGeneratorImplTest() {
    }

    @BeforeEach
    void setUp() throws Exception {
        DomainPolicyImpl domainPolicy = new DomainPolicyImpl();
        CanonicalUrlServiceImpl canonicalUrlService = new CanonicalUrlServiceImpl();
        LlmsTxtGeneratorImplTest.set(canonicalUrlService, "domainPolicy", domainPolicy);
        PageEligibilityServiceImpl eligibility = new PageEligibilityServiceImpl();
        LlmsTxtGeneratorImplTest.set(eligibility, "canonicalUrlService", canonicalUrlService);
        this.generator = new LlmsTxtGeneratorImpl();
        LlmsTxtGeneratorImplTest.set(this.generator, "pageDiscoveryService", new PageDiscoveryServiceImpl());
        LlmsTxtGeneratorImplTest.set(this.generator, "pageEligibilityService", eligibility);
        LlmsTxtGeneratorImplTest.set(this.generator, "canonicalUrlService", canonicalUrlService);
        this.context.create().page("/content/site");
        this.put(this.context.create().page("/content/site/company", "company", "About Us"), "jcr:description", "Company overview.");
        this.context.create().page("/content/site/company/leadership", "leadership", "Leadership");
        this.put(this.context.create().page("/content/site/products", "products", "Products"), "jcr:description", "Product overview.");
        this.put(this.context.create().page("/content/site/secret", "secret", "Secret"), "discoverabilityExclude", true);
    }

    private void put(Page page, String name, Object value) {
        this.context.resourceResolver().getResource(page.getPath() + "/jcr:content")
                .adaptTo(ModifiableValueMap.class).put(name, value);
    }

    @Test
    void rendersSectionsCanonicalLinksAndSkipsExcluded() {
        ResolvedConfiguration config = LlmsTxtGeneratorImplTest.baseConfig().llmsSections(new String[]{"Company|/content/site/company", "Products|/content/site/products"}).build();
        Optional body = this.generator.generate(this.context.resourceResolver(), config);
        Assertions.assertTrue((boolean)body.isPresent());
        String text = (String)body.get();
        Assertions.assertTrue((boolean)text.startsWith("# Example Company\n"));
        Assertions.assertTrue((boolean)text.contains("> Digital services for customers.\n"));
        Assertions.assertTrue((boolean)text.contains("## Company\n"));
        Assertions.assertTrue((boolean)text.contains("- [About Us](https://www.example.com/company): Company overview."));
        Assertions.assertTrue((boolean)text.contains("- [Leadership](https://www.example.com/company/leadership)"));
        Assertions.assertTrue((boolean)text.contains("## Products\n"));
        Assertions.assertTrue((boolean)text.contains("- [Products](https://www.example.com/products): Product overview."));
        Assertions.assertFalse((boolean)text.contains("Secret"));
        Assertions.assertFalse((boolean)text.contains("/content/"));
    }

    @Test
    void enforcesMaxLinksAndIncludePaths() {
        ResolvedConfiguration config = LlmsTxtGeneratorImplTest.baseConfig().llmsIncludePaths(new String[]{"/content/site/company"}).llmsMaxLinks(1).build();
        Optional body = this.generator.generate(this.context.resourceResolver(), config);
        Assertions.assertTrue((boolean)body.isPresent());
        String text = (String)body.get();
        Assertions.assertTrue((boolean)text.contains("About Us"));
        Assertions.assertFalse((boolean)text.contains("Leadership"));
        Assertions.assertFalse((boolean)text.contains("Products"));
    }

    @Test
    void disabledReturnsEmpty() {
        ResolvedConfiguration off = ResolvedConfiguration.builder().projectRoot("/content/site").publicDomain("https://www.example.com").enabled(true).llmsEnabled(false).build();
        Assertions.assertFalse((boolean)this.generator.generate(this.context.resourceResolver(), off).isPresent());
    }

    @Test
    void overMaxBytesReturnsEmpty() {
        ResolvedConfiguration tiny = LlmsTxtGeneratorImplTest.baseConfig().llmsMaxOutputBytes(8).build();
        Assertions.assertFalse((boolean)this.generator.generate(this.context.resourceResolver(), tiny).isPresent());
    }

    private static ResolvedConfiguration.Builder baseConfig() {
        return ResolvedConfiguration.builder().projectRoot("/content/site").publicDomain("https://www.example.com").enabled(true).llmsEnabled(true).llmsSiteName("Example Company").llmsSummary("Digital services for customers.").llmsMaxLinks(50).llmsMaxOutputBytes(100000);
    }

    private static void set(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

