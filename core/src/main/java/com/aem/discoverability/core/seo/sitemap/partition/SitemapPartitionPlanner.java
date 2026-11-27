package com.aem.discoverability.core.seo.sitemap.partition;

import com.aem.discoverability.core.seo.sitemap.api.CanonicalUrlService;
import com.aem.discoverability.core.seo.sitemap.api.LanguageRelationshipService;
import com.aem.discoverability.core.seo.sitemap.api.PageDiscoveryService;
import com.aem.discoverability.core.seo.sitemap.api.PageEligibilityService;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.seo.sitemap.models.SitemapEntry;
import com.aem.discoverability.core.seo.sitemap.models.SitemapPartitionPlan;
import com.aem.discoverability.core.seo.sitemap.writers.SitemapXmlWriter;
import com.day.cq.wcm.api.Page;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service={SitemapPartitionPlanner.class})
public class SitemapPartitionPlanner {
    public static final int HARD_MAX_URLS = 50000;
    public static final long HARD_MAX_BYTES = 0x3200000L;
    @Reference
    private PageDiscoveryService pageDiscoveryService;
    @Reference
    private PageEligibilityService pageEligibilityService;
    @Reference
    private CanonicalUrlService canonicalUrlService;
    @Reference
    private LanguageRelationshipService languageRelationshipService;

    public SitemapPartitionPlan plan(ResourceResolver resolver, ResolvedConfiguration configuration) {
        List<SitemapEntry> entries = this.collect(resolver, configuration);
        entries.sort(Comparator.comparing(SitemapEntry::getPath));
        int maxUrls = SitemapPartitionPlanner.effectiveMaxUrls(configuration, entries);
        return new SitemapPartitionPlan(entries, maxUrls);
    }

    private List<SitemapEntry> collect(ResourceResolver resolver, ResolvedConfiguration configuration) {
        ArrayList<SitemapEntry> entries = new ArrayList<SitemapEntry>();
        this.pageDiscoveryService.discover(resolver, configuration, batch -> {
            for (Page page : batch) {
                if (!this.pageEligibilityService.evaluate(page, configuration).isEligible()) continue;
                this.canonicalUrlService.resolve(page, configuration).ifPresent(loc -> {
                    Calendar cal;
                    Instant lastmod = null;
                    if (configuration.isSitemapIncludeLastModified() && (cal = page.getLastModified()) != null) {
                        lastmod = cal.toInstant();
                    }
                    List alternates = configuration.isSitemapIncludeHreflang() ? this.languageRelationshipService.resolveReciprocalAlternates(page, configuration) : List.of();
                    entries.add(new SitemapEntry(page.getPath(), (String)loc, lastmod, alternates));
                });
            }
            return true;
        });
        return entries;
    }

    static int effectiveMaxUrls(ResolvedConfiguration configuration, List<SitemapEntry> entries) {
        int configured = Math.min(50000, Math.max(1, configuration.getSitemapMaxUrlsPerFile()));
        long maxBytes = Math.min(0x3200000L, Math.max(1L, configuration.getSitemapMaxBytesPerFile()));
        if (entries.isEmpty()) {
            return configured;
        }
        long sampleBytes = SitemapPartitionPlanner.estimateBytes(entries.subList(0, Math.min(entries.size(), 20)), configuration.isSitemapIncludeLastModified(), configuration.isSitemapIncludeHreflang());
        long avg = Math.max(1L, sampleBytes / (long)Math.min(entries.size(), 20));
        int byBytes = (int)Math.max(1L, maxBytes / avg);
        return Math.min(configured, byBytes);
    }

    static long estimateBytes(List<SitemapEntry> sample, boolean includeLastmod, boolean includeHreflang) {
        String xml = SitemapXmlWriter.writeUrlset(sample, includeLastmod, includeHreflang);
        return xml.getBytes(StandardCharsets.UTF_8).length;
    }
}

