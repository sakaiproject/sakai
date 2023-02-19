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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

public class ComponentLoadingTest {
    public Path componentRoot;

    @Rule
    public TemporaryFolder tmpDir = TemporaryFolder.builder().assureDeletion().build();

    @Before
    public void makeComponentRoot() throws IOException {
        componentRoot = tmpDir.newFolder("sample-component").toPath();
    }

    @Test
    public void givenAMissingComponentDirectory_thenItIsMalformed() {
        Exception e = catchException(() -> new TraditionalComponent(componentRoot.resolve("missing-dir")));
        assertThat(e).hasMessageContaining("is not a directory");
    }

    @Test
    public void givenNoWebInf_thenItIsMalformed() {
        Exception e = catchException(() -> new TraditionalComponent(componentRoot));
        assertThat(e).hasMessageContaining("does not contain a WEB-INF/ directory");
    }

    @Test
    public void givenNoWebInf_whenUsingTheSafeFactory_thenItIsEmpty() {
        Optional<TraditionalComponent> component = TraditionalComponent.fromDisk(componentRoot);
        assertThat(component).isEmpty();
    }

    @Test
    public void givenNoComponentsXml_thenItIsMalformed() throws IOException {
        Files.createDirectory(componentRoot.resolve("WEB-INF"));

        Exception e = catchException(() -> new TraditionalComponent(componentRoot));
        assertThat(e).hasMessageContaining("does not contain WEB-INF/components.xml");
    }

    @Test
    public void givenAComponentDirectory_thenItsNameMatches() throws URISyntaxException, MalformedComponentException {
        Path hello = fixtureComponentPath("hello-component");
        TraditionalComponent component = new TraditionalComponent(hello);

        assertThat(component.getName()).isEqualTo("hello-component");
    }

    @Test
    public void givenAComponentDirectory_thenItsPathIsExposed() throws URISyntaxException, MalformedComponentException {
        Path hello = fixtureComponentPath("hello-component");
        TraditionalComponent component = new TraditionalComponent(hello);

        assertThat(component.getPath()).isEqualTo(hello);
    }

    @Test
    public void givenAComponentWithOneBean_whenRegisteringTheComponent_theBeanIsRegistered() throws URISyntaxException, MalformedComponentException {
        Path hello = fixtureComponentPath("hello-component");
        TraditionalComponent component = new TraditionalComponent(hello);
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        component.registerBeans(registry);

        assertThat(registry.containsBeanDefinition("hello")).isTrue();
    }

    @Test
    public void givenAComponentWithAClass_thenTheBeanHasTheLoadedClass() throws URISyntaxException, MalformedComponentException {
        Path path = fixtureComponentPath("everything-component");
        TraditionalComponent component = new TraditionalComponent(path);
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        component.registerBeans(registry);

        BeanDefinition bd = registry.getBeanDefinition("classSpy");
        assertThat(bd.getBeanClassName()).isEqualTo("org.sakaiproject.testspy.ClassSpy");
    }

    @Test
    public void givenAComponentWithAJar_thenTheBeanHasTheClassFromTheJar() throws URISyntaxException, MalformedComponentException {
        Path path = fixtureComponentPath("everything-component");
        TraditionalComponent component = new TraditionalComponent(path);
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        component.registerBeans(registry);

        BeanDefinition bd = registry.getBeanDefinition("jarSpy");
        assertThat(bd.getBeanClassName()).isEqualTo("org.sakaiproject.testspy.JarSpy");
    }

    @Test
    public void givenAComponentWithDemoBean_whenInDemoMode_theBeanIsRegistered() throws URISyntaxException, MalformedComponentException {
        System.setProperty("sakai.demo", "true");
        Path path = fixtureComponentPath("everything-component");
        TraditionalComponent component = new TraditionalComponent(path);
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        component.registerBeans(registry);

        BeanDefinition bd = registry.getBeanDefinition("demoSpy");
        assertThat(bd.getBeanClassName()).isEqualTo("org.sakaiproject.testspy.ClassSpy");
    }

    @Test
    public void givenAComponentWithDemoBean_whenNotInDemoMode_theBeanIsRegistered() throws URISyntaxException, MalformedComponentException {
        System.setProperty("sakai.demo", "false");
        Path path = fixtureComponentPath("everything-component");
        TraditionalComponent component = new TraditionalComponent(path);
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        component.registerBeans(registry);

        assertThatExceptionOfType(NoSuchBeanDefinitionException.class).isThrownBy(() -> {
            BeanDefinition bd = registry.getBeanDefinition("demoSpy");
        });
    }

    // This is a special case test of a protected method. We don't know of anywhere the URIs we
    // get from Path for actual local files would result in bad URLs, but we do catch it, and want
    // to ensure that we would handle it properly in case some strange filesystem thing applies.
    @Test
    public void givenABasicComponent_whenWeConvertABadURL_thenItGivesAnEmpty() throws URISyntaxException, MalformedComponentException {
        Path hello = fixtureComponentPath("hello-component");
        TraditionalComponent component = new TraditionalComponent(hello);

        URI goodUri = new URI("badproto:badurl");
        assertThat(component.toURL(goodUri)).isEmpty();
    }

    private Path fixtureComponentPath(String name) throws URISyntaxException {
        return Path.of(getClass().getClassLoader().getResource(name).toURI());
    }
}
