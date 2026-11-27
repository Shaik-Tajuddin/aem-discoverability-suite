# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Restructured `seo` / `geo` / `aeo` packages by feature (`robots`, `sitemap`, `markdown`, `llm`, `jsonld`) with `configs`, `services`/`impl`, `servlets`, etc.

## [0.11.0-phase10] - 2026-11-27

### Added
- `GenerationEvent` / `GenerationDiagnostics` structured INFO logs (correlationId, project, output, timings, bytes, limits) — schema in `docs/generation-events.md`.
- Servlets emit generation events; robots errors use `Cache-Control: no-store`.
- Enablement/install/upgrade/uninstall notes (`docs/ENABLEMENT.md`); pilot E2E checklist (`docs/pilot-checklist.md`).

### Fixed
- `llms.txt` over `maxOutputBytes` now fails closed (404) instead of returning a truncated body (error contract §6).

### Decisions
- Coverage target for `core`: ≥40% line (JaCoCo report; not a hard fail gate in v1).
- CDN purge remains ops/manual; no product access groups.

## [0.10.0-phase9] - 2026-11-27

### Added
- Dispatcher reference snippets: filters, cache allows, invalidate globs (`dispatcher/snippets/`).
- Public rewrites for `/robots.txt`, `/sitemap.xml`, `/sitemap-N.xml`, `/llms.txt`, and short `*.md` paths.
- Freshness matrix filled in `docs/plan/09`; AMS/on-prem enablement + error-cache notes in `dispatcher/snippets/ENABLEMENT.md`.

### Decisions
- Snippets are merge-in only — do not replace immutable AMS farm/filter files.
- CDN purge remains ops/manual; product purge deferred.
- Prefer farm `/enableTTL` so origin `Cache-Control` (including `no-store` on errors) is honored.

## [0.9.0-phase8] - 2026-11-27

### Added
- `JsonLdConfig` + graph service: Organization/WebSite defaults, auto WebPage + BreadcrumbList, page `jsonld` component collector.
- Entity registry (first `@id` wins); invalid optional nodes omitted; safe JSON escaping (`<`/`>`/`&`).
- Authoring component `discoverability/components/jsonld` (Article, FAQPage, WebPage, Organization, WebSite).
- Head injection: HTL `jsonld-head` in suite page `customheaderlibs`; optional rewriter type `discoverability-jsonld`.

### Decisions
- Precedence: page components → auto WebPage/Breadcrumb → org/site defaults.
- v1 types: Organization, WebSite, WebPage, BreadcrumbList, Article, FAQPage.
- Do not replace existing rewriter pipelines; add transformer type or use HTL fallback (see `docs/jsonld-rewriter.md`).

## [0.8.0-phase7] - 2026-11-27

### Added
- `LlmsConfig` (site name, summary, context, sections as `Title|/path`, include/exclude, maxLinks, maxBytes, cache TTL).
- `LlmsTxtGenerator` reuses shared discovery/eligibility; absolute canonical links only; deterministic section+path sort; limits enforced.
- `LlmsServlet` on `cq/Page` selector `llms` + extension `txt`; `text/markdown`; `Cache-Control` max-age 3600; feature-off → 404 `no-store`.
- Optional page property `discoverabilityLlmsSection` for section override.

### Decisions
- Public route: Dispatcher maps `/llms.txt` → `{projectRoot}.llms.txt`.
- Section entries: `Section Title|/content/path/prefix`; unmatched eligible pages under `## Pages`.
- Links use HTML canonical URLs (not `.md`).

## [0.7.0-phase6] - 2026-11-27

### Added
- `ComponentExtractor` SPI with deterministic lowest-rank selection (equal ranks → skip).
- Built-in extractors: Title, Text, Image, List, Teaser, Accordion, Table, Content Fragment, Experience Fragment.
- `PageMarkdownGenerator` with depth/component/reference/output/time limits; never dumps JCR properties.
- `MarkdownServlet` on `cq/Page` extension `md` (`/content/.../page.md`); `Cache-Control` max-age 3600; feature-off/ineligible → 404 `no-store`.

### Decisions
- Public Markdown route is the page path with `.md` extension (no selector).
- Image/CF/XF links must resolve to absolute public URLs; unsafe paths are skipped.

