package org.sakaiproject.modi;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class LauncherTest {

    public final Path fixtures = Paths.get("src", "test", "resources");
    public final Path fakeTomcat = fixtures.resolve("fake-tomcat");

    @Before
    public void resetSpyCount() {
        SpyBean.instances = 0;
    }

    @Test
    public void givenABad() throws IOException {
        assertThatNoException().isThrownBy(new Launcher(fakeTomcat)::start);
    }

    @Test
    public void createsComponentBeans() {
        new Launcher(fakeTomcat).start();

        assertThat(SpyBean.instances).isEqualTo(1);
    }

    @Test
    public void givenABeanInComponentsXml_whenCheckingTheGlobalContext_itIsPresent() {
        new Launcher(fakeTomcat).start();

        SpyBean spy = (SpyBean) GlobalApplicationContext.getContext().getBean("aSpy");

        assertThat(spy.getName()).isEqualTo("Bond");
    }

    @Test
    public void givenTwoComponents_theirBeansAreRegistered() {
        new Launcher(fakeTomcat).start();

        Object first = GlobalApplicationContext.getContext().getBean("aSpy");
        Object second = GlobalApplicationContext.getContext().getBean("secondBean");

        assertThatList(List.of(first, second)).allMatch(Objects::nonNull);
    }
}
