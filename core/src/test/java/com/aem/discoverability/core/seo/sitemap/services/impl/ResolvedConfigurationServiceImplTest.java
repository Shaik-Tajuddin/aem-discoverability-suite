package com.aem.discoverability.core.seo.sitemap.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.sling.testing.mock.caconfig.MockContextAwareConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.aem.discoverability.core.geo.llm.configs.LlmsConfig;
import com.aem.discoverability.core.geo.markdown.configs.MarkdownConfig;
import com.aem.discoverability.core.seo.robots.configs.RobotsConfig;
import com.aem.discoverability.core.seo.robots.services.impl.DomainPolicyImpl;
import com.aem.discoverability.core.seo.robots.services.impl.EnvironmentPolicyImpl;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.seo.sitemap.configs.SitemapConfig;
import com.aem.discoverability.core.testcontext.AppAemContext;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class ResolvedConfigurationServiceImplTest {

    private final AemContext context = AppAemContext.newAemContext();
    private ResolvedConfigurationServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        MockContextAwareConfig.registerAnnotationClasses(context,
                RobotsConfig.class, SitemapConfig.class, MarkdownConfig.class, LlmsConfig.class);

        EnvironmentPolicyImpl environmentPolicy = new EnvironmentPolicyImpl();
        EnvironmentPolicyImpl.Config envConfig = mock(EnvironmentPolicyImpl.Config.class);
        when(envConfig.production()).thenReturn(false);
        var activate = EnvironmentPolicyImpl.class.getDeclaredMethod("activate", EnvironmentPolicyImpl.Config.class);
        activate.setAccessible(true);
        activate.invoke(environmentPolicy, envConfig);

        service = new ResolvedConfigurationServiceImpl();
        set(service, "projectResolver", new ProjectResolverImpl());
        set(service, "environmentPolicy", environmentPolicy);
        set(service, "domainPolicy", new DomainPolicyImpl());
    }

    @Test
    void buildsImmutableSnapshotWithFeaturesOffByDefault() {
        context.create().resource("/conf/site");
        context.create().resource("/content/site", "sling:configRef", "/conf/site");
        context.create().resource("/content/site/page");

        Optional<ResolvedConfiguration> resolved =
                service.resolve(context.resourceResolver().getResource("/content/site/page"));

        assertTrue(resolved.isPresent());
        ResolvedConfiguration snapshot = resolved.get();
        assertEquals("/content/site", snapshot.getProjectRoot());
        assertFalse(snapshot.isEnabled());
        assertFalse(snapshot.isRobotsEnabled());
        assertFalse(snapshot.isSitemapEnabled());
        assertFalse(snapshot.isMarkdownEnabled());
        assertFalse(snapshot.isLlmsEnabled());
        assertFalse(snapshot.isJsonLdEnabled());
        assertFalse(snapshot.isProductionEnvironment());
    }

    @Test
    void failsClosedWhenFeatureEnabledWithoutPublicDomain() {
        context.create().resource("/conf/site");
        context.create().resource("/content/site", "sling:configRef", "/conf/site");
        context.create().resource("/content/site/page");
        MockContextAwareConfig.writeConfiguration(context, "/content/site", MarkdownConfig.class,
                "enabled", true);

        assertFalse(service.resolve(context.resourceResolver().getResource("/content/site/page"))
                .isPresent());
    }

    @Test
    void resolvesWhenMarkdownEnabledWithPublicDomain() {
        context.create().resource("/conf/site");
        context.create().resource("/content/site", "sling:configRef", "/conf/site");
        context.create().resource("/content/site/page");
        MockContextAwareConfig.writeConfiguration(context, "/content/site", MarkdownConfig.class,
                "enabled", true,
                "publicDomain", "https://www.example.com");

        Optional<ResolvedConfiguration> resolved =
                service.resolve(context.resourceResolver().getResource("/content/site/page"));

        assertTrue(resolved.isPresent());
        assertTrue(resolved.get().isMarkdownEnabled());
        assertEquals("https://www.example.com", resolved.get().getPublicDomain());
    }

    private static void set(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
