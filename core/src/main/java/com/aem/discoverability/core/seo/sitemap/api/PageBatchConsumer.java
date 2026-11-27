package com.aem.discoverability.core.seo.sitemap.api;

import java.util.List;

import com.day.cq.wcm.api.Page;

/**
 * Receives discovery batches. Return {@code false} to stop further discovery.
 */
@FunctionalInterface
public interface PageBatchConsumer {

    boolean accept(List<Page> batch);
}
