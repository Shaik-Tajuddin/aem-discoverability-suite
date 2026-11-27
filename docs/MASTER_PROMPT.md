# Master Prompt — Build the AEM Discoverability Suite

> **SUPERSEDED for v1 implementation.** Source of truth is [`docs/plan/`](plan/) and [`tasks/plan.md`](../tasks/plan.md). This prompt targets an earlier activation/`/var`/dashboard model that is **out of scope for v1**. Do not implement from this file.

> Historical note: this prompt was written for scaffolding from an empty folder against the old architecture doc.

---

## Role and goal

You are a senior AEM engineer building **AEM Discoverability Suite**, a commercial, installable AEM 6.5 package. It generates `robots.txt`, `sitemap.xml`, `llms.txt`, Markdown page renditions and JSON-LD structured data per project, and provides an author-only Granite UI dashboard. Read `ARCHITECTURE.md` fully before writing any code, and follow it. If something in this prompt and `ARCHITECTURE.md` conflict, stop and ask.

## Fixed project facts

| Item | Value |
|---|---|
| Product name | AEM Discoverability Suite |
| Repository / artifactId | `aem-discoverability-suite` |
| groupId and Java package | `com.aem.discoverability` |
| appId | `discoverability` |
| Paths | `/apps/discoverability`, `/conf/global/settings/discoverability`, `/conf/<project>/settings/discoverability`, `/var/discoverability/<project>` |
| Target | AEM 6.5.17+ on-premise and AMS; must also run on AEM 6.5 LTS |
| Java | 11 source/target |
| Build | Maven, AEM Project Archetype 57 |
| Tests | JUnit 5, wcm.io AEM Mocks, Mockito |

Scaffold command (run once, in phase 0):

```bash
mvn -B org.apache.maven.plugins:maven-archetype-plugin:3.3.1:generate -D archetypeGroupId=com.adobe.aem -D archetypeArtifactId=aem-project-archetype -D archetypeVersion=57 -D appTitle="AEM Discoverability Suite" -D appId="discoverability" -D artifactId="aem-discoverability-suite" -D groupId="com.aem.discoverability" -D package="com.aem.discoverability" -D version="1.0.0-SNAPSHOT" -D aemVersion="6.5.17" -D frontendModule="none" -D includeDispatcherConfig="y" -D includeExamples="n" -D includeErrorHandler="n" -D datalayer="n"
```

## Non-negotiable rules

1. **Never write to `/libs`.** Overlays only under `/apps`; Tools nav entry under `/apps/cq/core/content/nav/tools/general/discoverability`.
2. **No customer code changes required** beyond one documented include node for the page properties tab and dispatcher snippets.
3. **Live truth:** content-derived output is generated on publish from published content only. Never include pages under closed user groups, unpublished pages or draft values.
4. **Secrets:** only in author-run-mode OSGi factory configs, read through `CryptoSupport`; never in `/conf`, never logged, never returned by any servlet.
5. **Security:** dashboard and data/preview servlets require an authenticated AEM author session. **Access groups and `PermissionService` are deferred** — do not add `discoverability-admins` / `discoverability-users` / project-mapped groups or per-project ACL filtering in v1. Secrets stay author-only OSGi. When access groups return later, every servlet must check the requesting user before service-user reads; render conditions stay UX-only.
6. **No deprecated APIs:** no `loginAdministrative`, no `getAdministrativeResourceResolver`, no run-mode checks via `SlingSettingsService` (use run-mode OSGi configs), no JCR session leaks — always close resolvers.
7. **OSGi:** Declarative Services R7 annotations and metatype (`@Component`, `@Designate`, `@ObjectClassDefinition`). Every service has a sensible default config.
8. **Dependencies:** embed jsoup and flexmark (if used) with bnd `-conditionalpackage` or `Embed-Dependency`. Add nothing that AEM 6.5.17 does not provide unless embedded.
9. **Packaging:** narrow `filter.xml` roots; `ui.content` uses `mode="merge"`; the `dispatcher` module is excluded from `all`.
10. **Tests:** each phase ships unit tests; target at least 80% line coverage on `core`. `mvn clean install` must pass before a phase is done.
11. **One code path for preview and publish.** Every generator takes a `GenerationContext` (effective config, page source, output writer) and has no other inputs or side effects. Publish writes to `/var`; preview writes to the HTTP response. Never duplicate generation logic for preview.
12. **Do not invent requirements.** If a detail is missing, choose the simplest option consistent with `ARCHITECTURE.md`, write it in the Decisions log section of `ARCHITECTURE.md`, and mention it in your phase summary.

