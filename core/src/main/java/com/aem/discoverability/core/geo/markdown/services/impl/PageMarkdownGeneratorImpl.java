package com.aem.discoverability.core.geo.markdown.services.impl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.discoverability.core.geo.markdown.extractors.ComponentExtractor;
import com.aem.discoverability.core.geo.markdown.extractors.ComponentExtractorRegistry;
import com.aem.discoverability.core.geo.markdown.models.MarkdownContext;
import com.aem.discoverability.core.geo.markdown.models.MarkdownContextImpl;
import com.aem.discoverability.core.geo.markdown.services.PageMarkdownGenerator;
import com.aem.discoverability.core.geo.markdown.util.ExtractorSupport;
import com.aem.discoverability.core.seo.robots.api.DomainPolicy;
import com.aem.discoverability.core.seo.sitemap.api.CanonicalUrlService;
import com.aem.discoverability.core.seo.sitemap.api.LanguageRelationshipService;
import com.aem.discoverability.core.seo.sitemap.api.PageEligibilityService;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.day.cq.wcm.api.Page;

@Component(service = PageMarkdownGenerator.class)
public class PageMarkdownGeneratorImpl implements PageMarkdownGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(PageMarkdownGeneratorImpl.class);
    private static final DateTimeFormatter PUBLISHED_FMT =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC);

    @Reference
    private PageEligibilityService pageEligibilityService;

    @Reference
    private ComponentExtractorRegistry extractorRegistry;

    @Reference
    private DomainPolicy domainPolicy;

    @Reference
    private CanonicalUrlService canonicalUrlService;

    @Reference
    private LanguageRelationshipService languageRelationshipService;

    @Override
    public Optional<String> generate(Page page, ResolvedConfiguration configuration) {
        if (page == null || configuration == null || !configuration.isMarkdownEnabled()) {
            return Optional.empty();
        }
        if (!pageEligibilityService.evaluate(page, configuration).isEligible()) {
            return Optional.empty();
        }
        Resource content = page.getContentResource();
        if (content == null) {
            return Optional.empty();
        }
        long deadline = System.currentTimeMillis() + Math.max(1L, configuration.getTimeoutMs());
        MarkdownContextImpl context = new MarkdownContextImpl(
                configuration, content.getResourceResolver(), domainPolicy, deadline);
        StringBuilder md = new StringBuilder();
        String pageTitle = StringUtils.defaultIfBlank(page.getTitle(), page.getName());
        md.append(ExtractorSupport.heading(1, pageTitle));
        appendPageMeta(md, page, configuration);
        walk(content, context, 0, md);
        if (context.timeExceeded()) {
            LOG.warn("Markdown generation timed out for {}", page.getPath());
            return Optional.empty();
        }
        String body = md.toString().trim() + "\n";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        if ((long) bytes.length > configuration.getMaxOutputBytes()) {
            LOG.warn("Markdown output exceeds max bytes for {}", page.getPath());
            return Optional.empty();
        }
        return Optional.of(body);
    }

    private void appendPageMeta(StringBuilder md, Page page, ResolvedConfiguration configuration) {
        canonicalUrlService.resolve(page, configuration).ifPresent(url ->
                md.append("Canonical: ").append(url).append("\n"));
        md.append("Path: ").append(page.getPath()).append("\n");

        String locale = languageRelationshipService.resolveLocale(page, configuration)
                .orElseGet(() -> localeFromPage(page));
        if (StringUtils.isNotBlank(locale)) {
            md.append("Locale: ").append(locale).append("\n");
        }

        lastPublished(page).ifPresent(published ->
                md.append("Last published: ").append(published).append("\n"));
        md.append("\n");
    }

    private static String localeFromPage(Page page) {
        Locale language = page.getLanguage(false);
        if (language != null && StringUtils.isNotBlank(language.toLanguageTag())) {
            return language.toLanguageTag();
        }
        ValueMap props = page.getProperties();
        return StringUtils.trimToEmpty(props.get("jcr:language", ""));
    }

    private static Optional<String> lastPublished(Page page) {
        Calendar replicated = page.getProperties().get("cq:lastReplicated", Calendar.class);
        if (replicated == null) {
            return Optional.empty();
        }
        return Optional.of(PUBLISHED_FMT.format(Instant.ofEpochMilli(replicated.getTimeInMillis())));
    }

    private void walk(Resource resource, MarkdownContext context, int depth, StringBuilder md) {
        if (context.timeExceeded()) {
            return;
        }
        for (Resource child : resource.getChildren()) {
            if (!context.acceptComponent(depth)) {
                return;
            }
            String type = child.getResourceType();
            if (StringUtils.isBlank(type) || "cq/Page".equals(type)) {
                walk(child, context, depth + 1, md);
                continue;
            }
            Optional<ComponentExtractor> extractor = extractorRegistry.select(type);
            if (extractor.isPresent()) {
                extractor.get().extract(child, context).ifPresent(md::append);
            } else {
                context.skipped(type, "unsupported or ambiguous");
            }
            walk(child, context, depth + 1, md);
        }
    }
}
