package com.aem.discoverability.core.seo.robots.services.impl;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.aem.discoverability.core.seo.robots.api.EnvironmentPolicy;

/**
 * Environment classification via OSGi (run-mode config), not SlingSettingsService or Host.
 */
@Component(service = EnvironmentPolicy.class, configurationPolicy = ConfigurationPolicy.OPTIONAL)
@Designate(ocd = EnvironmentPolicyImpl.Config.class)
public class EnvironmentPolicyImpl implements EnvironmentPolicy {

    @ObjectClassDefinition(name = "Discoverability Environment Policy",
            description = "Marks this instance as production for crawl policy. Set true only via config.prod.")
    public @interface Config {
        @AttributeDefinition(name = "Production environment")
        boolean production() default false;
    }

    private boolean production;

    @Activate
    protected void activate(Config config) {
        this.production = config.production();
    }

    @Override
    public boolean isProduction() {
        return production;
    }
}
