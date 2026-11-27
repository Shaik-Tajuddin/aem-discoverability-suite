package com.aem.discoverability.core.seo.sitemap.services.impl;

import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import com.aem.discoverability.core.seo.sitemap.api.ProjectResolution;
import com.aem.discoverability.core.seo.sitemap.api.ProjectResolver;

/**
 * Resolves project root as the innermost content resource with {@code sling:configRef} / {@code cq:conf}.
 * Fail closed when no CA context exists.
 */
@Component(service = ProjectResolver.class)
public class ProjectResolverImpl implements ProjectResolver {

    @Override
    public ProjectResolution resolve(Resource resource) {
        if (resource == null) {
            return ProjectResolution.none("resource is null");
        }

        String contextRoot = findInnermostContextRoot(resource);
        if (contextRoot == null || contextRoot.isEmpty()) {
            return ProjectResolution.none("no sling:configRef/cq:conf context");
        }
        if (!isUnder(resource.getPath(), contextRoot)) {
            return ProjectResolution.none("resource outside config context");
        }

        return ProjectResolution.resolved(normalizeRoot(contextRoot));
    }

    private static String findInnermostContextRoot(Resource resource) {
        Resource current = resource;
        while (current != null && !"/".equals(current.getPath())) {
            if (hasConfigContext(current)) {
                return normalizeRoot(current.getPath());
            }
            current = current.getParent();
        }
        return null;
    }

    private static boolean hasConfigContext(Resource resource) {
        if (resource == null) {
            return false;
        }
        var props = resource.getValueMap();
        if (props.containsKey("cq:conf") || props.containsKey("sling:configRef")) {
            return true;
        }
        Resource content = resource.getChild("jcr:content");
        if (content == null) {
            return false;
        }
        var contentProps = content.getValueMap();
        return contentProps.containsKey("cq:conf") || contentProps.containsKey("sling:configRef");
    }

    public static boolean isUnder(String path, String root) {
        String normalizedPath = normalizeRoot(path);
        String normalizedRoot = normalizeRoot(root);
        return normalizedPath.equals(normalizedRoot)
                || normalizedPath.startsWith(normalizedRoot + "/");
    }

    public static String normalizeRoot(String path) {
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return path == null ? "" : path;
        }
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }
}
