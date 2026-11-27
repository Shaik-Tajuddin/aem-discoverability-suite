package com.aem.discoverability.core.geo.markdown.extractors;

import com.aem.discoverability.core.geo.markdown.extractors.ComponentExtractor;
import com.aem.discoverability.core.geo.markdown.models.MarkdownContext;
import com.aem.discoverability.core.geo.markdown.util.ExtractorSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

@Component(service={ComponentExtractor.class})
public class TableExtractor
implements ComponentExtractor {
    @Override
    public boolean supports(String resourceType) {
        return ExtractorSupport.matches(resourceType, "table");
    }

    @Override
    public int rank() {
        return 100;
    }

    @Override
    public Optional<String> extract(Resource component, MarkdownContext context) {
        String html = (String)component.getValueMap().get("text", (Object)"");
        if (StringUtils.isNotBlank((CharSequence)html) && html.toLowerCase().contains("<table")) {
            String plain = ExtractorSupport.plainText(html);
            return plain.isEmpty() ? Optional.empty() : Optional.of(plain + "\n\n");
        }
        ArrayList rows = new ArrayList();
        Resource rowsParent = component.getChild("rows");
        if (rowsParent != null) {
            for (Resource row : rowsParent.getChildren()) {
                ArrayList<String> cells = new ArrayList<String>();
                for (Resource cell : row.getChildren()) {
                    cells.add(ExtractorSupport.plainText((String)cell.getValueMap().get("text", (Object)((String)cell.getValueMap().get("value", (Object)"")))));
                }
                if (cells.isEmpty()) continue;
                rows.add(cells);
            }
        }
        if (rows.isEmpty()) {
            context.skipped(component.getResourceType(), "no table structure");
            return Optional.empty();
        }
        StringBuilder md = new StringBuilder();
        List header = (List)rows.get(0);
        md.append("| ").append(String.join((CharSequence)" | ", header)).append(" |\n| ");
        for (int i = 0; i < header.size(); ++i) {
            md.append("--- | ");
        }
        md.append('\n');
        for (int r = 1; r < rows.size(); ++r) {
            md.append("| ").append(String.join((CharSequence)" | ", (Iterable)rows.get(r))).append(" |\n");
        }
        md.append('\n');
        return Optional.of(md.toString());
    }
}

