package org.sakaiproject.lti.api;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 2/15/12
 * Time: 3:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class AbstractBLTIProcessor implements BLTIProcessor {


    public int getOrder() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void beforeValidation(Map payload, boolean trustedConsumer) throws LTIException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void afterValidation(Map payload, boolean trustedConsumer) throws LTIException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void afterUserCreation(Map payload, User user) throws LTIException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void afterLogin(Map payload, boolean trustedConsumer, User user) throws LTIException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void afterSiteCreation(Map payload, boolean trustedConsumer, User user, Site site) throws LTIException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void afterSiteMembership(Map payload, boolean trustedConsumer, User user, Site site) throws LTIException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void beforeLaunch(Map payload, boolean trustedConsumer, User user, Site site, String toolPlacementId) throws LTIException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
