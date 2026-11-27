package com.aem.discoverability.core.seo.sitemap.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable per-request snapshot of effective discoverability configuration.
 */
public final class ResolvedConfiguration {

    private final String projectRoot;
    private final String publicDomain;
    private final boolean enabled;
    private final boolean robotsEnabled;
    private final boolean sitemapEnabled;
    private final boolean markdownEnabled;
    private final boolean llmsEnabled;
    private final boolean jsonLdEnabled;
    private final List<String> languageRoots;
    private final List<String> locales;
    private final boolean sameRelativePathEnabled;
    private final List<String> explicitMappings;
    private final String xDefaultPath;
    private final int batchSize;
    private final int maxDepth;
    private final long timeoutMs;
    private final long maxOutputBytes;
    private final boolean productionEnvironment;
    private final List<String> robotsUserAgents;
    private final List<String> robotsAllowRules;
    private final List<String> robotsDisallowRules;
    private final List<String> robotsManualSitemapUrls;
    private final int robotsCacheMaxAgeSeconds;
    private final int sitemapMaxUrlsPerFile;
    private final long sitemapMaxBytesPerFile;
    private final boolean sitemapIncludeLastModified;
    private final boolean sitemapIncludeHreflang;
    private final int sitemapCacheMaxAgeSeconds;
    private final int markdownMaxComponentDepth;
    private final int markdownMaxComponents;
    private final int markdownMaxReferences;
    private final int markdownCacheMaxAgeSeconds;
    private final String llmsSiteName;
    private final String llmsSummary;
    private final String llmsContextText;
    private final List<String> llmsSections;
    private final List<String> llmsIncludePaths;
    private final List<String> llmsExcludePaths;
    private final int llmsMaxLinks;
    private final int llmsMaxOutputBytes;
    private final int llmsCacheMaxAgeSeconds;
    private final String jsonLdOrganizationName;
    private final String jsonLdOrganizationLogoUrl;
    private final String jsonLdWebsiteName;
    private final boolean jsonLdIncludeOrganization;
    private final boolean jsonLdIncludeWebSite;
    private final boolean jsonLdAutoWebPage;
    private final boolean jsonLdAutoBreadcrumb;

    private ResolvedConfiguration(Builder builder) {
        this.projectRoot = builder.projectRoot;
        this.publicDomain = builder.publicDomain;
        this.enabled = builder.enabled;
        this.robotsEnabled = builder.robotsEnabled;
        this.sitemapEnabled = builder.sitemapEnabled;
        this.markdownEnabled = builder.markdownEnabled;
        this.llmsEnabled = builder.llmsEnabled;
        this.jsonLdEnabled = builder.jsonLdEnabled;
        this.languageRoots = Collections.unmodifiableList(builder.languageRoots);
        this.locales = Collections.unmodifiableList(builder.locales);
        this.sameRelativePathEnabled = builder.sameRelativePathEnabled;
        this.explicitMappings = Collections.unmodifiableList(builder.explicitMappings);
        this.xDefaultPath = builder.xDefaultPath;
        this.batchSize = builder.batchSize;
        this.maxDepth = builder.maxDepth;
        this.timeoutMs = builder.timeoutMs;
        this.maxOutputBytes = builder.maxOutputBytes;
        this.productionEnvironment = builder.productionEnvironment;
        this.robotsUserAgents = Collections.unmodifiableList(builder.robotsUserAgents);
        this.robotsAllowRules = Collections.unmodifiableList(builder.robotsAllowRules);
        this.robotsDisallowRules = Collections.unmodifiableList(builder.robotsDisallowRules);
        this.robotsManualSitemapUrls = Collections.unmodifiableList(builder.robotsManualSitemapUrls);
        this.robotsCacheMaxAgeSeconds = builder.robotsCacheMaxAgeSeconds;
        this.sitemapMaxUrlsPerFile = builder.sitemapMaxUrlsPerFile;
        this.sitemapMaxBytesPerFile = builder.sitemapMaxBytesPerFile;
        this.sitemapIncludeLastModified = builder.sitemapIncludeLastModified;
        this.sitemapIncludeHreflang = builder.sitemapIncludeHreflang;
        this.sitemapCacheMaxAgeSeconds = builder.sitemapCacheMaxAgeSeconds;
        this.markdownMaxComponentDepth = builder.markdownMaxComponentDepth;
        this.markdownMaxComponents = builder.markdownMaxComponents;
        this.markdownMaxReferences = builder.markdownMaxReferences;
        this.markdownCacheMaxAgeSeconds = builder.markdownCacheMaxAgeSeconds;
        this.llmsSiteName = builder.llmsSiteName;
        this.llmsSummary = builder.llmsSummary;
        this.llmsContextText = builder.llmsContextText;
        this.llmsSections = Collections.unmodifiableList(builder.llmsSections);
        this.llmsIncludePaths = Collections.unmodifiableList(builder.llmsIncludePaths);
        this.llmsExcludePaths = Collections.unmodifiableList(builder.llmsExcludePaths);
        this.llmsMaxLinks = builder.llmsMaxLinks;
        this.llmsMaxOutputBytes = builder.llmsMaxOutputBytes;
        this.llmsCacheMaxAgeSeconds = builder.llmsCacheMaxAgeSeconds;
        this.jsonLdOrganizationName = builder.jsonLdOrganizationName;
        this.jsonLdOrganizationLogoUrl = builder.jsonLdOrganizationLogoUrl;
        this.jsonLdWebsiteName = builder.jsonLdWebsiteName;
        this.jsonLdIncludeOrganization = builder.jsonLdIncludeOrganization;
        this.jsonLdIncludeWebSite = builder.jsonLdIncludeWebSite;
        this.jsonLdAutoWebPage = builder.jsonLdAutoWebPage;
        this.jsonLdAutoBreadcrumb = builder.jsonLdAutoBreadcrumb;
    }

