package com.aem.discoverability.core.geo.markdown.extractors;

import com.aem.discoverability.core.geo.markdown.extractors.ComponentExtractor;
import com.aem.discoverability.core.geo.markdown.models.MarkdownContext;
import com.aem.discoverability.core.geo.markdown.util.ExtractorSupport;
import java.util.Optional;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

@Component(service={ComponentExtractor.class})
public class ListExtractor
implements ComponentExtractor {
    @Override
    public boolean supports(String resourceType) {
        return ExtractorSupport.matches(resourceType, "list");
    }

    @Override
    public int rank() {
        return 100;
    }

    @Override
    public Optional<String> extract(Resource component, MarkdownContext context) {
        StringBuilder md = new StringBuilder();
        Resource items = component.getChild("items");
        Iterable<Resource> children = items != null ? items.getChildren() : component.getChildren();
        for (Resource child : children) {
            String text = ExtractorSupport.firstNonBlank((String)child.getValueMap().get("jcr:title", (Object)""), (String)child.getValueMap().get("text", (Object)""), (String)child.getValueMap().get("linkText", (Object)""));
            if ((text = ExtractorSupport.plainText(text)).isEmpty()) continue;
            md.append("- ").append(text).append('\n');
        }
        if (md.length() == 0) {
            return Optional.empty();
        }
        md.append('\n');
        return Optional.of(md.toString());
    }
}

