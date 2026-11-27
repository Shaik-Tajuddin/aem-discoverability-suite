package com.aem.discoverability.core.seo.sitemap.servlets;

import com.aem.discoverability.core.ops.api.GenerationDiagnostics;
import com.aem.discoverability.core.ops.impl.GenerationSupport;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfigurationService;
import com.aem.discoverability.core.seo.sitemap.services.SitemapGenerator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.servlet.Servlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service={Servlet.class})
@SlingServletResourceTypes(resourceTypes={"cq/Page"}, methods={"GET"}, selectors={"sitemap"}, extensions={"xml"})
public class SitemapServlet
extends SlingSafeMethodsServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(SitemapServlet.class);
    private static final String SLING_SITEMAP_ROOT = "sling:sitemapRoot";
    @Reference
    private ResolvedConfigurationService resolvedConfigurationService;
    @Reference
    private SitemapGenerator sitemapGenerator;
    @Reference
    private GenerationDiagnostics generationDiagnostics;

    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        Optional<String> body;
        long started = System.currentTimeMillis();
        String correlationId = GenerationSupport.correlationId((SlingHttpServletRequest)request);
        Resource resource = request.getResource();
        Optional resolved = this.resolvedConfigurationService.resolve(resource);
        if (resolved.isEmpty()) {
            SitemapServlet.sendErrorUncached(response, 404);
            return;
        }
        ResolvedConfiguration configuration = (ResolvedConfiguration)resolved.get();
        if (!configuration.isSitemapEnabled()) {
            SitemapServlet.sendErrorUncached(response, 404);
            return;
        }
        this.warnIfSlingSitemapConflict(resource, configuration);
        String[] selectors = request.getRequestPathInfo().getSelectors();
        if (selectors.length >= 2) {
            Integer partition = SitemapServlet.parsePartition(selectors[1]);
            if (partition == null) {
                SitemapServlet.sendErrorUncached(response, 404);
                return;
            }
            body = this.sitemapGenerator.generatePartition(request.getResourceResolver(), configuration, partition);
        } else {
            body = this.sitemapGenerator.generateIndex(request.getResourceResolver(), configuration);
        }
        if (body.isEmpty()) {
            this.generationDiagnostics.emit(GenerationSupport.base((String)"sitemap", (ResolvedConfiguration)configuration, (String)correlationId, (long)started).status("error").errorCategory("empty_or_limit").limitHit(true).build());
            SitemapServlet.sendErrorUncached(response, 404);
            return;
        }
        byte[] bytes = body.get().getBytes(StandardCharsets.UTF_8);
        this.generationDiagnostics.emit(GenerationSupport.base((String)"sitemap", (ResolvedConfiguration)configuration, (String)correlationId, (long)started).outputBytes((long)bytes.length).status("success").build());
        int maxAge = configuration.getSitemapCacheMaxAgeSeconds();
        response.setStatus(200);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/xml");
        response.setHeader("Cache-Control", "public, max-age=" + maxAge);
        response.getWriter().write(body.get());
    }

    private void warnIfSlingSitemapConflict(Resource resource, ResolvedConfiguration configuration) {
        for (Resource current = resource; current != null && !"/".equals(current.getPath()); current = current.getParent()) {
            if (!((Boolean)current.getValueMap().get(SLING_SITEMAP_ROOT, (Object)false)).booleanValue() && (current.getChild("jcr:content") == null || !((Boolean)current.getChild("jcr:content").getValueMap().get(SLING_SITEMAP_ROOT, (Object)false)).booleanValue())) continue;
            LOG.warn("Sling Sitemap root detected at {} while Discoverability sitemap is enabled for {}; disable one to avoid duplicate ownership", (Object)current.getPath(), (Object)configuration.getProjectRoot());
            return;
        }
    }

    private static Integer parsePartition(String selector) {
        try {
            int value = Integer.parseInt(selector);
            return value >= 0 ? Integer.valueOf(value) : null;
        }
        catch (NumberFormatException ex) {
            return null;
        }
    }

    private static void sendErrorUncached(SlingHttpServletResponse response, int status) throws IOException {
        response.setHeader("Cache-Control", "no-store");
        response.sendError(status);
    }
}

