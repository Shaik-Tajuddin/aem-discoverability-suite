package com.aem.discoverability.core.seo.robots.servlets;

import com.aem.discoverability.core.ops.api.GenerationDiagnostics;
import com.aem.discoverability.core.ops.impl.GenerationSupport;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfigurationService;
import com.aem.discoverability.core.seo.robots.services.RobotsGenerator;
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
@SlingServletResourceTypes(resourceTypes={"cq/Page"}, methods={"GET"}, selectors={"robots"}, extensions={"txt"})
public class RobotsServlet
extends SlingSafeMethodsServlet {
    private static final long serialVersionUID = 1L;
    @Reference
    private ResolvedConfigurationService resolvedConfigurationService;
    @Reference
    private RobotsGenerator robotsGenerator;
    @Reference
    private GenerationDiagnostics generationDiagnostics;

    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        long started = System.currentTimeMillis();
        String correlationId = GenerationSupport.correlationId((SlingHttpServletRequest)request);
        Resource resource = request.getResource();
        Optional resolved = this.resolvedConfigurationService.resolve(resource);
        if (resolved.isEmpty()) {
            RobotsServlet.sendErrorUncached(response, 404);
            return;
        }
        ResolvedConfiguration configuration = (ResolvedConfiguration)resolved.get();
        if (!configuration.isRobotsEnabled()) {
            RobotsServlet.sendErrorUncached(response, 404);
            return;
        }
        String body = this.robotsGenerator.generate(configuration);
        if (body.isEmpty()) {
            this.generationDiagnostics.emit(GenerationSupport.base((String)"robots", (ResolvedConfiguration)configuration, (String)correlationId, (long)started).status("error").errorCategory("disabled_or_empty").build());
            RobotsServlet.sendErrorUncached(response, 404);
            return;
        }
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        this.generationDiagnostics.emit(GenerationSupport.base((String)"robots", (ResolvedConfiguration)configuration, (String)correlationId, (long)started).outputBytes((long)bytes.length).status("success").build());
        int maxAge = configuration.getRobotsCacheMaxAgeSeconds();
        response.setStatus(200);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/plain");
        response.setHeader("Cache-Control", "public, max-age=" + maxAge);
        response.getWriter().write(body);
    }

    private static void sendErrorUncached(SlingHttpServletResponse response, int status) throws IOException {
        response.setHeader("Cache-Control", "no-store");
        response.sendError(status);
    }
}

