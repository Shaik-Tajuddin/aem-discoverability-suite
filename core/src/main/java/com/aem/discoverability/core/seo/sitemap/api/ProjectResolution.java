package com.aem.discoverability.core.seo.sitemap.api;

import java.util.Objects;
import java.util.Optional;

/**
 * Fail-closed result of resolving exactly one project for a request resource.
 */
public final class ProjectResolution {

    public enum Status {
        RESOLVED,
        NONE,
        AMBIGUOUS,
        INVALID
    }

    private final Status status;
    private final String projectRoot;
    private final String reason;

    private ProjectResolution(Status status, String projectRoot, String reason) {
        this.status = Objects.requireNonNull(status, "status");
        this.projectRoot = projectRoot;
        this.reason = reason;
    }

    public static ProjectResolution resolved(String projectRoot) {
        return new ProjectResolution(Status.RESOLVED, projectRoot, null);
    }

    public static ProjectResolution none(String reason) {
        return new ProjectResolution(Status.NONE, null, reason);
    }

    public static ProjectResolution ambiguous(String reason) {
        return new ProjectResolution(Status.AMBIGUOUS, null, reason);
    }

    public static ProjectResolution invalid(String reason) {
        return new ProjectResolution(Status.INVALID, null, reason);
    }

    public Status getStatus() {
        return status;
    }

    public Optional<String> getProjectRoot() {
        return Optional.ofNullable(projectRoot);
    }

    public Optional<String> getReason() {
        return Optional.ofNullable(reason);
    }

    public boolean isResolved() {
        return status == Status.RESOLVED;
    }
}
