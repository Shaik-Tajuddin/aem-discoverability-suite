# AEM Discoverability Suite — Feature Documentation

**Documentation set:** Feature Guides 1.0  
**Architecture baseline:** AEM Discoverability Suite — Architecture Baseline 1.0  
**Target:** AEM 6.5.17+ · Java 11 runtime  
**Generation:** On-demand on Publish  
**Status:** Design and author guidance only — implementation is not yet authorized

> These guides explain the intended v1 behavior, configuration responsibilities, author workflows, best practices, sample outputs, and validation. They do not define final Java APIs, exact CA Config resource names, dialog field names, servlet registrations, or Dispatcher syntax. Those belong in the detailed technical specification.

## 1. Feature catalogue

| File | Feature | Primary audience |
|---|---|---|
| [01-platform-configuration-and-project-isolation.md](01-platform-configuration-and-project-isolation.md) | Project resolution, CA Config, environments, enablement | AEM admins, architects |
| [02-shared-eligibility-and-canonical-urls.md](02-shared-eligibility-and-canonical-urls.md) | Shared page eligibility, canonical URLs, reference safety | Authors, SEO admins, developers |
| [03-robots-txt.md](03-robots-txt.md) | Robots directives and sitemap discovery | SEO admins, authors |
| [04-xml-sitemap.md](04-xml-sitemap.md) | Custom XML sitemap index and partitions | SEO admins, authors |
| [05-multilingual-and-hreflang.md](05-multilingual-and-hreflang.md) | Language relationships, hreflang, x-default | Authors, translation managers, SEO admins |
| [06-markdown-page-output.md](06-markdown-page-output.md) | Page-to-Markdown output using Component Extractor SPI | Authors, content architects, developers |
| [07-llms-txt.md](07-llms-txt.md) | Site-level `llms.txt` | Content strategists, SEO/GEO admins |
| [08-json-ld-structured-data.md](08-json-ld-structured-data.md) | JSON-LD graph, entity components, HTML head integration | Authors, SEO admins, developers |
| [09-dispatcher-and-cdn-caching.md](09-dispatcher-and-cdn-caching.md) | Dispatcher/CDN caching and freshness | AEM/infra admins |
| [10-diagnostics-and-operations.md](10-diagnostics-and-operations.md) | Logs, metrics, troubleshooting, operational checks | AEM admins, operations |

## 2. How to read these guides

- **Author** means a content author working with pages/components in AEM Sites.
- **Configuration owner** means a user with permission to manage project-level Context-Aware Configuration.
- **AEM administrator** means a user responsible for OSGi services, packages, Publish, and environment settings.
- **Infrastructure administrator** means a user responsible for Dispatcher, web-server/CDN configuration, and deployment.
- **Developer** means the team implementing and extending the suite.

Authors should not be expected to edit robots rules, sitemap XML, generated Markdown, or generated JSON-LD by hand. They author source content and metadata in AEM. The suite generates public outputs from that content and approved configuration.

## 3. Confirmed design decisions

| Decision | Baseline |
|---|---|
| Sitemap implementation | Completely custom sitemap engine |
| Caching | Dispatcher/CDN first; no dedicated application cache in v1 |
| Discovery | Streaming/batched page discovery |
| Markdown | Component Extractor SPI |
| AEM compatibility | AEM 6.5.17+ |
| Runtime | Java 11 |
| Generation | On-demand, Publish-side |
| v1 scope | Outputs and lightweight diagnostics |

## 4. Common operating model

1. Resolve exactly one project for the request.
2. Resolve and validate its effective configuration.
3. Discover pages under that project root in bounded batches.
4. Apply the shared eligibility policy.
5. Resolve public canonical URLs and valid language/reference relationships.
6. Pass eligible content to the selected output generator.
7. Stream a deterministic output with suitable content type and cache headers.
8. Log lightweight diagnostics without exposing protected content.

All feature guides assume this common flow.

## 5. Important authoring rule

A page appearing in AEM Author does not automatically mean it belongs in public discovery outputs. It must be available to anonymous users on Publish, belong to the resolved project, pass the common eligibility policy, and satisfy the output-specific rules.

## 6. Reference and status conventions

- **Confirmed:** a decision already selected in the architecture baseline.
- **Proposed logical setting:** a configuration concept; exact field name and persistence location are to be finalized.
- **Illustrative endpoint:** sample route only; exact selector/extension mapping is a detailed-design item.
- **Illustrative output:** a standards-shaped sample using example domains/content, not a claim that the implementation already exists.

## 7. External references

- [AEM 6.5 SEO and URL management](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/managing/managing-further-reference/seo-and-url-management)
- [wcm.io Context-Aware Configuration](https://wcm.io/caconfig/)
- [wcm.io CA Config Editor](https://wcm.io/caconfig/editor/)
- [Adobe Dispatcher configuration](https://experienceleague.adobe.com/en/docs/experience-manager-dispatcher/using/configuring/dispatcher-configuration)
- [Google Search Central](https://developers.google.com/search/docs)

## 8. Approval boundary

This documentation set does not authorize coding or deployment. The next engineering artifact should be a detailed technical implementation specification. Coding begins only after explicit approval.
