# robots.txt

Generates a project-scoped `robots.txt` from CA Config. Off by default.

## Authoring

1. Open the CA Config Editor under your site context (page with `sling:configRef` / `cq:conf` in an ancestor).
2. Edit **Discoverability Robots**.
3. Set:
   - **Enabled** = true
   - **Public domain** = trusted origin (e.g. `https://www.example.com`) — required when the feature is on
   - Optional: user-agents, allow/disallow rules, sitemap URLs, cache max-age
4. Save. Publish the `/conf/...` config for Publish.

OSGi **Discoverability Environment Policy** (`production=true` only on prod) drives crawl allow/deny with site crawl flags if you use them elsewhere; robots content itself comes from this config’s rules.

### Preview on Author

```
http://localhost:4502/content/<site>.robots.txt
```

Example: `/content/wknd.robots.txt` or any page under the context, e.g. `/content/wknd/us/en.robots.txt` (resolved via the page’s CA context).

### Public URL

Dispatcher maps `/robots.txt` → `{projectRoot}.robots.txt` (see `dispatcher` rewrites).

## Example output

```text
User-agent: *
Allow: /
Disallow: /internal/
Sitemap: https://www.example.com/sitemap.xml
```

Minimal default (enabled, no extra rules):

```text
User-agent: *

```

## Notes

- Feature off or missing/invalid `publicDomain` (when any feature is on) → **404** + `Cache-Control: no-store`.
- Success: `text/plain`, configurable `Cache-Control` (default max-age 1200).
- Code: `core/.../seo/robots/`.
