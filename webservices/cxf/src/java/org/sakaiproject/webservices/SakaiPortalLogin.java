/**
 * Copyright (c) 2005 The Apereo Foundation
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
package org.sakaiproject.webservices;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.User;


/**
 * Web service endpoints to support obtaining a Sakai session by using a Sakai user ID and a shared
 * portal secret.
 * <p>
 * <em>Note: also see {@link https://confluence.sakaiproject.org/display/DOC/Sakai+Admin+Guide+-+Advanced+Configuration+Topics}</em>
 * </p>
 * <p>
 * These web service endpoints can be used to login as a specific user by supplying a Sakai user ID
 * with a shared key rather than a password. This approach to authentication can be useful when the
 * user authenticates with an external provider such as CAS. This can allow external portals to
 * retrieve information from Sakai.
 * <p>
 * The parameters of interest when configuration this web service are:
 * <table>
 * <th>
 * <td colspan="2">Configuration Properties</td>
 * </th>
 * <tr>
 * <td>webservices.allowlogin</td>
 * <td>Set to true to allow logging in through web services.</td>
 * </tr>
 * <tr>
 * <td>webservice.portalsecret</td>
 * <td>The portal secret that is shared to external integration points.</td>
 * </tr>
 * <tr>
 * <td>session.parameter.allow</td>
 * <td>Set to true in your properties file to enable this web service to work without a
 *     password.</td>
 * </tr>
 * </table>
 *
 * <table>
 * <th>
 * <td colspan="2">Request Parameters</td>
 * </th>
 * <tr>
 * <td>sakai.session</td>
 * <td>Set as a <strong>request</strong> parameter to the value returned from SakaiPortalLogin.login
 *     or SakaiPortalLogin.loginAndCreate</td>
 * </tr>
 * </table>
 * </p>
 */

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
@Slf4j
public class SakaiPortalLogin extends AbstractWebService {

    private User getSakaiUser(String id) {
        User user = null ;

        try {
            user = userDirectoryService.getUserByEid(id);
        } catch (Exception e) {
            user = null;
        }
        return user;
    }

    /**
     * Login to an existing Sakai account with the Sakai user ID and shared portal secret. If the
     * account doesn't exist it will be created.
     *
     * @param id Sakai user ID.
     * @param pw Shared portal secret.
     * @param firstName The first name to use when creating the account.
     * @param lastName The last name to use when creating the account.
     * @param eMail The email to use when create that account.
     * @return Session ID of successful login.
     * @ if there are any problems logging in.
     */
    @WebMethod
    @Path("/loginAndCreate")
    @Produces("text/plain")
    @GET
    public String loginAndCreate(
            @WebParam(name = "id", partName = "id") @QueryParam("id") String id,
            @WebParam(name = "pw", partName = "pw") @QueryParam("pw") String pw,
            @WebParam(name = "firstName", partName = "firstName") @QueryParam("firstName") String firstName,
            @WebParam(name = "lastName", partName = "lastName") @QueryParam("lastName") String lastName,
            @WebParam(name = "eMail", partName = "eMail") @QueryParam("eMail") String eMail) {

        String ipAddress = getUserIp();

        String portalSecret = serverConfigurationService.getString("webservice.portalsecret");
        String portalIPs = serverConfigurationService.getString("webservice.portalIP");
        String ipCheck = serverConfigurationService.getString("webservice.IPCheck");

        if (log.isDebugEnabled()) {
            log.debug("SakaiPortalLogin.loginAndCreate id="+id);
            log.debug("SakaiPortalLogin.loginAndCreate ip="+ipAddress+" portalIP="+portalIPs+" IPCheck="+ipCheck);
            log.debug("        fn="+firstName+" ln="+lastName+" em="+eMail+" ip="+ipAddress);
        }

        if ( portalSecret == null || pw == null || portalSecret.equals("") || ! portalSecret.equals(pw) ) {
            log.info("SakaiPortalLogin secret mismatch ip="+ipAddress);
                    throw new RuntimeException("Failed login");
        }

        // Verify that this IP address matches our string
        if ( "true".equalsIgnoreCase(ipCheck) ) {
            if (  portalIPs == null || portalIPs.equals("") ||  portalIPs.indexOf(ipAddress) == -1 ) {
                log.info("SakaiPortalLogin Trusted IP not found");
                        throw new RuntimeException("Failed login");
            }
        }

        User user = getSakaiUser(id);

        if ( user == null && firstName != null && lastName != null && eMail != null ) {
            log.debug("Creating Sakai Account...");
            try {
                // Set password to something unguessable - they can set a new PW once they are logged in
                String hiddenPW = idManager.createUuid();
                userDirectoryService.addUser(null,id,firstName,lastName,eMail,hiddenPW,"registered", null);
                            log.debug("User Created...");
            } catch(Exception e) {
                log.error("Unable to create user...");
                    throw new RuntimeException("Failed login");
            }
                user = getSakaiUser(id);
        }

        if ( user != null ) {
            log.debug("Have User");
            Session s = sessionManager.startSession();
            sessionManager.setCurrentSession(s);
            if (s == null) {
                log.warn("Web Services Login failed to establish session for id="+id+" ip="+ipAddress);
                throw new RuntimeException("Unable to establish session");
            } else {
                // We do not care too much on the off-chance that this fails - folks simply won't show up in presense
                // and events won't be trackable back to people / IP Addresses - but if it fails - there is nothing
                // we can do anyways.

                usageSessionService.login(user.getId(), id, ipAddress, "SakaiPortalLogin.jws", usageSessionService.EVENT_LOGIN_WS);

                try {
                    String siteId = siteService.getUserSiteId(s.getUserId());
                    log.debug("Site exists..."+siteId);
                } catch(Exception e) {
                    log.debug("Site does not exist...");
                        throw new RuntimeException("Failed login");
                }
                if ( log.isDebugEnabled() ) log.debug("Sakai Portal Login id="+id+" ip="+ipAddress+" session="+s.getId());
                    return s.getId();
            }
        }
        log.info("SakaiPortalLogin Failed ip="+ipAddress);
            throw new RuntimeException("Failed login");
    }

    /**
     * Login to an existing Sakai account with the Sakai user ID and shared portal secret.
     *
     * @param id Sakai user ID.
     * @param pw Shared portal secret.
     * @return Session ID of successful login.
     * @ if there are any problems logging in.
     */

    @WebMethod
    @Path("/login")
    @Produces("text/plain")
    @GET
    public String login(
            @WebParam(name = "id", partName = "id") @QueryParam("id") String id,
            @WebParam(name = "pw", partName = "pw") @QueryParam("pw") String pw) {
        log.debug("SakaiPortalLogin.login()");
        return loginAndCreate(id, pw, null, null, null);
    }
}
