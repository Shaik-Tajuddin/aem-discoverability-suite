# Markdown Page Output

**Feature area:** GEO / content representation  
**Audience:** Content authors, content architects, AEM developers  
**Generation:** On-demand, Publish-side  
**Status:** Phase 6 implemented (2026-11-27) — see `tasks/plan.md` / CHANGELOG

## 1. Purpose

The Markdown output provides a clean, text-oriented representation of an eligible AEM page. It is generated from explicitly supported AEM components using the selected Component Extractor SPI.

The feature does not dump JCR properties or attempt to serialize every component. It converts approved content structures to readable Markdown.

The exact endpoint mapping is not yet fixed. An illustrative route is a `.md` representation of a public page, such as `/about-us.md`. The implementation design must decide the mapping without creating collisions with existing routes.

## 2. Architecture

```text
Request for page Markdown
        |
Resolve project and page
        |
Shared eligibility check
        |
Discover page component resources
        |
For each component:
  choose registered extractor
  extract supported fields
  resolve references safely
        |
Markdown renderer
        |
Apply depth/node/reference/output limits
        |
text/markdown response + cache headers
```

### 2.1 Component Extractor SPI

Each extractor should conceptually provide:

- A method to declare whether it supports a component resource type.
- A deterministic selection priority or unique matching rule.
- An extraction method receiving the component and Markdown context.
- A Markdown result or a safe skip result.
- Diagnostics for unsupported or malformed content.

Selection must be deterministic. Ambiguous extractors must not be selected unpredictably.

### 2.2 Initial built-in extractor candidates

- Title
- Text
- Image
- List
- Teaser
- Accordion
- Table
- Content Fragment
- Experience Fragment

The exact AEM resource types and component property mappings must be confirmed against the target site's component library.

## 3. Author guide: prepare a page for Markdown

Authors do not manually maintain the generated `.md` output.

1. Build the page using supported, semantic components.
2. Use one clear main page title and meaningful subheadings.
3. Use Text components for explanatory content rather than embedding important text in images.
4. Use descriptive link text that makes sense outside its visual context.
5. Add meaningful image alternative text; use empty alt text only for decorative images where appropriate.
6. Use List and Table components for genuinely structured information.
7. Keep accordions and tabs understandable when read in a linear text format.
8. Use Content Fragments/Experience Fragments only when the referenced content is public and intended for reuse.
9. Review page language, canonical URL, and public status.
10. Publish and validate the Markdown response.

### Authoring example

**Preferred component structure**

```text
Page
├── Title: About Us
├── Text: Introductory summary
├── Image: Team photograph (descriptive alt text)
├── Title: Our Mission
├── Text: Mission statement
├── List: Our principles
└── Teaser: Learn more
```

This is easier to represent than a page where critical information is embedded only in decorative layouts or custom scripts.

## 4. Extractor behavior

| Component | Expected Markdown behavior |
|---|---|
| Title | Heading with appropriate level |
| Text | Paragraphs and supported inline formatting |
| Image | Image link/description and alt text where available |
| List | Ordered/unordered Markdown list |
| Teaser | Heading, summary, and canonical destination link |
| Accordion | Heading and body content in readable sequence |
| Table | Markdown table when structure is suitable |
| Content Fragment | Explicitly approved fields only |
| Experience Fragment | Traverse approved child components within depth limits |

Unsupported components should be skipped with diagnostics. The system must never serialize all JCR properties as a fallback.

## 5. Example output

```markdown
# About Us

We build products and services that help customers solve meaningful
problems.

![Our team working together](https://www.example.com/content/dam/site/team.jpg)

## Our Mission

Our mission is to make reliable information and services easier to use.

## Our Principles

- Put customers first.
- Build accessible experiences.
- Protect information.
- Improve continuously.

## Learn More

[Contact our team](https://www.example.com/contact)
```

Example only. Actual formatting, heading levels, image URL transformation, and endpoint suffix are implementation decisions.

## 6. Best practices

- Use semantic components with clear content meaning.
- Maintain a logical heading hierarchy.
- Keep important information as text.
- Provide descriptive link text and useful image alt text.
- Avoid empty headings and duplicate page titles.
- Avoid conveying essential meaning only through color, image, or icon.
- Keep tables simple; use a prose/list alternative for complex nested tables.
- Ensure reusable fragments are approved for public output.
- Do not assume that hidden accordion content should be omitted; the final extractor policy must define how authored page content is represented.
- Keep extracted content consistent with what users can access on the page.
- Do not add hidden text solely for machine consumers.

## 7. Limits and security

Proposed bounded controls:

- Maximum traversal depth.
- Maximum component/node count.
- Maximum reference depth.
- Maximum output size.
- Maximum generation time.
- Project-root and anonymous-access checks.

The actual values are to be benchmarked and configured. Broken or inaccessible references are skipped and recorded; internal AEM repository paths must not leak.

## 8. Enablement guide

### Configuration owner

1. Enable Markdown for the project.
2. Set traversal and output limits.
3. Confirm the approved component extractor set.
4. Confirm the public Markdown URL mapping.
5. Publish configuration.
6. Validate output on Publish through Dispatcher/CDN.
7. Review generated output for representative templates before broad enablement.

### Author

Use supported components and follow the author checklist above. There is no separate per-page Markdown editor unless the project explicitly designs one.

## 9. Validation and troubleshooting

- [ ] Response has a Markdown content type.
- [ ] Output contains the expected title and body.
- [ ] Unsupported components are skipped safely.
- [ ] No raw JCR properties or internal paths are present.
- [ ] Image alt text and public URLs are handled correctly.
- [ ] References are public and within allowed boundaries.
- [ ] Output is within limits.
- [ ] Output matches the published page's visible meaning.

| Symptom | Check |
|---|---|
| Component missing | Extractor registered for the actual resource type? |
| Duplicate content | Nested Experience Fragment traversal/duplicate policy |
| Link points to AEM internal path | ReferenceResolver and CanonicalUrlService |
| Markdown unexpectedly empty | Page eligibility, feature enabled, supported components |
| Generation too slow | Component count, references, recursion, output limits |

## 10. References

- [llms.txt website convention proposal](https://llmstxt.org/)
- [Google Search Essentials — helpful content and crawlable links](https://developers.google.com/search/docs/essentials)