## How to work

- Build **one phase at a time**, in order. At the end of each phase: run `mvn clean install`, fix failures, then write a short summary (what was built, files touched, decisions made, open questions) and **wait for confirmation** before starting the next phase.
- Prefer small, focused classes with interfaces in `api` subpackages and implementations in `impl`.
- Write Javadoc on every public interface. Keep a `CHANGELOG.md`.
- When you are unsure how AEM 6.5 behaves (for example servlet resolution on `cq:Page`, rewriter pipelines, render conditions), write a small test or note it as an open question; don't guess silently.

---

## Phase 0 — Scaffold and reshape

1. Run the scaffold command above.
2. Remove the sample site: pages under `/content/discoverability`, templates and policies under `/conf/discoverability`, `ui.tests` and `it.tests` sample content (keep the modules if they build cleanly; empty them otherwise).
3. Repurpose `ui.content` to hold only `/conf/global/settings/discoverability` defaults (merge mode).
4. Exclude the `dispatcher` module from the `all` package; keep it as reference.
5. Create the package layout inside `core`: `platform`, `seo`, `geo`, `geo.markdown`, `aeo`, `delivery`, `delivery.cdn`, `dashboard`, each with `api` and `impl`.
6. Add test dependencies (wcm.io AEM Mocks JUnit 5) and JaCoCo.

**Done when:** `mvn clean install` passes and the `all` package installs on a clean AEM 6.5.17 author with no errors in `error.log`.

## Phase 1 — Platform core

Build in `core/platform`:

- `ProjectConfig` — Context-Aware Configuration interface (`@Configuration(label = "Discoverability Project")`) with nested settings for robots, sitemap, llms.txt, Markdown, JSON-LD, and connection name. Every field has a code default. **Do not add access group lists (editors/viewers) yet.**
- `ProjectRegistry` — lists registered projects (stored under `/conf/<project>/settings/discoverability/registration`), resolves the project for a resource, and maps hostnames to projects.
- `ConfigResolver` — resolves effective config for a resource: product defaults → project config → page properties.
- `PageWalker` — iterates live, anonymous-visible pages of a project: skips `hideInNav`-style exclusions configured in project config, pages with closed user group policies, excluded templates and the page-level exclude flag.
- `UrlService` — externalizes URLs via `Externalizer` and `ResourceResolver.map`.
- `GenerationContext` — immutable holder of effective config, a `PageSource` and an output target. Two `PageSource` implementations: `PublishedPageSource` (publish: all live pages) and `PreviewPageSource` (author: pages whose replication status is activated, with an optional flag to include unpublished pages, and the ability to report which values differ from the last published version where detectable).
- `EnvironmentGuard` — OSGi config `environmentType` (`dev`, `stage`, `prod`) set per run mode; non-prod forces disallow-all robots and no production URLs.
- Repoinit in `ui.config`: service users `discoverability-reader` (read on `/content`, `/conf`) and `discoverability-writer` (write on `/var/discoverability`); ACLs on `/var/discoverability`. Service user mappings per bundle. **Skip product groups** (`discoverability-admins`, `discoverability-users`) and `PermissionService` for now.

**Tests:** config precedence, hostname mapping, CUG exclusion, environment guard.

## Phase 2 — robots.txt and sitemap.xml (SEO)

- Generators in `core/seo` writing to `/var/discoverability/<project>/`:
  - `RobotsGenerator`: rules from project config, AI bot allow/deny list, sitemap reference; environment guard applied.
  - `SitemapGenerator`: `<loc>`, `<lastmod>` from last replication date, hreflang alternates across MSM language copies; split into a sitemap index above 50,000 URLs or 50 MB.
