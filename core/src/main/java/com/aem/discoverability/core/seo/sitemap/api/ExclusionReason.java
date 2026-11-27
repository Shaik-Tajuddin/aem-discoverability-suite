package com.aem.discoverability.core.seo.sitemap.api;

/**
 * Why a page was rejected by shared eligibility. {@link #NONE} means eligible.
 */
public enum ExclusionReason {
    NONE,
    OUTSIDE_ROOT,
    UNPUBLISHED,
    CUG_PROTECTED,
    EXCLUDED,
    NOINDEX,
    REDIRECT,
    BAD_CANONICAL,
    INVALID
}
