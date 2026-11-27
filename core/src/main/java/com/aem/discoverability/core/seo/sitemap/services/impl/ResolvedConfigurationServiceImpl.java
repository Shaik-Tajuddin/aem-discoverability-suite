package com.aem.discoverability.core.seo.sitemap.services.impl;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.aem.discoverability.core.geo.llm.configs.LlmsConfig;
import com.aem.discoverability.core.geo.markdown.configs.MarkdownConfig;
import com.aem.discoverability.core.seo.robots.api.DomainPolicy;
import com.aem.discoverability.core.seo.robots.api.EnvironmentPolicy;
import com.aem.discoverability.core.seo.robots.configs.RobotsConfig;
import com.aem.discoverability.core.seo.sitemap.api.ProjectResolution;
import com.aem.discoverability.core.seo.sitemap.api.ProjectResolver;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfigurationService;
import com.aem.discoverability.core.seo.sitemap.configs.SitemapConfig;

/**
 * Assembles an immutable configuration snapshot after fail-closed project resolution.
 */
@Component(service = ResolvedConfigurationService.class)
public class ResolvedConfigurationServiceImpl implements ResolvedConfigurationService {

    @Reference
    private ProjectResolver projectResolver;

    @Reference
    private EnvironmentPolicy environmentPolicy;

    @Reference
    private DomainPolicy domainPolicy;

    @Override
    public Optional<ResolvedConfiguration> resolve(Resource resource) {
        ProjectResolution resolution = projectResolver.resolve(resource);
        if (!resolution.isResolved()) {
            return Optional.empty();
        }

        ConfigurationBuilder builder = resource.adaptTo(ConfigurationBuilder.class);
        if (builder == null) {
            return Optional.empty();
        }

        RobotsConfig robots = builder.as(RobotsConfig.class);
        SitemapConfig sitemap = builder.as(SitemapConfig.class);
        MarkdownConfig markdown = builder.as(MarkdownConfig.class);
        LlmsConfig llms = builder.as(LlmsConfig.class);

        boolean anyFeature = robots.enabled() || sitemap.enabled() || markdown.enabled() || llms.enabled();
        String publicDomain = firstPublicDomain(robots, sitemap, markdown, llms);

        int batchSize = sitemap.enabled() ? sitemap.batchSize()
                : llms.enabled() ? llms.batchSize() : 100;
        int maxDepth = sitemap.enabled() ? sitemap.maxDepth()
                : llms.enabled() ? llms.maxDepth() : 20;

        ResolvedConfiguration snapshot = ResolvedConfiguration.builder()
                .projectRoot(resolution.getProjectRoot().orElse(""))
                .publicDomain(publicDomain)
                .enabled(anyFeature)
                .robotsEnabled(robots.enabled())
                .sitemapEnabled(sitemap.enabled())
                .markdownEnabled(markdown.enabled())
                .llmsEnabled(llms.enabled())
                .jsonLdEnabled(false)
                .languageRoots(sitemap.languageRoots())
                .locales(sitemap.locales())
                .sameRelativePathEnabled(sitemap.sameRelativePathEnabled())
                .explicitMappings(sitemap.explicitMappings())
                .xDefaultPath(sitemap.xDefaultPath())
                .batchSize(batchSize)
                .maxDepth(maxDepth)
                .timeoutMs(markdown.timeoutMs())
                .maxOutputBytes(markdown.maxOutputBytes())
                .productionEnvironment(environmentPolicy.isProduction())
                .robotsUserAgents(robots.userAgents())
                .robotsAllowRules(robots.allowRules())
                .robotsDisallowRules(robots.disallowRules())
                .robotsManualSitemapUrls(robots.manualSitemapUrls())
                .robotsCacheMaxAgeSeconds(robots.cacheMaxAgeSeconds())
                .sitemapMaxUrlsPerFile(sitemap.maxUrlsPerFile())
                .sitemapMaxBytesPerFile(sitemap.maxBytesPerFile())
                .sitemapIncludeLastModified(sitemap.includeLastModified())
                .sitemapIncludeHreflang(sitemap.includeHreflang())
                .sitemapCacheMaxAgeSeconds(sitemap.cacheMaxAgeSeconds())
                .markdownMaxComponentDepth(markdown.maxComponentDepth())
                .markdownMaxComponents(markdown.maxComponents())
                .markdownMaxReferences(markdown.maxReferences())
                .markdownCacheMaxAgeSeconds(markdown.cacheMaxAgeSeconds())
                .llmsSiteName(llms.siteName())
                .llmsSummary(llms.summary())
                .llmsContextText(llms.contextText())
                .llmsSections(llms.sections())
                .llmsIncludePaths(llms.includePaths())
                .llmsExcludePaths(llms.excludePaths())
                .llmsMaxLinks(llms.maxLinks())
                .llmsMaxOutputBytes(llms.maxOutputBytes())
                .llmsCacheMaxAgeSeconds(llms.cacheMaxAgeSeconds())
                .build();

        if (snapshot.isEnabled() && !domainPolicy.publicOrigin(snapshot).isPresent()) {
            return Optional.empty();
        }

        return Optional.of(snapshot);
    }

    private static String firstPublicDomain(RobotsConfig robots, SitemapConfig sitemap,
            MarkdownConfig markdown, LlmsConfig llms) {
        if (robots.enabled() && StringUtils.isNotBlank(robots.publicDomain())) {
            return robots.publicDomain().trim();
        }
        if (sitemap.enabled() && StringUtils.isNotBlank(sitemap.publicDomain())) {
            return sitemap.publicDomain().trim();
        }
        if (markdown.enabled() && StringUtils.isNotBlank(markdown.publicDomain())) {
            return markdown.publicDomain().trim();
        }
        if (llms.enabled() && StringUtils.isNotBlank(llms.publicDomain())) {
            return llms.publicDomain().trim();
        }
        for (String candidate : new String[] {
                robots.publicDomain(), sitemap.publicDomain(),
                markdown.publicDomain(), llms.publicDomain() }) {
            String trimmed = StringUtils.trimToEmpty(candidate);
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return "";
    }
}
