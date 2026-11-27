# Shared Page Eligibility and Canonical URL Policy

**Feature area:** Shared content-processing engine  
**Audience:** Authors, SEO owners, AEM administrators, developers  
**Status:** Phase 2 implemented (2026-11-27) — see `tasks/plan.md` / CHANGELOG field contract

## 1. Purpose

Every public output must agree on which pages are eligible and which URL represents each page. This guide defines the shared policy used by robots-adjacent diagnostics, sitemap, Markdown, llms.txt, hreflang, and JSON-LD integrations.

A page being present in the repository is not enough. The page must be publicly available and eligible under the resolved project's policy.

## 2. Shared eligibility flow

```text
Discovered page candidate
        |
Inside resolved project root?
        |
Available on Publish / public?
        |
Anonymous access allowed?
        |
Not explicitly excluded?
        |
Not noindex under the shared policy?
        |
Not an invalid redirect / duplicate destination?
        |
Valid canonical public URL?
        |
Eligible for shared processing
        |
Apply output-specific rules
```

### 2.1 Common exclusion conditions

- Page is not available on Publish.
- Page is outside the resolved project root.
- Page is protected by CUG or not anonymously visible.
- Page has an explicit suite exclusion property.
- Page is marked `noindex` under the agreed policy.
- Page redirects or is not the preferred canonical destination.
- Canonical URL is missing, malformed, or outside the allowed domains.
- Page is invalid according to the project's publishing/discoverability rules.

The service should return a structured eligibility result and an exclusion reason, not only `true` or `false`.

## 3. Author guide: controlling page inclusion

Exact page-property names are implementation details. The eventual authoring UI should expose clear labels similar to the following:

| Illustrative authoring control | Intended result |
|---|---|
| Include in discoverability outputs | Explicitly include/exclude where policy allows |
| Search indexing | Index / noindex intent |
| Canonical URL | Preferred public URL, if an override is permitted |
| Page language | Locale associated with the page |
| Redirect destination | Destination used by the site's redirect implementation |

### Author workflow

1. Open the page in AEM Sites.
2. Open Page Properties.
3. Review the SEO/Discoverability section, when delivered by the project.
4. Confirm that the page is intended for anonymous public access.
5. Set the indexing/exclusion intent according to content policy.
6. Use a canonical override only when the page intentionally represents a different canonical destination.
7. Save and request publication through the normal workflow.
8. After Publish, validate the page HTML, canonical URL, and applicable generated outputs.

Authors must not use a discoverability exclusion as a substitute for access control. Confidential information must be secured through AEM permissions/CUG and delivery controls.

## 4. Canonical URL service

The suite has one CanonicalUrlService. It is the only component that creates absolute public URLs.

Responsibilities:

- Use approved AEM Externalizer and mapping configuration.
- Resolve the public domain from trusted project configuration.
- Normalize path segments and encoding.
- Apply canonical override only after validation.
- Enforce a domain allowlist.
- Handle vanity/mapping policy consistently.
- Support language-specific domains.
- Reject untrusted Host-header values as a source of canonical domains.

### Canonical consistency

The canonical URL in HTML, the URL in the sitemap, and links generated for Markdown/llms should agree. Conflicting canonical signals make the preferred URL ambiguous.

Google recommends consistent canonical signals and warns against specifying different canonical URLs through different methods. See [Google canonicalization guidance](https://developers.google.com/search/docs/crawling-indexing/consolidate-duplicate-urls).

## 5. Best practices

- Use a self-referential canonical for the preferred page URL.
- Keep canonical URLs absolute, stable, HTTPS, and on the approved domain.
- Avoid canonical chains and canonical loops.
- Do not canonicalize a page to an unrelated page merely to suppress duplicates.
- Do not use robots.txt to choose a canonical.
- Do not use `noindex` as a replacement for canonicalization when a duplicate should consolidate with a preferred URL.
- Keep internal links pointed at canonical URLs.
- Do not include non-canonical duplicates in the sitemap.
- Ensure `noindex` pages are not accidentally advertised as indexable sitemap destinations under the suite's eligibility policy.
- A canonical URL is not an access-control mechanism.

## 6. Example HTML output (illustrative)

```html
<link rel="canonical"
      href="https://www.example.com/about-us" />
<meta name="robots" content="index,follow" />
```

For an excluded page, the eventual implementation may emit a noindex directive according to site policy. The exact page-property mapping and whether the policy is applied by existing project components or this suite must be finalized during detailed design.

## 7. Example eligibility diagnostic

```json
{
  "project": "wknd",
  "path": "/content/wknd/en/about-us",
  "eligible": false,
  "reason": "NOINDEX",
  "output": "sitemap"
}
```

Example only. Actual event names and diagnostic schema remain to be defined.

## 8. References and validation

- [Google canonical URL guidance](https://developers.google.com/search/docs/crawling-indexing/consolidate-duplicate-urls)
- [Google robots and indexing overview](https://developers.google.com/search/docs/fundamentals/get-started)

Validation checklist:

- [ ] All output generators use the same eligibility service.
- [ ] CUG/protected pages and references are excluded.
- [ ] No cross-project page is discovered.
- [ ] Canonical domain belongs to the resolved project.
- [ ] Canonical URL agrees across HTML and sitemap.
- [ ] Invalid canonical overrides are rejected.
- [ ] Exclusion reason is available in diagnostics.
