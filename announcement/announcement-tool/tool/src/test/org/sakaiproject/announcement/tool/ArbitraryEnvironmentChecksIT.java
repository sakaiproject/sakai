package org.sakaiproject.announcement.tool;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.util.SakaiProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = AnncCtxInitializer.class, locations = "../announcement-impl/impl/src/webapp/WEB-INF/components.xml")
public class ArbitraryEnvironmentChecksIT {
    @Inject
    public SakaiProperties props;

    @Inject
    public ArbitraryComponentToTestScanning injectedComponentFromTestPackage;

    @Test
    public void readprop() {
        String autoddl = props.getProperties().getProperty("auto.ddl");
        assertThat(autoddl).isEqualTo("true");
    }

    @Test
    public void injectionOfTestBeans() {
        assertThat(injectedComponentFromTestPackage.doesItWork()).isEqualTo("yes");
    }
}
