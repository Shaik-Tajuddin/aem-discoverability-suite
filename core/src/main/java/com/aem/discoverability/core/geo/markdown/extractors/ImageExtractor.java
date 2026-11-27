package com.aem.discoverability.core.geo.markdown.extractors;

import com.aem.discoverability.core.geo.markdown.extractors.ComponentExtractor;
import com.aem.discoverability.core.geo.markdown.models.MarkdownContext;
import com.aem.discoverability.core.geo.markdown.util.ExtractorSupport;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;

@Component(service={ComponentExtractor.class})
public class ImageExtractor
implements ComponentExtractor {
    @Override
    public boolean supports(String resourceType) {
        return ExtractorSupport.matches(resourceType, "image");
    }

    @Override
    public int rank() {
        return 100;
    }

    @Override
    public Optional<String> extract(Resource component, MarkdownContext context) {
        if (!context.acceptReference()) {
            context.skipped(component.getResourceType(), "max references");
            return Optional.empty();
        }
        ValueMap props = component.getValueMap();
        String file = StringUtils.trimToEmpty((String)((String)props.get("fileReference", (Object)"")));
        String alt = StringUtils.defaultString((String)((String)props.get("alt", (Object)((String)props.get("jcr:title", (Object)"")))));
        Optional<String> url = context.toPublicUrl(file);
        if (url.isEmpty()) {
            context.skipped(component.getResourceType(), "unsafe image path");
            return Optional.empty();
        }
        return Optional.of("![" + ExtractorSupport.escapeMd(alt) + "](" + url.get() + ")\n\n");
    }
}

