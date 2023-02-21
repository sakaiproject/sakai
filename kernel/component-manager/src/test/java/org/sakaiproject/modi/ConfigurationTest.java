/*
 * Copyright (c) 2003-2022 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.modi;

import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Test that the fundamental configuration loading is working.
 * <p>
 * We don't want to test every detail of sakai-configuration.xml, but we do want to confirm that the three main
 * config/property beans are loaded and that we can supply custom configuration. Everything else depends on these beans,
 * so if something changes, we want to be aware.
 */
public class ConfigurationTest {
    @Test
    public void givenDefaultConfiguration_whenRegistering_thenDefaultSakaiPropertiesIsLoaded() {
        Configuration config = new Configuration();
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        config.registerBeans(registry);

        boolean loaded = registry.containsBeanDefinition("org.sakaiproject.component.DefaultSakaiProperties");
        assertThat(loaded).as("DefaultSakaiProperties bean is loaded").isTrue();
    }

    @Test
    public void givenDefaultConfiguration_whenRegistering_thenSakaiPropertiesIsLoaded() {
        Configuration config = new Configuration();
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        config.registerBeans(registry);

        boolean loaded = registry.containsBeanDefinition("org.sakaiproject.component.SakaiProperties");
        assertThat(loaded).as("SakaiProperties bean is loaded").isTrue();
    }

    @Test
    public void givenDefaultConfiguration_whenRegistering_thenPropertyPromoterIsLoaded() {
        Configuration config = new Configuration();
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        config.registerBeans(registry);

        boolean loaded = registry.containsBeanDefinition("org.sakaiproject.component.SakaiPropertyPromoter");
        assertThat(loaded).as("SakaiPropertyPromoter bean is loaded").isTrue();
    }

    @Test
    public void givenAMissingConfigFile_whenConstructing_thenExceptionIsThrown() throws IOException {
        Path tmpFile = Files.createTempFile("missing-config-", ".xml");
        Files.delete(tmpFile);

        Exception thrown = catchException(() -> new Configuration(tmpFile));

        assertThat(thrown).hasMessageContaining("missing supplied startup configuration");
    }

    @Test
    public void givenACustomSakaiProperties_whenRegistering_thenTheDefaultIsOverridden() throws MissingConfigurationException {
        Configuration config = new Configuration(Path.of("src/test/resources/override-properties/sakai-configuration.xml"));
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        config.registerBeans(registry);

        BeanDefinition bean = registry.getBeanDefinition("org.sakaiproject.component.SakaiProperties");
        String value = beanPropertyString(bean, "testprop");
        assertThat(value).isEqualTo("is set");
    }

    private String beanPropertyString(BeanDefinition bean, String property) {
        return ((TypedStringValue) bean.getPropertyValues().get(property)).getValue();
    }
}
