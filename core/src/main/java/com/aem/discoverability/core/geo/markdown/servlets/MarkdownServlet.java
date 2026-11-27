package com.aem.discoverability.core.geo.markdown.servlets;

import com.aem.discoverability.core.geo.markdown.services.PageMarkdownGenerator;
import com.aem.discoverability.core.ops.api.GenerationDiagnostics;
import com.aem.discoverability.core.ops.impl.GenerationSupport;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfigurationService;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
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

@Component(service={Servlet.class})
@SlingServletResourceTypes(resourceTypes={"cq/Page"}, methods={"GET"}, extensions={"md"})
public class MarkdownServlet
extends SlingSafeMethodsServlet {
    private static final long serialVersionUID = 1L;
    @Reference
    private ResolvedConfigurationService resolvedConfigurationService;
    @Reference
    private PageMarkdownGenerator pageMarkdownGenerator;
    @Reference
    private GenerationDiagnostics generationDiagnostics;

    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        Page page;
        long started = System.currentTimeMillis();
        String correlationId = GenerationSupport.correlationId((SlingHttpServletRequest)request);
        Resource resource = request.getResource();
        Optional resolved = this.resolvedConfigurationService.resolve(resource);
        if (resolved.isEmpty()) {
            MarkdownServlet.sendErrorUncached(response, 404);
            return;
        }
        ResolvedConfiguration configuration = (ResolvedConfiguration)resolved.get();
        if (!configuration.isMarkdownEnabled()) {
            MarkdownServlet.sendErrorUncached(response, 404);
            return;
        }
        PageManager pageManager = (PageManager)request.getResourceResolver().adaptTo(PageManager.class);
        Page page2 = page = pageManager != null ? pageManager.getContainingPage(resource) : null;
        if (page == null) {
            MarkdownServlet.sendErrorUncached(response, 404);
            return;
        }
        Optional<String> body = this.pageMarkdownGenerator.generate(page, configuration);
        if (body.isEmpty()) {
            this.generationDiagnostics.emit(GenerationSupport.base((String)"markdown", (ResolvedConfiguration)configuration, (String)correlationId, (long)started).status("error").errorCategory("ineligible_or_limit").build());
            MarkdownServlet.sendErrorUncached(response, 404);
            return;
        }
        byte[] bytes = body.get().getBytes(StandardCharsets.UTF_8);
        this.generationDiagnostics.emit(GenerationSupport.base((String)"markdown", (ResolvedConfiguration)configuration, (String)correlationId, (long)started).pagesIncluded(1).outputBytes((long)bytes.length).status("success").build());
        response.setStatus(200);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/markdown");
        response.setHeader("Cache-Control", "public, max-age=" + configuration.getMarkdownCacheMaxAgeSeconds());
        response.getWriter().write(body.get());
    }

    private static void sendErrorUncached(SlingHttpServletResponse response, int status) throws IOException {
        response.setHeader("Cache-Control", "no-store");
        response.sendError(status);
    }
}

