# robots.txt — Feature Guide

**Feature area:** SEO  
**Audience:** SEO administrators, AEM configuration owners, content authors  
**Generation:** On-demand on Publish  
**Status:** Design guide — implementation not yet authorized

## 1. Purpose

The robots.txt output provides crawler directives for a project's public website. It is generated from project-level configuration and is delivered at the site's root through an approved public route.

Illustrative endpoint: `https://www.example.com/robots.txt`.

The robots file is not a security boundary. Disallow directives request that compliant crawlers do not fetch matching paths; they do not protect private content and are not a reliable way to remove a URL from search indexes.

## 2. Architecture

```text
Request /robots.txt
       |
Resolve project from trusted host/routing configuration
       |
Resolve RobotsConfig and environment
       |
Validate directives and sitemap mode
       |
Render deterministic robots text
       |
Return text/plain + cache headers
       |
Dispatcher/CDN
```

The robots generator must not inspect arbitrary projects or construct a sitemap URL from an untrusted Host header.

## 3. Configuration

Proposed logical settings; exact CA Config field names are not yet final.

| Setting | Purpose |
|---|---|
| enabled | Enables/disables this project's robots output |
| userAgents | Crawler user-agent groups |
| allowRules | Allowed path prefixes/patterns |
| disallowRules | Disallowed path prefixes/patterns |
| sitemapMode | `AUTO`, `MANUAL`, or `NONE` |
| manualSitemapUrls | Explicit sitemap URLs for MANUAL mode |
| cacheMaxAge | Output cache lifetime |
| nonProductionPolicy | Default crawl policy for non-production environments |

### Sitemap modes

- **AUTO:** Emit the sitemap URL resolved through the project's canonical URL policy.
- **MANUAL:** Emit only explicitly configured, validated sitemap URLs.
- **NONE:** Do not add a sitemap directive.

## 4. Author/admin enablement guide

### AEM configuration owner

1. Resolve the correct project CA Config context.
2. Confirm the project's public domain and production environment mapping.
3. Enable the robots feature for the project.
4. Configure crawler groups and required Allow/Disallow entries.
5. Select the sitemap declaration mode.
6. For MANUAL mode, enter only absolute public sitemap URLs belonging to the project.
7. Keep non-production crawling blocked by default.
8. Publish the configuration to Publish.
9. Request `/robots.txt` through the public domain, not only through the Author instance.
10. Validate the response content and cache headers.

### Content author

Authors generally do not edit robots.txt. Their responsibility is to author public content correctly and set page-level indexing/exclusion metadata through the provided Page Properties workflow. Do not add a Disallow rule to hide a single page when page-level `noindex` or access control is the correct solution.

## 5. Best practices

- Keep the file at the public site root.
- Return a valid plain-text response.
- Use the smallest set of precise rules needed.
- Avoid accidentally blocking CSS, JavaScript, or resources needed to render pages.
- Use page-level `noindex` for URLs that should be crawled but excluded from indexing; do not assume Disallow removes an already-known URL from results.
- Never put secrets or private URL inventories in robots.txt.
- Do not rely on robots.txt to secure CUG, DAM, or internal content.
- Ensure every Sitemap directive is a fully qualified URL for the current project.
- Review production and non-production behavior separately.
- Keep rule order and output stable for caching and change review.

Google explains that robots.txt is a crawl-control mechanism and recommends using `noindex` or authentication where indexing/access control is intended: [Google Search Essentials](https://developers.google.com/search/docs/fundamentals/get-started).

## 6. Example output

```text
User-agent: *
Disallow: /content/dam/private/
Disallow: /internal/
Allow: /

Sitemap: https://www.example.com/sitemap.xml
```

This is illustrative only. The `/content/dam/private/` example is not protection for those assets. Actual protected assets must be secured with AEM/Dispatcher access controls.

### Non-production example

```text
User-agent: *
Disallow: /
```

Use this only as an environment-specific crawler policy. Do not deploy it to production accidentally.

## 7. Validation guide

- `GET https://www.example.com/robots.txt` returns the expected status.
- Content type is `text/plain`.
- The response contains only this project's rules.
- Sitemap URL points to the correct public domain.
- Non-production policy is verified.
- No required public resources are inadvertently blocked.
- Rules do not expose private path information.
- Dispatcher and CDN return the intended current version.

## 8. Common issues

| Symptom | Check |
|---|---|
| Wrong site's rules appear | Project/host resolution and cache key |
| Sitemap URL points to Author | Public domain and Externalizer configuration |
| URL still appears in search | robots.txt is not a removal/noindex mechanism |
| File is stale after config change | Dispatcher and CDN TTL/invalidation |
| Assets cannot be rendered by crawler | Overly broad Disallow rule |

## 9. References

- [Google robots and crawling guidance](https://developers.google.com/search/docs/fundamentals/get-started)
- [Google robots.txt specification interpretation](https://developers.google.com/search/docs/crawling-indexing/robots/robots_txt)
- [AEM 6.5 SEO and URL management](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/managing/managing-further-reference/seo-and-url-management)
