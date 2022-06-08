package org.sakaiproject.modi;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

public class TraditionalComponentTest {
    public Path componentRoot;

    @Rule public TemporaryFolder tmpDir = TemporaryFolder.builder().assureDeletion().build();
    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

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
    public void givenAComponentWithOneBean_whenRegisteringTheComponent_theBeanIsRegistered() throws URISyntaxException, MalformedComponentException {
        Path hello = fixtureComponentPath("hello-component");
        TraditionalComponent component = new TraditionalComponent(hello);
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        component.registerBeans(registry);

        assertThat(registry.containsBeanDefinition("hello")).isTrue();
    }

    private Path fixtureComponentPath(String name) throws URISyntaxException {
        return Path.of(getClass().getClassLoader().getResource(name).toURI());
    }
}