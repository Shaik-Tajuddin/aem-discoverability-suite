package com.aem.discoverability.core.seo.sitemap.api;

import java.util.Optional;

import com.day.cq.wcm.api.Page;

/**
 * Sole builder of absolute public URLs for the suite. Never uses the HTTP Host header.
 */
public interface CanonicalUrlService {

    /**
     * @return absolute public URL when domain and path are valid; empty when fail-closed
     */
    Optional<String> resolve(Page page, ResolvedConfiguration configuration);
}
