# XML Sitemap — Feature Guide

**Feature area:** SEO  
**Audience:** SEO administrators, AEM administrators, content authors  
**Generation:** Custom, on-demand, Publish-side  
**Status:** Phase 5 implemented (2026-11-27) — see `tasks/plan.md` / CHANGELOG

## 1. Purpose

The custom sitemap engine publishes a machine-readable list of public canonical pages for a project. The selected architecture intentionally uses a custom engine rather than delegating generation to the built-in Sling Sitemap feature.

AEM 6.5.11.0 introduced Sling Sitemap as a product feature. Because the suite uses a custom engine, the implementation must explicitly prevent conflicting registrations or duplicate sitemap ownership for suite-managed roots. See [AEM 6.5 SEO and URL management](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/managing/managing-further-reference/seo-and-url-management).

## 2. Proposed endpoints

Illustrative routes:

- `/sitemap.xml` — single sitemap or sitemap index.
- `/sitemap-0.xml` — first partition.
- `/sitemap-1.xml` — second partition.
- `/sitemap-N.xml` — additional partitions.

Exact servlet/selector/extension routing is a detailed-design item.

## 3. Architecture

```text
GET /sitemap.xml
       |
Resolve project + SitemapConfig
       |
Streaming PageDiscoveryService
       |
Shared PageEligibilityService
       |
CanonicalUrlService + language relationships
       |
Sitemap partition planner
       |
Sitemap index or sitemap XML writer
       |
Validate size, count, URLs and XML
       |
Return complete XML or controlled error
```

### 3.1 Main components

- SitemapServlet
- SitemapIndexGenerator
- SitemapGenerator
- SitemapPartitioner
- SitemapUrlProvider
- SitemapXmlWriter
- SitemapValidationService

### 3.2 Deterministic two-pass approach

Selected baseline strategy:

1. Traverse eligible pages to determine partition boundaries/counts.
2. Generate the sitemap index.
3. When a partition is requested, repeat deterministic traversal and stream only that partition.
4. Use stable sorting/ordering so the index and partition requests agree.

Tradeoff: cache misses repeat repository traversal. This avoids persisting a full URL inventory and keeps memory bounded, at the cost of additional Publish work.

The design must not claim transactional consistency across independent traversals unless the repository model provides it.

## 4. Standards and limits

Each sitemap file must remain within:

- 50,000 URL entries.
- 50 MB uncompressed.
- UTF-8 encoding.
- Absolute, fully qualified public URLs.

These limits are defined in Google's sitemap guidance: [Build and submit a sitemap](https://developers.google.com/search/docs/crawling-indexing/sitemaps/build-sitemap).

The sitemap should contain preferred canonical URLs, not every alias or duplicate route. Google treats sitemap URLs as canonical hints, not a guarantee of indexing.

## 5. Configuration

Proposed logical settings:

| Setting | Purpose |
|---|---|
| enabled | Enable sitemap generation |
| projectRoot | Root for page discovery |
| maxDepth | Maximum traversal depth |
| batchSize | Page batch size |
| maxUrlsPerFile | Hard maximum no greater than 50,000 |
| maxBytesPerFile | Hard maximum no greater than 50 MB uncompressed |
| includeLastModified | Include lastmod when meaningful |
| includeHreflang | Include validated language alternatives |
| languageRoots | Project language scopes |
| xDefault | Optional validated fallback |
| cacheMaxAge | Cache lifetime |
| excludedPaths | Additional project exclusions |

The final implementation should enforce standards limits even if a configuration value is accidentally set higher.

## 6. Author guide: getting a page into the sitemap

Authors do not manually edit the generated XML.

1. Create or edit the page under the correct project/language root.
2. Ensure the page is intended for anonymous public use.
3. Complete page title and required content metadata.
4. Review the page's indexing and exclusion settings.
5. Confirm the canonical URL is correct or leave the approved default.
6. Ensure the page is not a redirect/alias unless the site policy explicitly includes it.
7. Request publication.
8. After activation, validate the public page and sitemap output.

A page is included only if it passes shared eligibility and the sitemap-specific policy.

## 7. Example output

### 7.1 Sitemap index

```xml
<?xml version="1.0" encoding="UTF-8"?>
<sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
  <sitemap>
    <loc>https://www.example.com/sitemap-0.xml</loc>
  </sitemap>
  <sitemap>
    <loc>https://www.example.com/sitemap-1.xml</loc>
  </sitemap>
</sitemapindex>
```

### 7.2 Sitemap URL set

```xml
<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
  <url>
    <loc>https://www.example.com/about-us</loc>
    <lastmod>2026-10-15</lastmod>
  </url>
  <url>
    <loc>https://www.example.com/contact</loc>
    <lastmod>2026-10-21</lastmod>
  </url>
</urlset>
```

`lastmod` must represent a meaningful content modification date. Do not substitute generation time merely because the sitemap was requested now.

## 8. Best practices

- Include only canonical, indexable, publicly accessible URLs.
- Use absolute HTTPS URLs.
- Keep one preferred URL for duplicate content.
- Keep sitemap URLs aligned with HTML canonical signals.
- Do not include redirected, protected, `noindex`, or explicitly excluded pages.
- Do not include Author, Stage, local, or preview domains.
- Use stable URL ordering for deterministic output.
- Escape XML values with a proper XML writer.
- Never return truncated or partially formed XML.
- Keep partition sizes below protocol limits with safety margin.
- Verify hreflang targets are valid and reciprocal where required.
- Use the public site root where possible so the sitemap scope is not unintentionally restricted by its directory.

Google's guidance recommends fully qualified absolute URLs, canonical URLs, and sitemap index files when limits are exceeded: [Google sitemap guide](https://developers.google.com/search/docs/crawling-indexing/sitemaps/build-sitemap).

## 9. Validation and monitoring

- [ ] Index references only existing partition endpoints.
- [ ] Every `<loc>` is absolute and on the approved domain.
- [ ] No duplicate canonical URL.
- [ ] No private/CUG URL.
- [ ] No `noindex` page under the shared policy.
- [ ] URL count and uncompressed size remain within limits.
- [ ] XML parses successfully.
- [ ] Lastmod is meaningful.
- [ ] Hreflang targets are eligible.
- [ ] Search Console accepts the sitemap and reports expected processing.

## 10. Troubleshooting

| Symptom | Likely cause |
|---|---|
| URL missing | Page fails eligibility, unpublished, wrong root, or invalid canonical |
| Duplicate URL | Mapping/vanity normalization or canonical dedupe defect |
| Sitemap index points to missing shard | Partition planner and generator disagree |
| Old page remains | Dispatcher/CDN freshness or activation invalidation |
| Wrong hostname | Project mapping/Externalizer/domain validation |
| XML rejected | Encoding, escaping, size, namespace, or partial-output defect |
| High Publish load | Repeated cold traversals; review cache hit ratio and partition strategy |

## 11. References

- [Google — Build and submit a sitemap](https://developers.google.com/search/docs/crawling-indexing/sitemaps/build-sitemap)
- [Adobe — AEM 6.5 SEO and URL management](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/managing/managing-further-reference/seo-and-url-management)
- [Adobe — AEM Sites sitemap learning guide](https://experienceleague.adobe.com/en/docs/experience-manager-learn/sites/seo/sitemaps)
