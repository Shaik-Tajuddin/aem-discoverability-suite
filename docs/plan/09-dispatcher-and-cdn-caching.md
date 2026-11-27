# Dispatcher and CDN Caching

**Feature area:** Delivery and operations  
**Audience:** AEM administrators, Dispatcher/CDN administrators, release engineers  
**Status:** Phase 9 implemented (2026-11-27) — see `tasks/plan.md` / CHANGELOG / `dispatcher/snippets/`

## 1. Purpose

The selected caching strategy uses Dispatcher and CDN as the primary caching layers. v1 does not introduce a dedicated application-level cache for generated sitemap, Markdown, or llms output.

Caching reduces repeated Publish traversal. It also introduces freshness requirements that must be managed across each layer.

## 2. Cache layers

```text
Crawler / browser
       |
      CDN  (its own TTL / purge rules)
       |
   Dispatcher (cache + invalidation + optional TTL)
       |
  AEM Publish (on-demand generation)
```

Dispatcher invalidation does not automatically guarantee CDN invalidation. The CDN may continue serving an object until its own expiry or a CDN-specific purge is performed.

## 3. Initial cache policy

Starting values from the architecture baseline:

| Output | Initial max-age |
|---|---:|
| robots.txt | 1,200 seconds |
| Sitemap index/partitions | 3,600 seconds |
| Markdown | 3,600 seconds |
| llms.txt | 3,600 seconds |
| HTML containing JSON-LD | Existing HTML policy |

These values are not universal best-practice values. They must be tuned to publishing frequency, site size, traffic, cache purge capability, and the required freshness SLA.

## 4. Enablement and deployment guide

### 4.1 AEM administrator

1. Confirm each output returns an intentional cache policy.
2. Confirm headers are emitted only for the intended public responses.
3. Check that errors and user-specific responses are not cached as public generated output.
4. Validate activation behavior and configuration-change behavior.
5. Confirm Publish is not unexpectedly regenerating the same output on every request due to cache misses.

### 4.2 Dispatcher administrator

1. Review the existing farm and cache configuration.
2. Add only the exact public endpoint paths/extensions required by the suite.
3. Add narrow filter allowlists; do not broadly expose servlet paths.
4. Confirm `.txt`, `.xml`, and `.md` responses are cacheable only when safe.
5. Configure header caching where required for downstream behavior.
6. Review `/enableTTL`, invalidation, `/statfileslevel`, and `/gracePeriod` in the context of the site's complete configuration.
7. Test cache hit/miss and activation invalidation on a non-production environment.
8. Integrate changes through normal Dispatcher deployment/review. Do not overwrite project configuration.

### 4.3 CDN administrator

1. Confirm CDN cache key includes the correct host and path.
2. Set CDN TTL consistently with the agreed freshness SLA.
3. Define purge behavior for critical content/config changes.
4. Test cache behavior separately from Dispatcher.
5. Document who owns a purge and how it is requested.
6. Confirm errors are not cached longer than intended.

## 5. Header example

Illustrative response:

```http
HTTP/1.1 200 OK
Content-Type: application/xml; charset=UTF-8
Cache-Control: public, max-age=3600
```

Exact `Cache-Control`, `Surrogate-Control`, `Expires`, and downstream header behavior must be agreed for the actual hosting architecture.

## 6. Dispatcher TTL notes

Adobe's Dispatcher documentation states that `/enableTTL` uses backend expiration headers to create TTL metadata in the Dispatcher cache. It also notes that `/enableTTL` affects Dispatcher caching and that downstream browser/CDN headers require appropriate header handling.

`/gracePeriod` relates to throttling/serving behavior around auto-invalidation; it is not a substitute for defining a CDN TTL or a full end-to-end freshness policy.

