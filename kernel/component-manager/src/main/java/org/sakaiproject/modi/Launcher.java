package org.sakaiproject.modi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
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

    /** The Sakai configuration components, which must be the first loaded. */
    protected final static String[] CONFIGURATION_COMPONENTS = {
            "org.sakaiproject.component.SakaiPropertyPromoter",
            "org.sakaiproject.log.api.LogConfigurationManager" };

    protected final static String DEFAULT_CONFIGURATION_FILE = "classpath:/org/sakaiproject/config/sakai-configuration.xml";
    protected final static String CONFIGURATION_FILE_NAME = "sakai-configuration.xml";

    // take catalina.base as the only param
    // load config by convention (pick up sysprops)
    // load components by convention (Traditional, in components/, ComponentsDirectory?)
    // load overrides by convention -- probably belongs in component load... filtering the files, but also: why?

    protected void start() {
        log.info("Booting Sakai in Modern Dependency Injection Mode");

//        System.setProperty("sakai.modi", "true");
        Path componentsRoot = getComponentsRoot();
        Path overridePath = getOverridePath();

        context = GlobalApplicationContext.getContext();

        try {
            Resource r = new ClassPathResource("org/sakaiproject/config/sakai-configuration.xml");
            Files.readAllLines(Path.of(r.getURI())).forEach(log::info);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BeanDefinitionSource config = new BeanDefinitionSource() {
            @Override
            public void registerBeans(BeanDefinitionRegistry registry) {
                Resource config = new ClassPathResource(DEFAULT_CONFIGURATION_FILE);
                XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);
                reader.loadBeanDefinitions(config);
            }
        };

        context.registerBeanSource(config);

        if (componentsRoot != null) {
            System.setProperty(SAKAI_COMPONENTS_ROOT_SYS_PROP, componentsRoot.toString());
            components = new TraditionalComponents(componentsRoot, overridePath);
            components.starting(context);
        }
        context.refresh();
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

}