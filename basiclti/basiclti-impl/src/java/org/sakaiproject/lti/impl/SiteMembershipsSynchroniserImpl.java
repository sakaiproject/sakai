package org.sakaiproject.lti.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.tsugi.basiclti.BasicLTIConstants;

import net.oauth.*;
import net.oauth.signature.OAuthSignatureMethod;

import org.sakaiproject.basiclti.util.LegacyShaUtil;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
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

	private void pushAdvisor() {

		// setup a security advisor
		SecurityService.pushAdvisor(new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function,
					String reference) {
				return SecurityAdvice.ALLOWED;
			}
		});
	}

	private void popAdvisor() {
		SecurityService.popAdvisor();
	}

    public void synchroniseSiteMemberships(final String siteId, final String membershipsId, final String membershipsUrl, final String oauth_consumer_key, boolean isEmailTrustedConsumer, final String callbackType) {

        Site site = null;

        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException iue) {
            M_log.error("site.notfound id: " + siteId + ". This site's memberships will NOT be synchronised.", iue);
            return;
        }

        if (BasicLTIConstants.LTI_VERSION_1.equals(callbackType)) {
            synchronizeLTI1SiteMemberships(site, membershipsId, membershipsUrl, oauth_consumer_key, isEmailTrustedConsumer);
        } else if ("ext-moodle-2".equals(callbackType)) {
            // This is non standard. Moodle's core LTI plugin does not currently do memberships and 
            // a fix for this has been proposed at https://tracker.moodle.org/browse/MDL-41724. I don't
            // think this will ever become core and the first time memberships will appear in core lti
            // is with LTI2. At that point this code will be replaced with standard LTI2 JSON type stuff.
            synchronizeMoodleExtSiteMemberships(site, membershipsId, membershipsUrl, oauth_consumer_key, isEmailTrustedConsumer);
        }
    }

    private final void synchronizeLTI1SiteMemberships(final Site site, final String membershipsId, final String membershipsUrl, final String oauth_consumer_key, boolean isEmailTrustedConsumer) {

        // Lookup the secret
        final String configPrefix = "basiclti.provider." + oauth_consumer_key + ".";
        final String oauth_secret = serverConfigurationService.getString(configPrefix+ "secret", null);
        if (oauth_secret == null) {
            M_log.error("launch.key.notfound " + oauth_consumer_key + ". This site's memberships will NOT be synchronised.");
            return;
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

            processMembershipsResponse(connection, site, oauth_consumer_key, isEmailTrustedConsumer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final void synchronizeMoodleExtSiteMemberships(final Site site, final String membershipsId, final String membershipsUrl, final String oauth_consumer_key, boolean isEmailTrustedConsumer) {

        // Lookup the secret
        final String configPrefix = "basiclti.provider." + oauth_consumer_key + ".";
        final String oauth_secret = serverConfigurationService.getString(configPrefix+ "secret", null);
        if (oauth_secret == null) {
            M_log.error("launch.key.notfound " + oauth_consumer_key + ". This site's memberships will NOT be synchronised.");
            return;
        }

        String type = "readMembershipsWithGroups";
        String uuid = UUID.randomUUID().toString();
        String xml = "<sourcedId>" + membershipsId + "</sourcedId>";

        StringBuilder sb = new StringBuilder("<?xml version = \"1.0\" encoding = \"UTF-8\"?>");
        sb.append("<imsx_POXEnvelope xmlns = \"http://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">");
        sb.append("<imsx_POXHeader>");
        sb.append("<imsx_POXRequestHeaderInfo>");
        sb.append("<imsx_version>V1.0</imsx_version>");
        sb.append("<imsx_messageIdentifier>" + uuid + "</imsx_messageIdentifier>");
        sb.append("</imsx_POXRequestHeaderInfo>");
        sb.append("</imsx_POXHeader>");
        sb.append("<imsx_POXBody>");
        sb.append("<" + type + "Request>");
        sb.append(xml);
        sb.append("</" + type + "Request>");
        sb.append("</imsx_POXBody>");
        sb.append("</imsx_POXEnvelope>");

        String callXml = sb.toString();

        if(M_log.isDebugEnabled()) M_log.debug("callXml: " + callXml);

        String bodyHash = OAuthSignatureMethod.base64Encode(LegacyShaUtil.sha1(callXml));
        M_log.debug(bodyHash);

        OAuthMessage om = new OAuthMessage("POST", membershipsUrl, null);
        om.addParameter("oauth_body_hash", bodyHash);
        om.addParameter("oauth_consumer_key", oauth_consumer_key);
        om.addParameter("oauth_signature_method", "HMAC-SHA1");
        om.addParameter("oauth_version", "1.0");
        om.addParameter("oauth_timestamp", new Long(new Date().getTime()).toString());

        OAuthConsumer oc = new OAuthConsumer(null, oauth_consumer_key, oauth_secret, null);

        try {
            OAuthSignatureMethod osm = OAuthSignatureMethod.newMethod("HMAC-SHA1",new OAuthAccessor(oc));
            osm.sign(om);

            String authzHeader = om.getAuthorizationHeader(null);

            if(M_log.isDebugEnabled()) M_log.debug("AUTHZ HEADER: " + authzHeader);

            URL url = new URL(membershipsUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false); 
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", authzHeader);
            connection.setRequestProperty("Content-Length", "" + Integer.toString(callXml.getBytes().length));
            connection.setRequestProperty("Content-Type", "text/xml");
            connection.setUseCaches (false);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            bw.write(callXml);
            bw.flush();
            bw.close();

            processMembershipsResponse(connection, site, oauth_consumer_key, isEmailTrustedConsumer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMembershipsResponse(HttpURLConnection connection, Site site, String oauth_consumer_key, boolean isEmailTrustedConsumer) throws Exception {

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

        site.removeMembers();

        for (POXMembershipsResponse.Member member : members) {

            Map map = new HashMap();
            map.put(BasicLTIConstants.USER_ID, member.userId);
            map.put(BasicLTIConstants.LIS_PERSON_NAME_GIVEN, member.firstName);
            map.put(BasicLTIConstants.LIS_PERSON_NAME_FAMILY, member.lastName);
            map.put(BasicLTIConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY, member.email);
            map.put(BasicLTIConstants.ROLES, member.role);
            map.put(OAuth.OAUTH_CONSUMER_KEY, oauth_consumer_key);
            map.put("tool_id", "n/a");

            User user = userFinderOrCreator.findOrCreateUser(map, false,isEmailTrustedConsumer);
            member.userId = user.getId();
            siteMembershipUpdater.addOrUpdateSiteMembership(map, false, user, site);
        }

        // Do this so we don't get a concurrent mod exception
        List groups = new ArrayList(site.getGroups());

        // Remove the existing groups
        for (Iterator i = groups.iterator(); i.hasNext(); ) {
            site.removeGroup((Group) i.next());
        }

        for (String consumerGroupTitle : consumerGroups.keySet()) {
            if (M_log.isDebugEnabled()) {
                M_log.debug("Creating group with title '" + consumerGroupTitle + "' ...");
            }

            Group sakaiGroup = site.addGroup();
            sakaiGroup.getProperties().addProperty(sakaiGroup.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
            sakaiGroup.setTitle(consumerGroupTitle);

            for (POXMembershipsResponse.Member consumerGroupMember : consumerGroups.get(consumerGroupTitle)) {
                if (M_log.isDebugEnabled()) {
                    M_log.debug("Adding '" + consumerGroupMember.firstName + " " + consumerGroupMember.lastName + "' to '" + consumerGroupTitle + "' ...");
                }

                sakaiGroup.addMember(consumerGroupMember.userId, consumerGroupMember.role, true, false);
            }
        }

        pushAdvisor();
        try {
            siteService.save(site);
            //M_log.info("Updated  site=" + site.getId() + " group=" + consumerGroupTitle);
            M_log.info("Updated  site=" + site.getId());
        } catch (Exception e) {
            //M_log.error("Failed to add group '" + consumerGroupTitle + "' to site", e);
            M_log.info("Failed to update site=" + site.getId());
        } finally {
            popAdvisor();
        }
    }
}