References:
- [Adobe Dispatcher configuration — enableTTL and gracePeriod](https://experienceleague.adobe.com/en/docs/experience-manager-dispatcher/using/configuring/dispatcher-configuration)
- [Adobe — Understanding Dispatcher cache](https://experienceleague.adobe.com/en/docs/experience-manager-learn/ams/dispatcher/understanding-cache)

## 7. Best practices

- Define freshness SLA per output.
- Treat Dispatcher TTL and CDN TTL as separate controls.
- Keep cache keys project/host-aware.
- Avoid caching personalized or authenticated responses publicly.
- Do not cache errors as successful generated output.
- Use explicit endpoint filters and rewrites.
- Preserve the existing invalidation model unless a reviewed change is approved.
- Do not assume activation purges every CDN cache.
- Monitor hit ratio and cold-generation duration.
- Test configuration changes as well as page activations.
- Document stale-serving expectations during Publish outages.

## 8. Freshness matrix (v1 reference)

Assumes publish farm `/enableTTL "1"`, invalidate globs from `dispatcher/snippets/discoverability_invalidate.any`, and CDN that respects origin `Cache-Control` (or uses the listed CDN TTL). CDN purge is manual/ops in v1.

| Output | Publish change trigger | Dispatcher behavior | CDN behavior | Maximum expected stale period |
|---|---|---|---|---|
| robots.txt | CA Config publish (robots/platform) | Invalidate `*.robots.txt`; TTL from `max-age=1200` | TTL ≤ 1,200s or purge | ~1,200s (+ CDN if longer than origin) |
| sitemap | Page activate/deactivate/move; sitemap/config publish | Invalidate `*.sitemap*.xml`; TTL `max-age=3600` | TTL ≤ 3,600s or purge | ~3,600s (+ CDN) |
| Markdown | Page activate/deactivate; markdown/config publish | Invalidate `*.md` for that path; TTL `max-age=3600` | TTL ≤ 3,600s or purge | ~3,600s (+ CDN) |
| llms.txt | Page activate/deactivate; llms/config publish | Invalidate `*.llms.txt`; TTL `max-age=3600` | TTL ≤ 3,600s or purge | ~3,600s (+ CDN) |
| HTML JSON-LD | Page activation (HTML body) | Existing `*.html` invalidate | Existing HTML CDN policy | Existing HTML SLA |

**Notes**

- Origin errors use `Cache-Control: no-store` — do not treat as cacheable 200s.
- Without `/enableTTL`, Dispatcher may keep files until activation invalidation only; root vanity files still need the invalidate globs above.
- `serveStaleOnError` may serve last good body during Publish outages — document as intentional stale.

## 9. Troubleshooting

| Symptom | Check |
|---|---|
| New page absent from sitemap | Publish eligibility, Dispatcher invalidation, CDN TTL |
| Config changes not reflected | CA Config publication, Dispatcher cache, CDN cache |
| Wrong project's output | Host-aware cache key and project resolution |
| Publish is under load | Cache bypass/miss, repeated sitemap traversals |
| Headers absent at CDN | Dispatcher cached headers and CDN origin/header policy |
| Old output persists after purge | Verify both CDN and Dispatcher layers |

## 10. Release checklist

Use with `dispatcher/snippets/ENABLEMENT.md`.

- [ ] Exact routes permitted by Dispatcher filters (`discoverability_filters.any` merged).
- [ ] Unintended servlet paths remain blocked.
- [ ] Cache-Control policy verified on origin, Dispatcher, and CDN.
- [ ] CDN cache key is project/host aware.
- [ ] Activation and config-publish freshness tested.
- [ ] Error response caching reviewed (`no-store` / CDN 4xx policy).
- [ ] Rollback plan documented.
- [ ] Existing project rules preserved.

## 11. References

- [Adobe Dispatcher configuration](https://experienceleague.adobe.com/en/docs/experience-manager-dispatcher/using/configuring/dispatcher-configuration)
- [Adobe Dispatcher caching overview](https://experienceleague.adobe.com/en/docs/experience-manager-learn/ams/dispatcher/understanding-cache)
- [AEM as a Cloud Service caching overview](https://experienceleague.adobe.com/en/docs/experience-manager-cloud-service/content/implementing/content-delivery/caching) — reference for layered cache concepts; validate applicability to the target AEM 6.5/AMS topology.
