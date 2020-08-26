/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.Evidence;
import org.sakaiproject.util.IdPwEvidence;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.annotation.Resource;

import java.util.List;

/**
 */
@Slf4j
@RestController
public class LoginController extends AbstractSakaiApiController {


	@Resource
	private SiteService siteService;

	@Resource
	private SessionManager sessionManager;

	@Resource
	private UsageSessionService usageSessionService;

	@Resource
	private UserDirectoryService userDirectoryService;

    @Resource
    private AuthenticationManager authenticationManager;

	@GetMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        String cookieName = "JSESSIONID";
        boolean displayModJkWarning = true;

        String ipAddress = request.getRemoteAddr();

        Evidence e = new IdPwEvidence(username, password, ipAddress);
        Authentication a = authenticationManager.authenticate(e);

        Session s = sessionManager.startSession();
        sessionManager.setCurrentSession(s);

        if (s == null) {
            log.warn("/api/login failed to establish session for username={} ip={}", username, ipAddress);
            throw new RuntimeException("Unable to establish session");
        } else {
            // We do not care too much on the off-chance that this fails - folks simply won't show up in presense
            // and events won't be trackable back to people / IP Addresses - but if it fails - there is nothing
            // we can do anyways.

            usageSessionService.login(a.getUid(), username, ipAddress, "/api/login", UsageSessionService.EVENT_LOGIN_WS);

            log.debug("/api/login username={} ip={} session={}", username, ipAddress, s.getId());

            // retrieve the configured cookie name, if any
            if (System.getProperty(RequestFilter.SAKAI_COOKIE_PROP) != null) {
                cookieName = System.getProperty(RequestFilter.SAKAI_COOKIE_PROP);
            }

            // retrieve the configured cookie domain, if any

            // compute the session cookie suffix, based on this configured server id
            String suffix = System.getProperty(RequestFilter.SAKAI_SERVERID);
            if (StringUtils.isEmpty(suffix)) {
                if (displayModJkWarning) {
                    log.warn("no sakai.serverId system property set - mod_jk load balancing will not function properly");
                }
                displayModJkWarning = false;
                suffix = "sakai";
            }

            Cookie c = new Cookie(cookieName, s.getId() + "." + suffix);
            c.setPath("/");
            c.setMaxAge(-1);
            if (System.getProperty(RequestFilter.SAKAI_COOKIE_DOMAIN) != null) {
                c.setDomain(System.getProperty(RequestFilter.SAKAI_COOKIE_DOMAIN));
            }
            if (request.isSecure() == true) {
                c.setSecure(true);
            }

            if (response != null) {
                response.addCookie(c);
            }

            log.debug("/api/login username={} ip={} session={}", username, ipAddress, s.getId());
            return s.getId();
        }
	}
}
