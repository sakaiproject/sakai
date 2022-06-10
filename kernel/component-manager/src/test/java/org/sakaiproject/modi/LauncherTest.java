package org.sakaiproject.modi;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class LauncherTest {

    public final Path fixtures = Paths.get("src", "test", "resources");
    public final Path fakeTomcat = fixtures.resolve("fake-tomcat");

    @Before
    public void cleanStart() {
        GlobalApplicationContext.destroyContext();
        SpyBean.instances = 0;
        System.clearProperty("sakai.home");
        System.clearProperty("sakai.demo");
    }

    @Test
    public void givenABeanInComponentsXml_whenStarted_thenTheBeanIsInitialized() {
        new Launcher(fakeTomcat).start();

        SpyBean bean = (SpyBean) globalContext().getBean("firstBean");

        assertThat(bean.getName()).isEqualTo("First Bean");
    }

    @Test
    public void givenSeparateComponents_whenStarted_thenTheirBeansAreAllRegistered() {
        new Launcher(fakeTomcat).start();

        SpyBean first = (SpyBean) globalContext().getBean("firstBean");
        SpyBean second = (SpyBean) globalContext().getBean("secondBean");
        List<String> names = List.of(first.getName(), second.getName());

        assertThat(names).containsExactly("First Bean", "Second Bean");
    }

    @Test
    public void givenALocalSakaiConfigurationXml_whenStarted_thenTheConfigIsApplied() {
        new Launcher(fakeTomcat).start();

        SpyBean customBean = (SpyBean) globalContext().getBean("customConfig");

        assertThat(customBean.getName()).isEqualTo("custom");
    }

    @Test
    public void givenAComponentOverride_whenStarted_thenTheComponentIsModified() {
        new Launcher(fakeTomcat).start();

        SpyBean overridden = (SpyBean) globalContext().getBean("firstOverride");

        assertThat(overridden.getName()).isEqualTo("overridden name");
    }

    @Test
    public void givenTwoComponentOverrides_whenStarted_thenBothComponentsAreModified() {
        new Launcher(fakeTomcat).start();

        SpyBean first = (SpyBean) globalContext().getBean("firstOverride");
        SpyBean second = (SpyBean) globalContext().getBean("secondOverride");
        List<String> names = List.of(first.getName(), second.getName());

        assertThat(names).containsExactly("overridden name", "also overridden");
    }

    /** We should only load files that match component names, not just any XML files in the directory. */
    @Test
    public void givenAnExtraneousFileInOverrides_whenStarted_thenItIsNotApplied() {
        new Launcher(fakeTomcat).start();

        assertThatExceptionOfType(NoSuchBeanDefinitionException.class)
                .isThrownBy(() -> globalContext().getBean("bogusBean"));
    }

    @Test
    public void givenDemoModeIsOff_whenStarted_thenDemoFilesAreNotLoaded() {
        new Launcher(fakeTomcat).start();

        assertThatExceptionOfType(NoSuchBeanDefinitionException.class)
                .isThrownBy(() -> globalContext().getBean("demoBean"));
    }

    @Test
    public void givenDemoModeIsOn_whenStarted_thenDemoFilesAreLoaded() {
        System.setProperty("sakai.demo", "true");
        new Launcher(fakeTomcat).start();

        SpyBean demo = (SpyBean) globalContext().getBean("demoBean");

        assertThat(demo.getName()).isEqualTo("Demo Bean");
    }

    /** We enforce that the property ends in a slash because it is used for bare concatenation. */
    @Test
    public void givenSakaHomeIsUnset_whenStarted_thenItIsSetToTomcatSakaiSlash() {
        new Launcher(fakeTomcat).start();

        assertThat(System.getProperty("sakai.home")).isEqualTo(fakeTomcat.resolve("sakai") + "/");
    }

    private SharedApplicationContext globalContext() {
        return GlobalApplicationContext.getContext() ;
    }
}