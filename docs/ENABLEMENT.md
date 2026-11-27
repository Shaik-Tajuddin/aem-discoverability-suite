# Enablement, install, upgrade, uninstall (AEM 6.5.17+)

## Install

1. Build: `mvn clean install`
2. Deploy `all` package to Author (and Publish, or replicate):  
   `aem-discoverability-suite.all-*.zip`
3. Confirm OSGi bundle `aem-discoverability-suite.core` is Active.
4. Confirm repoinit created `discoverability-service` (no product access groups in v1).
5. **All features default off** until CA Config enablement under the project.

Dispatcher ZIP (`dispatcher` module) is reference-only — merge snippets; see `dispatcher/snippets/ENABLEMENT.md`.

## Configure (before public traffic)

1. Ensure the site root has `sling:configRef` / `cq:conf` (that path becomes the project root).
2. Set environment policy OSGi (production vs non-prod) for crawl.
3. Enable features in their own CA configs (`RobotsConfig` / `SitemapConfig` / `MarkdownConfig` / `LlmsConfig`) and set each feature’s `publicDomain` (and other fields) there. Authoring guides: [`docs/features/`](features/).
4. Merge Dispatcher filters/rewrites/invalidate; prefer `/enableTTL`.
5. Publish CA Config + sample pages; smoke public routes.

## Upgrade

1. Install newer `all` package over the previous version.
2. Review CHANGELOG for config field additions (defaults remain fail-closed / off).
3. Re-smoke one URL per enabled output; check generation events in Publish logs.

## Uninstall

1. Disable features in CA Config and publish.
2. Uninstall the `all` package (and module packages if installed separately).
3. Remove customer Dispatcher merges for suite routes if no longer needed.
4. Optional: delete leftover CA Config nodes under `/conf/...` for the project.
5. Bundle and servlets should be gone; no suite content is required under `/content` for clean uninstall.

## Coverage target

- JaCoCo report on `core` (`mvn -pl core test` → `core/target/site/jacoco`).
- v1 target: **≥ 40%** line coverage on `core` unit tests; raise over time — do not weaken fail-closed tests to inflate %.

## Error behavior (guide §6)

| Condition | Behavior |
|---|---|
| Unknown project / feature off | 404 + `Cache-Control: no-store` |
| Invalid / missing public domain | Fail closed (no resolve) |
| Output over byte/time limit | Controlled error (empty → 404); **no truncated success body** |
| Unsupported Markdown component | Skip + diagnostic |
| Invalid JSON-LD node | Omit node; HTML still renders |
