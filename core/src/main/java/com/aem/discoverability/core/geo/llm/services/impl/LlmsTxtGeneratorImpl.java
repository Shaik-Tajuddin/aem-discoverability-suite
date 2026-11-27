package com.aem.discoverability.core.geo.llm.services.impl;

import com.aem.discoverability.core.geo.llm.services.LlmsTxtGenerator;
import com.aem.discoverability.core.seo.sitemap.api.CanonicalUrlService;
import com.aem.discoverability.core.seo.sitemap.api.PageDiscoveryService;
import com.aem.discoverability.core.seo.sitemap.api.PageEligibilityService;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.aem.discoverability.core.seo.sitemap.services.impl.ProjectResolverImpl;
import com.day.cq.wcm.api.Page;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service={LlmsTxtGenerator.class})
public class LlmsTxtGeneratorImpl
implements LlmsTxtGenerator {
    static final String DEFAULT_SECTION = "Pages";
    @Reference
    private PageDiscoveryService pageDiscoveryService;
    @Reference
    private PageEligibilityService pageEligibilityService;
    @Reference
    private CanonicalUrlService canonicalUrlService;

    @Override
    public Optional<String> generate(ResourceResolver resolver, ResolvedConfiguration configuration) {
        int maxBytes;
        String body;
        byte[] bytes;
        if (resolver == null || configuration == null || !configuration.isLlmsEnabled()) {
            return Optional.empty();
        }
        List<SectionDef> sectionDefs = LlmsTxtGeneratorImpl.parseSections(configuration.getLlmsSections());
        List<LinkEntry> links = this.collect(resolver, configuration, sectionDefs);
        links.sort(Comparator.comparing(LinkEntry::sectionOrder).thenComparing(LinkEntry::path));
        int maxLinks = configuration.getLlmsMaxLinks();
        if (links.size() > maxLinks) {
            links = links.subList(0, maxLinks);
        }
        if ((bytes = (body = LlmsTxtGeneratorImpl.render(configuration, sectionDefs, links)).getBytes(StandardCharsets.UTF_8)).length > (maxBytes = configuration.getLlmsMaxOutputBytes())) {
            return Optional.empty();
        }
        return Optional.of(body);
    }

    private List<LinkEntry> collect(ResourceResolver resolver, ResolvedConfiguration configuration, List<SectionDef> sectionDefs) {
        ArrayList<LinkEntry> links = new ArrayList<LinkEntry>();
        List includes = configuration.getLlmsIncludePaths();
        List excludes = configuration.getLlmsExcludePaths();
        String root = ProjectResolverImpl.normalizeRoot((String)configuration.getProjectRoot());
        this.pageDiscoveryService.discover(resolver, configuration, batch -> {
            for (Page page : batch) {
                Optional loc;
                String path;
                if (!this.pageEligibilityService.evaluate(page, configuration).isEligible() || !LlmsTxtGeneratorImpl.matchesAnyPrefix(path = page.getPath(), includes) && !includes.isEmpty() || LlmsTxtGeneratorImpl.matchesAnyPrefix(path, excludes) || !ProjectResolverImpl.isUnder((String)path, (String)root) || (loc = this.canonicalUrlService.resolve(page, configuration)).isEmpty()) continue;
                SectionPick pick = LlmsTxtGeneratorImpl.resolveSection(page, sectionDefs);
                String title = StringUtils.trimToEmpty((String)page.getTitle());
                if (title.isEmpty()) {
                    title = page.getName();
                }
                String note = StringUtils.trimToEmpty((String)page.getDescription());
                links.add(new LinkEntry(pick.name, pick.order, path, title, (String)loc.get(), note));
            }
            return true;
        });
        return links;
    }

    private static String render(ResolvedConfiguration configuration, List<SectionDef> sectionDefs, List<LinkEntry> links) {
        String context;
        String siteName = StringUtils.trimToEmpty((String)configuration.getLlmsSiteName());
        if (siteName.isEmpty()) {
            String root = configuration.getProjectRoot();
            int slash = root.lastIndexOf(47);
            String string = siteName = slash >= 0 && slash < root.length() - 1 ? root.substring(slash + 1) : root;
            if (siteName.isEmpty()) {
                siteName = "Site";
            }
        }
        StringBuilder out = new StringBuilder();
        out.append("# ").append(LlmsTxtGeneratorImpl.escapeInline(siteName)).append("\n\n");
        String summary = StringUtils.trimToEmpty((String)configuration.getLlmsSummary());
        if (!summary.isEmpty()) {
            for (String string : summary.split("\\R")) {
                out.append("> ").append(LlmsTxtGeneratorImpl.escapeInline(string.trim())).append('\n');
            }
            out.append('\n');
        }
        if (!(context = StringUtils.trimToEmpty((String)configuration.getLlmsContextText())).isEmpty()) {
            out.append(LlmsTxtGeneratorImpl.escapeInline(context)).append("\n\n");
        }
        LinkedHashMap<String, List> bySection = new LinkedHashMap<String, List>();
        if (sectionDefs.isEmpty()) {
            bySection.put(DEFAULT_SECTION, new ArrayList());
        } else {
            for (SectionDef sectionDef : sectionDefs) {
                bySection.put(sectionDef.name, new ArrayList());
            }
            bySection.put(DEFAULT_SECTION, new ArrayList());
        }
        for (LinkEntry linkEntry : links) {
            bySection.computeIfAbsent(linkEntry.sectionName, k -> new ArrayList()).add(linkEntry);
        }
        for (Map.Entry entry : bySection.entrySet()) {
            if (((List)entry.getValue()).isEmpty()) continue;
            out.append("## ").append(LlmsTxtGeneratorImpl.escapeInline((String)entry.getKey())).append("\n\n");
            for (LinkEntry link : (List<LinkEntry>) entry.getValue()) {
                out.append("- [").append(LlmsTxtGeneratorImpl.escapeInline(link.title)).append("](").append(link.canonicalUrl).append(')');
                if (!link.note.isEmpty()) {
                    out.append(": ").append(LlmsTxtGeneratorImpl.escapeInline(link.note));
                }
                out.append('\n');
            }
            out.append('\n');
        }
        return out.toString().trim() + "\n";
    }

    static List<SectionDef> parseSections(List<String> raw) {
        ArrayList<SectionDef> defs = new ArrayList<SectionDef>();
        if (raw == null) {
            return defs;
        }
        int order = 0;
        for (String entry : raw) {
            int bar;
            String value = StringUtils.trimToEmpty((String)entry);
            if (value.isEmpty() || (bar = value.indexOf(124)) <= 0 || bar >= value.length() - 1) continue;
            String name = value.substring(0, bar).trim();
            String prefix = ProjectResolverImpl.normalizeRoot((String)value.substring(bar + 1).trim());
            if (name.isEmpty() || prefix.isEmpty()) continue;
            defs.add(new SectionDef(name, prefix, order++));
        }
        return defs;
    }

    static SectionPick resolveSection(Page page, List<SectionDef> sectionDefs) {
        String override = StringUtils.trimToEmpty((String)((String)page.getProperties().get("discoverabilityLlmsSection", (Object)"")));
        if (!override.isEmpty()) {
            for (SectionDef def : sectionDefs) {
                if (!def.name.equals(override)) continue;
                return new SectionPick(def.name, def.order);
            }
            return new SectionPick(override, 0x7FFFFFFE);
        }
        String path = page.getPath();
        for (SectionDef def : sectionDefs) {
            if (!ProjectResolverImpl.isUnder((String)path, (String)def.prefix) && !path.equals(def.prefix)) continue;
            return new SectionPick(def.name, def.order);
        }
        return new SectionPick(DEFAULT_SECTION, Integer.MAX_VALUE);
    }

    static boolean matchesAnyPrefix(String path, List<String> prefixes) {
        if (prefixes == null || prefixes.isEmpty()) {
            return false;
        }
        for (String raw : prefixes) {
            String prefix = ProjectResolverImpl.normalizeRoot((String)StringUtils.trimToEmpty((String)raw));
            if (prefix.isEmpty() || !path.equals(prefix) && !ProjectResolverImpl.isUnder((String)path, (String)prefix)) continue;
            return true;
        }
        return false;
    }

    static String escapeInline(String text) {
        return text.replace("\r", " ").replace("\n", " ").trim();
    }

    static final class SectionDef {
        final String name;
        final String prefix;
        final int order;

        SectionDef(String name, String prefix, int order) {
            this.name = name;
            this.prefix = prefix;
            this.order = order;
        }
    }

    static final class LinkEntry {
        final String sectionName;
        final int sectionOrder;
        final String path;
        final String title;
        final String canonicalUrl;
        final String note;

        LinkEntry(String sectionName, int sectionOrder, String path, String title, String canonicalUrl, String note) {
            this.sectionName = sectionName;
            this.sectionOrder = sectionOrder;
            this.path = path;
            this.title = title;
            this.canonicalUrl = canonicalUrl;
            this.note = note;
        }

        int sectionOrder() {
            return this.sectionOrder;
        }

        String path() {
            return this.path;
        }
    }

    static final class SectionPick {
        final String name;
        final int order;

        SectionPick(String name, int order) {
            this.name = name;
            this.order = order;
        }
    }
}

