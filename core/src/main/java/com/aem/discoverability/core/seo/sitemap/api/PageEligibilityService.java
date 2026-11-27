package com.aem.discoverability.core.seo.sitemap.api;

import com.day.cq.wcm.api.Page;

/**
 * Shared page eligibility used by all discoverability generators.
 */
public interface PageEligibilityService {

    /**
     * Evaluates a page against project isolation and publishability rules.
     */
    EligibilityResult evaluate(Page page, ResolvedConfiguration configuration);
}
