# Diagnostics and Operations

**Feature area:** Cross-cutting operations  
**Audience:** AEM administrators, release engineers, support teams  
**Status:** Design guide — implementation not yet authorized

## 1. Purpose

v1 includes lightweight diagnostics, not a full operational dashboard. Diagnostics should help teams understand generation behavior, exclusions, configuration errors, limits, and output failures without exposing protected content or sensitive repository details.

## 2. What should be observable

Suggested diagnostic fields:

- Correlation/request ID.
- Project identifier.
- Output type.
- Environment.
- Generation start/end and duration.
- Candidate pages visited.
- Pages included/excluded.
- Exclusion reason counts.
- Component extractor usage/skip counts.
- Reference resolution failures.
- Output byte size.
- Timeout/limit events.
- Controlled error category.

The final log schema and metric names must be defined in the detailed technical specification.

## 3. Example structured event

```json
{
  "event": "discoverability.generation.completed",
  "correlationId": "req-example-123",
  "project": "wknd",
  "output": "sitemap",
  "durationMs": 842,
  "pagesVisited": 1250,
  "pagesIncluded": 972,
  "pagesExcluded": 278,
  "outputBytes": 184220,
  "status": "success"
}
```

Example only. The final event schema is a technical-design decision.

## 4. Author guide: what authors can check

Authors generally do not access system logs. Their diagnostic workflow should be:

1. Confirm the page is under the correct project and language root.
2. Check that the page is published.
3. Review indexing, canonical, and exclusion settings.
4. Check page components against the supported Markdown extractor list, if relevant.
5. Verify referenced Content Fragments/Experience Fragments are published and publicly accessible.
6. Check the public output after publication.
7. Report the page path, public URL, output type, and time of verification to the support team.

Do not include confidential content or credentials in support tickets.

## 5. Administrator operations checklist

### Before enabling a feature

- [ ] Project and public domain resolve correctly.
- [ ] Feature config is valid.
- [ ] Limits are configured.
- [ ] Environment policy is correct.
- [ ] Endpoint routing is reviewed.
- [ ] Dispatcher/CDN rules are tested.
- [ ] Representative page output is validated.

### After publishing content/config

- [ ] Confirm content is available on Publish.
- [ ] Check expected output at the public domain.
- [ ] Check cache freshness.
- [ ] Review diagnostics for exclusions/failures.
- [ ] Verify no cross-project or protected content is exposed.

### During an incident

1. Identify the project, output, public URL, and time.
2. Check whether the response is from CDN, Dispatcher, or Publish.
3. Verify project/config resolution.
4. Check recent content/config activation.
5. Review generation diagnostics and limit events.
6. Disable only the affected output if it is unsafe; keep unrelated outputs operational where possible.
7. Restore service after the underlying issue and cache state are verified.

## 6. Error behavior contract

| Condition | Expected behavior |
|---|---|
| Unknown project | Controlled 404/disabled response |
| Invalid config | Fail closed for that output |
| Invalid domain | Reject public URL generation |
| Repository failure | Controlled server error and diagnostic |
| Invalid sitemap content | No partial/malformed XML |
| Unsupported Markdown component | Skip and log diagnostic |
| Broken/inaccessible reference | Skip and log diagnostic |
| Invalid JSON-LD contribution | Omit contribution; do not break HTML |
| Invalid language relationship | Omit alternate |
| Output limit exceeded | Controlled error; do not silently emit malformed/truncated output |

## 7. Best practices

- Use structured logs with correlation IDs.
- Keep diagnostics useful but low-volume.
- Aggregate exclusion counts rather than logging every normal exclusion at high severity.
- Do not log page body text, PII, credentials, tokens, or protected paths unnecessarily.
- Distinguish validation failure, configuration error, repository error, and output-limit failure.
- Avoid treating an unsupported optional component as a fatal site-wide error.
- Keep output generators independent so one feature's failure does not break unrelated outputs.
- Record the project and output type so multi-site issues can be isolated.
- Monitor cache hit ratios and generation duration together.

## 8. Suggested operational checks

| Check | Expected result |
|---|---|
| Project resolution | Exactly one project |
| Configuration validation | Valid, features intentionally enabled |
| Page discovery | Within project root and configured limits |
| Eligibility | No protected content |
| Canonical URL | Approved public domain |
| Sitemap | Valid XML and partition limits |
| Markdown | Supported components only |
| llms.txt | Curated public links |
| JSON-LD | Valid JSON and page-consistent data |
| Cache | TTL and freshness match SLA |

## 9. Troubleshooting matrix

| Problem | First area to inspect |
|---|---|
| All outputs disabled | Platform config, environment guard |
| Only one project affected | Project CA Config and domain mapping |
| Missing individual page | Eligibility reason and publication state |
| Output changes slowly | Dispatcher/CDN TTL and invalidation |
| Sitemap generation is slow | Cache state, discovery batches, partition traversal |
| Markdown loses a component | Registered extractor/resource type mapping |
| JSON-LD absent | Source component, graph validation, Rewriter pipeline |
| hreflang missing | Translation relationship and locale validation |

## 10. Operational references

- [Adobe Dispatcher configuration](https://experienceleague.adobe.com/en/docs/experience-manager-dispatcher/using/configuring/dispatcher-configuration)
- [wcm.io CA Config Editor usage](https://wcm.io/caconfig/editor/)
- [Google Search Console](https://search.google.com/search-console/about)
