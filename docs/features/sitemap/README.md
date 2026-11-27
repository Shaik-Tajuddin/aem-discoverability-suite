# XML sitemap

On-demand XML sitemap index / urlsets from eligible pages under the CA context root. Off by default.

## Authoring

1. Open the CA Config Editor for the site context.
2. Edit **Discoverability Sitemap**.
3. Set:
   - **Enabled** = true
   - **Public domain** = trusted origin (required when enabled)
   - Optional: batch size, max depth, URL/byte caps, lastmod, hreflang (`languageRoots` + `locales`), cache max-age
4. Save and publish config.

Project root is the innermost content path with `sling:configRef` / `cq:conf` (not a separate platform config).

### Preview on Author

```
http://localhost:4502/content/<site>.sitemap.xml
http://localhost:4502/content/<site>.sitemap.0.xml
```

Example: `/content/wknd/us/en.sitemap.xml`.

### Public URL

- `/sitemap.xml` → `{projectRoot}.sitemap.xml`
- `/sitemap-N.xml` → `{projectRoot}.sitemap.N.xml`

## Example output

Single partition (urlset):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:xhtml="http://www.w3.org/1999/xhtml">
  <url>
    <loc>https://www.example.com/us/en</loc>
    <lastmod>2020-10-01T00:38:05.651Z</lastmod>
  </url>
  <url>
    <loc>https://www.example.com/us/en/about-us</loc>
    <lastmod>2020-07-09T15:56:39.75Z</lastmod>
  </url>
</urlset>
```

Multi-partition index:

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

## Notes

- Only eligible pages (published intent, no exclude/noindex/redirect/CUG, valid canonical under public domain).
- Caps: ≤ 50,000 URLs / ≤ 50 MB per file (configurable downward).
- Feature off → **404** `no-store`.
- Code: `core/.../seo/sitemap/`.
