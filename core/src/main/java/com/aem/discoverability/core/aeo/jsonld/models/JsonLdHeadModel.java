package com.aem.discoverability.core.aeo.jsonld.models;

import com.aem.discoverability.core.aeo.jsonld.services.JsonLdGraphService;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfigurationService;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.servlet.ServletRequest;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

@Model(adaptables={SlingHttpServletRequest.class})
public class JsonLdHeadModel {
    public static final String REQUEST_ATTR_WRITTEN = "discoverability.jsonld.written";
    @Self
    private SlingHttpServletRequest request;
    @SlingObject
    private Resource resource;
    @OSGiService
    private ResolvedConfigurationService resolvedConfigurationService;
    @OSGiService
    private JsonLdGraphService jsonLdGraphService;
    private String json;
    private boolean available;

    @PostConstruct
    void init() {
        Page page;
        if (this.request.getAttribute(REQUEST_ATTR_WRITTEN) != null) {
            return;
        }
        Optional resolved = this.resolvedConfigurationService.resolve(this.resource);
        if (resolved.isEmpty()) {
            return;
        }
        ResolvedConfiguration configuration = (ResolvedConfiguration)resolved.get();
        if (!configuration.isJsonLdEnabled()) {
            return;
        }
        PageManager pageManager = (PageManager)this.request.getResourceResolver().adaptTo(PageManager.class);
        Page page2 = page = pageManager != null ? pageManager.getContainingPage(this.resource) : null;
        if (page == null) {
            return;
        }
        Optional<String> body = this.jsonLdGraphService.buildJson(page, configuration);
        if (body.isEmpty()) {
            return;
        }
        this.json = body.get();
        this.available = true;
        this.request.setAttribute(REQUEST_ATTR_WRITTEN, (Object)Boolean.TRUE);
    }

    public boolean isAvailable() {
        return this.available;
    }

    public String getJson() {
        return this.json;
    }

    public static void markWritten(ServletRequest request) {
        if (request != null) {
            request.setAttribute(REQUEST_ATTR_WRITTEN, (Object)Boolean.TRUE);
        }
    }
}

