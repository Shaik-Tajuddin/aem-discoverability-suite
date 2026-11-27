package com.aem.discoverability.core.seo.sitemap.services.impl;

import com.aem.discoverability.core.seo.robots.api.DomainPolicy;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.seo.sitemap.models.SitemapEntry;
import com.aem.discoverability.core.seo.sitemap.models.SitemapPartitionPlan;
import com.aem.discoverability.core.seo.sitemap.partition.SitemapPartitionPlanner;
import com.aem.discoverability.core.seo.sitemap.services.SitemapGenerator;
import com.aem.discoverability.core.seo.sitemap.writers.SitemapXmlWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service={SitemapGenerator.class})
public class SitemapGeneratorImpl
implements SitemapGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(SitemapGeneratorImpl.class);
    @Reference
    private SitemapPartitionPlanner partitionPlanner;
    @Reference
    private DomainPolicy domainPolicy;

    @Override
    public Optional<String> generateIndex(ResourceResolver resolver, ResolvedConfiguration configuration) {
        if (!SitemapGeneratorImpl.isEnabled(configuration)) {
            return Optional.empty();
        }
        SitemapPartitionPlan plan = this.partitionPlanner.plan(resolver, configuration);
        if (plan.getPartitionCount() == 0) {
            return Optional.of(SitemapXmlWriter.writeUrlset(List.of(), configuration.isSitemapIncludeLastModified(), configuration.isSitemapIncludeHreflang()));
        }
        if (plan.getPartitionCount() == 1) {
            return this.finalizeXml(SitemapXmlWriter.writeUrlset(plan.partition(0), configuration.isSitemapIncludeLastModified(), configuration.isSitemapIncludeHreflang()), configuration);
        }
        Optional origin = this.domainPolicy.publicOrigin(configuration);
        if (origin.isEmpty()) {
            return Optional.empty();
        }
        ArrayList<String> locs = new ArrayList<String>(plan.getPartitionCount());
        for (int i = 0; i < plan.getPartitionCount(); ++i) {
            locs.add((String)origin.get() + "/sitemap-" + i + ".xml");
        }
        return this.finalizeXml(SitemapXmlWriter.writeIndex(locs), configuration);
    }

    @Override
    public Optional<String> generatePartition(ResourceResolver resolver, ResolvedConfiguration configuration, int partitionIndex) {
        if (!SitemapGeneratorImpl.isEnabled(configuration) || partitionIndex < 0) {
            return Optional.empty();
        }
        SitemapPartitionPlan plan = this.partitionPlanner.plan(resolver, configuration);
        if (partitionIndex >= plan.getPartitionCount()) {
            return Optional.empty();
        }
        List<SitemapEntry> entries = plan.partition(partitionIndex);
        return this.finalizeXml(SitemapXmlWriter.writeUrlset(entries, configuration.isSitemapIncludeLastModified(), configuration.isSitemapIncludeHreflang()), configuration);
    }

    private Optional<String> finalizeXml(String xml, ResolvedConfiguration configuration) {
        long maxBytes = Math.min(0x3200000L, configuration.getSitemapMaxBytesPerFile());
        byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
        if ((long)bytes.length > maxBytes) {
            LOG.error("Sitemap XML exceeds max bytes ({} > {}); refusing truncated response", (Object)bytes.length, (Object)maxBytes);
            return Optional.empty();
        }
        return Optional.of(xml);
    }

    private static boolean isEnabled(ResolvedConfiguration configuration) {
        return configuration != null && configuration.isSitemapEnabled();
    }
}

