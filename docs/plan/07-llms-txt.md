# llms.txt — Feature Guide

**Feature area:** GEO / site-level context discovery  
**Audience:** Content strategists, SEO/GEO administrators, AEM configuration owners  
**Generation:** On-demand, Publish-side  
**Status:** Design guide — implementation not yet authorized

## 1. Purpose and expectations

`llms.txt` is a proposed website convention: a Markdown file at the site root that gives language-model-oriented tools a concise introduction and links to useful public resources.

It is not the same as `robots.txt`, does not grant or deny access, and should not be presented as a guaranteed search ranking or AI-citation mechanism. The proposal is a community convention, not a universal crawler requirement.

The v1 suite generates a bounded site-level document from project configuration and eligible page metadata. It does not call an LLM or generate summaries using AI.

Reference: [llms.txt proposal](https://llmstxt.org/).

## 2. Architecture

```text
GET /llms.txt
      |
Resolve project + LlmsConfig
      |
Use shared discovery/eligibility services
      |
Collect configured site context + eligible page links
      |
Sort deterministically and enforce size limits
      |
Render Markdown text
      |
Return text/markdown + cache headers
```

The generator must not perform a second independent page-tree traversal. It reuses the shared engine.

## 3. Configuration

Proposed logical settings:

| Setting | Purpose |
|---|---|
| enabled | Enable/disable output |
| siteName | Public project/site name |
| summary | Short, approved site description |
| contextText | Optional factual context/instructions |
| sectionDefinitions | Grouping of pages into link sections |
| includePaths | Optional curated paths |
| excludePaths | Optional exclusions |
| maxLinks | Maximum number of links |
| maxOutputBytes | Maximum output size |
| languagePolicy | Default language or language-specific variant |
| cacheMaxAge | Output TTL |

Exact property names and UI are not finalized.

## 4. Author/admin enablement guide

### Content strategist / configuration owner

1. Confirm the project name and public domain.
2. Write a short factual summary of the organization/site.
3. Identify a small set of useful public content categories.
4. Select high-value, stable, representative pages.
5. Avoid confidential, internal, draft, or low-quality content.
6. Configure sections and optional curation paths.
7. Enable `llms.txt` only after validating the selected links.
8. Publish configuration and test the root endpoint.
9. Establish an owner and review cadence for the summary and selected content.

### AEM content author

Authors improve the source pages referenced by llms.txt:

- Use clear page titles and headings.
- Keep factual descriptions accurate and current.
- Ensure page is public and canonical.
- Maintain descriptive page metadata.
- Avoid publishing duplicate or obsolete pages as preferred references.

Authors do not edit the generated `llms.txt` file directly.

## 5. Example output

```markdown
# Example Company

> Example Company provides digital services and product information for
> customers and partners.

This file lists selected public resources for understanding the company,
its products, and customer support information. Refer to each linked page
for the authoritative and current details.

## Company

- [About Us](https://www.example.com/about-us): Company overview and mission.
- [Leadership](https://www.example.com/company/leadership): Public leadership information.

## Products

- [Product Overview](https://www.example.com/products): Summary of available products.
- [Product Support](https://www.example.com/support/products): Public product support resources.

## Help and Contact

- [Contact](https://www.example.com/contact): Public contact channels.
- [Frequently Asked Questions](https://www.example.com/faq): Common customer questions.
```

This sample follows the broad structure of the proposal. The actual output should contain only links that pass the shared eligibility policy.

## 6. Best practices

- Keep the document concise, factual, and useful.
- Use a clear H1 and a short summary.
- Group links under meaningful H2 sections.
- Use descriptive link titles and short factual notes.
- Prefer stable canonical public URLs.
- Keep link count and output size bounded.
- Exclude private, draft, protected, obsolete, or duplicate content.
- Avoid unsupported claims, promotional superlatives, and keyword stuffing.
- Do not include secrets or private instructions.
- Keep the file updated as the site changes.
- Do not claim that this file guarantees crawling, ranking, model ingestion, or citations.

## 7. Relationship to Markdown page output

The features serve different purposes:

| Output | Role |
|---|---|
| Per-page Markdown | Readable representation of one eligible page |
| `llms.txt` | Site-level index/context that points to selected public pages |
| XML sitemap | Search crawler discovery of canonical URLs |
| robots.txt | Crawler access directives |

The llms.txt file should not copy every page's complete content. It points to the detailed resources.

## 8. Validation checklist

- [ ] Root endpoint resolves for the correct project.
- [ ] File is valid UTF-8 Markdown.
- [ ] Site name and summary are accurate.
- [ ] Every link is absolute and canonical.
- [ ] Every destination is public and eligible.
- [ ] No cross-project URL appears.
- [ ] Sections are stable and deterministic.
- [ ] Link and output limits are enforced.
- [ ] Cache freshness is appropriate.

## 9. Troubleshooting

| Symptom | Check |
|---|---|
| File is empty | Feature enabled, project context, shared discovery results |
| Internal URL appears | Canonical URL service and content curation |
| Wrong site's file served | Project routing and cache key |
| Link returns 404 | Content publication and eligibility |
| File becomes very large | maxLinks, curated paths, output-size limit |

## 10. References

- [llms.txt proposal](https://llmstxt.org/)
- [llms.txt format overview](https://research.tedneward.com/ai/llm/specs/llmstxt.html)
