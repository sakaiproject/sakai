package org.sakaiproject.modi;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

public class TraditionalComponentTest {
    public Path componentRoot;

    @Rule public TemporaryFolder tmpDir = TemporaryFolder.builder().assureDeletion().build();
    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock private BeanDefinitionRegistry registry;

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
}