- Robots validation used by the dashboard: flag `Disallow: /` for user agent `*`, conflicts where the sitemap lists a disallowed path.
- Serving servlets bound to `cq:Page` with selectors/extensions (for example `<root>.robots.txt`, `<root>.sitemap.xml`, `<root>.sitemap-<n>.xml`) that stream the stored file and fall back to on-demand generation when it is missing. Return 404 when the feature is disabled.
- Correct content types and UTF-8.

**Tests:** robots content per config, environment guard, sitemap entries, lastmod, hreflang, index split.

## Phase 3 — Markdown and llms.txt (GEO)

- `MarkdownComponentRenderer` SPI: `boolean accepts(Resource)`, `void render(Resource, MarkdownContext)`; services selected by resource type through the `sling:resourceSuperType` chain (`ResourceResolver.isResourceType`), ordered by `service.ranking`.
- v1 renderers for Core Components: Title, Text (HTML to Markdown), Image (alt text, externalized src), List, Teaser, Container traversal.
- `MarkdownPageGenerator`: front matter (title, description, canonical URL, last modified), body from renderers, per-page coverage stats (rendered vs skipped components) stored alongside the file.
- `LlmsTxtGenerator`: llmstxt.org format: `# <site title>`, `> <summary>`, optional details, `## <section>` with `- [Title](url.md): description`; sections from page properties or the page tree. `llms-full.txt` behind its own toggle.
- Serving servlets: `page.md` (and `page.html.md`) on `cq:Page`, `<root>.llms.txt`, `<root>.llms-full.txt`; 404 when disabled or the page is excluded.

**Tests:** each renderer, proxy component resolution, coverage stats, llms.txt format, exclusions.

## Phase 4 — JSON-LD and page properties (AEO)

- `JsonLdContributor` SPI; contributors: Organization and WebSite (home page only, from project config), BreadcrumbList (every page), page-type schema from template mapping and page properties, auto-mapping (Accordion → FAQPage, Image → image), JSON-LD component instances.
- `JsonLdGraphBuilder`: applies precedence (component beats auto-mapping for the same type; page overrides on top), deduplicates, validates required properties for the core types (WebPage, Article, Product, FAQPage, Event, Organization, WebSite, BreadcrumbList), records the source of each node.
- Head injection: a Sling Rewriter `TransformerFactory` (global pipeline, publish) inserts one `<script type="application/ld+json">` before `</head>`; skips types the page already emits unless configured to replace; escapes `</script>`. Provide an HTL include fallback (`/apps/discoverability/components/jsonld-head`).
- Page properties tab at `/apps/discoverability/dialogs/page-properties-tab` storing values under `jcr:content/discoverability`: JSON-LD switch, type, map-from-components, overrides, admins-only custom JSON-LD, read-only preview; Markdown exclude, LLM description, llms.txt section, selector override, draft preview. Render conditions hide sections when the project feature is off.
- JSON-LD component `/apps/discoverability/components/jsonld`: component group "AEM Discoverability Suite"; dialog fields switch by schema type; renders nothing on publish; edit-mode summary card with validity; multiple instances allowed; body output only as fallback when the transformer is inactive.
- Preview servlet returning the merged graph with sources (author only; authenticated session).
- Document the one include node customers add to their base page component dialog.

**Tests:** each contributor, precedence, dedupe, validation, escaping, transformer injection.

## Phase 5 — Delivery: triggers, batching, flush, purge

- On publish: a `ResourceChangeListener` on registered project roots and `/conf/<project>` enqueues Sling Jobs; a debounced job (quiet period configurable, default 2 minutes) regenerates once per project; single-page activation regenerates that page's `.md` immediately and the project files in the batch.
- Handle deactivation, deletion, move/rename (remove old `.md`, update sitemap and llms.txt).
- On author: after replication of project content or config, `DeliveryService` flushes affected files on dispatcher (flush agents) and, when the project references a connection, calls the CDN purge.
- `CdnAdapter` SPI with Akamai (Fast Purge API, EdgeGrid auth) and Cloudflare (purge by URL) implementations.
- `ConnectionConfig` OSGi factory (author run mode): name, provider, endpoint, zone/property ids, auth mode (`access-key` or `instance-identity`), protected secrets via `CryptoSupport`; hot reload on modification; never logged.
- Publish `StatusEndpoint` (internal only): last generation time, page counts, errors, coverage per project; protected by a technical credential from OSGi config.

