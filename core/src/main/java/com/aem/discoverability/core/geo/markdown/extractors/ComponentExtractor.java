package com.aem.discoverability.core.geo.markdown.extractors;

import com.aem.discoverability.core.geo.markdown.models.MarkdownContext;
import java.util.Optional;
import org.apache.sling.api.resource.Resource;

public interface ComponentExtractor {
    public boolean supports(String var1);

    public int rank();

    public Optional<String> extract(Resource var1, MarkdownContext var2);
}

