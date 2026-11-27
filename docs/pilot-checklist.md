# Pilot E2E checklist (Publish + Dispatcher)

Use a real pilot tree (WKND or customer site) with CA Config pointing at that project root and a trusted public domain.

## Preconditions

- [ ] `all` package installed on Author + Publish
- [ ] Platform enabled; public domain set; environment policy correct
- [ ] Dispatcher snippets merged; `CONTENT_FOLDER_NAME` / rewrite root correct
- [ ] Features under test enabled and config published

## Routes (through public host)

| # | Check | Pass? |
|---|---|---|
| 1 | `GET /robots.txt` → 200, expected rules, `Cache-Control` ~1200 | |
| 2 | Feature-off robots → 404 `no-store` (spot-check once) | |
| 3 | `GET /sitemap.xml` → well-formed XML; locs are absolute canonical HTTPS | |
| 4 | If partitioned: `GET /sitemap-0.xml` returns urlset | |
| 5 | Eligible page `GET /…/page.md` → Markdown; ineligible → 404 | |
| 6 | `GET /llms.txt` → site H1/summary; links absolute + eligible only | |
| 7 | HTML page view-source: one `application/ld+json` graph when JSON-LD on | |
| 8 | No cross-project URLs in sitemap / llms / JSON-LD | |
| 9 | Publish logs show `discoverability.generation.completed` JSON for exercised outputs | |
| 10 | After page activation, stale window matches freshness matrix (`docs/plan/09`) | |

## Sign-off

| Role | Name | Date |
|---|---|---|
| Pilot owner | | |
| AEM admin | | |
| Dispatcher/CDN | | |

**Checkpoint 10 — v1 ready for pilot:** ________________