**Tests:** debounce behaviour, removal handling, adapter request building (mock HTTP), secret handling never logged.

## Phase 6 — Dashboard (Granite UI, author only)

- Console at `/apps/discoverability/content/dashboard` with Tools nav entry; available to authenticated authors (no product access-group gating in v1).
- Views: Overview (projects, features, live status, last regeneration, issues); Project detail (config editor with feature toggles and show/hide, pending vs live diff, robots validation and preview, generated outputs, Markdown coverage, JSON-LD validity, live checks); Project registration and connection status.
- Project filter: project → locale, conditions (has issues, live check failing, unpublished config, feature enabled), state in URL hash/query, last selection remembered per user.
- Data servlets (JSON) bound to resource types under `/apps/discoverability`; require an authenticated author session. No per-project group filtering yet.
- `LiveVerificationService`: fetches public URLs per project through the CDN, compares with expected output, records status and time.
- **Preview before publish:** each output card (robots.txt, sitemap.xml, llms.txt, llms-full.txt, Markdown per page, JSON-LD per page) shows three links:
  - **Preview** — author-only servlet generating the output in memory from the saved (unpublished) project config using `PreviewPageSource`; never written to `/var`, sent with `Cache-Control: no-store`; authenticated author session required.
  - **Live** — the public URL through the CDN.
  - **Diff** — line-by-line diff of preview against live.
  - Sitemap preview shows the entry count, the first 200 entries and a full download. Markdown and JSON-LD previews take a page picked from the project tree. Values that come from unpublished page edits are highlighted.
  - The Publish config confirmation dialog shows the diff summary and validation results; dangerous robots rules block publishing.
  - On non-production authors, show the environment guard output next to the configured rules.
  - Page properties tab links to the page's Markdown and JSON-LD previews (labelled draft).
  - Preview servlet paths are added to the dispatcher deny rules for publish.
- Publish config action from the dashboard with confirmation; audit log of config changes, publishes and connection changes under `/var/discoverability/audit`.
- Small embedded chart library in the dashboard clientlib for coverage and (later) traffic charts.

**Tests:** authenticated-only servlets, live verification comparison.

## Phase 7 — Hardening and release

- Reference dispatcher snippets in `dispatcher/` for on-prem and AMS: vhost rewrites for `/robots.txt`, `/sitemap.xml`, `/llms.txt`, `/llms-full.txt`, `*.md`; filters allowing them; deny rules for dashboard, data and status paths on publish; cache rules. Plus a ready-to-send AMS CSE request template.
- Coexistence check on install: detect existing sitemap/robots servlets or files and report them in the dashboard; features stay off until enabled.
- Install, upgrade and uninstall tested on clean 6.5.17 author/publish and 6.5 LTS.
- Docs in `docs/`: installation, project setup, page properties include, dispatcher and CDN setup per platform, connection setup with least-privilege scopes, troubleshooting.

**Done when:** full build passes, coverage target met, install/uninstall clean, and the WKND sample site can be enabled end to end with live verification green.

---

## Out of scope for this build (v2+)

Traffic analytics (log ingestion, data contract, bot classification, traffic views), companion cloud functions, additional CDN presets beyond Akamai and Cloudflare, additional schema types, per-locale access control, product access groups / `PermissionService`, AEM as a Cloud Service. Design interfaces so these can be added without breaking changes, but do not implement them.

## Output expected after every phase

1. Summary of what was built.
2. List of files added or changed.
3. Decisions made (also appended to `ARCHITECTURE.md`).
4. Open questions.
5. Result of `mvn clean install`.
