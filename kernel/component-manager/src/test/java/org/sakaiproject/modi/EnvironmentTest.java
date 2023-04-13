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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

public class EnvironmentTest {
    @Rule
    public TemporaryFolder tmpDir = TemporaryFolder.builder().assureDeletion().build();

    public Path tmp;

    @Before
    public void setup() {
        System.clearProperty("catalina.base");
        System.clearProperty("sakai.home");
        System.clearProperty("sakai.security");
        System.clearProperty("sakai.components.root");
        System.clearProperty("sakai.modi.enabled");
        tmp = tmpDir.getRoot().toPath();
    }

    @Test
    public void givenMissingCatalinaBase_whenInitialized_thenInitializationFails() {
        System.setProperty("catalina.base", tmp.resolve("missing").toString());

        assertThatExceptionOfType(InitializationException.class).isThrownBy(() -> {
            Environment env = Environment.initialize();
        }).withMessageContaining("catalina.base is missing or unreadable");
    }

    @Test
    public void givenCatalinaBaseIsSet_whenInitialized_thenCatalinaBaseIsUnchanged() throws IOException {
        tmpDir.newFolder("catalina");
        tmpDir.newFolder("catalina/components");
        System.setProperty("catalina.base", tmp.resolve("catalina").toString());
        Environment env = Environment.initialize();

        assertThat(env.getCatalinaBase()).isEqualTo(tmp.resolve("catalina"));
    }

    @Test
    public void givenCatalinaBaseAndNoSakaiHome_whenInitialized_thenSakaiHomeIsInCatalinaBase() throws IOException {
        tmpDir.newFolder("components");
        System.setProperty("catalina.base", tmp.toString());
        Environment env = Environment.initialize();

        assertThat(env.getSakaiHome()).isEqualTo(tmp.resolve("sakai/"));
    }

    @Test
    public void givenDefaultStartup_whenInitialized_thenConfigurationFileIsInSakaiHome() throws IOException {
        tmpDir.newFolder("components");
        System.setProperty("catalina.base", tmp.toString());
        Environment env = Environment.initialize();

        assertThat(env.getConfigurationFile()).isEqualTo(tmp.resolve("sakai/sakai-configuration.xml"));
    }

    @Test
    public void givenDefaultStartup_whenInitialized_thenOverridesAreInSakaiHome() throws IOException {
        tmpDir.newFolder("components");
        System.setProperty("catalina.base", tmp.toString());
        Environment env = Environment.initialize();

        assertThat(env.getOverridesFolder()).isEqualTo(tmp.resolve("sakai/override"));
    }

    @Test
    public void givenSakaiHomeWithNoSlash_whenInitialized_thenSystemPropertyHasASlash() throws IOException {
        tmpDir.newFolder("components");
        System.setProperty("catalina.base", tmp.toString());
        System.setProperty("sakai.home", tmp.resolve("-SAKAI-").toString());
        Environment env = Environment.initialize();

        assertThat(System.getProperty("sakai.home")).isEqualTo(tmp.resolve("-SAKAI-") + "/");
    }

    @Test
    public void givenSakaiHomeWithASlash_whenInitialized_thenSakaiHomeIsProperPath() throws IOException {
        tmpDir.newFolder("components");
        System.setProperty("catalina.base", tmp.toString());
        System.setProperty("sakai.home", tmp.resolve("slash") + "/");
        Environment env = Environment.initialize();

        assertThat(env.getSakaiHome()).isEqualTo(tmp.resolve("slash"));
    }

    @Test
    public void givenUnwritableParentOfSakaiHome_whenInitialized_thenInitializationFails() throws IOException {
        tmpDir.newFolder("components");
        File file = tmpDir.newFolder("readonly");
        if (file.setReadOnly()) {
            System.setProperty("catalina.base", tmp.toString());
            System.setProperty("sakai.home", tmp.resolve("readonly/sakai").toString());

            assertThatExceptionOfType(InitializationException.class)
                    .isThrownBy(Environment::initialize)
                    .withMessageContaining("could not create sakai.home");
        }
    }

    @Test
    public void givenUnwritableSakaiHome_whenInitialized_thenInitializationFails() throws IOException {
        tmpDir.newFolder("components");
        File file = tmpDir.newFolder("readonly/sakai");
        if (file.setReadOnly()) {
            System.setProperty("catalina.base", tmp.toString());
            System.setProperty("sakai.home", tmp.resolve("readonly/sakai").toString());

            assertThatExceptionOfType(InitializationException.class)
                    .isThrownBy(Environment::initialize)
                    .withMessageContaining("sakai.home is missing or unreadable");
        }
    }

    @Test
    public void givenNoComponentsRoot_whenInitialized_thenComponentsAreUnderCatalina() throws IOException {
        tmpDir.newFolder("components");
        System.setProperty("catalina.base", tmp.toString());
        Environment env = Environment.initialize();

        assertThat(env.getComponentsRoot()).isEqualTo(tmp.resolve("components"));
    }

    @Test
    public void givenComponentsRoot_whenInitialized_thenComponentsAreThere() throws IOException {
        tmpDir.newFolder("somewhere-else");
        System.setProperty("catalina.base", tmp.toString());
        System.setProperty("sakai.components.root", tmp.resolve("somewhere-else").toString());
        Environment env = Environment.initialize();

        assertThat(env.getComponentsRoot()).isEqualTo(tmp.resolve("somewhere-else"));
    }

    @Test
    public void givenMissingComponentsRoot_whenInitialized_thenInitializationFails() throws IOException {
        System.setProperty("catalina.base", tmp.toString());
        System.setProperty("sakai.components.root", tmp.resolve("missing-components").toString());

        assertThatExceptionOfType(InitializationException.class)
                .isThrownBy(Environment::initialize)
                .withMessageContaining("sakai.components.root is missing or unreadable");
    }

    @Test
    public void givenNoSecurityDirectory_whenInitialized_thenNoSecurityDirectoryIsSet() throws IOException {
        tmpDir.newFolder("components");
        System.setProperty("catalina.base", tmp.toString());
        Environment env = Environment.initialize();

        assertThat(env.getSakaiSecurity()).isNull();
    }

    @Test
    public void givenASecurityDirectory_whenInitialized_thenTheSecurityDirectoryIsSet() throws IOException {
        tmpDir.newFolder("components");
        tmpDir.newFolder("secure");
        System.setProperty("catalina.base", tmp.toString());
        System.setProperty("sakai.security", tmp.resolve("secure").toString());
        Environment env = Environment.initialize();

        assertThat(env.getSakaiSecurity()).isEqualTo(tmp.resolve("secure"));
    }

    @Test
    public void givenAnUnreadableSecurityDirectory_whenInitialized_thenInitializationFails() throws IOException {
        tmpDir.newFolder("components");
        System.setProperty("catalina.base", tmp.toString());
        System.setProperty("sakai.security", tmp.resolve("secure-missing").toString());

        assertThatExceptionOfType(InitializationException.class)
                .isThrownBy(Environment::initialize)
                .withMessageContaining("sakai.security is missing or unreadable");
    }
}
