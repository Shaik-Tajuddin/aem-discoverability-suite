# Features

Authoring and output guides for each Discoverability Suite feature. Every feature is **off by default** and has its own CA Config (including `publicDomain`).

| Feature | Package | Author guide | Author URL pattern | Public route (Dispatcher) |
|---|---|---|---|---|
| [robots.txt](robots/README.md) | `seo/robots` | [README](robots/README.md) | `{projectRoot}.robots.txt` | `/robots.txt` |
| [XML sitemap](sitemap/README.md) | `seo/sitemap` | [README](sitemap/README.md) | `{projectRoot}.sitemap.xml` | `/sitemap.xml`, `/sitemap-N.xml` |
| [Page Markdown](markdown/README.md) | `geo/markdown` | [README](markdown/README.md) | `{pagePath}.md` | short `/{path}.md` under project |
| [llms.txt](llms/README.md) | `geo/llm` | [README](llms/README.md) | `{projectRoot}.llms.txt` | `/llms.txt` |

Shared prerequisites: site root must have `sling:configRef` / `cq:conf`; install notes in [`docs/ENABLEMENT.md`](../ENABLEMENT.md).
