package com.aem.discoverability.core.seo.sitemap.api;

import java.util.List;
import java.util.Optional;

import com.day.cq.wcm.api.Page;

/**
 * Shared language/hreflang relationship resolution for sitemap and HTML.
 */
public interface LanguageRelationshipService {

    /**
     * Raw resolution order: MSM/LanguageManager → explicit map → optional same-relative-path.
     * Invalid/ineligible targets are omitted. Does not enforce reciprocity.
     */
    List<LanguageAlternate> resolveAlternates(Page page, ResolvedConfiguration configuration);

    /**
     * Self-referencing reciprocal alternate set for generators: includes self, drops
     * one-way links, and appends eligible {@code x-default} when configured.
     */
    List<LanguageAlternate> resolveReciprocalAlternates(Page page, ResolvedConfiguration configuration);

    /**
     * Locale for a page from configured language roots, if any.
     */
    Optional<String> resolveLocale(Page page, ResolvedConfiguration configuration);
}
