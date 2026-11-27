package com.aem.discoverability.core.aeo.jsonld.services.impl;

import com.aem.discoverability.core.aeo.jsonld.serialization.JsonLdSafeJson;
import com.aem.discoverability.core.aeo.jsonld.services.impl.JsonLdGraphServiceImpl;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.seo.sitemap.services.impl.CanonicalUrlServiceImpl;
import com.aem.discoverability.core.seo.robots.services.impl.DomainPolicyImpl;
import com.aem.discoverability.core.seo.robots.services.impl.EnvironmentPolicyImpl;
import com.aem.discoverability.core.seo.sitemap.services.impl.PageEligibilityServiceImpl;
import com.aem.discoverability.core.testcontext.AppAemContext;
import com.day.cq.wcm.api.Page;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(value={AemContextExtension.class})
class JsonLdGraphServiceImplTest {
    private final AemContext context = AppAemContext.newAemContext();
    private JsonLdGraphServiceImpl service;
    private ResolvedConfiguration config;

    JsonLdGraphServiceImplTest() {
    }

    @BeforeEach
    void setUp() throws Exception {
        DomainPolicyImpl domainPolicy = new DomainPolicyImpl();
        CanonicalUrlServiceImpl canonicalUrlService = new CanonicalUrlServiceImpl();
        JsonLdGraphServiceImplTest.set(canonicalUrlService, "domainPolicy", domainPolicy);
        PageEligibilityServiceImpl eligibility = new PageEligibilityServiceImpl();
        JsonLdGraphServiceImplTest.set(eligibility, "canonicalUrlService", canonicalUrlService);
        this.service = new JsonLdGraphServiceImpl();
        JsonLdGraphServiceImplTest.set(this.service, "pageEligibilityService", eligibility);
        JsonLdGraphServiceImplTest.set(this.service, "canonicalUrlService", canonicalUrlService);
        JsonLdGraphServiceImplTest.set(this.service, "domainPolicy", domainPolicy);
        this.config = ResolvedConfiguration.builder().projectRoot("/content/site").publicDomain("https://www.example.com").enabled(true).jsonLdEnabled(true).jsonLdOrganizationName("Example Company").jsonLdWebsiteName("Example Site").jsonLdAutoWebPage(true).jsonLdAutoBreadcrumb(true).jsonLdIncludeOrganization(true).jsonLdIncludeWebSite(true).build();
    }

    @Test
    void buildsGraphWithDefaultsAndArticle() {
        Page page = this.context.create().page("/content/site/news/story", "story", "Example Story");
        this.context.create().resource(page.getPath() + "/jcr:content/root/jsonld", new Object[]{"sling:resourceType", "discoverability/components/jsonld", "schemaType", "Article", "headline", "Example Story", "description", "A factual summary.", "authorName", "Ada Example"});
        Optional json = this.service.buildJson(page, this.config);
        Assertions.assertTrue((boolean)json.isPresent());
        String body = (String)json.get();
        Assertions.assertTrue((boolean)body.contains("\"@context\":\"https://schema.org\""));
        Assertions.assertTrue((boolean)body.contains("\"@type\":\"Article\""));
        Assertions.assertTrue((boolean)body.contains("\"headline\":\"Example Story\""));
        Assertions.assertTrue((boolean)body.contains("\"@type\":\"WebPage\""));
        Assertions.assertTrue((boolean)body.contains("\"@type\":\"BreadcrumbList\""));
        Assertions.assertTrue((boolean)body.contains("\"@type\":\"Organization\""));
        Assertions.assertTrue((boolean)body.contains("\"@type\":\"WebSite\""));
        Assertions.assertFalse((boolean)body.contains("</script>"));
    }

    @Test
    void omitsInvalidFaqAndEscapesBreakout() {
        Page page = this.context.create().page("/content/site/help", "help", "Help");
        this.context.create().resource(page.getPath() + "/jcr:content/root/jsonld", new Object[]{"sling:resourceType", "discoverability/components/jsonld", "schemaType", "FAQPage"});
        Optional json = this.service.buildJson(page, this.config);
        Assertions.assertTrue((boolean)json.isPresent());
        Assertions.assertFalse((boolean)((String)json.get()).contains("\"@type\":\"FAQPage\""));
        Assertions.assertTrue((boolean)JsonLdSafeJson.stringify((Object)"x</script>y").contains("\\u003c"));
    }

    @Test
    void disabledReturnsEmpty() {
        Page page = this.context.create().page("/content/site/x");
        ResolvedConfiguration off = ResolvedConfiguration.builder().projectRoot("/content/site").publicDomain("https://www.example.com").enabled(true).jsonLdEnabled(false).build();
        Assertions.assertFalse((boolean)this.service.buildJson(page, off).isPresent());
    }

    private static void set(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

