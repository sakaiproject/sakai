package org.sakaiproject.announcement.tool;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "file:../../announcement-impl/impl/src/webapp/WEB-INF/components.xml"
})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class KernelIntegrityTest extends ModiTest {
    @Inject
    public SiteService siteService;

    @Inject
    public SecurityService securityService;

    @Test
    public void _1_givenAModiKernel_thenWeCanRunRealIntegrationTests() throws IdUnusedException {
        SecurityAdvisor advisor = new SecurityAdvisor() {
            public SecurityAdvice isAllowed(String userId, String function, String reference) {
                return SecurityAdvice.ALLOWED;
            }
        };

        securityService.pushAdvisor(advisor);
        String siteTitle = siteService.getSite("!admin").getTitle();
        securityService.popAdvisor(advisor);

        assertThat(siteTitle).isEqualTo("Administration Workspace");
    }

    @Test
    public void _2_givenAModiKernel_whenStartingUp_thenWeHaveACleanDatabase() throws IdUnusedException, PermissionException, IdInvalidException, IdUsedException {
        SecurityAdvisor advisor = new SecurityAdvisor() {
            public SecurityAdvice isAllowed(String userId, String function, String reference) {
                return SecurityAdvice.ALLOWED;
            }
        };

        securityService.pushAdvisor(advisor);
        assertThatExceptionOfType(IdUnusedException.class).isThrownBy(() -> {
            siteService.getSite("this-is-a-test");
        });
        securityService.popAdvisor(advisor);
    }

    @Test
    public void _3_givenAModiKernel_whenAddingASite_thenItIsSaved() throws IdUnusedException, PermissionException, IdInvalidException, IdUsedException {
        SecurityAdvisor advisor = new SecurityAdvisor() {
            public SecurityAdvice isAllowed(String userId, String function, String reference) {
                return SecurityAdvice.ALLOWED;
            }
        };

        securityService.pushAdvisor(advisor);
        Site saved = siteService.addSite("this-is-a-test", "project");
        Site retrieved = siteService.getSite("this-is-a-test");
        securityService.popAdvisor(advisor);

        assertThat(saved).isEqualTo(retrieved);
    }

    @Test
    public void _4_givenATestSuite_whenWeLeaveSetupAround_thenItWillAffectSubsequentTests() throws IdUnusedException {
        SecurityAdvisor advisor = new SecurityAdvisor() {
            public SecurityAdvice isAllowed(String userId, String function, String reference) {
                return SecurityAdvice.ALLOWED;
            }
        };

        securityService.pushAdvisor(advisor);
        assertThatCode(() -> {
            siteService.getSite("this-is-a-test");
        }).doesNotThrowAnyException();
        securityService.popAdvisor(advisor);
    }
}
