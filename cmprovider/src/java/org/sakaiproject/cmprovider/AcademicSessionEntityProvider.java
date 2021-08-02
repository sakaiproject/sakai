/**
 * Copyright (c) 2015 The Apereo Foundation
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
package org.sakaiproject.cmprovider;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sakaiproject.cmprovider.data.AcademicSessionData;
import org.sakaiproject.cmprovider.data.DateUtils;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

/**
 * Provides a REST API for working with academic sessions.
 * @see AcademicSession
 *
 * @author Christopher Schauer
 */
public class AcademicSessionEntityProvider extends AbstractCmEntityProvider {

  public String getEntityPrefix() {
    return "cm-academic-session";
  }

  public Object getSampleEntity() {
    return new AcademicSessionData();
  }

  @Override
  public boolean entityExists(String id) {
    return cmService.isAcademicSessionDefined(id);
  }

  /**
   * Get all academic sessions. Wraps CourseManagementService.getAcademicSessions.
   * @see CourseManagementService#getAcademicSessions
   *
   * GET requests to /direct/cm-academic-session.json will be routed here.
   *
   * Possible responses:
   * 200 = success, list of academic sessions in response body
   * 403 = current user does not have permission to access academic sessions
   */
  public List getEntities(EntityReference ref, Search search) {
    validateUser();
    List<AcademicSessionData> sessions = new ArrayList<AcademicSessionData>();
    for (AcademicSession session : cmService.getAcademicSessions()) {
      sessions.add(new AcademicSessionData(session));
    }
    return sessions;
  }

  /**
   * Create a new academic session. Wraps CourseAdministration.createAcademicSession.
   * @see CourseAdministration#createAcademicSession
   * @see AcademicSessionData
   */
  public void create(Object entity) {
    AcademicSessionData data = (AcademicSessionData) entity;
    validateDates(data);
    
    cmAdmin.createAcademicSession(
      data.eid,
      data.title,
      data.description,
      DateUtils.stringToDate(data.startDate),
      DateUtils.stringToDate(data.endDate)
    );
  }

  /**
   * Update an academic session. Wraps CourseManagementAdministration.updateAcademicSession.
   * @see CourseManagementAdministration#updateAcademicSession
   * @see AcademicSessionData
   */
  public void update(Object entity) {
    AcademicSessionData data = (AcademicSessionData) entity;
    validateDates(data);

    AcademicSession updated = cmService.getAcademicSession(data.eid);
    updated.setTitle(data.title);
    updated.setDescription(data.description);
    updated.setStartDate(DateUtils.stringToDate(data.startDate));
    updated.setEndDate(DateUtils.stringToDate(data.endDate));

    // TODO: return 404 instead of 500 if there's no session with given eid
    cmAdmin.updateAcademicSession(updated);
  }

  /**
   * Get an academic session by eid. Wraps CourseManagementService.getAcademicSession.
   * @see CourseManagementService#getAcademicSession
   */
  public Object get(String eid) {
    AcademicSession session = cmService.getAcademicSession(eid);
    return new AcademicSessionData(session);
  }

  /**
   * Delete an academic session by eid. Wraps CourseManagementAdministration.removeAcademicSession.
   * @see CourseManagementAdministration#removeAcademicSession
   */
  public void delete(String eid) {
    cmAdmin.removeAcademicSession(eid);
  }

  /**
   * A custom action is required to set current sessions since AcademicSession interface doesn't contain
   * any methods for setting the session as current.
   *
   * PUT requests to /direct/cm-academic-session/setCurrentAcademicSessions?list=eid1,eid2,...
   * will be routed here.
   *
   * Possible responses:
   * 200 = current acadmic sessions set successfully
   * 400 = missing or invalid parameter: list
   */
  @EntityCustomAction(action="setCurrentAcademicSessions", viewKey=EntityView.VIEW_EDIT)
  public ActionReturn setCurrentAcademicSessions(EntityView view, Map<String, Object> params) {
    validateRequest(params, null);
    String eidListString = (String) params.get("list");
    if (eidListString == null) throw new IllegalArgumentException("Missing required parameter: list");
    List<String> eidList = Arrays.asList(eidListString.split("\\s*,\\s*"));
    cmAdmin.setCurrentAcademicSessions(eidList);
    return null;
  }

  private void validateDates(AcademicSessionData data) {
    Date startDate = DateUtils.stringToDate(data.startDate);
    Date endDate = DateUtils.stringToDate(data.endDate);

    if (startDate == null && endDate == null) return;

    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Invalid AcademicSessionData: startDate and endDate must both be null or both be non-null");
    }

    if (startDate.compareTo(endDate) >= 0) {
      throw new IllegalArgumentException("Invalid CourseOfferingData: startDate must occur before endDate");
    }
  }
}
