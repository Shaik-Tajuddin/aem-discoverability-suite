# Optional JSON-LD rewriter wiring

Primary injection is HTL: `discoverability/components/jsonld-head` (included from the suite page `customheaderlibs`).

To inject via Sling Rewriter instead (or in addition — duplicate script is prevented per request):

1. Keep your existing HTML rewriter pipeline transformers.
2. Add `discoverability-jsonld` to `transformerTypes` (do not replace the whole list).
3. Ensure the pipeline still includes your site's `linkchecker` / other required transformers.

Factory property: `pipeline.type=discoverability-jsonld`.
