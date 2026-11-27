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
public class TeaserExtractor
implements ComponentExtractor {
    @Override
    public boolean supports(String resourceType) {
        return ExtractorSupport.matches(resourceType, "teaser");
    }

    @Override
    public int rank() {
        return 100;
    }

    @Override
    public Optional<String> extract(Resource component, MarkdownContext context) {
        ValueMap props = component.getValueMap();
        StringBuilder md = new StringBuilder();
        String title = ExtractorSupport.firstNonBlank((String)props.get("jcr:title", (Object)""), (String)props.get("title", (Object)""));
        String pretitle = (String)props.get("pretitle", (Object)"");
        String description = (String)props.get("description", (Object)"");
        String link = ExtractorSupport.firstNonBlank((String)props.get("linkURL", (Object)""), (String)props.get("fileReference", (Object)""));
        if (StringUtils.isNotBlank((CharSequence)pretitle)) {
            md.append(ExtractorSupport.plainText(pretitle)).append("\n\n");
        }
        if (StringUtils.isNotBlank((CharSequence)title)) {
            md.append(ExtractorSupport.heading(3, title));
        }
        if (StringUtils.isNotBlank((CharSequence)description)) {
            md.append(ExtractorSupport.plainText(description)).append("\n\n");
        }
        if (StringUtils.isNotBlank((CharSequence)link)) {
            context.toPublicUrl(link).ifPresent(url -> md.append("[").append(ExtractorSupport.escapeMd((String)StringUtils.defaultIfBlank((CharSequence)ExtractorSupport.plainText(title), (CharSequence)"Learn more"))).append("](").append((String)url).append(")\n\n"));
        }
        return md.length() == 0 ? Optional.empty() : Optional.of(md.toString());
    }
}

