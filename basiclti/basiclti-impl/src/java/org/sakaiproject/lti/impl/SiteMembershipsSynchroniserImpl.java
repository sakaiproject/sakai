package org.sakaiproject.lti.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.imsglobal.basiclti.BasicLTIConstants;

import net.oauth.*;
import net.oauth.signature.OAuthSignatureMethod;

import org.sakaiproject.basiclti.util.ShaUtil;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.lti.api.LTIException;
import org.sakaiproject.lti.api.SiteMembershipUpdater;
import org.sakaiproject.lti.api.UserFinderOrCreator;
import org.sakaiproject.lti.api.SiteMembershipsSynchroniser;
import org.sakaiproject.lti.extensions.POXMembershipsResponse;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;

import org.sakaiproject.component.api.ServerConfigurationService;

import java.net.HttpURLConnection;
import java.net.URL;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SiteMembershipsSynchroniserImpl implements SiteMembershipsSynchroniser {

	private static Log M_log = LogFactory.getLog(SiteMembershipsSynchroniserImpl.class);

    private UserFinderOrCreator userFinderOrCreator = null;
    public void setUserFinderOrCreator(UserFinderOrCreator userFinderOrCreator) {
        this.userFinderOrCreator = userFinderOrCreator;
    }

    private ServerConfigurationService serverConfigurationService;
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    private SiteMembershipUpdater siteMembershipUpdater = null;
    public void setSiteMembershipUpdater(SiteMembershipUpdater siteMembershipUpdater) {
        this.siteMembershipUpdater = siteMembershipUpdater;
    }

    private SiteService siteService = null;
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void synchroniseSiteMemberships(final String siteId, final String membershipsId, final String membershipsUrl, final String oauth_consumer_key, final String callbackType) throws LTIException {

        Site site = null;

        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException iue) {
            throw new LTIException("site.notfound", siteId, null);
        }

        if (BasicLTIConstants.LTI_VERSION_1.equals(callbackType)) {
            synchronizeLTI1SiteMemberships(site, membershipsId, membershipsUrl, oauth_consumer_key);
        }
    }

    private final void synchronizeLTI1SiteMemberships(final Site site, final String membershipsId, final String membershipsUrl, final String oauth_consumer_key) throws LTIException {

        // Lookup the secret
        final String configPrefix = "basiclti.provider." + oauth_consumer_key + ".";
        final String oauth_secret = serverConfigurationService.getString(configPrefix+ "secret", null);
        if (oauth_secret == null) {
            throw new LTIException( "launch.key.notfound", oauth_consumer_key, null);
        }

        OAuthMessage om = new OAuthMessage("POST", membershipsUrl, null);
        om.addParameter(OAuth.OAUTH_CONSUMER_KEY, oauth_consumer_key);
        om.addParameter(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.HMAC_SHA1);
        om.addParameter(OAuth.OAUTH_VERSION, "1.0");
        om.addParameter(OAuth.OAUTH_TIMESTAMP, new Long((new Date().getTime()) / 1000).toString());
        om.addParameter(OAuth.OAUTH_NONCE, UUID.randomUUID().toString());
        om.addParameter(BasicLTIConstants.LTI_MESSAGE_TYPE, "basic-lis-readmembershipsforcontext");
        om.addParameter(BasicLTIConstants.LTI_VERSION, "LTI-1p0");
        om.addParameter("id", membershipsId);

        OAuthConsumer oc = new OAuthConsumer(null, oauth_consumer_key, oauth_secret, null);

        try {
            OAuthSignatureMethod osm = OAuthSignatureMethod.newMethod(OAuth.HMAC_SHA1, new OAuthAccessor(oc));
            osm.sign(om);

            URL url = new URL(membershipsUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false); 
            connection.setRequestMethod("POST");
            connection.setUseCaches (false);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            bw.write(OAuth.formEncode(om.getParameters()));
            bw.flush();
            bw.close();

            processMembershipsResponse(connection, site, oauth_consumer_key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMembershipsResponse(HttpURLConnection connection, Site site, String oauth_consumer_key) throws Exception {

        M_log.debug("processMembershipsResponse");

        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        POXMembershipsResponse poxMembershipsResponse = new POXMembershipsResponse(br);

        connection.disconnect();

        List<POXMembershipsResponse.Member> members = poxMembershipsResponse.getMembers();

        Map<String,List<POXMembershipsResponse.Member>> consumerGroups = poxMembershipsResponse.getGroups();

        if (M_log.isDebugEnabled()) {
            for (POXMembershipsResponse.Member member : members) {
                M_log.debug("Member:");
                M_log.debug("\tUser ID: " + member.userId);
                M_log.debug("\tFirst Name: " + member.firstName);
                M_log.debug("\tLast Name: " + member.lastName);
                M_log.debug("\tEmail: " + member.email);
                M_log.debug("\tRole: " + member.role);
            }

            for (String groupTitle : consumerGroups.keySet()) {
                M_log.debug("Group: " + groupTitle);
                for(POXMembershipsResponse.Member groupMember : consumerGroups.get(groupTitle)) {
                    M_log.debug("\tGroup Member ID: " + groupMember.userId);
                }
            }
        }

        for (POXMembershipsResponse.Member member : members) {

            Map map = new HashMap();
            map.put(BasicLTIConstants.USER_ID, member.userId);
            map.put(BasicLTIConstants.LIS_PERSON_NAME_GIVEN, member.firstName);
            map.put(BasicLTIConstants.LIS_PERSON_NAME_FAMILY, member.lastName);
            map.put(BasicLTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY, member.email);
            map.put(BasicLTIConstants.ROLES, member.role);
            map.put(OAuth.OAUTH_CONSUMER_KEY, oauth_consumer_key);
            map.put("tool_id", "n/a");

            User user = userFinderOrCreator.findOrCreateUser(map, false);
            member.userId = user.getId();
            siteMembershipUpdater.addOrUpdateSiteMembership(map, false, user, site);
        }

        Collection sakaiGroups = site.getGroups();

        for (String consumerGroupTitle : consumerGroups.keySet()) {
            M_log.debug("Processing consumer group '" + consumerGroupTitle + "' ...");
            Group sakaiGroup = null;
            // See if the group exists already
            for (Iterator i = sakaiGroups.iterator();i.hasNext();) {
                Group currentSakaiGroup = (Group) i.next();
                if (consumerGroupTitle.equals(currentSakaiGroup.getTitle())) {
                    sakaiGroup = currentSakaiGroup;
                    break;
                }
            }

            if (sakaiGroup == null) {
                // New group. Create it.
                if (M_log.isDebugEnabled()) M_log.debug("Creating group with title '" + consumerGroupTitle + "' ...");
                sakaiGroup = site.addGroup();
                sakaiGroup.getProperties().addProperty(sakaiGroup.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
                sakaiGroup.setTitle(consumerGroupTitle);

            } else {
                M_log.debug("Existing group. Removing current membership ...");
                sakaiGroup.removeMembers();
            }

            for (POXMembershipsResponse.Member consumerGroupMember : consumerGroups.get(consumerGroupTitle)) {
                if (M_log.isDebugEnabled()) M_log.debug("Adding '" + consumerGroupMember.firstName + " " + consumerGroupMember.lastName + "' to '" + consumerGroupTitle + "' ...");
                sakaiGroup.addMember(consumerGroupMember.userId,consumerGroupMember.role,true,false);
            }

            try {
                siteService.save(site);
                M_log.info("Updated  site=" + site.getId() + " group=" + consumerGroupTitle);
            } catch (Exception e) {
                M_log.warn("Failed to add group '" + consumerGroupTitle + "' to site");
            }
        }
    }
}
