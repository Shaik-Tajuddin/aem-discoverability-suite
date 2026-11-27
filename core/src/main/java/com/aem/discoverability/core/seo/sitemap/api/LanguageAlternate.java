package com.aem.discoverability.core.seo.sitemap.api;

import java.util.Objects;

/**
 * One hreflang alternate (including self and optional x-default).
 */
public final class LanguageAlternate {

    public static final String HREFLANG_X_DEFAULT = "x-default";

    private final String path;
    private final String hreflang;
    private final String absoluteUrl;

    public LanguageAlternate(String path, String hreflang, String absoluteUrl) {
        this.path = Objects.requireNonNull(path, "path");
        this.hreflang = Objects.requireNonNull(hreflang, "hreflang");
        this.absoluteUrl = Objects.requireNonNull(absoluteUrl, "absoluteUrl");
    }

    public String getPath() {
        return path;
    }

    public String getHreflang() {
        return hreflang;
    }

    public String getAbsoluteUrl() {
        return absoluteUrl;
    }

    public boolean isXDefault() {
        return HREFLANG_X_DEFAULT.equals(hreflang);
    }
}
