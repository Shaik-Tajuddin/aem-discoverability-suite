package com.aem.discoverability.core.geo.markdown.extractors;

import com.aem.discoverability.core.geo.markdown.extractors.ComponentExtractor;
import com.aem.discoverability.core.geo.markdown.models.MarkdownContext;
import com.aem.discoverability.core.geo.markdown.util.ExtractorSupport;
import java.util.Optional;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;

@Component(service={ComponentExtractor.class})
public class TitleExtractor
implements ComponentExtractor {
    @Override
    public boolean supports(String resourceType) {
        return ExtractorSupport.matches(resourceType, "title");
    }

    @Override
    public int rank() {
        return 100;
    }

    @Override
    public Optional<String> extract(Resource component, MarkdownContext context) {
        String md;
        ValueMap props = component.getValueMap();
        String title = ExtractorSupport.firstNonBlank((String)props.get("jcr:title", (Object)""), (String)props.get("title", (Object)""));
        int level = 2;
        String type = ExtractorSupport.normalizeType((String)props.get("type", (Object)"h2"));
        if (type.matches("h[1-6]")) {
            level = type.charAt(1) - 48;
        }
        return (md = ExtractorSupport.heading(level, title)).isEmpty() ? Optional.empty() : Optional.of(md);
    }
}

