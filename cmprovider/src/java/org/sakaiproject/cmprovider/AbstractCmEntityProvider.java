/**
 * Copyright (c) 2015-2016 The Apereo Foundation
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

import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.Validation;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.cmprovider.data.CmEntityData;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseManagementAdministration;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Session;

/**
 * Base class for course management entity providers. Handles validation, error handling, and logging.
 *
 * @author Christopher Schauer
 */
@Slf4j
public abstract class AbstractCmEntityProvider implements RESTful, CoreEntityProvider, AutoRegisterEntityProvider {

  /**
  * Create a new entity.
  *
  * POST requests to /direct/ENTITY_PREFIX.json will be routed here.
  *
  * Request body should be a CmEntityData in json.  
  * @see CmEntityData
  *
  * Possible responses:
  * 201 = create successful
  * 400 = request json not valid
  * 403 = current user does not have permission to create this entity
  */
  public abstract void create(Object entity);

  /**
   * Update an entity.
   *
   * PUT requests to /direct/ENTITY_PREFIX.json will be routed here.
   *
   * Request body should be a CmEntityData in json.
   * @see CmEntityData
   *
   * Possible responses:
   * 204 = update successful
   * 400 = request json was not valid
   * 403 = current user does not have permission to edit this entity
   */
  public abstract void update(Object entity);

  /**
   * Get an entity by eid.
   *
   * GET requests to /direct/ENTITY_PREFIX/eid.json will be routed here.
   *
   * Possible responses:
   * 200 = success, entity in response body
   * 403 = current user does not have permission to access this entity
   * 404 = no entity exists with given eid
   */
  public abstract Object get(String eid) throws IdNotFoundException;

  /**
   * Delete an academic session by eid.
   *
   * DELETE requests to /direct/ENTITY_PREFIX/eid.json will be routed here.
   *
   * Possible responses:
   * 204 = delete successful
   * 403 = current user does not have permission to delete this entity
   * 404 = no entity exists with given eid
   */
  public abstract void delete(String eid);

  protected CourseManagementService cmService;
  public void setCmService(CourseManagementService service) { cmService = service; }

  protected CourseManagementAdministration cmAdmin;
  public void setCmAdmin(CourseManagementAdministration admin) { cmAdmin = admin; }

  protected DeveloperHelperService developerService;
  public void setDeveloperHelperService(DeveloperHelperService service) { developerService = service; }

  protected CmProviderHibernateService hibernateService;
  public void setHibernateService(CmProviderHibernateService service) { hibernateService = service; }
  
  protected AuthzGroupService authzGroupService;
  public void setAuthzGroupService(AuthzGroupService service) { authzGroupService = service; }

  protected SessionManager sessionManager;
  public void setSessionManager(SessionManager manager) { sessionManager = manager; }

  public String[] getHandledOutputFormats() {
    return new String[] { Formats.JSON };
  }

  public String[] getHandledInputFormats() {
    return new String[] { Formats.JSON };
  }

  private Validator validator;

  public void createValidator() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  /**
   * Subclasses that don't override both updateEntity and createEntity must override this method.
   */
  public boolean entityExists(String id) {
    return true;
  }

  protected String getIdFromReference(EntityReference ref) {
    if (ref == null || ref.getId() == null) throw new IllegalArgumentException("Id cannot be null.");
    return ref.getId();
  }

  /**
   * GET requests to /direct/ENTITY_PREFIX/eid.json will be routed here.
   */
  public Object getEntity(EntityReference ref) {
    validateUser();
    String eid = getIdFromReference(ref);

    log.info("Retrieving " + getEntityPrefix() + " with eid=" + eid + "...");
    try {
      return get(eid);
    } catch (IdNotFoundException ex) {
      return null;
    }
  }

  /**
   * PUT requests to /direct/ENTITY_PREFIX.json will be routed here.
   */
  public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
    validateRequest(params, entity);
    createOrUpdateEntity(entity);
  }

  /**
   * POST requests to /direct/ENTITY_PREFIX.json will be routed here.
   */
  public String createEntity(EntityReference ref, Object entity, Map<String,Object> params) {
    validateRequest(params, entity);
    createOrUpdateEntity(entity);
    return "success";
  }

  /**
   * DELETE requests to /direct/ENTITY_PREFIX/eid.json will be routed here.
   */
  public void deleteEntity(EntityReference ref, Map<String, Object> params) {
    validateRequest(params, null);
    String eid = getIdFromReference(ref);
    log.info("Deleting " + getEntityPrefix() + " with eid=" + eid + "...");
    delete(eid);
  }

  // Allow POST/PUT to both create/update an entity
  private void createOrUpdateEntity(Object entity) {
    validateDataObject(entity);
    CmEntityData data = (CmEntityData)entity;

    log.info("Inserting " + getEntityPrefix() + " with eid=" + data.getId() + "...");
    if (!entityExists(data.getId())) {
      create(data);
    } else {
      update(data);
    }
  }

  // Check for sessionid parameter or csrftoken parameter and validate entity for POST/PUT.
  // TODO: remove this check if/when it gets added as part of the entitybroker system.
  protected void validateRequest(Map<String, Object> params, Object data) {
    Session session = sessionManager.getCurrentSession();
    if (session == null) throw new SecurityException("No session found.");

    validateUser();

    if (session.getId().equals(params.get("sessionid"))) return;

    String csrfToken = (String)session.getAttribute("sakai.csrf.token");
    if (csrfToken == null || !csrfToken.equals(params.get("csrftoken")))
      throw new SecurityException("Request must contain a sessionid or csrftoken parameter.");

    if (data != null) validateDataObject(data);
  }

  protected void validateUser() {
    if (!developerService.isUserAdmin(developerService.getCurrentUserReference()))
      throw new SecurityException("Current user doesn't have permission to access this resource.");
  }

  protected void validateDataObject(Object data) {
    if (!(data instanceof CmEntityData))
      throw new IllegalArgumentException("Request body must implement CmEntityData interface.");

    Set<ConstraintViolation<Object>> constraintViolations = validator.validate(data);

    if (constraintViolations.isEmpty()) return;

    String errorMessage = "Invalid " + data.getClass().getSimpleName() + ":";
    for (ConstraintViolation violation : constraintViolations) {
      errorMessage += "\n" + violation.getPropertyPath() + " " + violation.getMessage();
    }

    throw new IllegalArgumentException(errorMessage);
  }
}
