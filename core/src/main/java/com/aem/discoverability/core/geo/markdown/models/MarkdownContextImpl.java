package com.aem.discoverability.core.geo.markdown.models;

import com.aem.discoverability.core.geo.markdown.models.MarkdownContext;
import com.aem.discoverability.core.seo.robots.api.DomainPolicy;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.seo.sitemap.services.impl.ProjectResolverImpl;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MarkdownContextImpl
implements MarkdownContext {
    private static final Logger LOG = LoggerFactory.getLogger(MarkdownContextImpl.class);
    private final ResolvedConfiguration configuration;
    private final ResourceResolver resourceResolver;
    private final DomainPolicy domainPolicy;
    private final long deadlineMs;
    private int components;
    private int references;

    public MarkdownContextImpl(ResolvedConfiguration configuration, ResourceResolver resourceResolver, DomainPolicy domainPolicy, long deadlineMs) {
        this.configuration = configuration;
        this.resourceResolver = resourceResolver;
        this.domainPolicy = domainPolicy;
        this.deadlineMs = deadlineMs;
    }

    @Override
    public ResolvedConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public ResourceResolver getResourceResolver() {
        return this.resourceResolver;
    }

    @Override
    public boolean timeExceeded() {
        return System.currentTimeMillis() > this.deadlineMs;
    }

    @Override
    public boolean acceptComponent(int depth) {
        if (this.timeExceeded()) {
            return false;
        }
        if (depth > this.configuration.getMarkdownMaxComponentDepth()) {
            return false;
        }
        if (this.components >= this.configuration.getMarkdownMaxComponents()) {
            return false;
        }
        ++this.components;
        return true;
    }

    @Override
    public boolean acceptReference() {
        if (this.references >= this.configuration.getMarkdownMaxReferences()) {
            return false;
        }
        ++this.references;
        return true;
    }

    @Override
    public Optional<String> toPublicUrl(String absolutePath) {
        String path = StringUtils.trimToEmpty((String)absolutePath);
        if (!path.startsWith("/") || path.contains("://")) {
            return Optional.empty();
        }
        if (!path.startsWith("/content/") && !path.startsWith("/content/dam/")) {
            return Optional.empty();
        }
        String root = ProjectResolverImpl.normalizeRoot((String)this.configuration.getProjectRoot());
        if (path.startsWith("/content/") && !path.startsWith("/content/dam/") && !ProjectResolverImpl.isUnder((String)path, (String)root)) {
            return Optional.empty();
        }
        return this.domainPolicy.publicOrigin(this.configuration).map(origin -> origin + path);
    }

    @Override
    public void skipped(String resourceType, String reason) {
        LOG.debug("Markdown skip type={} reason={}", (Object)resourceType, (Object)reason);
    }
}

