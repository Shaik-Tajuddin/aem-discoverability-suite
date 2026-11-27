package com.aem.discoverability.core.geo.markdown.extractors;

import com.aem.discoverability.core.geo.markdown.extractors.ComponentExtractor;
import com.aem.discoverability.core.geo.markdown.models.MarkdownContext;
import com.aem.discoverability.core.geo.markdown.util.ExtractorSupport;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;

@Component(service={ComponentExtractor.class})
public class ContentFragmentExtractor
implements ComponentExtractor {
    private static final Set<String> APPROVED = Set.of("jcr:title", "title", "jcr:description", "description", "text", "main", "contentText");

    @Override
    public boolean supports(String resourceType) {
        return ExtractorSupport.matches(resourceType, "contentfragment");
    }

    @Override
    public int rank() {
        return 100;
    }

    @Override
    public Optional<String> extract(Resource component, MarkdownContext context) {
        Resource data;
        if (!context.acceptReference()) {
            context.skipped(component.getResourceType(), "max references");
            return Optional.empty();
        }
        String fragmentPath = StringUtils.trimToEmpty((String)((String)component.getValueMap().get("fragmentPath", (Object)"")));
        Resource fragment = StringUtils.isNotBlank((CharSequence)fragmentPath) ? context.getResourceResolver().getResource(fragmentPath) : null;
        Resource resource = data = fragment != null ? fragment.getChild("jcr:content/data/master") : null;
        if (data == null) {
            data = component;
        }
        ValueMap props = data.getValueMap();
        StringBuilder md = new StringBuilder();
        for (String key : APPROVED) {
            String plain;
            Object value = props.get((Object)key);
            if (!(value instanceof String) || !StringUtils.isNotBlank((CharSequence)((String)value)) || (plain = ExtractorSupport.plainText((String)value)).isEmpty()) continue;
            if ("jcr:title".equals(key) || "title".equals(key)) {
                md.append(ExtractorSupport.heading(3, plain));
                continue;
            }
            md.append(plain).append("\n\n");
        }
        if (md.length() == 0) {
            context.skipped(component.getResourceType(), "no approved CF fields");
            return Optional.empty();
        }
        return Optional.of(md.toString());
    }
}

