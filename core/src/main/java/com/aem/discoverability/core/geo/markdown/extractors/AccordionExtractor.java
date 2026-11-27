package com.aem.discoverability.core.geo.markdown.extractors;

import com.aem.discoverability.core.geo.markdown.extractors.ComponentExtractor;
import com.aem.discoverability.core.geo.markdown.models.MarkdownContext;
import com.aem.discoverability.core.geo.markdown.util.ExtractorSupport;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

@Component(service={ComponentExtractor.class})
public class AccordionExtractor
implements ComponentExtractor {
    @Override
    public boolean supports(String resourceType) {
        return ExtractorSupport.matches(resourceType, "accordion");
    }

    @Override
    public int rank() {
        return 100;
    }

    @Override
    public Optional<String> extract(Resource component, MarkdownContext context) {
        StringBuilder md = new StringBuilder();
        for (Resource panel : component.getChildren()) {
            String title = ExtractorSupport.firstNonBlank((String)panel.getValueMap().get("cq:panelTitle", (Object)""), (String)panel.getValueMap().get("jcr:title", (Object)""));
            if (StringUtils.isNotBlank((CharSequence)title)) {
                md.append(ExtractorSupport.heading(3, title));
            }
            for (Resource child : panel.getChildren()) {
                String text = (String)child.getValueMap().get("text", (Object)"");
                String plain = ExtractorSupport.plainText(text);
                if (plain.isEmpty()) continue;
                md.append(plain).append("\n\n");
            }
        }
        return md.length() == 0 ? Optional.empty() : Optional.of(md.toString());
    }
}

