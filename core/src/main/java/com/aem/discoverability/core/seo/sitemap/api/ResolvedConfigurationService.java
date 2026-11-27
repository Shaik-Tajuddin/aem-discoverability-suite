package com.aem.discoverability.core.seo.sitemap.api;

import java.util.Optional;

import org.apache.sling.api.resource.Resource;

/**
 * Builds an immutable {@link ResolvedConfiguration} for a trusted content resource.
 */
public interface ResolvedConfigurationService {

    /**
     * @return snapshot when project resolution and domain validation succeed; empty when fail-closed
     */
    Optional<ResolvedConfiguration> resolve(Resource resource);
}
