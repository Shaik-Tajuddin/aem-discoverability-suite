package com.aem.discoverability.core.geo.markdown.extractors;

import com.aem.discoverability.core.geo.markdown.extractors.ComponentExtractor;
import com.aem.discoverability.core.geo.markdown.models.MarkdownContext;
import com.aem.discoverability.core.geo.markdown.util.ExtractorSupport;
import java.util.Optional;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

@Component(service={ComponentExtractor.class})
public class TextExtractor
implements ComponentExtractor {
    @Override
    public boolean supports(String resourceType) {
        return ExtractorSupport.matches(resourceType, "text");
    }

    @Override
    public int rank() {
        return 100;
    }

    @Override
    public Optional<String> extract(Resource component, MarkdownContext context) {
        String text = ExtractorSupport.plainText((String)component.getValueMap().get("text", (Object)""));
        if (text.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(text + "\n\n");
    }
}

