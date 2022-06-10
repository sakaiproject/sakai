package org.sakaiproject.modi;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class Launcher {
    protected final Path catalinaBase;
    protected TraditionalComponents components;
    protected SharedApplicationContext context;

    public Launcher(Path catalinaBase) {
        this.catalinaBase = catalinaBase;
    }

    /** The java system property name where the full path to the components packages. */
    public static final String SAKAI_COMPONENTS_ROOT_SYS_PROP = "sakai.components.root";

    protected final static String CONFIGURATION_FILE_NAME = "sakai-configuration.xml";

    // take catalina.base as the only param
    // load config by convention (pick up sysprops)
    // load components by convention (Traditional, in components/, ComponentsDirectory?)
    // load overrides by convention -- probably belongs in component load... filtering the files, but also: why?

    protected void start() throws MissingConfigurationException {
        log.info("Booting Sakai in Modern Dependency Injection Mode");
        System.setProperty("sakai.use.modi", "true");
        Path componentsRoot = getComponentsRoot();
        Path overridePath = getOverridePath();

        ensureSakaiHome();
        checkSecurityPath();

        context = GlobalApplicationContext.getContext();

        context.registerBeanSource(getConfiguration());

        if (componentsRoot != null) {
            System.setProperty(SAKAI_COMPONENTS_ROOT_SYS_PROP, componentsRoot.toString());
            components = new TraditionalComponents(componentsRoot, overridePath);
            components.starting(context);
        }
        context.refresh();
        // These are MAGIC beans.......
//        context.getBean("org.sakaiproject.component.SakaiPropertyPromoter");
//        context.getBean("org.sakaiproject.log.api.LogConfigurationManager");
        context.start();
        log.info("===================================== and we have started");
//        ComponentManager.getInstance();
    }

    protected void stop() {
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

    private void ensureSakaiHome() {
        // find a path to sakai files on the app server - if not set, set it
        String sakaiHomePath = System.getProperty("sakai.home");
        if (sakaiHomePath == null) {
            String catalina = catalinaBase.toString();
            if (catalina != null) {
                sakaiHomePath = catalina + File.separatorChar + "sakai"
                        + File.separatorChar;
            }
        }

        // strange case...
        if (sakaiHomePath == null) {
            // last resort try /tmp/sakai
            sakaiHomePath = File.separatorChar + "tmp" + File.separatorChar + "sakai" + File.separatorChar;
        }
        if (!sakaiHomePath.endsWith(File.separator))
            sakaiHomePath = sakaiHomePath + File.separatorChar;

        final File sakaiHomeDirectory = new File(sakaiHomePath);
        if (!sakaiHomeDirectory.exists()) // no sakai.home directory exists,
        // try to create one
        {
            if (sakaiHomeDirectory.mkdir()) {
                log.debug("Created sakai.home directory at: "
                        + sakaiHomePath);
            } else {
                log.warn("Could not create sakai.home directory at: "
                        + sakaiHomePath);
            }
        }

        // make sure it's set properly
        System.setProperty("sakai.home", sakaiHomePath);
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