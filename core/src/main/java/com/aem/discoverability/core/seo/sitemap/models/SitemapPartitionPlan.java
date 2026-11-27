package com.aem.discoverability.core.seo.sitemap.models;

import com.aem.discoverability.core.seo.sitemap.models.SitemapEntry;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class SitemapPartitionPlan {
    private final List<SitemapEntry> entries;
    private final int maxUrlsPerFile;
    private final int partitionCount;

    public SitemapPartitionPlan(List<SitemapEntry> entries, int maxUrlsPerFile) {
        this.entries = Collections.unmodifiableList(Objects.requireNonNull(entries, "entries"));
        this.maxUrlsPerFile = Math.max(1, maxUrlsPerFile);
        this.partitionCount = entries.isEmpty() ? 0 : (entries.size() + this.maxUrlsPerFile - 1) / this.maxUrlsPerFile;
    }

    public List<SitemapEntry> getEntries() {
        return this.entries;
    }

    public int getPartitionCount() {
        return this.partitionCount;
    }

    public int getMaxUrlsPerFile() {
        return this.maxUrlsPerFile;
    }

    public List<SitemapEntry> partition(int index) {
        if (index < 0 || index >= this.partitionCount) {
            return List.of();
        }
        int from = index * this.maxUrlsPerFile;
        int to = Math.min(this.entries.size(), from + this.maxUrlsPerFile);
        return this.entries.subList(from, to);
    }
}

