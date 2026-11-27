package com.aem.discoverability.core.geo.markdown.extractors;

import com.aem.discoverability.core.geo.markdown.extractors.ComponentExtractor;
import com.aem.discoverability.core.geo.markdown.extractors.ComponentExtractorRegistry;
import com.aem.discoverability.core.geo.markdown.models.MarkdownContext;
import com.aem.discoverability.core.geo.markdown.util.ExtractorSupport;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service={ComponentExtractor.class})
public class ExperienceFragmentExtractor
implements ComponentExtractor {
    @Reference
    private ComponentExtractorRegistry registry;

    @Override
    public boolean supports(String resourceType) {
        return ExtractorSupport.matches(resourceType, "experiencefragment");
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
        String path = ExtractorSupport.firstNonBlank((String)component.getValueMap().get("fragmentVariationPath", (Object)""), (String)component.getValueMap().get("fragmentPath", (Object)""));
        if (StringUtils.isBlank((CharSequence)path)) {
            return Optional.empty();
        }
        Resource variation = context.getResourceResolver().getResource(path);
        if (variation == null) {
            context.skipped(component.getResourceType(), "xf missing");
            return Optional.empty();
        }
        Resource content = variation.getChild("jcr:content") != null ? variation.getChild("jcr:content") : variation;
        StringBuilder md = new StringBuilder();
        this.walk(content, context, 0, md);
        return md.length() == 0 ? Optional.empty() : Optional.of(md.toString());
    }

    private void walk(Resource resource, MarkdownContext context, int depth, StringBuilder md) {
        if (context.timeExceeded() || !context.acceptComponent(depth)) {
            return;
        }
        String type = resource.getResourceType();
        if (StringUtils.isNotBlank((CharSequence)type) && !"cq/Page".equals(type) && !type.startsWith("cq/Page/")) {
            this.registry.select(type).flatMap(ex -> ex.extract(resource, context)).ifPresent(md::append);
        }
        for (Resource child : resource.getChildren()) {
            this.walk(child, context, depth + 1, md);
        }
    }
}

