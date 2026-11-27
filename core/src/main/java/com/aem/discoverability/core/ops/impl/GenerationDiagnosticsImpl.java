package com.aem.discoverability.core.ops.impl;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.discoverability.core.ops.api.GenerationDiagnostics;
import com.aem.discoverability.core.ops.api.GenerationEvent;

@Component(service = GenerationDiagnostics.class)
public class GenerationDiagnosticsImpl implements GenerationDiagnostics {

    private static final Logger LOG = LoggerFactory.getLogger(GenerationDiagnosticsImpl.class);

    @Override
    public void emit(GenerationEvent event) {
        if (event == null) {
            return;
        }
        LOG.info("{}", event.toJson());
    }
}
