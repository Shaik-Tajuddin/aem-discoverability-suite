package com.aem.discoverability.core.geo.markdown.services;

import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.day.cq.wcm.api.Page;
import java.util.Optional;

public interface PageMarkdownGenerator {
    public Optional<String> generate(Page var1, ResolvedConfiguration var2);
}

