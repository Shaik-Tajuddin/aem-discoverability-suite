package com.aem.discoverability.core.seo.robots.services.impl;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

import com.aem.discoverability.core.seo.robots.api.DomainPolicy;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;

/**
 * Public origin from trusted configuration only. Host header is never consulted.
 */
@Component(service = DomainPolicy.class)
public class DomainPolicyImpl implements DomainPolicy {

    @Override
    public Optional<String> publicOrigin(ResolvedConfiguration configuration) {
        if (configuration == null) {
            return Optional.empty();
        }
        return normalizeOrigin(configuration.getPublicDomain());
    }

    @Override
    public boolean isCrawlAllowed(ResolvedConfiguration configuration) {
        return configuration != null;
    }

    static Optional<String> normalizeOrigin(String publicDomain) {
        String raw = StringUtils.trimToEmpty(publicDomain);
        if (raw.isEmpty()) {
            return Optional.empty();
        }
        try {
            URI uri = URI.create(raw);
            String scheme = uri.getScheme();
            if (scheme == null || uri.getHost() == null) {
                return Optional.empty();
            }
            String normalizedScheme = scheme.toLowerCase(Locale.ROOT);
            if (!"https".equals(normalizedScheme) && !"http".equals(normalizedScheme)) {
                return Optional.empty();
            }
            StringBuilder origin = new StringBuilder()
                    .append(normalizedScheme)
                    .append("://")
                    .append(uri.getHost().toLowerCase(Locale.ROOT));
            if (uri.getPort() > 0) {
                origin.append(':').append(uri.getPort());
            }
            return Optional.of(origin.toString());
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
