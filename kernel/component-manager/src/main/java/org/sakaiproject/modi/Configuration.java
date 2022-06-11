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

@Slf4j
class Configuration implements BeanDefinitionSource {
    /**
     * The default config beans are defined in sakai-configuration.xml, embedded the kernel-impl jar,
     * and read from the classpath.
     */
    public final static String CLASSPATH_SAKAI_CONFIG = "org/sakaiproject/config/sakai-configuration.xml";

    /** The local file for customization of sakai-configuration.xml. May be null. */
    protected final Path localConfigFile;

    /**
     * Create a Configuration loader that will load the default config/properties beans.
     *
     * If there is a local configuration file, use {@link #Configuration(Path)}.
     */
    public Configuration() {
        this.localConfigFile = null;
    }

    /**
     * Create a Configuration that will load the default config/properties beans and those in the
     * supplied local configuration file.
     *
     * This file is conventionally known as sakai-configuration.xml, but could now be called
     * something else. The {@link Launcher} is responsible for determining the location and
     * whether it should be loaded. If there is no local file, use the default constructor.
     *
     * Within this class, we consider it fatal if the file is missing. We assume that,
     * if supplied, it has essential settings, and it would be more harmful to disregard
     * the problem and continue booting than stopping immediately.
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
     * Register beans from the default configuration from the kernel and, if supplied, the
     * custom config file.
     *
     * @param registry the bean registry (application context) to configure
     */
    @Override
    public void registerBeans(BeanDefinitionRegistry registry) {
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);
        reader.loadBeanDefinitions(baseConfig());
        localConfig().ifPresent(reader::loadBeanDefinitions);
    }

    protected Resource baseConfig() {
        return new ClassPathResource(CLASSPATH_SAKAI_CONFIG);
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