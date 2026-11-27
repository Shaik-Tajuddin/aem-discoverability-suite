package com.aem.discoverability.core.ops.impl;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;

import com.aem.discoverability.core.ops.api.GenerationEvent;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.seo.sitemap.services.impl.ProjectResolverImpl;

/**
 * Shared helpers for servlet/generator diagnostics.
 */
public final class GenerationSupport {

    public static final String REQUEST_ATTR_CORRELATION = "discoverability.correlationId";

    private GenerationSupport() {
    }

    public static String correlationId(SlingHttpServletRequest request) {
        if (request == null) {
            return UUID.randomUUID().toString();
        }
        Object existing = request.getAttribute(REQUEST_ATTR_CORRELATION);
        if (existing instanceof String && StringUtils.isNotBlank((String) existing)) {
            return (String) existing;
        }
        String id = StringUtils.trimToEmpty(request.getHeader("X-Correlation-Id"));
        if (id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }
        request.setAttribute(REQUEST_ATTR_CORRELATION, id);
        return id;
    }

    public static String projectLabel(ResolvedConfiguration configuration) {
        if (configuration == null) {
            return "";
        }
        String root = ProjectResolverImpl.normalizeRoot(configuration.getProjectRoot());
        int slash = root.lastIndexOf('/');
        return slash >= 0 && slash < root.length() - 1 ? root.substring(slash + 1) : root;
    }

    public static GenerationEvent.Builder base(String output, ResolvedConfiguration configuration,
            String correlationId, long startedMs) {
        return GenerationEvent.builder()
                .correlationId(correlationId)
                .project(projectLabel(configuration))
                .output(output)
                .productionEnvironment(configuration != null && configuration.isProductionEnvironment())
                .durationMs(Math.max(0L, System.currentTimeMillis() - startedMs));
    }
}
