package com.aem.discoverability.core.seo.sitemap.api;

/**
 * Page {@code jcr:content} property contract for discoverability eligibility and canonical URLs.
 * <p>
 * Finalized in Phase 2 — keep generators reading only these names.
 */
public final class DiscoverabilityPageProperties {

    /** Boolean — when true, page is excluded from all suite outputs. */
    public static final String EXCLUDE = "discoverabilityExclude";

    /** Boolean — when true, page is treated as noindex for suite eligibility. */
    public static final String NO_INDEX = "discoverabilityNoIndex";

    /** String — optional absolute canonical URL override (AEM SEO convention). */
    public static final String CANONICAL_URL = "cq:canonicalUrl";

    /** String — redirect target; redirected pages are not eligible. */
    public static final String REDIRECT_TARGET = "cq:redirectTarget";

    /** Boolean — CUG enabled marker. */
    public static final String CUG_ENABLED = "cq:cugEnabled";

    /** String — optional llms.txt section title override (must match a configured section name). */
    public static final String LLMS_SECTION = "discoverabilityLlmsSection";

    private DiscoverabilityPageProperties() {
    }
}
