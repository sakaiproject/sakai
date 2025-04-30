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

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.webapi.beans.AssessmentScheduledRestBean;
import org.sakaiproject.webapi.beans.MembershipScheduledRestBean;
import org.sakaiproject.webapi.beans.PaginatedResponse;
import org.sakaiproject.webapi.beans.SiteScheduledRestBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class SitesController extends AbstractSakaiApiController {

	@Autowired
	@Qualifier("org.sakaiproject.coursemanagement.api.CourseManagementService")
	private CourseManagementService cmService;

	@Autowired
	private ServerConfigurationService serverConfigurationService;

	@Autowired
	private UserMessagingService userMessagingService;

    @Autowired
    private SqlService sqlService;

    @Autowired
    private UserDirectoryService userDirectoryService;

    @GetMapping(value = "/assessment/scheduled", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedResponse<AssessmentScheduledRestBean>> getAssessmentScheduled(
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam LocalDateTime startDate,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam LocalDateTime endDate,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "50") Integer size) {

        // Main query to retrieve assessment data using date range and limit/offset for pagination
        // The parameters returned are ID, TITLE, CREATEDDATE, LASTMODIFIEDDATE, STARTDATE, and DUEDATE
        String query = "SELECT pa.ID, pa.TITLE, pa.CREATEDDATE, pa.LASTMODIFIEDDATE, ac.STARTDATE, ac.DUEDATE " +
            "FROM SAM_PUBLISHEDASSESSMENT_T pa " +
            "JOIN SAM_ASSESSMENTBASE_T ab ON ab.ID = pa.ASSESSMENTID " +
            "JOIN SAM_ASSESSACCESSCONTROL_T ac ON ab.ID = ac.ASSESSMENTID " +
            "WHERE ((pa.CREATEDDATE > ? AND pa.CREATEDDATE <= ?) " +
            "OR (pa.LASTMODIFIEDDATE > ? AND pa.LASTMODIFIEDDATE <= ?)) " +
            "AND pa.status = 1 " +
            "LIMIT ? OFFSET ?";

        // Count query to retrieve the total number of elements matching the date range
        String countQuery = "SELECT COUNT(*) " +
            "FROM SAM_PUBLISHEDASSESSMENT_T pa " +
            "JOIN SAM_ASSESSMENTBASE_T ab ON ab.ID = pa.ASSESSMENTID " +
            "JOIN SAM_ASSESSACCESSCONTROL_T ac ON ab.ID = ac.ASSESSMENTID " +
            "WHERE ((pa.CREATEDDATE > ? AND pa.CREATEDDATE <= ?) " +
            "OR (pa.LASTMODIFIEDDATE > ? AND pa.LASTMODIFIEDDATE <= ?)) " +
            "AND pa.status = 1";

        List<AssessmentScheduledRestBean> assessmentScheduledList = new ArrayList<>();
        Integer totalElements = 0;

        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement countPs = null;
        ResultSet rs = null;
        ResultSet countRs = null;

        try {
            conn = sqlService.borrowConnection();
            conn.setReadOnly(true);

            // Set date parameters and retrieve total elements
            countPs = conn.prepareStatement(countQuery);
            countPs.setTimestamp(1, java.sql.Timestamp.valueOf(startDate));
            countPs.setTimestamp(2, java.sql.Timestamp.valueOf(endDate));
            countPs.setTimestamp(3, java.sql.Timestamp.valueOf(startDate));
            countPs.setTimestamp(4, java.sql.Timestamp.valueOf(endDate));
            countRs = countPs.executeQuery();

            if (countRs.next()) {
                totalElements = countRs.getInt(1);
            }

            // Set date and pagination parameters for the main query to retrieve assessment data
            ps = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ps.setTimestamp(1, java.sql.Timestamp.valueOf(startDate));
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(endDate));
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(startDate));
            ps.setTimestamp(4, java.sql.Timestamp.valueOf(endDate));
            ps.setInt(5, size);
            ps.setInt(6, page * size);

            PublishedAssessmentService service = new PublishedAssessmentService();
            rs = ps.executeQuery();

            while (rs.next()) {
                Long publishedAssessmentId = rs.getLong("ID");
                String siteId = service.getPublishedAssessmentOwner(publishedAssessmentId);
                String title = rs.getString("TITLE");
                String assessmentStartDate = rs.getString("STARTDATE") == null ?  rs.getString("CREATEDDATE") : rs.getString("STARTDATE");
                String assessmentDueDate = rs.getString("DUEDATE");

                assessmentScheduledList.add(new AssessmentScheduledRestBean(siteId, publishedAssessmentId.toString(),
                    title, assessmentStartDate, assessmentDueDate));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } finally {
            try {
                if (rs != null) rs.close();
                if (countRs != null) countRs.close();
                if (ps != null) ps.close();
                if (countPs != null) countPs.close();
            } catch (SQLException e) {
                log.error("SQLException in finally block: " + e);
            }

            if (conn != null) {
                sqlService.returnConnection(conn);
            }
        }

        PaginatedResponse<AssessmentScheduledRestBean> response = new PaginatedResponse<>(
            assessmentScheduledList,
            size,
            totalElements
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/membership/scheduled", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedResponse<MembershipScheduledRestBean>> getMembershipScheduled(
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam LocalDateTime startDate,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam LocalDateTime endDate,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "50") Integer size) {

        // Main query to retrieve membership data using date range and limit/offset for pagination
        // The parameters returned are site_id, user_id, role_name, audit_stamp, and action_taken
        String query = "SELECT site_id, user_id, role_name, audit_stamp, action_taken " +
            "FROM user_audits_log " +
            "WHERE (audit_stamp > ? AND audit_stamp <= ?) " +
            "AND action_taken IN ('A', 'U') " +
            "LIMIT ? OFFSET ?";

        // Count query to retrieve the total number of elements matching the date range
        String countQuery = "SELECT COUNT(*) " +
            "FROM user_audits_log " +
            "WHERE (audit_stamp > ? AND audit_stamp <= ?) " +
            "AND action_taken IN ('A', 'U')";

        List<MembershipScheduledRestBean> membershipScheduledRestBeans = new ArrayList<>();
        Integer totalElements = 0;

        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement countPs = null;
        ResultSet rs = null;
        ResultSet countRs = null;

        try {
            conn = sqlService.borrowConnection();
            conn.setReadOnly(true);

            // Set date parameters and retrieve total elements
            countPs = conn.prepareStatement(countQuery);
            countPs.setTimestamp(1, java.sql.Timestamp.valueOf(startDate));
            countPs.setTimestamp(2, java.sql.Timestamp.valueOf(endDate));
            countRs = countPs.executeQuery();

            if (countRs.next()) {
                totalElements = countRs.getInt(1);
            }

            // Set date and pagination parameters for the main query to retrieve membership data
            ps = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ps.setTimestamp(1, java.sql.Timestamp.valueOf(startDate));
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(endDate));
            ps.setInt(3, size);
            ps.setInt(4, page * size);

            rs = ps.executeQuery();

            while (rs.next()) {
                String siteId = rs.getString("site_id");
                String userId = rs.getString("user_id");

                String userUuid = null;

                try {
                    User user = userDirectoryService.getUserByEid(userId);
                    userUuid = user.getId();
                } catch (UserNotDefinedException e) {
                    userUuid = userId;
                }

                String role = rs.getString("role_name");
                String auditStamp = rs.getString("audit_stamp");
                membershipScheduledRestBeans.add(new MembershipScheduledRestBean(siteId, userUuid, role, auditStamp));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } finally {
            try {
                if (rs != null) rs.close();
                if (countRs != null) countRs.close();
                if (ps != null) ps.close();
                if (countPs != null) countPs.close();
            } catch (SQLException e) {
                log.error("SQLException in finally block: " + e);
            }

            if (conn != null) {
                sqlService.returnConnection(conn);
            }
        }

        PaginatedResponse<MembershipScheduledRestBean> response = new PaginatedResponse<>(
            membershipScheduledRestBeans,
            size,
            totalElements
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/sites/scheduled", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedResponse<SiteScheduledRestBean>> getSitesScheduled(
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam LocalDateTime from,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam LocalDateTime until,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "50") Integer size) {

        // Main query to retrieve site data using date range and limit/offset for pagination
        // The parameters returned are SITE_ID, TITLE, CREATEDON, MODIFIEDON, and SOFTLY_DELETED_DATE
        String query = "SELECT SITE_ID, TITLE, CREATEDON, MODIFIEDON, SOFTLY_DELETED_DATE " +
            "FROM SAKAI_SITE " +
            "WHERE TYPE in ('project', 'course') " +
            "AND ((CREATEDON > ? AND CREATEDON <= ?) OR (MODIFIEDON > ? AND MODIFIEDON <= ?)) " +
            "LIMIT ? OFFSET ?";

        Object[] baseParams = new Object[] {
            java.sql.Timestamp.valueOf(from),
            java.sql.Timestamp.valueOf(until),
            java.sql.Timestamp.valueOf(from),
            java.sql.Timestamp.valueOf(until)
        };

        Object[] paginatedParams = Arrays.copyOf(baseParams, baseParams.length + 2);
        paginatedParams[baseParams.length] = size;
        paginatedParams[baseParams.length + 1] = page * size;

        List<SiteScheduledRestBean> siteScheduledList = sqlService.dbRead(query, paginatedParams, new SqlReader<SiteScheduledRestBean>(){
            public SiteScheduledRestBean readSqlResultRecord(ResultSet result) {
                try {
                    String siteId = result.getString("SITE_ID");
                    String title = result.getString("TITLE");
                    String createdOn = result.getString("CREATEDON");
                    String modifiedOn = result.getString("MODIFIEDON");
                    String softlyDeletedDate = result.getString("SOFTLY_DELETED_DATE");

                    return new SiteScheduledRestBean(siteId, title, createdOn, modifiedOn, softlyDeletedDate);
                } catch (SQLException sqle) {
                    log.error("Failed to read post from DB.", sqle);
                    return null;
                }
            }
        });

        // Count query to retrieve the total number of elements matching the date range
        String countQuery = "SELECT COUNT(*) FROM SAKAI_SITE " +
            "WHERE TYPE in ('project', 'course') " +
            "AND ((CREATEDON > ? AND CREATEDON <= ?) OR (MODIFIEDON > ? AND MODIFIEDON <= ?))";

        Integer totalElements = getTotalElements(countQuery, baseParams);

        PaginatedResponse<SiteScheduledRestBean> response = new PaginatedResponse<>(
            siteScheduledList,
            size,
            totalElements
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public Integer getTotalElements(String query, Object[] params) {
        List<Integer> listInteger = sqlService.dbRead(query, params, new SqlReader<Integer>() {
            public Integer readSqlResultRecord(ResultSet result) {
                try {
                    return result.getInt(1);
                } catch (SQLException sqle) {
                    log.error("Failed to read count from DB.", sqle);
                    return 0;
                }
            }
        });

        return (listInteger != null && !listInteger.isEmpty()) ? listInteger.get(0) : 0;
    }

	@GetMapping(value = "/users/{userId}/sites", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<Map<String, Object>>> getSites(@PathVariable String userId, @RequestParam Optional<Boolean> pinned)
        throws UserNotDefinedException {

		checkSakaiSession();

        List<String> pinnedSites = portalService.getPinnedSites();
        List<UserNotification> notifications = userMessagingService.getNotifications();

        return Map.of(
            "terms", cmService.getAcademicSessions().stream().map(as -> {
                    return Map.<String, Object>of("id", as.getEid(), "name", as.getTitle());
                }).collect(Collectors.toList()),
            "sites", siteService.getUserSites().stream().map(s -> {

                    if (pinned.isPresent() && pinned.get().equals(Boolean.TRUE) && !pinnedSites.contains(s.getId())) {
                        return null;
                    }

                    List<UserNotification> siteNotifications = notifications.stream().filter(n -> StringUtils.equals(n.getSiteId(), s.getId())).collect(Collectors.toList());

                    Map<String, Object> site = new HashMap<>();
                    site.put("siteId", s.getId());
                    site.put("title", s.getTitle());
                    site.put("url", s.getUrl());
                    site.put("pinned", pinnedSites.contains(s.getId()));

                    site.put("image", s.getProperties().getProperty(Site.PROP_COURSE_IMAGE_URL));

                    site.put("tools", s.getPages().stream().map(sp -> {

                            List<ToolConfiguration> tools = sp.getTools();
                            if (tools.size() != 1) return null;
                            if (tools.get(0).getTool() == null) return null;
                            String url = serverConfigurationService.getPortalUrl() + "/site/" + s.getId() + "/tool/" + tools.get(0).getId();
                            String commonToolId = tools.get(0).getTool().getId();
                            boolean hasAlerts = siteNotifications.stream().anyMatch(sn -> !sn.getViewed() && StringUtils.equals(sn.getTool(), commonToolId));
                            return Map.of("id", commonToolId, "title", sp.getTitle(), "url", url, "iconClass", "si-" + commonToolId.replace(".", "-"), "hasAlerts", hasAlerts);
                        }).filter(Objects::nonNull).collect(Collectors.toList()));

                    if (StringUtils.equals(s.getType(), "course")) {
                        site.put("course", true);
                        site.put("term", s.getProperties().getProperty(Site.PROP_SITE_TERM));
                    } else if (StringUtils.equals(s.getType(), "project")) {
                        site.put("project", true);
                    }
                    return site;
                }).filter(Objects::nonNull).collect(Collectors.toList()));
	}
}
