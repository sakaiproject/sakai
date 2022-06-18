package org.sakaiproject.modi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * A BeanDefinitionSource representing the beans in the core Sakai configuration in the kernel as well as the main local
 * configuration file implementors can use (sakai-configuration.xml).
 * <p>
 * There are typically very few beans at this level: the {@link org.sakaiproject.util.SakaiProperties} abstract bean,
 * the concrete one that lists the standard set of property files to read, and the
 * {@link org.sakaiproject.util.SakaiPropertyPromoter}, which sets certain system properties to the values supplied in
 * the cascading properties files. This is all so the fundamental environment is in place for component loading.
 * <p>
 * Typical beans in a local configuration are things like directory providers and so on. The vast majority of beans are
 * loaded in the component phase, including all of those from the kernel.
 * <p>
 * It should be noted: these files load before any component files, and those before overrides. The application context
 * merges them, with the latter files taking precedence.
 */
@Slf4j
public class Configuration implements BeanDefinitionSource {
    /**
     * Create a Configuration loader that will load the default config/properties beans.
     * <p>
     * If there is a local configuration file, use {@link #Configuration(Path)}.
     */
    public Configuration() {
        this.localConfigFile = null;
    }

    /** The local file for customization of sakai-configuration.xml. May be null. */
    protected final Path localConfigFile;

    /**
     * Create a Configuration that will load the default config/properties beans and those in the supplied local
     * configuration file.
     * <p>
     * This file is conventionally known as sakai-configuration.xml, but could now be called something else. The
     * {@link Launcher} is responsible for determining the location and whether it should be loaded. If there is no
     * local file, use the default constructor.
     * <p>
     * Within this class, we consider it fatal if the file is missing. We assume that, if supplied, it has essential
     * settings, and it would be more harmful to disregard the problem and continue booting than stopping immediately.
     *
     * @param localConfigFile a Spring beans XML file with customizations to the base configuration
     * @throws MissingConfigurationException if the supplied file is missing or unreadable
     */
    public Configuration(Path localConfigFile) throws MissingConfigurationException {
        this.localConfigFile = localConfigFile;
        validate();
    }

    @Override
    public String getName() {
        return "Base Sakai Configuration / Properties";
    }

    /**
     * Register beans from the default configuration from the kernel and, if supplied, the custom config file.
     *
     * @param registry the bean registry (application context) to configure
     */
    @Override
    public void registerBeans(BeanDefinitionRegistry registry) {
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);
        reader.loadBeanDefinitions(baseConfig());
        localConfig().ifPresent(reader::loadBeanDefinitions);
    }

    /**
     * The base configuration is read directly from the kernel jar, via the classpath.
     */
    protected Resource baseConfig() {
        return new ClassPathResource("org/sakaiproject/config/modi-configuration.xml");
    }

    protected Optional<Resource> localConfig() {
        return localConfigFile == null
                ? Optional.empty()
                : Optional.of(new FileSystemResource(localConfigFile));
    }

    protected void validate() throws MissingConfigurationException {
        if (localConfigFile == null || !Files.isRegularFile(localConfigFile))
            throw new MissingConfigurationException(localConfigFile);
    }
}
