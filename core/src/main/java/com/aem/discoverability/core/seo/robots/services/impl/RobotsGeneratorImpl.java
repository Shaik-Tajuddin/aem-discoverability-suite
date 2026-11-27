package com.aem.discoverability.core.seo.robots.services.impl;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.aem.discoverability.core.seo.robots.api.DomainPolicy;
import com.aem.discoverability.core.seo.robots.services.RobotsGenerator;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;

@Component(service = RobotsGenerator.class)
public class RobotsGeneratorImpl implements RobotsGenerator {

    @Reference
    private DomainPolicy domainPolicy;

    @Override
    public String generate(ResolvedConfiguration configuration) {
        if (configuration == null || !configuration.isRobotsEnabled()) {
            return "";
        }
        if (!domainPolicy.isCrawlAllowed(configuration)) {
            return disallowAll();
        }
        StringBuilder out = new StringBuilder();
        List<String> agents = configuration.getRobotsUserAgents();
        if (agents.isEmpty()) {
            agents = List.of("*");
        }
        for (String agent : agents) {
            String ua = StringUtils.trimToEmpty(agent);
            if (ua.isEmpty()) {
                continue;
            }
            out.append("User-agent: ").append(ua).append('\n');
            appendRules(out, "Allow", configuration.getRobotsAllowRules());
            appendRules(out, "Disallow", configuration.getRobotsDisallowRules());
            out.append('\n');
        }
        appendSitemapLines(out, configuration);
        return out.toString().trim() + "\n";
    }

    private void appendSitemapLines(StringBuilder out, ResolvedConfiguration configuration) {
        Optional<String> origin = domainPolicy.publicOrigin(configuration);
        if (origin.isEmpty()) {
            return;
        }
        for (String candidate : configuration.getRobotsManualSitemapUrls()) {
            validateManualSitemapUrl(candidate, origin.get()).ifPresent(url ->
                    out.append("Sitemap: ").append(url).append('\n'));
        }
    }

    static Optional<String> validateManualSitemapUrl(String candidate, String allowedOrigin) {
        String raw = StringUtils.trimToEmpty(candidate);
        if (raw.isEmpty()) {
            return Optional.empty();
        }
        try {
            URI uri = URI.create(raw);
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
            return Optional.of(raw);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private static void appendRules(StringBuilder out, String directive, List<String> rules) {
        for (String rule : rules) {
            String path = StringUtils.trimToEmpty(rule);
            if (path.isEmpty()) {
                continue;
            }
            out.append(directive).append(": ").append(path).append('\n');
        }
    }

    private static String disallowAll() {
        return "User-agent: *\nDisallow: /\n";
    }
}