## [0.6.0-phase5] - 2026-11-27

### Added
- Custom XML sitemap: `SitemapConfig`, partition planner (≤50k URLs / ≤50 MB, path-sorted), index/urlset writers with `lastmod` + hreflang.
- `SitemapServlet` on `cq/Page` selector `sitemap` (+ optional partition selector) + extension `xml`; `Cache-Control` max-age 3600; errors use `no-store`.
- Coexistence guard: WARN when `sling:sitemapRoot` is detected under the project tree.

### Decisions
- Routes: `{projectRoot}.sitemap.xml` (index or single urlset); `{projectRoot}.sitemap.N.xml` (partition N).
- Dispatcher should map `/sitemap.xml` → `.sitemap.xml` and `/sitemap-N.xml` → `.sitemap.N.xml`.
- Single partition returns urlset from the index endpoint; multiple partitions return a sitemap index with public `/sitemap-N.xml` locs.

## [0.5.0-phase4] - 2026-11-27

### Added
- `RobotsConfig` + `RobotsGenerator` (user-agents, allow/disallow, sitemap AUTO/MANUAL/NONE).
- Non-production / crawl-denied policy emits `Disallow: /`.
- `RobotsServlet` on `cq/Page` selectors `robots` + extension `txt`; `Cache-Control: public, max-age=1200` (configurable); feature-off → 404.

### Decisions
- Public route: Dispatcher should map `/robots.txt` → `{projectRoot}.robots.txt`.
- AUTO sitemap URL is `{publicOrigin}/sitemap.xml` (Dispatcher → `.sitemap.xml`).
- MANUAL sitemap URLs must be absolute http(s) on the project public host.

## [0.4.0-phase3] - 2026-11-27

### Added
- `LanguageRelationshipService` — resolution order LanguageManager/MSM → explicit map → optional same-relative-path; eligibility + locale + canonical validation.
- Reciprocal alternate helper (`resolveReciprocalAlternates`) with self-references and absolute URLs.
- Optional `x-default` from `LanguageConfig.xDefaultPath` when the target is eligible.

### Changed
- `LanguageConfig` / `ResolvedConfiguration` now carry `locales`, `sameRelativePathEnabled`, and `explicitMappings` (`source=target`).

## [0.3.0-phase2] - 2026-11-27

### Added
- `PageDiscoveryService` — depth-limited BFS under project root with bounded batches.
- `PageEligibilityService` — structured `EligibilityResult` / `ExclusionReason` (outside root, unpublished, CUG, exclude, noindex, redirect, bad canonical).
- `CanonicalUrlService` — absolute public URLs from trusted domain; allowlisted `cq:canonicalUrl` overrides; never Host header.

### Decisions
- Page property contract (`jcr:content`):
  - `discoverabilityExclude` (Boolean)
  - `discoverabilityNoIndex` (Boolean)
  - `cq:canonicalUrl` (String override)
  - `cq:redirectTarget` (String)
  - `cq:cugEnabled` (Boolean)

## [0.2.0-phase1] - 2026-11-27

### Added
- CA Config groups: `PlatformConfig`, `SeoConfig`, `GeoConfig`, `AeoConfig`, `LanguageConfig`, `GenerationConfig` (feature flags default off).
- `ProjectResolver` with fail-closed one/none/ambiguous/invalid resolution.
- `EnvironmentPolicy` (OSGi; `config.prod` sets production=true) and `DomainPolicy` (trusted public domain only; never Host).
- Immutable `ResolvedConfiguration` snapshot via `ResolvedConfigurationService`.
- Repoinit service user `discoverability-service` with read on `/content` and `/conf`, plus service user mapping.

## [0.1.0-phase0] - 2026-11-27

### Changed
- Spec authority for v1 is `docs/plan/`; `docs/ARCHITECTURE.md` and `docs/MASTER_PROMPT.md` are superseded for implementation.
- Removed Archetype sample site (`/content/discoverability`), Hello World component/model, and sample OSGi demos.
- Core package layout established: `platform`, `seo`, `geo`, `aeo`, `ops` (`api` / `impl`).

### Added
- JaCoCo coverage reporting on the `core` module.
