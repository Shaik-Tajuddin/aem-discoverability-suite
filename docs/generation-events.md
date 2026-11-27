# Generation event schema (v1)

Emitted as a single INFO log line (JSON object) by `GenerationDiagnostics`.

| Field | Type | Notes |
|---|---|---|
| `event` | string | Always `discoverability.generation.completed` |
| `correlationId` | string | `X-Correlation-Id` or generated UUID |
| `project` | string | Last segment of project root (not a secret path dump) |
| `output` | string | `robots` \| `sitemap` \| `markdown` \| `llms` |
| `productionEnvironment` | boolean | From environment policy |
| `durationMs` | number | Wall time for the request generation |
| `pagesVisited` | number | Optional; `0` when not counted |
| `pagesIncluded` | number | Optional |
| `pagesExcluded` | number | Optional |
| `outputBytes` | number | UTF-8 byte length of success body |
| `limitHit` | boolean | True when a size/time limit caused failure |
| `status` | string | `success` \| `error` |
| `errorCategory` | string | Present on error only (e.g. `empty_or_limit`) |

**Never logged:** page body, Markdown/HTML, credentials, tokens, CUG details, full protected paths beyond project label.
