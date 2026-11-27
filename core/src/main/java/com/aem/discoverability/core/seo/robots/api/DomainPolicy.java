package com.aem.discoverability.core.seo.robots.api;

import java.util.Optional;

import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;

/**
 * Public domain and crawl policy derived from trusted configuration only.
 */
public interface DomainPolicy {

    /**
     * Public origin from {@link ResolvedConfiguration}; never from Host header.
     */
    Optional<String> publicOrigin(ResolvedConfiguration configuration);

    /**
     * Whether crawlers may be allowed for this project in the current environment.
     */
    boolean isCrawlAllowed(ResolvedConfiguration configuration);
}
