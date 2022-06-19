package org.sakaiproject.announcement.tool;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(name = "kernel", locations = {
        "file:../../announcement-impl/impl/src/webapp/WEB-INF/components.xml"
})
public class ContextSetupTest extends ModiTest {
    @Inject
    public SiteService siteService;

    @Inject
    SecurityService securityService;

    @Inject
    public ApplicationContext context;

    @Test
    public void givenAModiKernel_whenWeExtendPlainTest_thenTheContextIsGeneric() throws IdUnusedException {
        assertThat(context).isInstanceOf(GenericApplicationContext.class);
    }

    @Test
    public void givenAModiKernelAndInjectedServices_whenAddingASite_thenItIsSaved() throws IdUnusedException, PermissionException, IdInvalidException, IdUsedException {
        SecurityAdvisor advisor = new SecurityAdvisor() {
            public SecurityAdvice isAllowed(String userId, String function, String reference) {
                return SecurityAdvice.ALLOWED;
            }
        };

        securityService.pushAdvisor(advisor);
        Site saved = siteService.addSite("context-setup", "project");
        Site retrieved = siteService.getSite("context-setup");
        securityService.popAdvisor(advisor);

        assertThat(saved).isEqualTo(retrieved);
    }
}
