package org.sakaiproject.modi;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A launcher for Sakai in its traditional directory structure and conventions. This
 * serves as the main entrypoint in "modern" mode. It is used to kick off the configuration
 * and component loading process and ensure that the shared application context is ready for
 * webapps to load.
 *
 * The {@link TomcatListener} is the main consumer of this class, and there is some
 * conventional overlap and Tomcat naming exposed, despite very little in the way of
 * actual Tomcat dependency. It is conceivable that another container could serve.
 *
 * The actual conventions of which system properties, files, and directories are used are
 * housed in the {@link Environment}. The launcher is responsible for initializing the
 * environment, shared application context, and the components so the webapps have
 * all everything set up when they start.
 *
 * The Launcher is meant to start up a deployed instance. It would not be used during
 * integration tests, which would do some similar setup, not depending on the conventions
 * of deployment.
 */
@Slf4j
public class Launcher {
    protected final Environment env;
    protected ComponentsDirectory components;
    protected SharedApplicationContext context;

    /**
     * Create a Sakai launcher.
     *
     * Calling {@link #start()} will begin the boot process immediately, loading configs and the
     * default components in the global application context. Calling {@link #stop()} will shut down
     * the context and unwind all of the beans and webapps.
     *
     * There is a shim set up to preserve the legacy ComponentManager API for those services and
     * tools that do not make use of dependency injection.
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

        context = GlobalApplicationContext.getContext();
        context.registerBeanSource(getConfiguration());

        components = new ComponentsDirectory(env.getComponentsRoot(), env.getOverridesFolder());
        components.starting(context);

        context.refresh();
        context.start();
    }

    /**
     * Shut down Sakai. Notify the components that we are stopping and stop the global context.
     */
    public void stop() {
        log.info("Stopping Sakai components");
        if (components != null) {
            components.stopping();
        }
        context.stop();
    }

    protected Configuration getConfiguration() throws MissingConfigurationException {
        Path localConfig = env.getConfigurationFile();
        return Files.isRegularFile(localConfig)
                ? new Configuration(localConfig)
                : new Configuration();
    }
}