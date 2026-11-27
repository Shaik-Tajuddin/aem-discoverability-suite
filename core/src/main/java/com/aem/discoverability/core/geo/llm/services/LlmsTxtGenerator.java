package com.aem.discoverability.core.geo.llm.services;

import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import java.util.Optional;
import org.apache.sling.api.resource.ResourceResolver;

public interface LlmsTxtGenerator {
    public Optional<String> generate(ResourceResolver var1, ResolvedConfiguration var2);
}

