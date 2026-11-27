package com.aem.discoverability.core.seo.sitemap.services.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.discoverability.core.seo.sitemap.api.CanonicalUrlService;
import com.aem.discoverability.core.seo.sitemap.api.LanguageAlternate;
import com.aem.discoverability.core.seo.sitemap.api.LanguageRelationshipService;
import com.aem.discoverability.core.seo.sitemap.api.PageEligibilityService;
import com.aem.discoverability.core.seo.sitemap.api.ResolvedConfiguration;
import com.day.cq.wcm.api.LanguageManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/**
 * Resolves language alternates: LanguageManager/MSM → explicit map → same-relative-path.
 */
@Component(service = LanguageRelationshipService.class)
public class LanguageRelationshipServiceImpl implements LanguageRelationshipService {

    private static final Logger LOG = LoggerFactory.getLogger(LanguageRelationshipServiceImpl.class);
    private static final Pattern LOCALE_PATTERN =
            Pattern.compile("^[A-Za-z]{2,3}(-[A-Za-z0-9]{2,8})*$");

    @Reference
    private PageEligibilityService pageEligibilityService;

    @Reference
    private CanonicalUrlService canonicalUrlService;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
    private LanguageManager languageManager;

    @Override
    public List<LanguageAlternate> resolveAlternates(Page page, ResolvedConfiguration configuration) {
        if (page == null || configuration == null) {
            return List.of();
        }

        Set<String> candidatePaths = new LinkedHashSet<>();
        candidatePaths.add(page.getPath());
        addLanguageManagerCandidates(page, candidatePaths);
        addExplicitMappingCandidates(page.getPath(), configuration, candidatePaths);
        addSameRelativePathCandidates(page.getPath(), configuration, candidatePaths);

        Map<String, LanguageAlternate> byLocale = new LinkedHashMap<>();
        PageManager pageManager = page.getContentResource() != null
                ? page.getContentResource().getResourceResolver().adaptTo(PageManager.class)
                : null;
        if (pageManager == null) {
            return List.of();
        }

        for (String path : candidatePaths) {
            Page target = pageManager.getPage(path);
            if (target == null) {
                continue;
            }
            toAlternate(target, configuration).ifPresent(alt -> {
                // First wins per hreflang; resolution order already encoded in candidatePaths.
                byLocale.putIfAbsent(alt.getHreflang(), alt);
            });
        }
        return List.copyOf(byLocale.values());
    }

    @Override
    public List<LanguageAlternate> resolveReciprocalAlternates(Page page,
            ResolvedConfiguration configuration) {
        List<LanguageAlternate> raw = resolveAlternates(page, configuration);
        if (raw.isEmpty() || page.getContentResource() == null) {
            return List.of();
        }

        PageManager pageManager = page.getContentResource().getResourceResolver().adaptTo(PageManager.class);
        if (pageManager == null) {
            return List.of();
        }

        String sourcePath = page.getPath();
        List<LanguageAlternate> reciprocal = new ArrayList<>();
        for (LanguageAlternate alternate : raw) {
            if (alternate.getPath().equals(sourcePath)) {
                reciprocal.add(alternate);
                continue;
            }
            Page other = pageManager.getPage(alternate.getPath());
            if (other == null) {
                continue;
            }
            boolean returns = resolveAlternates(other, configuration).stream()
                    .anyMatch(a -> sourcePath.equals(a.getPath()));
            if (returns) {
                reciprocal.add(alternate);
            }
        }

        resolveXDefault(pageManager, configuration).ifPresent(reciprocal::add);
        return List.copyOf(reciprocal);
    }

    @Override
    public Optional<String> resolveLocale(Page page, ResolvedConfiguration configuration) {
        if (page == null || configuration == null) {
            return Optional.empty();
        }
        return localeForPath(page.getPath(), configuration);
    }

    private void addLanguageManagerCandidates(Page page, Set<String> candidatePaths) {
        if (languageManager == null || page.getContentResource() == null) {
            return;
        }
        try {
            ResourceResolver resolver = page.getContentResource().getResourceResolver();
            Map<Locale, LanguageManager.Info> adjacent =
                    languageManager.getAdjacentInfo(resolver, page.getPath());
            if (adjacent == null) {
                return;
            }
            for (LanguageManager.Info info : adjacent.values()) {
                if (info != null && info.exists() && StringUtils.isNotBlank(info.getPath())) {
                    candidatePaths.add(info.getPath());
                }
            }
        } catch (RuntimeException ex) {
            LOG.debug("LanguageManager adjacent lookup failed for {}", page.getPath(), ex);
        }
    }

