package org.sakaiproject.modi;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A launcher for Sakai in its traditional directory structure and conventions. Used to
 * kick off the configuration and component loading process and ensure that the shared
 * application context is ready for webapps to load.
 *
 * The {@link TomcatListener} is the main consumer of this class, and there is some
 * conventional overlap and Tomcat naming exposed, despite very little in the way of
 * actual Tomcat dependency. It is conceivable that another container could serve.
 */
@Slf4j
public class Launcher {
    protected ComponentsDirectory components;
    protected SharedApplicationContext context;
    protected final Environment env;

    /**
     * Create a Sakai launcher.
     *
     * The only parameter here is called catalinaBase because that is from where the other
     * conventions stem (components/, webapps/, lib/, sakai/). It is effectively the installation
     * directory for a Sakai instance.
     *
     * It should be the root of the Tomcat installation (aka CATALINA_BASE, ${catalina.base}). In
     * most installations, as by tarball or zip, the CATALINA_HOME ${catalina.home} will the same
     * directory. This is also sometimes called or set as TOMCAT_BASE, TOMCAT_HOME. In any case,
     * CATALINA_BASE is the most specific and accurate, so that's what we call it here.
     *
     * Calling {@link #start()} will begin the boot process immediately, loading configs and the
     * default components in the global application context. Calling {@link #stop()} will shut down
     * the context and unwind all of the beans and webapps.
     *
     * There is a shim to preserve the legacy ComponentManager API for those services and tools
     * that do not make use of dependency injection.
     *
     * {@link GlobalApplicationContext}
     * {@link SharedApplicationContext}
     * {@link org.sakaiproject.component.impl.ComponentManagerShim}
     *
     */
    public Launcher() {
        this.env = Environment.initialize();
    }

    /**
     * Start the Sakai instance.
     *
     * This begins the traditional boot process, checking and ensuring properties/variables
     * like sakai.home, and then loading all of the shared components into the shared
     * application context, with a ComponentManager shim in place to preserve the legacy API.
     *
     * @throws MissingConfigurationException if sakai-configuration.xml is specified and cannot be read
     * @throws InitializationException if there are unresolvable problems with the core directories
     */
    public void start() throws MissingConfigurationException, InitializationException {
        log.info("Booting Sakai in Modern Dependency Injection Mode");
        System.setProperty("sakai.use.modi", "true");

        Path sakaiHome = Path.of(env.getSakaiHome());
        Path componentsRoot = Path.of(env.getComponentsRoot());
        Path overridePath = sakaiHome.resolve("override");

        context = GlobalApplicationContext.getContext();
        context.registerBeanSource(getConfiguration());

        components = new ComponentsDirectory(componentsRoot, overridePath);
        components.starting(context);

        context.refresh();
        context.start();
    }

    public void stop() {
        log.info("Stopping Sakai components");
        if (components != null) {
            components.stopping();
        }
        context.stop();
    }

    protected Configuration getConfiguration() throws MissingConfigurationException {
        Path localConfig = Path.of(env.getSakaiHome()).resolve("sakai-configuration.xml");
        return Files.isRegularFile(localConfig)
                ? new Configuration(localConfig)
                : new Configuration();
    }
}