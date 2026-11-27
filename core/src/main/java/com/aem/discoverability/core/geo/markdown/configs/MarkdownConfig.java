package com.aem.discoverability.core.geo.markdown.configs;

import org.apache.sling.caconfig.annotation.Configuration;
import org.apache.sling.caconfig.annotation.Property;

/**
 * Page Markdown enablement, public origin, traversal/output limits, and cache TTL.
 */
@Configuration(label = "Discoverability Markdown",
        description = "Enablement, public domain, component/output limits, and cache TTL.")
public @interface MarkdownConfig {

    @Property(label = "Enabled")
    boolean enabled() default false;

    @Property(label = "Public domain",
            description = "Trusted public origin (https://example.com). Never taken from Host header.")
    String publicDomain() default "";

    @Property(label = "Timeout (ms)")
    long timeoutMs() default 30_000L;

    @Property(label = "Max output bytes")
    long maxOutputBytes() default 5_000_000L;

    @Property(label = "Max component depth")
    int maxComponentDepth() default 20;

    @Property(label = "Max components")
    int maxComponents() default 500;

    @Property(label = "Max references (CF/XF/image)")
    int maxReferences() default 25;

    @Property(label = "Cache-Control max-age (seconds)")
    int cacheMaxAgeSeconds() default 3600;
}
