package com.aem.discoverability.core.seo.sitemap.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.testcontext.AppAemContext;
import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class PageDiscoveryServiceImplTest {

    private final AemContext context = AppAemContext.newAemContext();
    private final PageDiscoveryServiceImpl discovery = new PageDiscoveryServiceImpl();

    @Test
    void discoversPagesInBatchesInsideRoot() {
        context.create().page("/content/site");
        context.create().page("/content/site/a");
        context.create().page("/content/site/b");
        context.create().page("/content/site/a/c");
        context.create().page("/content/outside");

        ResolvedConfiguration config = ResolvedConfiguration.builder()
                .projectRoot("/content/site")
                .publicDomain("https://www.example.com")
                .batchSize(2)
                .maxDepth(20)
                .build();

        List<String> paths = new ArrayList<>();
        AtomicInteger batches = new AtomicInteger();
        discovery.discover(context.resourceResolver(), config, batch -> {
            batches.incrementAndGet();
            batch.forEach(page -> paths.add(page.getPath()));
            return true;
        });

        assertTrue(batches.get() >= 2);
        assertTrue(paths.contains("/content/site"));
        assertTrue(paths.contains("/content/site/a/c"));
        assertEquals(false, paths.contains("/content/outside"));
    }

    @Test
    void respectsMaxDepth() {
        context.create().page("/content/site");
        context.create().page("/content/site/a");
        context.create().page("/content/site/a/b");

        ResolvedConfiguration config = ResolvedConfiguration.builder()
                .projectRoot("/content/site")
                .publicDomain("https://www.example.com")
                .batchSize(50)
                .maxDepth(1)
                .build();

        List<String> paths = new ArrayList<>();
        discovery.discover(context.resourceResolver(), config, batch -> {
            batch.forEach(page -> paths.add(page.getPath()));
            return true;
        });

        assertTrue(paths.contains("/content/site"));
        assertTrue(paths.contains("/content/site/a"));
        assertEquals(false, paths.contains("/content/site/a/b"));
    }

    @Test
    void stopsWhenConsumerReturnsFalse() {
        context.create().page("/content/site");
        context.create().page("/content/site/a");
        context.create().page("/content/site/b");

        ResolvedConfiguration config = ResolvedConfiguration.builder()
                .projectRoot("/content/site")
                .publicDomain("https://www.example.com")
                .batchSize(1)
                .maxDepth(20)
                .build();

        AtomicInteger seen = new AtomicInteger();
        discovery.discover(context.resourceResolver(), config, batch -> {
            seen.addAndGet(batch.size());
            return false;
        });

        assertEquals(1, seen.get());
    }
}
