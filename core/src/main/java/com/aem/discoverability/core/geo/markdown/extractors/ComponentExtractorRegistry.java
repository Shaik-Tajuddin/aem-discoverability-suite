package com.aem.discoverability.core.geo.markdown.extractors;

import com.aem.discoverability.core.geo.markdown.extractors.ComponentExtractor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(service={ComponentExtractorRegistry.class})
public class ComponentExtractorRegistry {
    private final List<ComponentExtractor> extractors = new ArrayList<ComponentExtractor>();

    @Reference(service=ComponentExtractor.class, cardinality=ReferenceCardinality.MULTIPLE, policy=ReferencePolicy.DYNAMIC, bind="bindExtractor", unbind="unbindExtractor")
    public synchronized void bindExtractor(ComponentExtractor extractor) {
        this.extractors.add(extractor);
    }

    protected synchronized void unbindExtractor(ComponentExtractor extractor) {
        this.extractors.remove(extractor);
    }

    public synchronized Optional<ComponentExtractor> select(String resourceType) {
        ArrayList<ComponentExtractor> matches = new ArrayList<ComponentExtractor>();
        for (ComponentExtractor extractor : this.extractors) {
            if (!extractor.supports(resourceType)) continue;
            matches.add(extractor);
        }
        if (matches.isEmpty()) {
            return Optional.empty();
        }
        matches.sort(Comparator.comparingInt(ComponentExtractor::rank));
        int best = ((ComponentExtractor)matches.get(0)).rank();
        long bestCount = matches.stream().filter(e -> e.rank() == best).count();
        if (bestCount != 1L) {
            return Optional.empty();
        }
        return Optional.of((ComponentExtractor)matches.get(0));
    }
}

