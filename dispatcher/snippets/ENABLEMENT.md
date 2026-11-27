# Discoverability Suite — Dispatcher / CDN enablement (AMS & on-prem)

## Scope

Reference-only snippets. AMS ships immutable filter/cache/farm files; merge these rules into a **customer** farm/filter overlay. Do not replace the whole AMS publish farm.

| Snippet | Purpose |
|---|---|
| `discoverability_filters.any` | Allow exact suite routes only |
| `discoverability_cache.any` | Optional explicit cache allows |
| `discoverability_invalidate.any` | Invalidate discovery files on activation |
| `../src/conf.d/rewrites/discoverability_rewrite.rules` | Public URL → `{projectRoot}.…` mappings |

Set `CONTENT_FOLDER_NAME` (or project root segment) in Apache vars to the site’s content root name.

## AMS steps

1. Confirm suite package is installed on Publish; features remain **default off** until CA Config enablement.
2. Copy filter rules into the customer publish filter include (numbers `≥ /0200`, after AMS denies).
3. Include rewrite rules from the publish vhost (already wired in `discoverability_publish.vhost`).
4. Append invalidate globs; set farm `/enableTTL "1"` so origin `Cache-Control` drives Dispatcher TTL.
5. Confirm `/headers` lists `Cache-Control` and `Content-Type`.
6. Deploy Dispatcher via normal AMS CSE / pipeline review — do not overwrite unrelated project rules.
7. Smoke: `/robots.txt`, `/sitemap.xml`, `/llms.txt`, one `…/page.md` through the public host.

## On-prem steps

Same merge pattern against the local publish farm. Enable `ModMimeUsePathInfo On` and `DispatcherUseProcessedURL 1` on the vhost (see `discoverability_publish.vhost`).

## Errors must not cache as success

Suite servlets send `Cache-Control: no-store` on 404/feature-off. With `/enableTTL`, Dispatcher should not retain those as warm public objects. Still verify:

- A disabled-feature 404 is not served as 200 from cache after enablement without purge.
- CDN does not apply a long positive TTL to 4xx/5xx (prefer respect origin, or short error TTL).

## CDN

- Cache key: host + path (project isolation).
- Align CDN TTL with the freshness matrix in `docs/plan/09-dispatcher-and-cdn-caching.md`.
- CDN purge is **ops/manual** in v1 (no product purge adapter).
- Activation invalidates Dispatcher; CDN may remain stale until TTL or purge.

## Checklist (guide §10)

- [ ] Exact routes permitted by Dispatcher filters
- [ ] Unintended servlet paths remain blocked
- [ ] Cache-Control verified at origin, Dispatcher, CDN
- [ ] CDN cache key is host/project aware
- [ ] Activation + config-publish freshness tested
- [ ] Error response caching reviewed
- [ ] Rollback plan documented
- [ ] Existing project rules preserved
