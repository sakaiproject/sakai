package org.sakaiproject.announcement.tool;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
// TODO: Remove this when everything moves to kernel. Needed for now because the bullhorn class defined here is picked up by the container, so we need the service available
@ContextConfiguration(locations = {
        "file:../../announcement-impl/impl/src/webapp/WEB-INF/components.xml"
})
public class WebContextSetupTest extends ModiWebTest {
    @Inject
    WebApplicationContext context;
    WebTestClient client;

    @Before
    public void setup() {
        client = MockMvcWebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    public void givenAModiKernelAndExtendingWebTest_thenTheContextIsGenericWeb() {
        assertThat(context).isInstanceOf(GenericWebApplicationContext.class);
    }

    @Test
    public void asdf() {
        client.get().uri("/").exchange().expectStatus().isOk();
    }
}
