/******************************************************************************
* Copyright (c) 2022 Apereo Foundation
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
******************************************************************************/
package org.sakaiproject.webapi.controllers;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.webapi.beans.MemberRestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
*/
@Slf4j
@RestController
public class SitesController extends AbstractSakaiApiController {

    @Autowired
    @Qualifier("org.sakaiproject.coursemanagement.api.CourseManagementService")
    private CourseManagementService cmService;

    @Autowired
    private SiteService siteService;

    @Autowired
    private SqlService sqlService;

    @Autowired
    private PreferencesService preferencesService;

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @Autowired
    private UserDirectoryService userDirectoryService;

    @Autowired
    private ToolManager toolManager;

    @GetMapping(value = "/sites/{siteId}/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<MemberRestBean>> getUsers(@PathVariable String siteId) {

        checkSakaiSession();

        Site site = getSite(siteId);
        if (site == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Set<Member> siteMembers = site.getMembers();

        //Map sites Members to a set of MemberRestBean
        final Set<MemberRestBean> memberBean = siteMembers.stream().map(member -> {
            return getMemberBean(member, siteId);
        }).filter(bean -> bean != null).collect(Collectors.toSet());

        return new ResponseEntity<Set<MemberRestBean>>(memberBean, HttpStatus.OK);
    }

    @DeleteMapping(value = "/sites/{siteId}/users")
    public ResponseEntity<HttpStatus> deleteUsers(@PathVariable String siteId, @RequestBody Set<String> userIds) {

        Session session = checkSakaiSession();

        try {
            Site site = siteService.getSite(siteId);

            //Get current ids of current members
            Set<String> currentMemberIds = site.getMembers()
                .stream().map(member -> member.getUserId()).collect(Collectors.toSet());

            //Check if supplied Set is not empty and
            //      if all userIds that are requested for deletion are present on the site
            //If not, return BAD_REQUEST
            if (!userIds.isEmpty() && !currentMemberIds.containsAll(userIds)) {
                return new ResponseEntity<HttpStatus>(HttpStatus.BAD_REQUEST);
            }

            //Remove all members that are requested for deletion
            userIds.stream().forEach(userId -> {
                site.removeMember(userId);
            });

            //Save edits
            siteService.saveSiteMembership(site);

            //Return OK
            return new ResponseEntity<HttpStatus>(HttpStatus.OK);

        } catch (IdUnusedException e) {
            log.error("Site with id {} does not exist: {}", siteId, e.toString());
            return new ResponseEntity<HttpStatus>(HttpStatus.BAD_REQUEST);
        } catch (PermissionException e) {
            log.error("User {} has no permission to update site {}: {}", session.getUserId(), siteId, e.toString());
            return new ResponseEntity<HttpStatus>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/sites/{siteId}/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MemberRestBean> getUser(@PathVariable String siteId, @PathVariable String userId) {

        checkSakaiSession();

        Site site = getSite(siteId);

        //Check if Site exists and if it has a Member with userId
        if (site != null && site.getMember(userId) != null) {
            return new ResponseEntity<MemberRestBean>(getMemberBean(site.getMember(userId), siteId), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value = "/sites/{siteId}/users/{userId}")
    public ResponseEntity<HttpStatus> updateUser(@PathVariable String siteId, @PathVariable String userId, @RequestBody MemberRestBean editMemberBean) {

        Session session = checkSakaiSession();

        try {
            Site site = siteService.getSite(siteId);
            //If recieved data defines a role -> check if is a valid role
            if (editMemberBean.getRole() != null) {
                boolean roleExists = site.getRoles().stream().map(role -> role.getId())
                    .collect(Collectors.toSet())
                    .contains(editMemberBean.getRole());
                if (!roleExists) {
                    //Invalid role
                    return new ResponseEntity<HttpStatus>(HttpStatus.BAD_REQUEST);
                }
            }

            Member member = site.getMember(userId);

            //Check if member exists
            if (member != null) {
                //Merge recieved data to member as editedMemberRestBean
                MemberRestBean editedMemberRestBean = MemberRestBean.of(member).merge(editMemberBean);

                //Update and save Member
                site.removeMember(userId);
                site.addMember(userId, editedMemberRestBean.getRole(), editedMemberRestBean.getStatus(), member.isProvided());
                siteService.saveSiteMembership(site);

                //Retuen OK
                return new ResponseEntity<HttpStatus>(HttpStatus.OK);
            } else {
                log.warn("No user for id {} on site {}; Deletion requested by user {}", userId, siteId, session.getUserId());
                return new ResponseEntity<HttpStatus>(HttpStatus.BAD_REQUEST);
            }
        } catch (IdUnusedException e) {
            log.warn("Site with id {} does not exist: {}", siteId, e.toString());
            return new ResponseEntity<HttpStatus>(HttpStatus.BAD_REQUEST);
        } catch (PermissionException e) {
            log.error("User {} has no permission to update site {}; Tried to remove user {}: {}", session.getUserId(), siteId, userId, e.toString());
            return new ResponseEntity<HttpStatus>(HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping(value = "/sites/{siteId}/users/{userId}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable String siteId, @PathVariable String userId) {

        Session session = checkSakaiSession();

        try {
            Site site = siteService.getSite(siteId);
            if (site != null && site.getMember(userId) != null) {
                //Remove user form site and save
                site.removeMember(userId);
                siteService.saveSiteMembership(site);

                //Return OK
                return new ResponseEntity<HttpStatus>(HttpStatus.OK);
            } else {
                log.warn("No user for id {} on site {}; Deletion requested by user {}", userId, siteId, session.getUserId());
                return new ResponseEntity<HttpStatus>(HttpStatus.NOT_FOUND);
            }
        } catch (IdUnusedException e) {
            log.error("Site with id {} does not exist: {}", siteId, e.toString());
            return new ResponseEntity<HttpStatus>(HttpStatus.BAD_REQUEST);
        } catch (PermissionException e) {
            log.error("User {} has no permission to update site {}; Tried to remove user {}: {}", session.getUserId(), siteId, userId, e.toString());
            return new ResponseEntity<HttpStatus>(HttpStatus.BAD_REQUEST);
        }
    }

    private MemberRestBean getMemberBean(Member member, String siteId) {
        try {
            MemberRestBean memberBean = MemberRestBean.of(member);
            memberBean.setDisplayName(userDirectoryService.getUser(member.getUserId()).getDisplayName(siteId));
            memberBean.setLink(Link.of(String.format("/api/sites/%s/users/%s", siteId, member.getUserId())));
            return memberBean;
        } catch (UserNotDefinedException e) {
            log.error("User with id {} not found: {}", member.getUserId(), e.toString());
            return null;
        }
    }

    private Site getSite(String siteId) {
        try {
            return siteService.getSite(siteId);
        } catch (IdUnusedException e) {
            log.error("Site with id {} does not exist: {}", siteId, e.toString());
            return null;
        }
    }

    @GetMapping(value = "/users/{userId}/sites", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<Map<String, Object>>> getSites(@PathVariable String userId)
        throws UserNotDefinedException {

        checkSakaiSession();

        final List<Map<String, Object>> terms = cmService.getAcademicSessions().stream().map(as -> {

            Map<String, Object> term = new HashMap<>();
            term.put("id", as.getEid());
            term.put("name", as.getTitle());
            return term;
        }).collect(Collectors.toList());

        final List<Map<String, Object>> sites = siteService.getUserSites().stream().map(s -> {

            Map<String, Object> site = new HashMap<>();
            site.put("id", s.getId());
            site.put("title", s.getTitle());
            site.put("url", s.getUrl());

            if (StringUtils.equals(s.getType(), "course")) {
                site.put("course", true);
                site.put("term", s.getProperties().getProperty(Site.PROP_SITE_TERM));
            } else if (StringUtils.equals(s.getType(), "project")) {
                site.put("project", true);
            }
            return site;
        }).collect(Collectors.toList());

        Map<String, List<Map<String, Object>>> data = new HashMap<>();

        data.put("terms", terms);
        data.put("sites", sites);

        return data;
    }

    @GetMapping(value = "/users/current/favourites", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<Map<String, Object>>> getFavouriteSitesWithPages() throws UserNotDefinedException {

        String userId = checkSakaiSession().getUserId();

        ResourceProperties resourceProperties = preferencesService.getPreferences(userId)
            .getProperties(PreferencesService.SITENAV_PREFS_KEY);
        List<String> siteIds = resourceProperties.getPropertyList("order");

        if (siteIds == null) return Collections.emptyMap();

        List<Map<String, Object>> sitesList = getSitesWithPages(siteIds);

        Map<String, List<Map<String, Object>>> data = new HashMap<>();
        data.put("favourites", sitesList);
        return data;
    }

    @GetMapping(value = "/users/current/recent-sites", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<Map<String, Object>>> getRecentSitesWithPages() throws UserNotDefinedException {

        String userId = checkSakaiSession().getUserId();

        int maxSites = serverConfigurationService.getInt("recent.sites.shown", 3);

        //Get list of last visited site ids ingoring users home site
        String sql = String.format("SELECT DISTINCT se.CONTEXT FROM SAKAI_EVENT se, SAKAI_SESSION ss " +
            "WHERE se.EVENT = 'pres.begin' AND se.SESSION_ID = ss.SESSION_ID " +
            "AND ss.SESSION_USER = '%s' AND se.CONTEXT != '~%s' " +
            "AND se.context != '!error' ORDER BY se.EVENT_DATE DESC LIMIT %d;", userId, userId, maxSites);

        List<String> siteIds = sqlService.dbRead(sql);

        if (siteIds == null) return Collections.emptyMap();

        List<Map<String, Object>> sitesList = getSitesWithPages(siteIds);

        Map<String, List<Map<String, Object>>> data = new HashMap<>();
        data.put("recents", sitesList);
        return data;
    }

    @GetMapping(value = "/sites/gateway", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> getGatewaySiteWithPages() {
        List<String> gatewaySiteList = new ArrayList<>();
        gatewaySiteList.add(serverConfigurationService.getGatewaySiteId());
        return getSitesWithPages(gatewaySiteList);
    }

    private List<Map<String, Object>> getSitesWithPages(List<String> siteIds) {
        return siteIds.stream().map(siteId -> {
            Map<String, Object> siteMap = new HashMap<>();
            try {
                Site site = siteService.getSite(siteId);
                siteMap.put("id", site.getId());
                siteMap.put("title", site.getTitle());
                siteMap.put("url", site.getUrl());
                siteMap.put("type", site.getType());
                List<Map<String, Object>> pageList = site.getOrderedPages().stream().map(page -> {
                    Map<String, Object> pageMap = new HashMap<>();
                    List<ToolConfiguration> toolList = page.getTools();
                    if (toolList.size() == 1 ) {
                        String toolId = toolList.get(0).getId();
                        String toolUrl = page.getUrl().replaceFirst("page.*", "tool/".concat(toolId));
                        pageMap.put("url", toolUrl);
                        pageMap.put("reset-url", toolUrl.replaceFirst("tool", "tool-reset"));
                        pageMap.put("toolid", toolList.get(0).getToolId());
                    } else {
                        pageMap.put("url", page.getUrl());
                        pageMap.put("reset-url",  page.getUrl().replaceFirst("page", "page-reset"));
                    }
                    if (toolList.size() > 0 && toolManager.isHidden(toolList.get(0))) {
                        pageMap.put("hidden", true);
                    }
                    if (!toolManager.isFirstToolVisibleToAnyNonMaintainerRole(page)) {
                        pageMap.put("locked", true);
                    }
                    if (page.isPopUp()) {
                        pageMap.put("isPopup", true);
                    }
                    pageMap.put("title", page.getTitle());
                    pageMap.put("url", page.getUrl());
                    return pageMap;
                }).collect(Collectors.toList());
                siteMap.put("pages", pageList);
            } catch (IdUnusedException e) {
                log.error(e.getMessage());
            }
            return siteMap;
        }).collect(Collectors.toList());
    }
}
