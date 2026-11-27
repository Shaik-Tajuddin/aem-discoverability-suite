package com.aem.discoverability.core.seo.sitemap.services.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.discoverability.core.seo.sitemap.api.PageBatchConsumer;
import com.aem.discoverability.core.seo.sitemap.api.PageDiscoveryService;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/**
 * Depth-limited BFS discovery that emits pages in bounded batches.
 */
@Component(service = PageDiscoveryService.class)
public class PageDiscoveryServiceImpl implements PageDiscoveryService {

    private static final Logger LOG = LoggerFactory.getLogger(PageDiscoveryServiceImpl.class);

    @Override
    public void discover(ResourceResolver resolver, ResolvedConfiguration configuration,
            PageBatchConsumer consumer) {
        if (resolver == null || configuration == null || consumer == null) {
            return;
        }

        String rootPath = StringUtils.trimToEmpty(configuration.getProjectRoot());
        if (rootPath.isEmpty()) {
            return;
        }

        PageManager pageManager = resolver.adaptTo(PageManager.class);
        if (pageManager == null) {
            LOG.warn("PageManager unavailable; discovery skipped for {}", rootPath);
            return;
        }

        Page rootPage = pageManager.getPage(rootPath);
        if (rootPage == null) {
            Resource rootResource = resolver.getResource(rootPath);
            if (rootResource == null) {
                return;
            }
            // Non-page project root: discover descendant pages only.
            walkFromResource(rootResource, pageManager, configuration, consumer);
            return;
        }

        walkFromPage(rootPage, pageManager, configuration, consumer);
    }

    private void walkFromPage(Page rootPage, PageManager pageManager, ResolvedConfiguration configuration,
            PageBatchConsumer consumer) {
        int maxDepth = Math.max(0, configuration.getMaxDepth());
        int batchSize = Math.max(1, configuration.getBatchSize());
        String rootPath = ProjectResolverImpl.normalizeRoot(configuration.getProjectRoot());

        Deque<Node> queue = new ArrayDeque<>();
        queue.add(new Node(rootPage, 0));
        List<Page> batch = new ArrayList<>(batchSize);

        while (!queue.isEmpty()) {
            Node node = queue.removeFirst();
            Page page = node.page;
            if (!ProjectResolverImpl.isUnder(page.getPath(), rootPath)) {
                continue;
            }

            batch.add(page);
            if (batch.size() >= batchSize) {
                if (!flush(batch, consumer)) {
                    return;
                }
            }

            if (node.depth >= maxDepth) {
                continue;
            }
            page.listChildren(null, false).forEachRemaining(child -> {
                if (ProjectResolverImpl.isUnder(child.getPath(), rootPath)) {
                    queue.addLast(new Node(child, node.depth + 1));
                }
            });
        }

        if (!batch.isEmpty()) {
            flush(batch, consumer);
        }
    }

    private void walkFromResource(Resource rootResource, PageManager pageManager,
            ResolvedConfiguration configuration, PageBatchConsumer consumer) {
        int maxDepth = Math.max(0, configuration.getMaxDepth());
        int batchSize = Math.max(1, configuration.getBatchSize());
        String rootPath = ProjectResolverImpl.normalizeRoot(configuration.getProjectRoot());

        Deque<ResourceNode> queue = new ArrayDeque<>();
        queue.add(new ResourceNode(rootResource, 0));
        List<Page> batch = new ArrayList<>(batchSize);

        while (!queue.isEmpty()) {
            ResourceNode node = queue.removeFirst();
            Resource resource = node.resource;
            if (!ProjectResolverImpl.isUnder(resource.getPath(), rootPath)) {
                continue;
            }

            Page page = pageManager.getContainingPage(resource);
            if (page != null && page.getPath().equals(resource.getPath())) {
                batch.add(page);
                if (batch.size() >= batchSize) {
                    if (!flush(batch, consumer)) {
                        return;
                    }
                }
            }

            if (node.depth >= maxDepth) {
                continue;
            }
            for (Resource child : resource.getChildren()) {
                if (ProjectResolverImpl.isUnder(child.getPath(), rootPath)) {
                    queue.addLast(new ResourceNode(child, node.depth + 1));
                }
            }
        }

        if (!batch.isEmpty()) {
            flush(batch, consumer);
        }
    }

    private static boolean flush(List<Page> batch, PageBatchConsumer consumer) {
        boolean cont = consumer.accept(List.copyOf(batch));
        batch.clear();
        return cont;
    }

    private static final class Node {
        private final Page page;
        private final int depth;

        private Node(Page page, int depth) {
            this.page = page;
            this.depth = depth;
        }
    }

    private static final class ResourceNode {
        private final Resource resource;
        private final int depth;

        private ResourceNode(Resource resource, int depth) {
            this.resource = resource;
            this.depth = depth;
        }
    }
}
