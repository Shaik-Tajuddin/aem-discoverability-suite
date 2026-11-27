package com.aem.discoverability.core.seo.sitemap.models;

import com.aem.discoverability.core.seo.sitemap.api.LanguageAlternate;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class SitemapEntry {
    private final String path;
    private final String loc;
    private final Instant lastModified;
    private final List<LanguageAlternate> alternates;

    public SitemapEntry(String path, String loc, Instant lastModified, List<LanguageAlternate> alternates) {
        this.path = Objects.requireNonNull(path, "path");
        this.loc = Objects.requireNonNull(loc, "loc");
        this.lastModified = lastModified;
        this.alternates = alternates == null ? List.of() : Collections.unmodifiableList(alternates);
    }

    public String getPath() {
        return this.path;
    }

    public String getLoc() {
        return this.loc;
    }

    public Optional<Instant> getLastModified() {
        return Optional.ofNullable(this.lastModified);
    }

    public List<LanguageAlternate> getAlternates() {
        return this.alternates;
    }
}

