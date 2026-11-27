package com.aem.discoverability.core.seo.sitemap.services;

import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import java.util.Optional;
import org.apache.sling.api.resource.ResourceResolver;

public interface SitemapGenerator {
    public Optional<String> generateIndex(ResourceResolver var1, ResolvedConfiguration var2);

    public Optional<String> generatePartition(ResourceResolver var1, ResolvedConfiguration var2, int var3);
}

