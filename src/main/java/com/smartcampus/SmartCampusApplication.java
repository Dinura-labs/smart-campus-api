package com.smartcampus;

import com.smartcampus.store.CampusStore;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;

import java.util.logging.Logger;

/**
 * JAX-RS application entry point. Deployed on Apache Tomcat via {@code ServletContainer} in {@code web.xml}.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {

    private static final Logger LOG = Logger.getLogger(SmartCampusApplication.class.getName());

    public SmartCampusApplication() {
        LOG.info("Initializing Smart Campus application configuration");
        packages("com.smartcampus");
        register(JacksonFeature.class);
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindAsContract(CampusStore.class).in(Singleton.class);
            }
        });
    }
}
