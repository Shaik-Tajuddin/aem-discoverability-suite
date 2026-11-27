package com.aem.discoverability.core.aeo.jsonld.services;

import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.day.cq.wcm.api.Page;
import java.util.Optional;

public interface JsonLdGraphService {
    public Optional<String> buildJson(Page var1, ResolvedConfiguration var2);
}