    private static void addExplicitMappingCandidates(String pagePath, ResolvedConfiguration configuration,
            Set<String> candidatePaths) {
        for (String mapping : configuration.getExplicitMappings()) {
            String raw = StringUtils.trimToEmpty(mapping);
            int sep = raw.indexOf('=');
            if (sep <= 0 || sep >= raw.length() - 1) {
                continue;
            }
            String left = raw.substring(0, sep).trim();
            String right = raw.substring(sep + 1).trim();
            if (pagePath.equals(left)) {
                candidatePaths.add(right);
            } else if (pagePath.equals(right)) {
                candidatePaths.add(left);
            }
        }
    }

    private static void addSameRelativePathCandidates(String pagePath, ResolvedConfiguration configuration,
            Set<String> candidatePaths) {
        if (!configuration.isSameRelativePathEnabled()) {
            return;
        }
        List<String> roots = configuration.getLanguageRoots();
        String matchedRoot = null;
        for (String root : roots) {
            String normalized = ProjectResolverImpl.normalizeRoot(root);
            if (ProjectResolverImpl.isUnder(pagePath, normalized)) {
                matchedRoot = normalized;
                break;
            }
        }
        if (matchedRoot == null) {
            return;
        }
        String relative = pagePath.equals(matchedRoot) ? "" : pagePath.substring(matchedRoot.length());
        for (String root : roots) {
            String normalized = ProjectResolverImpl.normalizeRoot(root);
            if (normalized.equals(matchedRoot)) {
                continue;
            }
            candidatePaths.add(normalized + relative);
        }
    }

    private Optional<LanguageAlternate> toAlternate(Page page, ResolvedConfiguration configuration) {
        if (!pageEligibilityService.evaluate(page, configuration).isEligible()) {
            return Optional.empty();
        }
        Optional<String> locale = localeForPath(page.getPath(), configuration);
        if (locale.isEmpty() || !isValidLocale(locale.get())) {
            return Optional.empty();
        }
        Optional<String> url = canonicalUrlService.resolve(page, configuration);
        if (url.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new LanguageAlternate(page.getPath(), locale.get(), url.get()));
    }

    private Optional<LanguageAlternate> resolveXDefault(PageManager pageManager,
            ResolvedConfiguration configuration) {
        String xDefaultPath = StringUtils.trimToEmpty(configuration.getXDefaultPath());
        if (xDefaultPath.isEmpty()) {
            return Optional.empty();
        }
        Page xDefaultPage = pageManager.getPage(xDefaultPath);
        if (xDefaultPage == null) {
            return Optional.empty();
        }
        if (!pageEligibilityService.evaluate(xDefaultPage, configuration).isEligible()) {
            return Optional.empty();
        }
        Optional<String> url = canonicalUrlService.resolve(xDefaultPage, configuration);
        return url.map(u -> new LanguageAlternate(xDefaultPath, LanguageAlternate.HREFLANG_X_DEFAULT, u));
    }

    static Optional<String> localeForPath(String path, ResolvedConfiguration configuration) {
        List<String> roots = configuration.getLanguageRoots();
        List<String> locales = configuration.getLocales();
        String bestRoot = null;
        int bestIndex = -1;
        for (int i = 0; i < roots.size(); i++) {
            String root = ProjectResolverImpl.normalizeRoot(roots.get(i));
            if (ProjectResolverImpl.isUnder(path, root)
                    && (bestRoot == null || root.length() > bestRoot.length())) {
                bestRoot = root;
                bestIndex = i;
            }
        }
        if (bestIndex < 0) {
            return Optional.empty();
        }
        if (bestIndex < locales.size() && StringUtils.isNotBlank(locales.get(bestIndex))) {
            return Optional.of(normalizeLocale(locales.get(bestIndex)));
        }
        // Fallback: last path segment of the language root (e.g. /content/site/en → en)
        int slash = bestRoot.lastIndexOf('/');
        if (slash >= 0 && slash < bestRoot.length() - 1) {
            return Optional.of(normalizeLocale(bestRoot.substring(slash + 1)));
        }
        return Optional.empty();
    }

    static boolean isValidLocale(String locale) {
        if (LanguageAlternate.HREFLANG_X_DEFAULT.equals(locale)) {
            return true;
        }
        return LOCALE_PATTERN.matcher(locale).matches();
    }

    static String normalizeLocale(String locale) {
        String trimmed = StringUtils.trimToEmpty(locale);
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        // Prefer BCP47-ish casing: en, en-US
        String[] parts = trimmed.split("-", 2);
        if (parts.length == 1) {
            return parts[0].toLowerCase(Locale.ROOT);
        }
        return parts[0].toLowerCase(Locale.ROOT) + "-" + parts[1].toUpperCase(Locale.ROOT);
    }
}
