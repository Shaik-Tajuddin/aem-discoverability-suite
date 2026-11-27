package com.aem.discoverability.core.seo.sitemap.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.aem.discoverability.core.seo.sitemap.api.ProjectResolution;
import com.aem.discoverability.core.testcontext.AppAemContext;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class ProjectResolverImplTest {

    private final AemContext context = AppAemContext.newAemContext();
    private final ProjectResolverImpl resolver = new ProjectResolverImpl();

    @Test
    void resolvesInnermostConfigContext() {
        context.create().resource("/conf/site");
        context.create().resource("/content/site", "sling:configRef", "/conf/site");
        context.create().resource("/content/site/en/page");

        Resource page = context.resourceResolver().getResource("/content/site/en/page");
        ProjectResolution result = resolver.resolve(page);

        assertTrue(result.isResolved());
        assertEquals("/content/site", result.getProjectRoot().orElse(null));
    }

    @Test
    void prefersNestedContextWhenPresent() {
        context.create().resource("/conf/site");
        context.create().resource("/conf/site-en");
        context.create().resource("/content/site", "sling:configRef", "/conf/site");
        context.create().resource("/content/site/en", "sling:configRef", "/conf/site-en");
        context.create().resource("/content/site/en/page");

        Resource page = context.resourceResolver().getResource("/content/site/en/page");
        ProjectResolution result = resolver.resolve(page);

        assertTrue(result.isResolved());
        assertEquals("/content/site/en", result.getProjectRoot().orElse(null));
    }

    @Test
    void failsClosedWithoutConfigContext() {
        context.create().resource("/content/site/page");

        Resource page = context.resourceResolver().getResource("/content/site/page");
        assertEquals(ProjectResolution.Status.NONE, resolver.resolve(page).getStatus());
    }

    @Test
    void failsClosedForNullResource() {
        assertEquals(ProjectResolution.Status.NONE, resolver.resolve(null).getStatus());
    }

    @Test
    void readsConfigRefFromJcrContent() {
        context.create().resource("/conf/site");
        context.create().resource("/content/site");
        context.create().resource("/content/site/jcr:content", "sling:configRef", "/conf/site");
        context.create().resource("/content/site/en/page");

        Resource page = context.resourceResolver().getResource("/content/site/en/page");
        ProjectResolution result = resolver.resolve(page);

        assertTrue(result.isResolved());
        assertEquals("/content/site", result.getProjectRoot().orElse(null));
    }
}
