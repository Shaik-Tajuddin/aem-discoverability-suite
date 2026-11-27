# JSON-LD Structured Data

**Feature area:** AEO / structured data  
**Audience:** Content authors, SEO owners, AEM developers  
**Generation:** Publish-side graph construction and HTML head contribution  
**Status:** Phase 8 implemented (2026-11-27) — see `tasks/plan.md` / CHANGELOG

## 1. Purpose

JSON-LD provides machine-readable structured information about a page and the entities it describes. The suite builds one validated graph from approved authoring sources and automatic page metadata, then injects the resulting JSON-LD into the HTML head through a controlled integration.

Structured data can help search engines understand page entities. Correct markup does not guarantee rich-result display, ranking changes, or visibility in an AI answer.

Google recommends JSON-LD as a supported format but requires that markup be accurate, relevant, complete for the selected feature, and representative of visible page content. See [Google's general structured data guidelines](https://developers.google.com/search/docs/appearance/structured-data/sd-policies).

## 2. Architecture

### 2.1 Sources

- Page-level JSON-LD authoring components.
- Configured Header Experience Fragment, where approved.
- Automatically generated WebPage metadata.
- Automatically generated BreadcrumbList.
- Resolved entity relationships.

### 2.2 Graph-building pipeline

```text
Authoring sources
      |
JSON-LD collector
      |
Entity registry
      |
Duplicate @id validation
      |
Relationship resolution
      |
Schema/type validation
      |
Graph builder
      |
Safe JSON serialization
      |
Single JSON-LD script in HTML head
```

### 2.3 Candidate types

- Organization
- WebSite
- WebPage
- BreadcrumbList
- Article
- Product
- Event
- FAQPage
- Person
- LocalBusiness
- VideoObject
- Controlled custom types

The availability of a Schema.org type does not mean Google supports a corresponding rich result. Feature eligibility must be checked against Google's current type-specific documentation before authors populate it.

## 3. Author guide: add structured data

Exact component names, dialog fields, and supported type matrix are implementation decisions. The authoring workflow is expected to follow this pattern:

1. Open the page in AEM Sites.
2. Add or open the approved structured-data component in the page's configured metadata area.
3. Select the schema/entity type appropriate to the page.
4. Populate only facts that are present and visible on the page.
5. Use canonical public URLs for entity/page identifiers.
6. Provide stable identifiers for entities shared across pages, such as the same Organization.
7. Add relationships only to valid, public entities.
8. Save and publish through normal workflows.
9. Validate the rendered page source and structured data using Google's tools.

Authors should not paste arbitrary JavaScript into a rich-text field. Custom JSON authoring, if supported, must be constrained and validated.

## 4. Type selection examples

| Page purpose | Candidate structured type |
|---|---|
| General landing page | WebPage |
| Article/news page | Article or a more specific article type |
| Product detail | Product |
| Event detail | Event |
| Organization overview | Organization |
| Video page | VideoObject |
| FAQ content | FAQPage only when content and current search feature rules qualify |

Use the most specific valid type for the page, not the type that appears most attractive in search results.

## 5. Example JSON-LD output

Illustrative Article graph:

```html
<script type="application/ld+json">
{
  "@context": "https://schema.org",
  "@type": "Article",
  "@id": "https://www.example.com/news/example-story#article",
  "mainEntityOfPage": {
    "@type": "WebPage",
    "@id": "https://www.example.com/news/example-story"
  },
  "headline": "Example Story",
  "description": "A factual summary of the article.",
  "image": [
    "https://www.example.com/content/dam/example/story.jpg"
  ],
  "datePublished": "2026-10-12T09:00:00Z",
  "dateModified": "2026-10-15T11:30:00Z",
  "author": {
    "@type": "Person",
    "name": "Example Author"
  },
  "publisher": {
    "@type": "Organization",
    "@id": "https://www.example.com/#organization",
    "name": "Example Company"
  }
}
</script>
```

This is a schema-shaped example, not a final suite payload contract. Required and recommended properties depend on the specific Google feature/type guidelines and content.

## 6. Authoring best practices

- Mark up only content visible to readers of the page.
- Ensure JSON-LD accurately matches page title, body, dates, images, and entity facts.
- Use the correct type and properties.
- Keep `@id` values stable and unique.
- Use canonical absolute URLs.
- Do not invent reviews, ratings, prices, dates, authors, locations, or product availability.
- Ensure dates and time zones are correct.
- Keep images relevant and publicly crawlable.
- Do not mark up hidden or unrelated content.
- Do not add structured data solely to obtain a rich result.
- Revalidate markup when page content or Google's feature guidelines change.

## 7. Technical best practices

- Use a proper JSON serializer; never concatenate untrusted strings into script markup.
- Escape data correctly to prevent script-breakout/injection.
- Validate duplicate `@id` values and graph references.
- Keep graph generation request-scoped.
- Omit invalid optional contributions without breaking HTML rendering.
- Prevent duplicate script insertion.
- Ensure the HTML Rewriter integration preserves existing transformer pipelines.
- Do not inject JSON-LD into non-HTML or binary responses.

## 8. Enablement guide

### Configuration owner / AEM administrator

1. Enable JSON-LD for the project.
2. Configure which entity types are supported.
3. Configure organization/site-wide entity defaults, where appropriate.
4. Confirm which source contributes each entity to avoid duplicate ownership.
5. Configure the approved Header Experience Fragment integration, if used.
6. Publish configuration.
7. Test one page per supported entity type.
8. Verify no duplicate JSON-LD script or `@id` is emitted.

### Author

Use the approved JSON-LD component, select a type matching the page, enter visible factual values, and publish. Do not paste generated sample JSON into content unless the project specifically provides a validated raw JSON authoring mode.

## 9. Validation

Use:

- [Google Rich Results Test](https://search.google.com/test/rich-results)
- [Google Search Console URL Inspection](https://search.google.com/search-console/about)
- [Google structured data policies](https://developers.google.com/search/docs/appearance/structured-data/sd-policies)
- [Schema.org](https://schema.org/)

Validation checklist:

- [ ] JSON parses.
- [ ] Schema type matches page purpose.
- [ ] Data matches visible content.
- [ ] Entity `@id` values are unique/stable.
- [ ] Referenced entities resolve.
- [ ] Images and URLs are public.
- [ ] No duplicate script is injected.
- [ ] Existing HTML still renders when structured data is invalid.
- [ ] Type-specific required fields are present.

## 10. Troubleshooting

| Symptom | Likely cause |
|---|---|
| No JSON-LD appears | Feature disabled, no eligible source, Rewriter not active |
| Duplicate entity | Multiple sources claim same `@id` |
| Rich result test warning | Missing required/recommended property or wrong type |
| Markup is valid but not shown | Search engines do not guarantee rich-result display |
| Page HTML breaks | Unsafe serialization or faulty Rewriter integration |
| Data disagrees with visible page | Authoring/source mapping problem; correct source content |

## 11. References

- [Google — General structured data guidelines](https://developers.google.com/search/docs/appearance/structured-data/sd-policies)
- [Google — Introduction to structured data](https://developers.google.com/search/docs/appearance/structured-data/intro-structured-data)
- [Schema.org](https://schema.org/)
