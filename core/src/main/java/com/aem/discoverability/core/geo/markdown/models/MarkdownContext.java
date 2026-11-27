package com.aem.discoverability.core.geo.markdown.models;

import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import java.util.Optional;
import org.apache.sling.api.resource.ResourceResolver;

public interface MarkdownContext {
    public ResolvedConfiguration getConfiguration();

    public ResourceResolver getResourceResolver();

    public boolean timeExceeded();

    public boolean acceptComponent(int var1);

    public boolean acceptReference();

    public Optional<String> toPublicUrl(String var1);

    public void skipped(String var1, String var2);
}

