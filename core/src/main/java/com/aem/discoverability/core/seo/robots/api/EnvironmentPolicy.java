package com.aem.discoverability.core.seo.robots.api;

/**
 * Runtime environment classification from trusted OSGi config (not request Host).
 */
public interface EnvironmentPolicy {

    /**
     * @return true when this instance is classified as production
     */
    boolean isProduction();
}
