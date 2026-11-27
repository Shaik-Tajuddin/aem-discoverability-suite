package com.aem.discoverability.core.seo.sitemap.api;

import org.apache.sling.api.resource.Resource;

/**
 * Resolves exactly one project from a trusted content resource/path context.
 * Never uses the HTTP Host header.
 */
public interface ProjectResolver {

    /**
     * @param resource content resource that triggered generation (page or descendant)
     * @return fail-closed resolution (one / none / ambiguous / invalid)
     */
    ProjectResolution resolve(Resource resource);
}
