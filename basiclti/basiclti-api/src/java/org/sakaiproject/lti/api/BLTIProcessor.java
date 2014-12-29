package org.sakaiproject.lti.api;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 2/15/12
 * Time: 3:44 PM
 * To change this template use File | Settings | File Templates.
 */
public interface BLTIProcessor {
    public int getOrder();
    public void beforeValidation(Map payload, boolean trustedConsumer) throws LTIException;
    public void afterValidation(Map payload, boolean trustedConsumer) throws LTIException;
    public void afterUserCreation(Map payload, User user) throws LTIException;
    public void afterLogin(Map payload, boolean trustedConsumer, User user) throws LTIException;
    public void afterSiteCreation(Map payload, boolean trustedConsumer, User user, Site site) throws LTIException;
    public void afterSiteMembership(Map payload, boolean trustedConsumer, User user, Site site) throws LTIException;
    public void beforeLaunch(Map payload, boolean trustedConsumer, User user, Site site, String toolPlacementId) throws LTIException;
}
