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

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class LauncherTest {

    public final Path fixtures = Paths.get("src", "test", "resources");
    public final Path fakeTomcat = fixtures.resolve("fake-tomcat").toAbsolutePath();

    @Before
    public void cleanStart() throws IOException {
        GlobalApplicationContext.destroyContext();
        SpyBean.instances = 0;
        System.clearProperty("sakai.home");
        System.clearProperty("sakai.demo");
        System.clearProperty("sakai.components.root");
        System.clearProperty("sakai.security");
        System.setProperty("catalina.base", fakeTomcat.toString());
        Files.deleteIfExists(fakeTomcat.resolveSibling("missing"));
    }

    @After
    public void tearDown() throws IOException {
        Files.deleteIfExists(fakeTomcat.resolveSibling("missing"));
    }

    @Test
    public void givenABeanInComponentsXml_whenStarted_thenTheBeanIsInitialized() {
        new Launcher().start();

        SpyBean bean = (SpyBean) globalContext().getBean("firstBean");

        assertThat(bean.getName()).isEqualTo("First Bean");
    }

    @Test
    public void givenSeparateComponents_whenStarted_thenTheirBeansAreAllRegistered() {
        new Launcher().start();

        SpyBean first = (SpyBean) globalContext().getBean("firstBean");
        SpyBean second = (SpyBean) globalContext().getBean("secondBean");
        List<String> names = List.of(first.getName(), second.getName());

        assertThat(names).containsExactly("First Bean", "Second Bean");
    }

    @Test
    public void givenALocalSakaiConfigurationXml_whenStarted_thenTheConfigIsApplied() {
        new Launcher().start();

        SpyBean customBean = (SpyBean) globalContext().getBean("customConfig");

        assertThat(customBean.getName()).isEqualTo("custom");
    }

    @Test
    public void givenAComponentOverride_whenStarted_thenTheComponentIsModified() {
        new Launcher().start();

        SpyBean overridden = (SpyBean) globalContext().getBean("firstOverride");

        assertThat(overridden.getName()).isEqualTo("overridden name");
    }

    @Test
    public void givenTwoComponentOverrides_whenStarted_thenBothComponentsAreModified() {
        new Launcher().start();

        SpyBean first = (SpyBean) globalContext().getBean("firstOverride");
        SpyBean second = (SpyBean) globalContext().getBean("secondOverride");
        List<String> names = List.of(first.getName(), second.getName());

        assertThat(names).containsExactly("overridden name", "also overridden");
    }

    /** We should only load files that match component names, not just any XML files in the directory. */
    @Test
    public void givenAnExtraneousFileInOverrides_whenStarted_thenItIsNotApplied() {
        new Launcher().start();

        assertThatExceptionOfType(NoSuchBeanDefinitionException.class)
                .isThrownBy(() -> globalContext().getBean("bogusBean"));
    }

    @Test
    public void givenDemoModeIsOff_whenStarted_thenDemoFilesAreNotLoaded() {
        new Launcher().start();

        assertThatExceptionOfType(NoSuchBeanDefinitionException.class)
                .isThrownBy(() -> globalContext().getBean("demoBean"));
    }

    @Test
    public void givenDemoModeIsOn_whenStarted_thenDemoFilesAreLoaded() {
        System.setProperty("sakai.demo", "true");
        new Launcher().start();

        SpyBean demo = (SpyBean) globalContext().getBean("demoBean");

        assertThat(demo.getName()).isEqualTo("Demo Bean");
    }

    /** We enforce that the property ends in a slash because it is used for bare concatenation. */
    @Test
    public void givenSakaHomeIsUnset_whenStarted_thenStartupHappensUnderTomcat() {
        new Launcher().start();

        assertThat(System.getProperty("sakai.home")).isEqualTo(fakeTomcat.resolve("sakai") + "/");
    }

    @Test
    public void givenSakaHomeIsSetWithNoSlash_whenStarted_thenItIsUpdatedWithSlash() {
        String externalHome = fakeTomcat.resolveSibling("external-home").toString();
        System.setProperty("sakai.home", externalHome);

        new Launcher().start();

        assertThat(System.getProperty("sakai.home")).isEqualTo(externalHome + "/");
    }

    @Test
    public void givenSakaHomeIsSetWithASlash_whenStarted_thenItIsUsedUnchanged() {
        String externalHome = fakeTomcat.resolveSibling("external-home").toString();
        System.setProperty("sakai.home", externalHome + "/");

        new Launcher().start();

        assertThat(System.getProperty("sakai.home")).isEqualTo(externalHome + "/");
    }

    @Test
    public void givenSakaHomeIsSetToSomethingMissing_whenStarted_thenItIsCreated() {
        Path missingHome = fakeTomcat.resolveSibling("missing");
        System.setProperty("sakai.home", missingHome.toString());

        new Launcher().start();

        assertThat(missingHome).isDirectory();
    }

    @Test
    public void givenANormalLaunch_whenStopping_thenTheContextIsClosed() {
        Launcher launcher = new Launcher();
        launcher.start();

        launcher.stop();

        SharedApplicationContext context = GlobalApplicationContext.getContext();
        assertThat(context.isActive()).isFalse();
    }

    @Test
    public void givenACompleteLaunch_whenStarting_thenConfigComponentsAndOverridesAreSourced() {
        Launcher launcher = new Launcher();
        launcher.start();

        SharedApplicationContext context = GlobalApplicationContext.getContext();
        List<Class> sources = context.sources.stream()
                .map(BeanDefinitionSource::getClass).collect(Collectors.toList());

        assertThat(sources).contains(Configuration.class, TraditionalComponent.class, ComponentOverrides.class);
    }

    @Test
    public void givenACompleteLaunch_whenStarting_thenComponentsNamesAreListedInSources() {
        Launcher launcher = new Launcher();
        launcher.start();

        SharedApplicationContext context = GlobalApplicationContext.getContext();
        List<String> sources = context.sources.stream()
                .map(BeanDefinitionSource::getName).collect(Collectors.toList());
        assertThat(sources).contains("fake-service", "second-component");
    }

    private SharedApplicationContext globalContext() {
        return GlobalApplicationContext.getContext();
    }
}
