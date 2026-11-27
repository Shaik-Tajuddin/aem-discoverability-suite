# Multi-language, Locale Mapping and hreflang

**Feature area:** Shared platform / SEO  
**Audience:** Authors, translation managers, AEM administrators, SEO owners  
**Status:** Phase 3 implemented (2026-11-27) — see `tasks/plan.md`

## 1. Purpose

This feature connects valid language/locale page variants so that generated sitemap and HTML metadata can describe relationships between equivalent localized pages.

It supports:

- MSM live relationships where appropriate.
- Explicit configured mappings.
- Optional same-relative-path mapping.
- Language-specific public domains.
- Hreflang in sitemap and/or HTML.
- Optional `x-default` destination.

## 2. Architecture

The LanguageRelationshipService is shared. Individual output generators must not independently guess language counterparts.

Resolution order:

1. MSM live relationship, if valid.
2. Explicit configured language mapping.
3. Same-relative-path mapping, only if enabled.
4. Verify target page is eligible.
5. Verify locale and canonical domain.
6. Omit invalid relationship.

```text
Source page
   |
Find language relationship
   |
Resolve target page
   |
Check project + public visibility + eligibility
   |
Resolve target canonical URL
   |
Validate hreflang locale
   |
Add valid alternate relationship
```

## 3. Author guide

### 3.1 Creating a language variant

1. Open the source page in AEM Sites.
2. Use the project's approved translation/MSM workflow to create the language copy or live copy.
3. Create the equivalent page in the target language.
4. Translate the main content, page title, headings, metadata, and image alternative text where needed.
5. Confirm the target page is in the configured language root.
6. Confirm the page language/locale metadata is correct.
7. Review the translated page's canonical and indexing settings.
8. Publish both source and target pages.
9. Verify the language alternate set in the rendered HTML or sitemap after publication.

The author should not manually add arbitrary hreflang tags to page text. Relationship metadata should be managed through the approved AEM authoring/translation workflow and the central language configuration.

### 3.2 Author checklist

- Is this actually an equivalent language/region version of the same page?
- Is the main page content translated or appropriately localized?
- Does the target page have its own canonical URL?
- Is the page published and publicly accessible?
- Does the page's configured language match the content?
- Does each variant have a return relationship to the other variants?

## 4. Locale configuration

Illustrative language map:

```yaml
languages:
  - root: /content/wknd/en
    locale: en
    publicDomain: https://www.example.com
  - root: /content/wknd/de
    locale: de
    publicDomain: https://www.example.de
  - root: /content/wknd/fr
    locale: fr-FR
    publicDomain: https://www.example.fr

xDefault:
  path: /content/wknd/en
```

This is illustrative logical configuration, not the final CA Config schema. Locale values must follow supported language/region conventions.

## 5. Example HTML output

```html
<link rel="alternate"
      hreflang="en"
      href="https://www.example.com/about-us" />
<link rel="alternate"
      hreflang="de"
      href="https://www.example.de/uber-uns" />
<link rel="alternate"
      hreflang="fr-FR"
      href="https://www.example.fr/a-propos" />
<link rel="alternate"
      hreflang="x-default"
      href="https://www.example.com/language-selector" />
```

The same language alternate set should be emitted for each member of the relationship group. Invalid or unavailable variants should be omitted rather than pointing to dead pages.

## 6. Example sitemap output

```xml
<url>
  <loc>https://www.example.com/about-us</loc>
  <xhtml:link rel="alternate"
              hreflang="en"
              href="https://www.example.com/about-us"/>
  <xhtml:link rel="alternate"
              hreflang="de"
              href="https://www.example.de/uber-uns"/>
  <xhtml:link rel="alternate"
              hreflang="fr-FR"
              href="https://www.example.fr/a-propos"/>
</url>
```

A production sitemap must declare the `xhtml` namespace on the root `urlset`, and each language variant's URL entry must carry the complete valid alternate set according to the selected implementation.

## 7. Best practices

- Use unique URLs for each language version.
- Use language codes and optional region codes correctly.
- Each page should list itself and the other valid variants.
- Ensure alternate relationships are bidirectional.
- Use fully qualified absolute URLs.
- Keep each variant's canonical URL in the same language where possible.
- Do not point every language version at one default-language canonical.
- Do not generate alternates to unpublished or protected pages.
- Use `x-default` for a useful fallback, often a language selector, only when the target is valid.
- Avoid multiple competing hreflang implementations unless there is a deliberate reason; keep HTML and sitemap relationships consistent.

Google's [localized versions guidance](https://developers.google.com/search/docs/specialty/international/localized-versions) describes self-referencing and reciprocal alternate links, fully qualified URLs, valid language/region codes, and x-default.

## 8. Validation checklist

- [ ] Every URL is fully qualified.
- [ ] Locale codes are valid language/region identifiers.
- [ ] Each page lists itself.
- [ ] Return links exist for linked variants.
- [ ] Canonical URL is appropriate for each language.
- [ ] No unpublished, CUG, or excluded target appears.
- [ ] x-default resolves and is intentionally selected.
- [ ] HTML and sitemap relationships do not conflict.

## 9. Troubleshooting

| Symptom | Check |
|---|---|
| Missing language link | Translation/MSM relationship, configured root, target eligibility |
| hreflang ignored | Return link missing, invalid locale, non-absolute URL |
| Wrong domain | Per-language public domain mapping |
| All variants canonicalize to English | Incorrect canonical override or template policy |
| x-default absent | Not configured or target fails eligibility |

## 10. Reference

- [Google Search Central — Localized versions](https://developers.google.com/search/docs/specialty/international/localized-versions)
- [Google Search Central — Canonicalization](https://developers.google.com/search/docs/crawling-indexing/consolidate-duplicate-urls)
