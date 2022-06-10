package org.sakaiproject.modi;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

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
    protected final Path catalinaBase;
    protected TraditionalComponents components;
    protected SharedApplicationContext context;

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
     * @param catalinaBase the root directory for the Sakai / Tomcat instance
     */
    public Launcher(Path catalinaBase) {
        this.catalinaBase = catalinaBase;
    }

    /** The java system property name where the full path to the components packages. */
    public static final String SAKAI_COMPONENTS_ROOT_SYS_PROP = "sakai.components.root";

    /**
     * Start the Sakai instance.
     *
     * This begins the traditional boot process, checking and ensuring properties/variables
     * like sakai.home, and then loading all of the shared components into the shared
     * application context, with a ComponentManager shim in place to preserve the legacy API.
     *
     * @throws MissingConfigurationException if sakai-configuration.xml is specified and cannot be read
     * @throws CouldNotReadWriteSakaiHomeException if the sakai.home directory cannot be read/written
     * @throws CouldNotCreateSakaiHomeException if the sakai.home directory does not exist and could not be created
     */
    public void start()
            throws MissingConfigurationException,
            CouldNotReadWriteSakaiHomeException,
            CouldNotCreateSakaiHomeException
    {
        log.info("Booting Sakai in Modern Dependency Injection Mode");
        System.setProperty("sakai.use.modi", "true");
        ensureSakaiHome();
        checkSecurityPath();

        Path componentsRoot = getComponentsRoot();
        Path overridePath = getOverridePath();

        context = GlobalApplicationContext.getContext();
        context.registerBeanSource(getConfiguration());

        if (componentsRoot != null) {
            System.setProperty(SAKAI_COMPONENTS_ROOT_SYS_PROP, componentsRoot.toString());
            components = new TraditionalComponents(componentsRoot, overridePath);
            components.starting(context);
        }

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
        Path localConfig = Path.of(System.getProperty("sakai.home"), "sakai-configuration.xml");
        return Files.isRegularFile(localConfig)
                ? new Configuration(localConfig)
                : new Configuration();
    }

    protected Path getComponentsRoot() {
        String rootPath = System.getProperty(SAKAI_COMPONENTS_ROOT_SYS_PROP);
        Path componentsPath = null;
        if (rootPath != null) {
            componentsPath = Path.of(rootPath);
        } else if (catalinaBase != null) {
            componentsPath = catalinaBase.resolve("components");
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
     * Use or infer the sakai.home value, and then check/create the directory.
     *
     * When done, we set the property again to ensure that it has a trailing slash
     * because many places use it for bare concatenation.
     *
     * @throws CouldNotCreateSakaiHomeException if the directory cannot be established/created
     */
    private void ensureSakaiHome() throws CouldNotCreateSakaiHomeException {
        Path home = getSakaiHomePath();
        createHomeIfNeeded(home);
        checkHomeReadWrite(home);
        System.setProperty("sakai.home", home.toAbsolutePath() + "/");
    }

    private Path getSakaiHomePath() {
        return getConfiguredHome()
                .or(this::getDefaultHome)
                .orElseGet(this::fallbackHome);
    }

    private void createHomeIfNeeded(Path path) {
        try {
            if (!Files.isDirectory(path))
                Files.createDirectory(path);
            log.info("Created Sakai home directory (sakai.home) at: {}", path);
        } catch (IOException e) {
            throw new CouldNotCreateSakaiHomeException(path);
        }
    }

    private void checkHomeReadWrite(Path path) {
        if (!(Files.isDirectory(path) && Files.isReadable(path) && Files.isWritable(path)))
            throw new CouldNotReadWriteSakaiHomeException(path);
    }

    private Optional<Path> getConfiguredHome() {
        return getSakaiHomeProp().map(Path::of);
    }

    private Optional<Path> getDefaultHome() {
        return getCatalinaBaseProp()
                .map(Path::of)
                .map(p -> p.resolve("sakai"));
    }

    private Path fallbackHome() {
        return Path.of("/tmp/sakai");
    }
    private Optional<String> getSakaiHomeProp() {
        return Optional.ofNullable(System.getProperty("sakai.home"));
    }

    private Optional<String> getCatalinaBaseProp() {
        return Optional.ofNullable(System.getProperty("catalina.base"));
    }

    private void checkSecurityPath() {
        // check for the security home
        String securityPath = System.getProperty("sakai.security");
        if (securityPath != null) {
            // make sure it's properly slashed
            if (!securityPath.endsWith(File.separator))
                securityPath = securityPath + File.separatorChar;
            System.setProperty("sakai.security", securityPath);
        }
    }
}