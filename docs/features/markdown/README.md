# Page Markdown

Renders an eligible page as Markdown via the `.md` extension. Off by default.

## Authoring

1. Open the CA Config Editor for the site context.
2. Edit **Discoverability Markdown**.
3. Set:
   - **Enabled** = true
   - **Public domain** = trusted origin (required when enabled — empty domain causes fail-closed 404)
   - Optional: timeout, max output bytes, component depth/count/references, cache max-age
4. Save and publish config.

Page-level opt-out (on `jcr:content`):

| Property | Effect |
|---|---|
| `discoverabilityExclude` | Excluded from Markdown (and other suite outputs) |
| `discoverabilityNoIndex` | Treated as noindex / ineligible |
| `cq:redirectTarget` | Ineligible |
| `cq:canonicalUrl` | Optional absolute canonical; must match public origin |

### Preview on Author

```
http://localhost:4502/content/<site>/.../<page>.md
```

Example: `/content/wknd/us/en/faqs.md`.

### Public URL

Dispatcher short-path rewrite: `/{relative}.md` → `/content/{project}/.../{relative}.md`.

## Example output

```markdown
# FAQs

Canonical: https://www.example.com/us/en/faqs
Path: /content/wknd/us/en/faqs
Locale: en-US

![](https://www.example.com/content/dam/wknd-shared/en/adventures/climbing-new-zealand/adobestock-277768563.jpeg)

WKND is a collective of outdoors, music, crafts, adventure sports, and travel enthusiasts...

### Who is WKND's intended audience?

We believe the best ...
```

## Notes

- Unsupported components are skipped (not fatal).
- Over size/time limit → **404** (no truncated body).
- Feature off / ineligible / missing `publicDomain` → **404** `no-store`.
- Code: `core/.../geo/markdown/`.
