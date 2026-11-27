# AEM Discoverability Suite

This is an AEM project targeting Adobe Managed Services or on-prem using the Java stack.

It is built locally using Maven and can be tested against a local Quickstart.

## Modules

- `core`: OSGi bundle. Contains the Java code for backend services, models, and business logic. Uses OSGi for dependency injection, Sling models for exposing content to Sling scripts and JUnit for unit testing.
- `dispatcher`: Contains Dispatcher configuration suitable for Adobe Managed Services or on-prem deployments, including caching and security settings.
- `ui.apps`: FileVault content package. Contains the application code, including components, templates, client libraries, and content structure. Uses HTL as the scripting engine.
- `ui.apps.structure`: FileVault content package. Empty module that defines the structure of the repository content.
- `ui.config`: FileVault content package. Contains OSGi configurations for the application.
- `ui.content`: FileVault content package. Contains the mutable content for the application, such as the initial site structure, templates, sample assets.
- `it.tests`: Integration tests module. Uses the AEM Testing clients to run tests against running AEM instances.
- `ui.tests`: UI tests module. Uses Cypress to run end-to-end tests against running AEM instances.
- `all`: FileVault content package. Includes all other FileVault packages for easy deployment.

## Build

The project uses Maven as the build tool. The following commands are commonly used:

- full build: `mvn clean install`
- build and deploy to a local Quickstart: `mvn clean install -PautoInstallSinglePackage`
- build and deploy a single FileVault content package: `mvn clean install -pl <module> -PautoInstallPackage`
- build and deploy a single OSGi bundle: `mvn clean install -pl <module> -PautoInstallBundle`

## Important resources

Note: there are significant architectural differences between AEM as a Cloud Service and Adobe Managed Services/on-prem deployments. The resources below have been curated to ensure they apply to this project. Use them in preference to generic AEM resources.
- [Core Concepts](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/introduction/the-basics)
- [AEM Technical Foundations](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/deploying/introduction/platform)
- [AEM Development Guidelines](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/introduction/dev-guidelines-bestpractices)
- [Java API Best Practices](https://experienceleague.adobe.com/en/docs/experience-manager-learn/foundation/development/understand-java-api-best-practices)
- [Sling Adapters](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/platform/sling-adapters)
- [Sling Resource Merger](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/platform/sling-resource-merger)
- [Getting Started with HTL](https://experienceleague.adobe.com/en/docs/experience-manager-htl/content/getting-started)
- [Overlays](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/platform/overlays)
- [Templates](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/platform/templates/templates)
- [Core Components Introduction](https://experienceleague.adobe.com/en/docs/experience-manager-core-components/using/introduction)
- [Deprecated and Removed Features and APIs](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/release-notes/deprecated-removed-features)
- [Using Client-Side Libraries](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/introduction/clientlibs)
- [Universal Editor in AEM](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/headless/universal-editor/introduction)
- [Content Fragments](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/assets/content-fragments/content-fragments)
- [Experience Fragments](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/sites/authoring/authoring/experience-fragments)
- [Developing and Extending Workflows](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/extending-aem/extending-workflows/workflows)
- [Replication](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/deploying/configuring/replication)
- [Content Search and Indexing](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/deploying/deploying/queries-and-indexing)
- [Deployment and Maintenance](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/deploying/deploying/deploy)
- [API Reference Materials](https://experienceleague.adobe.com/en/docs/experience-manager-65/content/implementing/developing/introduction/reference-materials)
- [WCM.io AEM Mocks](https://wcm.io/testing/aem-mock/)
- [Sling Mocks](https://sling.apache.org/documentation/development/sling-mock.html)

---

# Agent behavioral guidelines

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

## 5. Use Available Tooling

**Reach for skills, MCPs, plugins, and agents whenever the task calls for them — don't work around them manually.**

### find-skills

Use the **find-skills** skill when:
- The user asks "how do I do X", "find a skill for X", or "is there a skill that can…"
- A task might already have a specialized skill (testing, design, deployment, review, etc.)
- You are unsure which skill applies — discover first, then load and follow it

Prefer searching/installing via the Skills CLI (`npx skills find …`, `npx skills add …`) over inventing a one-off workflow.

### agent-browser

Use the **agent-browser** skill when the task needs real browser interaction:
- Navigate pages, click, fill forms, take screenshots, scrape, login flows
- Test or verify a web app / AEM author or publish UI in a real browser
- Exploratory QA, dogfooding, or bug hunts in the UI

Load and follow the agent-browser skill (start with `agent-browser skills get core`) before using other browser automation. Prefer agent-browser over improvising browser steps by hand.

### General

- A relevant skill exists for the task? Load and follow it before improvising.
- A plugin or agent is scoped for the task? Use it rather than reimplementing by hand.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.
