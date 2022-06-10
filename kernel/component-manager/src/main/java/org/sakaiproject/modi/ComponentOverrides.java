package org.sakaiproject.modi;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
class ComponentOverrides implements BeanDefinitionSource {

    private final List<TraditionalComponent> components;
    private final Path overridesFolder;

    public ComponentOverrides(@NonNull List<TraditionalComponent> components, @NonNull Path overridesFolder) {
        this.overridesFolder = overridesFolder;
        this.components = components;
    }

    @Override
    public String getName() {
        return "Local Component Overrides";
    }

    @Override
    public void registerBeans(BeanDefinitionRegistry registry) {
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);
        getOverrideFiles().forEach(file -> {
            FileSystemResource override = new FileSystemResource(file);
            reader.loadBeanDefinitions(override);
            log.info("Loaded component overrides from: {}", file);
        });
    }

    private Stream<Path> getOverrideFiles() {
        return components.stream()
                .map(c -> c.getName() + ".xml")
                .map(overridesFolder::resolve);
    }
}
