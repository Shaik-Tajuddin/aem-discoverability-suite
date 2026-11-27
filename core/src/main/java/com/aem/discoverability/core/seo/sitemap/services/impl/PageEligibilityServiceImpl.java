package com.aem.discoverability.core.seo.sitemap.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.aem.discoverability.core.seo.sitemap.api.CanonicalUrlService;
import com.aem.discoverability.core.seo.sitemap.api.DiscoverabilityPageProperties;
import com.aem.discoverability.core.seo.sitemap.api.EligibilityResult;
import com.aem.discoverability.core.seo.sitemap.api.ExclusionReason;
import com.aem.discoverability.core.seo.sitemap.api.PageEligibilityService;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.day.cq.replication.ReplicationStatus;
import com.day.cq.wcm.api.Page;

/**
 * Shared eligibility checks in guide order; returns structured exclusion reasons.
 */
@Component(service = PageEligibilityService.class)
public class PageEligibilityServiceImpl implements PageEligibilityService {

    @Reference
    private CanonicalUrlService canonicalUrlService;

    @Override
    public EligibilityResult evaluate(Page page, ResolvedConfiguration configuration) {
        if (page == null || configuration == null) {
            return EligibilityResult.excluded("", ExclusionReason.INVALID, "page or configuration missing");
        }

        String path = page.getPath();
        String root = ProjectResolverImpl.normalizeRoot(configuration.getProjectRoot());
        if (!ProjectResolverImpl.isUnder(path, root)) {
            return EligibilityResult.excluded(path, ExclusionReason.OUTSIDE_ROOT, root);
        }

        if (page.getContentResource() == null) {
            return EligibilityResult.excluded(path, ExclusionReason.INVALID, "missing jcr:content");
        }

        if (isUnpublished(page)) {
            return EligibilityResult.excluded(path, ExclusionReason.UNPUBLISHED, null);
        }

        ValueMap props = page.getProperties();
        if (props.get(DiscoverabilityPageProperties.CUG_ENABLED, false)) {
            return EligibilityResult.excluded(path, ExclusionReason.CUG_PROTECTED, null);
        }

        if (props.get(DiscoverabilityPageProperties.EXCLUDE, false)) {
            return EligibilityResult.excluded(path, ExclusionReason.EXCLUDED, null);
        }

        if (props.get(DiscoverabilityPageProperties.NO_INDEX, false)) {
            return EligibilityResult.excluded(path, ExclusionReason.NOINDEX, null);
        }

        String redirect = StringUtils.trimToEmpty(props.get(DiscoverabilityPageProperties.REDIRECT_TARGET, ""));
        if (!redirect.isEmpty()) {
            return EligibilityResult.excluded(path, ExclusionReason.REDIRECT, redirect);
        }

        if (canonicalUrlService.resolve(page, configuration).isEmpty()) {
            return EligibilityResult.excluded(path, ExclusionReason.BAD_CANONICAL, null);
        }

        return EligibilityResult.eligible(path);
    }

    private static boolean isUnpublished(Page page) {
        String action = page.getProperties().get("cq:lastReplicationAction", "");
        if ("Deactivate".equals(action)) {
            return true;
        }
        ReplicationStatus status = page.adaptTo(ReplicationStatus.class);
        // Only fail closed when replication metadata exists and is not activated.
        return status != null
                && status.getLastReplicationAction() != null
                && !status.isActivated();
    }
}
