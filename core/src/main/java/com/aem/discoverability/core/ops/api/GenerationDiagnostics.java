package com.aem.discoverability.core.ops.api;

/**
 * Emits structured generation events for operations (no protected content).
 */
public interface GenerationDiagnostics {

    void emit(GenerationEvent event);
}
