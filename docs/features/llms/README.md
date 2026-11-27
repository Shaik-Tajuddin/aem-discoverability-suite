# llms.txt

Site-level Markdown index of eligible pages for LLM discovery. Off by default.

## Authoring

1. Open the CA Config Editor for the site context.
2. Edit **Discoverability llms.txt**.
3. Set:
   - **Enabled** = true
   - **Public domain** = trusted origin (required when enabled)
   - **Site name** (H1), optional summary / context text
   - Optional: sections (`Title|/content/path/prefix`), include/exclude path prefixes, max links/bytes, batch/depth, cache max-age
4. Save and publish config.

Optional page property: `discoverabilityLlmsSection` — section title override (must match a configured section name).

### Preview on Author

```
http://localhost:4502/content/<site>.llms.txt
```

Example: `/content/wknd/us/en.llms.txt`.

### Public URL

Dispatcher maps `/llms.txt` → `{projectRoot}.llms.txt`.

## Example output

```markdown
# Example Company

> Digital services for customers.

## Pages

- [Home](https://www.example.com/us/en): Welcome to our site.
- [About Us](https://www.example.com/us/en/about-us): Who we are.
- [Adventures](https://www.example.com/us/en/adventures): Join us on one of our next adventures.
```

With sections configured:

```markdown
# Example Company

## Products

- [Widgets](https://www.example.com/us/en/products/widgets)

## Company

- [About Us](https://www.example.com/us/en/about-us)
```

## Notes

- Links use HTML canonical URLs (not `.md`).
- Over `maxOutputBytes` → **404** (no truncated body).
- Feature off / missing `publicDomain` → **404** `no-store`.
- Code: `core/.../geo/llm/`.
