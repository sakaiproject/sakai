/**
 * Copyright (c) 2009-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lti.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;

import net.oauth.signature.OAuthSignatureMethod;

import org.tsugi.basiclti.BasicLTIConstants;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.basiclti.util.LegacyShaUtil;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.lti.api.SiteMembershipUpdater;
import org.sakaiproject.lti.api.UserFinderOrCreator;
import org.sakaiproject.lti.api.SiteMembershipsSynchroniser;
import org.sakaiproject.lti.extensions.POXMembershipsResponse;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;

@Slf4j
public class SiteMembershipsSynchroniserImpl implements SiteMembershipsSynchroniser {

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
            log.error("site.notfound id: {}. This site's memberships will NOT be synchronised. {}", siteId, iue);
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
            log.error("launch.key.notfound {}. This site's memberships will NOT be synchronised.", oauth_consumer_key);
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
            log.warn("Problem synchronizing LTI1 memberships.", e);
        }
    }

    private final void synchronizeMoodleExtSiteMemberships(final Site site, final String membershipsId, final String membershipsUrl, final String oauth_consumer_key, boolean isEmailTrustedConsumer) {

        // Lookup the secret
        final String configPrefix = "basiclti.provider." + oauth_consumer_key + ".";
        final String oauth_secret = serverConfigurationService.getString(configPrefix+ "secret", null);
        if (oauth_secret == null) {
            log.error("launch.key.notfound {}. This site's memberships will NOT be synchronised.", oauth_consumer_key);
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

        if(log.isDebugEnabled()) log.debug("callXml: {}", callXml);

        String bodyHash = OAuthSignatureMethod.base64Encode(LegacyShaUtil.sha1(callXml));
        log.debug(bodyHash);

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

            if(log.isDebugEnabled()) log.debug("AUTHZ HEADER: {}", authzHeader);

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
            log.warn("Problem synchronizing Mooodle memberships.", e);
        }
    }

    private void processMembershipsResponse(HttpURLConnection connection, Site site, String oauth_consumer_key, boolean isEmailTrustedConsumer) throws Exception {

        log.debug("processMembershipsResponse");

        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        POXMembershipsResponse poxMembershipsResponse = new POXMembershipsResponse(br);

        connection.disconnect();

        List<POXMembershipsResponse.Member> members = poxMembershipsResponse.getMembers();

        Map<String,List<POXMembershipsResponse.Member>> consumerGroups = poxMembershipsResponse.getGroups();

        if (log.isDebugEnabled()) {
            for (POXMembershipsResponse.Member member : members) {
                log.debug("Member:");
                log.debug("\tUser ID: {}", member.userId);
                log.debug("\tFirst Name: {}", member.firstName);
                log.debug("\tLast Name: {}", member.lastName);
                log.debug("\tEmail: {}", member.email);
                log.debug("\tRole: {}", member.role);
            }

            for (String groupTitle : consumerGroups.keySet()) {
                log.debug("Group: {}", groupTitle);
                for(POXMembershipsResponse.Member groupMember : consumerGroups.get(groupTitle)) {
                    log.debug("\tGroup Member ID: {}", groupMember.userId);
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
            try {
                site.deleteGroup((Group) i.next());
            } catch (IllegalStateException e) {
                log.error(".processMembershipsResponse: Group with id {} cannot be removed because is locked", ((Group) i).getId());
            }
        }

        for (String consumerGroupTitle : consumerGroups.keySet()) {
            if (log.isDebugEnabled()) {
                log.debug("Creating group with title '{}' ...", consumerGroupTitle);
            }

            Group sakaiGroup = site.addGroup();
            sakaiGroup.getProperties().addProperty(sakaiGroup.GROUP_PROP_WSETUP_CREATED, Boolean.TRUE.toString());
            sakaiGroup.setTitle(consumerGroupTitle);

            for (POXMembershipsResponse.Member consumerGroupMember : consumerGroups.get(consumerGroupTitle)) {
                if (log.isDebugEnabled()) {
                    log.debug("Adding '{} {}' to '{}' ...", consumerGroupMember.firstName, consumerGroupMember.lastName, consumerGroupTitle);
                }

                try {
                    sakaiGroup.insertMember(consumerGroupMember.userId, consumerGroupMember.role, true, false);
                } catch (IllegalStateException e) {
                    log.error(".processMembershipsResponse: User with id {} cannot be inserted in group with id {} because the group is locked", consumerGroupMember.userId, sakaiGroup.getId());
                }
            }
        }

        pushAdvisor();
        try {
            siteService.save(site);
            log.info("Updated  site={}", site.getId());
        } catch (Exception e) {
            //M_log.error("Failed to add group '" + consumerGroupTitle + "' to site", e);
            log.info("Failed to update site={}", site.getId());
        } finally {
            popAdvisor();
        }
    }
}
