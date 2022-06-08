package org.sakaiproject.modi;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.sakaiproject.component.cover.ComponentManager;

import java.nio.file.Files;
import java.nio.file.Path;

//import java.io.File;

/**
 * Tomcat Bootstrapper for Sakai. Built as a LifecycleListener to be registered in server.xml, to start up all
 * required components early, rather than waiting for them to be tripped by the first webapp.
 */
@Slf4j
public class Bootstrap implements LifecycleListener {
    /** The java system property name where the full path to the components packages. */
    public static final String SAKAI_COMPONENTS_ROOT_SYS_PROP = "sakai.components.root";

    /** The Sakai configuration components, which must be the first loaded. */
    protected final static String[] CONFIGURATION_COMPONENTS = {
            "org.sakaiproject.component.SakaiPropertyPromoter",
            "org.sakaiproject.log.api.LogConfigurationManager" };

    protected final static String DEFAULT_CONFIGURATION_FILE = "classpath:/org/sakaiproject/config/sakai-configuration.xml";
    protected final static String CONFIGURATION_FILE_NAME = "sakai-configuration.xml";

    protected TraditionalComponents components;

    protected SharedApplicationContext context;

    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        switch (event.getType()) {
            case Lifecycle.START_EVENT: start(); break;
            case Lifecycle.STOP_EVENT: stop(); break;
        }
    }

    protected void start() {
        log.info("Booting Sakai components");
//        System.setProperty("sakai.modi", "true");
        Path componentsRoot = getComponentsRoot();
        Path overridePath = getOverridePath();

        context = GlobalApplicationContext.getContext();

        if (componentsRoot != null) {
            System.setProperty(SAKAI_COMPONENTS_ROOT_SYS_PROP, componentsRoot.toString());
            components = new TraditionalComponents(componentsRoot, overridePath);
            components.starting(context);
        }
        context.start();
//        ComponentManager.getInstance();
    }

    protected void stop() {
        log.info("Stopping Sakai components");
        if (components != null) {
            components.stopping();
        }
        context.stop();
    }

    protected Path getComponentsRoot() {
        String rootPath = System.getProperty(SAKAI_COMPONENTS_ROOT_SYS_PROP);
        Path componentsPath = null;
        if (rootPath != null) {
            componentsPath = Path.of(rootPath);
        } else if (getCatalina() != null) {
            componentsPath = Path.of(getCatalina(), "components");
        }

        if (Files.isDirectory(componentsPath)) {
            return componentsPath;
        } else {
            log.warn("Bootstrap error: cannot establish a root directory for the components packages");
            return null;
        }
    }

    protected Path getOverridePath() {
        Path override = Path.of(System.getProperty("sakai.home"), "override");
        if (Files.isDirectory(override)) {
            return override;
        } else {
            return null;
        }
    }

    /**
     * Check the environment for catalina's base or home directory.
     *
     * @return Catalina's base or home directory.
     */
    protected String getCatalina() {
        String catalina = System.getProperty("catalina.base");
        if (catalina == null) {
            catalina = System.getProperty("catalina.home");
        }

        return catalina;
    }
}