package com.aem.discoverability.core.seo.sitemap.api;

import java.util.Objects;
import java.util.Optional;

/**
 * Structured eligibility outcome for one page path.
 */
public final class EligibilityResult {

    private final String path;
    private final boolean eligible;
    private final ExclusionReason reason;
    private final String detail;

    private EligibilityResult(String path, boolean eligible, ExclusionReason reason, String detail) {
        this.path = Objects.requireNonNull(path, "path");
        this.eligible = eligible;
        this.reason = Objects.requireNonNull(reason, "reason");
        this.detail = detail;
    }

    public static EligibilityResult eligible(String path) {
        return new EligibilityResult(path, true, ExclusionReason.NONE, null);
    }

    public static EligibilityResult excluded(String path, ExclusionReason reason, String detail) {
        return new EligibilityResult(path, false, reason, detail);
    }

    public String getPath() {
        return path;
    }

    public boolean isEligible() {
        return eligible;
    }

    public ExclusionReason getReason() {
        return reason;
    }

    public Optional<String> getDetail() {
        return Optional.ofNullable(detail);
    }
}
