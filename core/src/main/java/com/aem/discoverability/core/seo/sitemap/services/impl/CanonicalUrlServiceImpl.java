package com.aem.discoverability.core.seo.sitemap.services.impl;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import com.aem.discoverability.core.seo.sitemap.api.CanonicalUrlService;
import com.aem.discoverability.core.seo.sitemap.api.DiscoverabilityPageProperties;
import com.aem.discoverability.core.seo.robots.api.DomainPolicy;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.day.cq.commons.Externalizer;
import com.day.cq.wcm.api.Page;

/**
 * Builds absolute public URLs from trusted project domain + path mapping.
 * Host header is never used. Canonical overrides must match the allowlisted origin.
 */
@Component(service = CanonicalUrlService.class)
public class CanonicalUrlServiceImpl implements CanonicalUrlService {

    @Reference
    private DomainPolicy domainPolicy;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
    private Externalizer externalizer;

    @Override
    public Optional<String> resolve(Page page, ResolvedConfiguration configuration) {
        if (page == null || configuration == null) {
            return Optional.empty();
        }

        Optional<String> origin = domainPolicy.publicOrigin(configuration);
        if (origin.isEmpty()) {
            return Optional.empty();
        }

        String override = StringUtils.trimToEmpty(
                page.getProperties().get(DiscoverabilityPageProperties.CANONICAL_URL, ""));
        if (!override.isEmpty()) {
            return validateOverride(override, origin.get());
        }

        Optional<String> mapped = mapPath(page, configuration, origin.get());
        return mapped.flatMap(url -> validateOverride(url, origin.get()));
    }

    private Optional<String> mapPath(Page page, ResolvedConfiguration configuration, String origin) {
        String pagePath = page.getPath();
        String root = ProjectResolverImpl.normalizeRoot(configuration.getProjectRoot());
        if (!ProjectResolverImpl.isUnder(pagePath, root)) {
            return Optional.empty();
        }

        if (externalizer != null) {
            ResourceResolver resolver = page.getContentResource() != null
                    ? page.getContentResource().getResourceResolver()
                    : null;
            if (resolver != null) {
                try {
                    String external = externalizer.publishLink(resolver, pagePath);
                    // Only keep Externalizer URLs that match the trusted public origin.
                    Optional<String> trusted = StringUtils.isNotBlank(external)
                            ? validateOverride(external, origin)
                            : Optional.empty();
                    if (trusted.isPresent()) {
                        return trusted;
                    }
                } catch (RuntimeException ignored) {
                    // Fall through to path-based mapping.
                }
            }
        }

        String relative = pagePath.equals(root) ? "/" : pagePath.substring(root.length());
        if (relative.isEmpty()) {
            relative = "/";
        }
        if (!relative.startsWith("/")) {
            relative = "/" + relative;
        }
        // Drop trailing slash except for site root.
        if (relative.length() > 1 && relative.endsWith("/")) {
            relative = relative.substring(0, relative.length() - 1);
        }
        return Optional.of(origin + relative);
    }

    static Optional<String> validateOverride(String candidate, String allowedOrigin) {
        try {
            URI uri = URI.create(candidate.trim());
            if (uri.getScheme() == null || uri.getHost() == null) {
                return Optional.empty();
            }
            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            if (!"https".equals(scheme) && !"http".equals(scheme)) {
                return Optional.empty();
            }
            URI allowed = URI.create(allowedOrigin);
            if (!uri.getHost().equalsIgnoreCase(allowed.getHost())) {
                return Optional.empty();
            }
            if (allowed.getPort() > 0 && uri.getPort() > 0 && allowed.getPort() != uri.getPort()) {
                return Optional.empty();
            }
            StringBuilder normalized = new StringBuilder()
                    .append(scheme)
                    .append("://")
                    .append(uri.getHost().toLowerCase(Locale.ROOT));
            if (uri.getPort() > 0) {
                normalized.append(':').append(uri.getPort());
            }
            String path = uri.getRawPath();
            if (path == null || path.isEmpty()) {
                path = "/";
            }
            normalized.append(path);
            if (uri.getRawQuery() != null) {
                normalized.append('?').append(uri.getRawQuery());
            }
            return Optional.of(normalized.toString());
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
