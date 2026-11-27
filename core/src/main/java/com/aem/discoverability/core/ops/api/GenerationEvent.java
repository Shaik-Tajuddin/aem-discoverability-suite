package com.aem.discoverability.core.ops.api;

import java.util.Objects;

/**
 * Structured discoverability generation event (no body text, PII, or secrets).
 */
public final class GenerationEvent {

    public static final String EVENT_COMPLETED = "discoverability.generation.completed";

    private final String correlationId;
    private final String project;
    private final String output;
    private final boolean productionEnvironment;
    private final long durationMs;
    private final int pagesVisited;
    private final int pagesIncluded;
    private final int pagesExcluded;
    private final long outputBytes;
    private final boolean limitHit;
    private final String status;
    private final String errorCategory;

    private GenerationEvent(Builder builder) {
        this.correlationId = builder.correlationId;
        this.project = builder.project;
        this.output = builder.output;
        this.productionEnvironment = builder.productionEnvironment;
        this.durationMs = builder.durationMs;
        this.pagesVisited = builder.pagesVisited;
        this.pagesIncluded = builder.pagesIncluded;
        this.pagesExcluded = builder.pagesExcluded;
        this.outputBytes = builder.outputBytes;
        this.limitHit = builder.limitHit;
        this.status = builder.status;
        this.errorCategory = builder.errorCategory;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getProject() {
        return project;
    }

    public String getOutput() {
        return output;
    }

    public boolean isProductionEnvironment() {
        return productionEnvironment;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public int getPagesVisited() {
        return pagesVisited;
    }

    public int getPagesIncluded() {
        return pagesIncluded;
    }

    public int getPagesExcluded() {
        return pagesExcluded;
    }

    public long getOutputBytes() {
        return outputBytes;
    }

    public boolean isLimitHit() {
        return limitHit;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorCategory() {
        return errorCategory;
    }

    /** Compact JSON object for structured logs. */
    public String toJson() {
        StringBuilder out = new StringBuilder(192);
        out.append('{');
        field(out, "event", EVENT_COMPLETED, true);
        field(out, "correlationId", correlationId, false);
        field(out, "project", project, false);
        field(out, "output", output, false);
        out.append(",\"productionEnvironment\":").append(productionEnvironment);
        out.append(",\"durationMs\":").append(durationMs);
        out.append(",\"pagesVisited\":").append(pagesVisited);
        out.append(",\"pagesIncluded\":").append(pagesIncluded);
        out.append(",\"pagesExcluded\":").append(pagesExcluded);
        out.append(",\"outputBytes\":").append(outputBytes);
        out.append(",\"limitHit\":").append(limitHit);
        field(out, "status", status, false);
        if (!errorCategory.isEmpty()) {
            field(out, "errorCategory", errorCategory, false);
        }
        out.append('}');
        return out.toString();
    }

    private static void field(StringBuilder out, String name, String value, boolean first) {
        if (!first) {
            out.append(',');
        }
        out.append('"').append(name).append("\":\"");
        escape(out, value == null ? "" : value);
        out.append('"');
    }

    private static void escape(StringBuilder out, String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"' || c == '\\') {
                out.append('\\').append(c);
            } else if (c < 0x20) {
                out.append(' ');
            } else {
                out.append(c);
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String correlationId = "";
        private String project = "";
        private String output = "";
        private boolean productionEnvironment;
        private long durationMs;
        private int pagesVisited;
        private int pagesIncluded;
        private int pagesExcluded;
        private long outputBytes;
        private boolean limitHit;
        private String status = "success";
        private String errorCategory = "";

        private Builder() {
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = nullToEmpty(correlationId);
            return this;
        }

        public Builder project(String project) {
            this.project = nullToEmpty(project);
            return this;
        }

        public Builder output(String output) {
            this.output = Objects.requireNonNull(output, "output");
            return this;
        }

        public Builder productionEnvironment(boolean productionEnvironment) {
            this.productionEnvironment = productionEnvironment;
            return this;
        }

        public Builder durationMs(long durationMs) {
            this.durationMs = Math.max(0L, durationMs);
            return this;
        }

        public Builder pagesVisited(int pagesVisited) {
            this.pagesVisited = Math.max(0, pagesVisited);
            return this;
        }

        public Builder pagesIncluded(int pagesIncluded) {
            this.pagesIncluded = Math.max(0, pagesIncluded);
            return this;
        }

        public Builder pagesExcluded(int pagesExcluded) {
            this.pagesExcluded = Math.max(0, pagesExcluded);
            return this;
        }

        public Builder outputBytes(long outputBytes) {
            this.outputBytes = Math.max(0L, outputBytes);
            return this;
        }

        public Builder limitHit(boolean limitHit) {
            this.limitHit = limitHit;
            return this;
        }

        public Builder status(String status) {
            this.status = nullToEmpty(status).isEmpty() ? "success" : status;
            return this;
        }

        public Builder errorCategory(String errorCategory) {
            this.errorCategory = nullToEmpty(errorCategory);
            return this;
        }

        public GenerationEvent build() {
            return new GenerationEvent(this);
        }

        private static String nullToEmpty(String value) {
            return value == null ? "" : value;
        }
    }
}
