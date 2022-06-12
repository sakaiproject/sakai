package org.sakaiproject.announcement.tool;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.util.SakaiProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "file:../../announcement-impl/impl/src/webapp/WEB-INF/components.xml")
public class ARealAnnouncementsIT extends ModiTest {
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