    public String getProjectRoot() {
        return projectRoot;
    }

    public String getPublicDomain() {
        return publicDomain;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isRobotsEnabled() {
        return robotsEnabled;
    }

    public boolean isSitemapEnabled() {
        return sitemapEnabled;
    }

    public boolean isMarkdownEnabled() {
        return markdownEnabled;
    }

    public boolean isLlmsEnabled() {
        return llmsEnabled;
    }

    public boolean isJsonLdEnabled() {
        return jsonLdEnabled;
    }

    public List<String> getLanguageRoots() {
        return languageRoots;
    }

    public List<String> getLocales() {
        return locales;
    }

    public boolean isSameRelativePathEnabled() {
        return sameRelativePathEnabled;
    }

    public List<String> getExplicitMappings() {
        return explicitMappings;
    }

    public String getXDefaultPath() {
        return xDefaultPath;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public long getMaxOutputBytes() {
        return maxOutputBytes;
    }

    public boolean isProductionEnvironment() {
        return productionEnvironment;
    }

    public List<String> getRobotsUserAgents() {
        return robotsUserAgents;
    }

    public List<String> getRobotsAllowRules() {
        return robotsAllowRules;
    }

    public List<String> getRobotsDisallowRules() {
        return robotsDisallowRules;
    }

    public List<String> getRobotsManualSitemapUrls() {
        return robotsManualSitemapUrls;
    }

    public int getRobotsCacheMaxAgeSeconds() {
        return robotsCacheMaxAgeSeconds;
    }

    public int getSitemapMaxUrlsPerFile() {
        return sitemapMaxUrlsPerFile;
    }

    public long getSitemapMaxBytesPerFile() {
        return sitemapMaxBytesPerFile;
    }

    public boolean isSitemapIncludeLastModified() {
        return sitemapIncludeLastModified;
    }

    public boolean isSitemapIncludeHreflang() {
        return sitemapIncludeHreflang;
    }

    public int getSitemapCacheMaxAgeSeconds() {
        return sitemapCacheMaxAgeSeconds;
    }

    public int getMarkdownMaxComponentDepth() {
        return markdownMaxComponentDepth;
    }

    public int getMarkdownMaxComponents() {
        return markdownMaxComponents;
    }

    public int getMarkdownMaxReferences() {
        return markdownMaxReferences;
    }

    public int getMarkdownCacheMaxAgeSeconds() {
        return markdownCacheMaxAgeSeconds;
    }

    public String getLlmsSiteName() {
        return llmsSiteName;
    }

    public String getLlmsSummary() {
        return llmsSummary;
    }

    public String getLlmsContextText() {
        return llmsContextText;
    }

    public List<String> getLlmsSections() {
        return llmsSections;
    }

    public List<String> getLlmsIncludePaths() {
        return llmsIncludePaths;
    }

    public List<String> getLlmsExcludePaths() {
        return llmsExcludePaths;
    }

    public int getLlmsMaxLinks() {
        return llmsMaxLinks;
    }

    public int getLlmsMaxOutputBytes() {
        return llmsMaxOutputBytes;
    }

    public int getLlmsCacheMaxAgeSeconds() {
        return llmsCacheMaxAgeSeconds;
    }

    public String getJsonLdOrganizationName() {
        return jsonLdOrganizationName;
    }

    public String getJsonLdOrganizationLogoUrl() {
        return jsonLdOrganizationLogoUrl;
    }

    public String getJsonLdWebsiteName() {
        return jsonLdWebsiteName;
    }

    public boolean isJsonLdIncludeOrganization() {
        return jsonLdIncludeOrganization;
    }

    public boolean isJsonLdIncludeWebSite() {
        return jsonLdIncludeWebSite;
    }

    public boolean isJsonLdAutoWebPage() {
        return jsonLdAutoWebPage;
    }

    public boolean isJsonLdAutoBreadcrumb() {
        return jsonLdAutoBreadcrumb;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String projectRoot = "";
        private String publicDomain = "";
        private boolean enabled;
        private boolean robotsEnabled;
        private boolean sitemapEnabled;
        private boolean markdownEnabled;
        private boolean llmsEnabled;
        private boolean jsonLdEnabled;
        private List<String> languageRoots = Collections.emptyList();
        private List<String> locales = Collections.emptyList();
        private boolean sameRelativePathEnabled;
        private List<String> explicitMappings = Collections.emptyList();
        private String xDefaultPath = "";
        private int batchSize = 100;
        private int maxDepth = 20;
        private long timeoutMs = 30_000L;
        private long maxOutputBytes = 5_000_000L;
        private boolean productionEnvironment;
        private List<String> robotsUserAgents = Collections.singletonList("*");
        private List<String> robotsAllowRules = Collections.emptyList();
        private List<String> robotsDisallowRules = Collections.emptyList();
        private List<String> robotsManualSitemapUrls = Collections.emptyList();
        private int robotsCacheMaxAgeSeconds = 1200;
        private int sitemapMaxUrlsPerFile = 50_000;
        private long sitemapMaxBytesPerFile = 52_428_800L;
        private boolean sitemapIncludeLastModified = true;
        private boolean sitemapIncludeHreflang = true;
        private int sitemapCacheMaxAgeSeconds = 3600;
        private int markdownMaxComponentDepth = 20;
        private int markdownMaxComponents = 500;
        private int markdownMaxReferences = 25;
        private int markdownCacheMaxAgeSeconds = 3600;
        private String llmsSiteName = "";
        private String llmsSummary = "";
        private String llmsContextText = "";
        private List<String> llmsSections = Collections.emptyList();
        private List<String> llmsIncludePaths = Collections.emptyList();
        private List<String> llmsExcludePaths = Collections.emptyList();
        private int llmsMaxLinks = 50;
        private int llmsMaxOutputBytes = 100_000;
        private int llmsCacheMaxAgeSeconds = 3600;
        private String jsonLdOrganizationName = "";
        private String jsonLdOrganizationLogoUrl = "";
        private String jsonLdWebsiteName = "";
        private boolean jsonLdIncludeOrganization = true;
        private boolean jsonLdIncludeWebSite = true;
        private boolean jsonLdAutoWebPage = true;
        private boolean jsonLdAutoBreadcrumb = true;

        private Builder() {
        }

        public Builder projectRoot(String projectRoot) {
            this.projectRoot = Objects.requireNonNull(projectRoot, "projectRoot");
            return this;
        }

        public Builder publicDomain(String publicDomain) {
            this.publicDomain = Objects.requireNonNull(publicDomain, "publicDomain");
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder robotsEnabled(boolean robotsEnabled) {
            this.robotsEnabled = robotsEnabled;
            return this;
        }

        public Builder sitemapEnabled(boolean sitemapEnabled) {
            this.sitemapEnabled = sitemapEnabled;
            return this;
        }

        public Builder markdownEnabled(boolean markdownEnabled) {
            this.markdownEnabled = markdownEnabled;
            return this;
        }

        public Builder llmsEnabled(boolean llmsEnabled) {
            this.llmsEnabled = llmsEnabled;
            return this;
        }

        public Builder jsonLdEnabled(boolean jsonLdEnabled) {
            this.jsonLdEnabled = jsonLdEnabled;
            return this;
        }

        public Builder languageRoots(String[] languageRoots) {
            this.languageRoots = copyStrings(languageRoots);
            return this;
        }

        public Builder locales(String[] locales) {
            this.locales = copyStrings(locales);
            return this;
        }

        public Builder sameRelativePathEnabled(boolean sameRelativePathEnabled) {
            this.sameRelativePathEnabled = sameRelativePathEnabled;
            return this;
        }

        public Builder explicitMappings(String[] explicitMappings) {
            this.explicitMappings = copyStrings(explicitMappings);
            return this;
        }

        public Builder xDefaultPath(String xDefaultPath) {
            this.xDefaultPath = xDefaultPath == null ? "" : xDefaultPath;
            return this;
        }

        private static List<String> copyStrings(String[] values) {
            return values == null
                    ? Collections.emptyList()
                    : Collections.unmodifiableList(Arrays.asList(values.clone()));
        }

        public Builder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder maxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }

        public Builder timeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public Builder maxOutputBytes(long maxOutputBytes) {
            this.maxOutputBytes = maxOutputBytes;
            return this;
        }

        public Builder productionEnvironment(boolean productionEnvironment) {
            this.productionEnvironment = productionEnvironment;
            return this;
        }

        public Builder robotsUserAgents(String[] robotsUserAgents) {
            this.robotsUserAgents = robotsUserAgents == null || robotsUserAgents.length == 0
                    ? Collections.singletonList("*")
                    : copyStrings(robotsUserAgents);
            return this;
        }

        public Builder robotsAllowRules(String[] robotsAllowRules) {
            this.robotsAllowRules = copyStrings(robotsAllowRules);
            return this;
        }

        public Builder robotsDisallowRules(String[] robotsDisallowRules) {
            this.robotsDisallowRules = copyStrings(robotsDisallowRules);
            return this;
        }

        public Builder robotsManualSitemapUrls(String[] robotsManualSitemapUrls) {
            this.robotsManualSitemapUrls = copyStrings(robotsManualSitemapUrls);
            return this;
        }

        public Builder robotsCacheMaxAgeSeconds(int robotsCacheMaxAgeSeconds) {
            this.robotsCacheMaxAgeSeconds = robotsCacheMaxAgeSeconds > 0 ? robotsCacheMaxAgeSeconds : 1200;
            return this;
        }

        public Builder sitemapMaxUrlsPerFile(int sitemapMaxUrlsPerFile) {
            this.sitemapMaxUrlsPerFile = sitemapMaxUrlsPerFile > 0 ? sitemapMaxUrlsPerFile : 50_000;
            return this;
        }

        public Builder sitemapMaxBytesPerFile(long sitemapMaxBytesPerFile) {
            this.sitemapMaxBytesPerFile = sitemapMaxBytesPerFile > 0 ? sitemapMaxBytesPerFile : 52_428_800L;
            return this;
        }

        public Builder sitemapIncludeLastModified(boolean sitemapIncludeLastModified) {
            this.sitemapIncludeLastModified = sitemapIncludeLastModified;
            return this;
        }

        public Builder sitemapIncludeHreflang(boolean sitemapIncludeHreflang) {
            this.sitemapIncludeHreflang = sitemapIncludeHreflang;
            return this;
        }

        public Builder sitemapCacheMaxAgeSeconds(int sitemapCacheMaxAgeSeconds) {
            this.sitemapCacheMaxAgeSeconds = sitemapCacheMaxAgeSeconds > 0 ? sitemapCacheMaxAgeSeconds : 3600;
            return this;
        }

        public Builder markdownMaxComponentDepth(int markdownMaxComponentDepth) {
            this.markdownMaxComponentDepth = markdownMaxComponentDepth > 0 ? markdownMaxComponentDepth : 20;
            return this;
        }

        public Builder markdownMaxComponents(int markdownMaxComponents) {
            this.markdownMaxComponents = markdownMaxComponents > 0 ? markdownMaxComponents : 500;
            return this;
        }

        public Builder markdownMaxReferences(int markdownMaxReferences) {
            this.markdownMaxReferences = markdownMaxReferences > 0 ? markdownMaxReferences : 25;
            return this;
        }

        public Builder markdownCacheMaxAgeSeconds(int markdownCacheMaxAgeSeconds) {
            this.markdownCacheMaxAgeSeconds = markdownCacheMaxAgeSeconds > 0 ? markdownCacheMaxAgeSeconds : 3600;
            return this;
        }

        public Builder llmsSiteName(String llmsSiteName) {
            this.llmsSiteName = llmsSiteName == null ? "" : llmsSiteName;
            return this;
        }

        public Builder llmsSummary(String llmsSummary) {
            this.llmsSummary = llmsSummary == null ? "" : llmsSummary;
            return this;
        }

        public Builder llmsContextText(String llmsContextText) {
            this.llmsContextText = llmsContextText == null ? "" : llmsContextText;
            return this;
        }

        public Builder llmsSections(String[] llmsSections) {
            this.llmsSections = copyStrings(llmsSections);
            return this;
        }

        public Builder llmsIncludePaths(String[] llmsIncludePaths) {
            this.llmsIncludePaths = copyStrings(llmsIncludePaths);
            return this;
        }

        public Builder llmsExcludePaths(String[] llmsExcludePaths) {
            this.llmsExcludePaths = copyStrings(llmsExcludePaths);
            return this;
        }

        public Builder llmsMaxLinks(int llmsMaxLinks) {
            this.llmsMaxLinks = llmsMaxLinks > 0 ? llmsMaxLinks : 50;
            return this;
        }

        public Builder llmsMaxOutputBytes(int llmsMaxOutputBytes) {
            this.llmsMaxOutputBytes = llmsMaxOutputBytes > 0 ? llmsMaxOutputBytes : 100_000;
            return this;
        }

        public Builder llmsCacheMaxAgeSeconds(int llmsCacheMaxAgeSeconds) {
            this.llmsCacheMaxAgeSeconds = llmsCacheMaxAgeSeconds > 0 ? llmsCacheMaxAgeSeconds : 3600;
            return this;
        }

        public Builder jsonLdOrganizationName(String jsonLdOrganizationName) {
            this.jsonLdOrganizationName = jsonLdOrganizationName == null ? "" : jsonLdOrganizationName;
            return this;
        }

        public Builder jsonLdOrganizationLogoUrl(String jsonLdOrganizationLogoUrl) {
            this.jsonLdOrganizationLogoUrl =
                    jsonLdOrganizationLogoUrl == null ? "" : jsonLdOrganizationLogoUrl;
            return this;
        }

        public Builder jsonLdWebsiteName(String jsonLdWebsiteName) {
            this.jsonLdWebsiteName = jsonLdWebsiteName == null ? "" : jsonLdWebsiteName;
            return this;
        }

        public Builder jsonLdIncludeOrganization(boolean jsonLdIncludeOrganization) {
            this.jsonLdIncludeOrganization = jsonLdIncludeOrganization;
            return this;
        }

        public Builder jsonLdIncludeWebSite(boolean jsonLdIncludeWebSite) {
            this.jsonLdIncludeWebSite = jsonLdIncludeWebSite;
            return this;
        }

        public Builder jsonLdAutoWebPage(boolean jsonLdAutoWebPage) {
            this.jsonLdAutoWebPage = jsonLdAutoWebPage;
            return this;
        }

        public Builder jsonLdAutoBreadcrumb(boolean jsonLdAutoBreadcrumb) {
            this.jsonLdAutoBreadcrumb = jsonLdAutoBreadcrumb;
            return this;
        }

        public ResolvedConfiguration build() {
            return new ResolvedConfiguration(this);
        }
    }
}
