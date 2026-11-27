package com.aem.discoverability.core.seo.sitemap.configs;

import org.apache.sling.caconfig.annotation.Configuration;
import org.apache.sling.caconfig.annotation.Property;

/**
 * XML sitemap enablement, public origin, discovery limits, hreflang, and cache TTL.
 */
@Configuration(label = "Discoverability Sitemap",
        description = "Enablement, public domain, discovery limits, hreflang, and cache TTL.")
public @interface SitemapConfig {

    @Property(label = "Enabled")
    boolean enabled() default false;

    @Property(label = "Public domain",
            description = "Trusted public origin (https://example.com). Never taken from Host header.")
    String publicDomain() default "";

    @Property(label = "Batch size")
    int batchSize() default 100;

    @Property(label = "Max traversal depth")
    int maxDepth() default 20;

    @Property(label = "Max URLs per file", description = "Hard-capped at 50,000.")
    int maxUrlsPerFile() default 50_000;

    @Property(label = "Max bytes per file", description = "Hard-capped at 50 MB.")
    long maxBytesPerFile() default 52_428_800L;

    @Property(label = "Include lastmod")
    boolean includeLastModified() default true;

    @Property(label = "Include hreflang alternates")
    boolean includeHreflang() default true;

    @Property(label = "Language roots", description = "Absolute language root paths under the project.",
            property = { "widgetType=pathbrowser", "pathbrowserRootPath=/content" })
    String[] languageRoots() default {};

    @Property(label = "Locales",
            description = "Hreflang locale codes aligned by index with language roots (e.g. en, de).")
    String[] locales() default {};

    @Property(label = "Same-relative-path mapping enabled")
    boolean sameRelativePathEnabled() default false;

    @Property(label = "Explicit mappings", description = "Optional path pairs source=target.")
    String[] explicitMappings() default {};

    @Property(label = "x-default path", description = "Optional absolute path for hreflang x-default.")
    String xDefaultPath() default "";

    @Property(label = "Cache-Control max-age (seconds)")
    int cacheMaxAgeSeconds() default 3600;
}
