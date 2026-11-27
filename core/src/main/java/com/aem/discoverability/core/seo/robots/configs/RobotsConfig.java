package com.aem.discoverability.core.seo.robots.configs;

import org.apache.sling.caconfig.annotation.Configuration;
import org.apache.sling.caconfig.annotation.Property;

/**
 * robots.txt enablement, public origin, and directives.
 */
@Configuration(label = "Discoverability Robots",
        description = "Enablement, public domain, user-agent rules, and optional sitemap URLs.")
public @interface RobotsConfig {

    @Property(label = "Enabled")
    boolean enabled() default false;

    @Property(label = "Public domain",
            description = "Trusted public origin (https://example.com). Never taken from Host header.")
    String publicDomain() default "";

    @Property(label = "User-agents", description = "One group per value; default is *.")
    String[] userAgents() default { "*" };

    @Property(label = "Allow rules")
    String[] allowRules() default {};

    @Property(label = "Disallow rules")
    String[] disallowRules() default {};

    @Property(label = "Sitemap URLs",
            description = "Optional absolute sitemap URLs (same host as public domain).")
    String[] manualSitemapUrls() default {};

    @Property(label = "Cache-Control max-age (seconds)")
    int cacheMaxAgeSeconds() default 1200;
}
