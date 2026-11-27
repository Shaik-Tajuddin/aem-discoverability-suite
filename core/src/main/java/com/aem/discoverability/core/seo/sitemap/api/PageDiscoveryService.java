package com.aem.discoverability.core.seo.sitemap.api;

import org.apache.sling.api.resource.ResourceResolver;

/**
 * Batched page discovery under a resolved project root.
 */
public interface PageDiscoveryService {

    /**
     * Walks {@code cq:Page} nodes under {@link ResolvedConfiguration#getProjectRoot()},
     * respecting {@link ResolvedConfiguration#getMaxDepth()} and
     * {@link ResolvedConfiguration#getBatchSize()}. Never leaves the project root.
     *
     * @param resolver resource resolver (typically the discoverability service user)
     * @param configuration immutable project snapshot
     * @param consumer batch callback; return false to stop
     */
    void discover(ResourceResolver resolver, ResolvedConfiguration configuration, PageBatchConsumer consumer);
}
