package com.aem.discoverability.core.aeo.jsonld.rewriter;

import com.aem.discoverability.core.aeo.jsonld.models.JsonLdHeadModel;
import com.aem.discoverability.core.aeo.jsonld.services.JsonLdGraphService;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfigurationService;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.ServletRequest;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.apache.sling.rewriter.Transformer;
import org.apache.sling.rewriter.TransformerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

@Component(service={TransformerFactory.class}, property={"pipeline.type=discoverability-jsonld"})
public class JsonLdTransformerFactory
implements TransformerFactory {
    @Reference
    private ResolvedConfigurationService resolvedConfigurationService;
    @Reference
    private JsonLdGraphService jsonLdGraphService;

    public Transformer createTransformer() {
        return new JsonLdTransformer(this.resolvedConfigurationService, this.jsonLdGraphService);
    }

    static final class JsonLdTransformer
    extends XMLFilterImpl
    implements Transformer {
        private final ResolvedConfigurationService resolvedConfigurationService;
        private final JsonLdGraphService jsonLdGraphService;
        private SlingHttpServletRequest request;
        private boolean injected;

        JsonLdTransformer(ResolvedConfigurationService resolvedConfigurationService, JsonLdGraphService jsonLdGraphService) {
            this.resolvedConfigurationService = resolvedConfigurationService;
            this.jsonLdGraphService = jsonLdGraphService;
        }

        public void init(ProcessingContext context, ProcessingComponentConfiguration config) throws IOException {
            this.request = context.getRequest();
            this.injected = false;
        }

        public void dispose() {
            this.request = null;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (!this.injected && this.request != null && "head".equalsIgnoreCase(localName)) {
                this.inject();
            }
            super.endElement(uri, localName, qName);
        }

        private void inject() throws SAXException {
            Page page;
            if (this.request.getAttribute("discoverability.jsonld.written") != null) {
                this.injected = true;
                return;
            }
            Optional resolved = this.resolvedConfigurationService.resolve(this.request.getResource());
            if (resolved.isEmpty()) {
                return;
            }
            ResolvedConfiguration configuration = (ResolvedConfiguration)resolved.get();
            if (!configuration.isJsonLdEnabled()) {
                return;
            }
            PageManager pageManager = (PageManager)this.request.getResourceResolver().adaptTo(PageManager.class);
            Page page2 = page = pageManager != null ? pageManager.getContainingPage(this.request.getResource()) : null;
            if (page == null) {
                return;
            }
            Optional<String> json = this.jsonLdGraphService.buildJson(page, configuration);
            if (json.isEmpty()) {
                return;
            }
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("", "type", "type", "CDATA", "application/ld+json");
            super.startElement("", "script", "script", attrs);
            char[] chars = json.get().toCharArray();
            super.characters(chars, 0, chars.length);
            super.endElement("", "script", "script");
            JsonLdHeadModel.markWritten((ServletRequest)this.request);
            this.injected = true;
        }
    }
}

