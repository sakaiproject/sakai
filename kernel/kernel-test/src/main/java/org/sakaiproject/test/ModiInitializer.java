package org.sakaiproject.test;

import org.sakaiproject.modi.SharedApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;

public class ModiInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        System.setProperty("sakai.home", Path.of("target/sakai-home").toAbsolutePath().toString() + "/");
        System.setProperty("sakai.modi.enabled", "true");
    }
}
