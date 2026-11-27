package com.aem.discoverability.core.geo.llm.configs;

import org.apache.sling.caconfig.annotation.Configuration;
import org.apache.sling.caconfig.annotation.Property;

/**
 * llms.txt enablement, public origin, site context, discovery limits, and filters.
 */
@Configuration(label = "Discoverability llms.txt",
        description = "Enablement, public domain, site context, discovery limits, and path filters.")
public @interface LlmsConfig {

    @Property(label = "Enabled")
    boolean enabled() default false;

    @Property(label = "Public domain",
            description = "Trusted public origin (https://example.com). Never taken from Host header.")
    String publicDomain() default "";

    @Property(label = "Batch size")
    int batchSize() default 100;

    @Property(label = "Max traversal depth")
    int maxDepth() default 20;

    @Property(label = "Site name", description = "H1 title in llms.txt.")
    String siteName() default "";

    @Property(label = "Summary", description = "Short factual site description (blockquote).")
    String summary() default "";

    @Property(label = "Context text", description = "Optional paragraph after the summary.")
    String contextText() default "";

    @Property(label = "Sections",
            description = "Ordered entries as Section Title|/content/path/prefix. First match wins.")
    String[] sections() default {};

    @Property(label = "Include paths",
            description = "When non-empty, only eligible pages under these prefixes are linked.")
    String[] includePaths() default {};

    @Property(label = "Exclude paths",
            description = "Eligible pages under these prefixes are omitted.")
    String[] excludePaths() default {};

    @Property(label = "Max links")
    int maxLinks() default 50;

    @Property(label = "Max output bytes")
    int maxOutputBytes() default 100_000;

    @Property(label = "Cache-Control max-age (seconds)")
    int cacheMaxAgeSeconds() default 3600;
}
