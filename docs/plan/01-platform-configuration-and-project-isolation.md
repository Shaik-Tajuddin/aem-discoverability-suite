# Platform Configuration and Project Isolation

**Feature area:** Platform foundation  
**Audience:** AEM architects, AEM administrators, configuration owners  
**Status:** Phase 1 implemented (2026-11-27) — see `tasks/plan.md`

## 1. Purpose

The platform configuration layer resolves which AEM project owns a request and which discoverability settings apply. It prevents one project's domain, page tree, robots policy, sitemap, language relationships, or generated content from being used for another project.

This is a prerequisite for every output feature.

## 2. Architecture

### 2.1 Main responsibilities

- Resolve exactly one project from the requested resource/path and applicable configuration context.
- Resolve the project's effective Context-Aware Configuration (CA Config).
- Validate project root, public domain, feature flags, languages, and generation limits.
- Identify the runtime environment and apply environment safeguards.
- Build an immutable configuration snapshot for each generation request.

### 2.2 Logical resolution sequence

```text
Incoming request/resource
        |
Identify project root and CA Config context
        |
Resolve exactly one project?
   | yes                 | no / ambiguous
Validate project config  Fail closed
        |
Validate public domain and environment
        |
Create immutable ResolvedConfiguration
        |
Pass project context to shared generation engine
```

### 2.3 Isolation rules

- One request must resolve to exactly one project.
- Never choose a project solely from an untrusted Host header.
- Never fall back to a different project's configuration after a resolution failure.
- Do not traverse beyond the configured project root by default.
- Do not allow a project to claim a public domain already owned by another project unless an explicit, reviewed routing model supports it.
- The public URL domain must come from trusted configuration and AEM mapping/Externalizer policy.
- Invalid or ambiguous configuration must fail closed.

## 3. Configuration model

The architecture uses wcm.io CA Config. The following is a logical hierarchy; actual persistence must follow the wcm.io conventions and the chosen implementation version.

```text
/conf
├── global/discoverability/
│   ├── platform/
│   ├── seo/
│   ├── geo/
│   ├── aeo/
│   └── language/
├── project-a/discoverability/
│   ├── platform/
│   ├── seo/
│   ├── geo/
│   ├── aeo/
│   └── language/
└── project-b/discoverability/
    └── ...
```

### 3.1 Logical configuration groups

| Group | Example settings | Owner |
|---|---|---|
| Platform | enabled, project root, public domain, environment policy | AEM/config admin |
| SEO | robots, sitemap enablement and policies | SEO/config owner |
| GEO | Markdown, extractor limits, llms.txt settings | Content/SEO config owner |
| AEO | JSON-LD enablement and entity policy | SEO/config owner |
| Language | language roots, locale codes, mappings, x-default | Translation/SEO owner |
| Generation | batch size, traversal depth, timeout, output limits | AEM admin |

These are concepts, not final property names or dialog labels.

## 4. Enablement guide

### 4.1 AEM administrator / configuration owner

1. Confirm the project root and the public domain for the project.
2. Confirm that the target host maps to the intended AEM Publish project.
3. Create or select the project's CA Config context.
4. Set the platform feature switch to enabled only after validating the domain and project root.
5. Keep each output feature disabled until its own configuration and smoke tests are complete.
6. Publish the required CA Config to the Publish tier.
7. Verify that Publish resolves the same intended project and configuration.
8. Verify that unknown hosts and ambiguous project roots fail closed.
9. Keep development and stage crawling disabled by policy.
10. Enable production crawling only after a release readiness review.

### 4.2 Using the CA Config Editor

If the project installs the wcm.io CA Config Editor, authorized users can manage configuration mapped to the current content context. The editor supports configuration data, defaults, inheritance/overrides, and publishing. The exact editor page location and permitted roles are project-specific.

For the selected AEM 6.5.17+ minimum, the wcm.io Editor compatibility table lists versions 1.15.10–1.17.x for AEM 6.5.17+, while 1.18.0+ is listed for AEM 6.5.24+. Re-check the matrix when selecting dependencies: [wcm.io CA Config Editor support matrix](https://wcm.io/caconfig/editor/).

### 4.3 Content author

Authors normally do not change the platform's public domain, global feature switches, or environment policy. Authors use page properties and components described by the individual feature guides.

## 5. Best practices

- Prefer project-level configuration for project-owned behavior; use global defaults only for genuinely shared defaults.
- Keep feature flags off until the endpoint is tested on Publish.
- Keep project root and public domain explicit.
- Avoid broad inheritance that unintentionally shares a different project's configuration.
- Document who owns each configuration group.
- Review configuration changes like code: test, publish, verify, and retain rollback information.
- Do not expose internal author/stage domains in production outputs.
- Keep authoring permissions separate from infrastructure deployment permissions.

## 6. Example project configuration (illustrative)

```yaml
project:
  id: wknd
  root: /content/wknd
  publicDomain: https://www.example.com
  enabled: true

environmentPolicy:
  productionCrawlAllowed: true
  nonProductionCrawlAllowed: false

features:
  robots: false
  sitemap: false
  markdown: false
  llms: false
  jsonLd: false

language:
  roots:
    - /content/wknd/en
    - /content/wknd/de
```

This YAML explains the intended model only. It is not a supported import format or final CA Config schema.

## 7. Validation checklist

- [ ] Request resolves to one and only one project.
- [ ] Project root is correct.
- [ ] Host/domain belongs to that project.
- [ ] Configuration is resolved on Publish.
- [ ] Unknown or ambiguous projects fail closed.
- [ ] One project's page tree cannot appear in another project's output.
- [ ] Non-production crawl policy is active.
- [ ] Configuration editor access is restricted to approved roles.

## 8. Troubleshooting

| Symptom | Likely cause | Action |
|---|---|---|
| Output disabled unexpectedly | Feature flag off or invalid config | Inspect resolved project config and diagnostics |
| Wrong public domain | Incorrect domain ownership/mapping | Correct project domain/Externalizer mapping; do not patch output text |
| Project not resolved | Missing context/root mapping | Correct project context and test exact Publish request |
| Configuration differs on Publish | CA Config not published or context mismatch | Publish config and re-check resolution |
| Cross-project content risk | Root overlap or ambiguous mapping | Disable affected outputs and correct isolation before re-enabling |

## 9. References

- [wcm.io Context-Aware Configuration](https://wcm.io/caconfig/)
- [wcm.io CA Config Editor](https://wcm.io/caconfig/editor/)
- [Adobe AEM 6.5 SEO and URL management](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/managing/managing-further-reference/seo-and-url-management)
