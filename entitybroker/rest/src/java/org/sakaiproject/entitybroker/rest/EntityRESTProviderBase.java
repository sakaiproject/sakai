/**
 * $Id$
 * $URL$
 * EntityRESTProviderBase.java - entity-broker - Jan 14, 2009 12:54:57 AM - azeckoski
 **********************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.entitybroker.rest;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.providers.EntityRESTProvider;
import org.sakaiproject.entitybroker.util.EntityResponse;

import lombok.extern.slf4j.Slf4j;


/**
 * This is the standard entity REST provider which will be created and set in the entity broker manager,
 * it will register itself with the entitybrokermanager on startup or construction,
 * it should also be unregistered correctly
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
public class EntityRESTProviderBase implements EntityRESTProvider {

    protected EntityRESTProviderBase() { }

    public EntityRESTProviderBase(EntityBrokerManager entityBrokerManager,
            EntityActionsManager entityActionsManager,
            EntityEncodingManager entityEncodingManager,
            EntityHandlerImpl entityRequestHandler) {
        super();
        this.entityBrokerManager = entityBrokerManager;
        this.entityActionsManager = entityActionsManager;
        this.entityEncodingManager = entityEncodingManager;
        this.entityRequestHandler = entityRequestHandler;
        init();
    }

    private EntityBrokerManager entityBrokerManager;
    public void setEntityBrokerManager(EntityBrokerManager entityBrokerManager) {
        this.entityBrokerManager = entityBrokerManager;
    }
    private EntityActionsManager entityActionsManager;
    public void setEntityActionsManager(EntityActionsManager entityActionsManager) {
        this.entityActionsManager = entityActionsManager;
    }
    private EntityEncodingManager entityEncodingManager;
    public void setEntityEncodingManager(EntityEncodingManager entityEncodingManager) {
        this.entityEncodingManager = entityEncodingManager;
    }
    private EntityHandlerImpl entityRequestHandler;
    public void setEntityRequestHandler(EntityHandlerImpl entityRequestHandler) {
        this.entityRequestHandler = entityRequestHandler;
    }

    public void init() {
        log.info("EntityRESTProviderBase init");
        // register with the entity broker manager
        this.entityBrokerManager.setEntityRESTProvider(this);
    }

    public void destroy() {
        log.info("EntityRESTProviderBase destroy");
        // unregister
        this.entityBrokerManager.setEntityRESTProvider(null);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.providers.EntityRESTProvider#decodeData(java.lang.String, java.lang.String)
     */
    public Map<String, Object> decodeData(String data, String format) {
        return this.entityEncodingManager.decodeData(data, format);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.providers.EntityRESTProvider#encodeData(java.lang.Object, java.lang.String, java.lang.String, java.util.Map)
     */
    public String encodeData(Object data, String format, String name, Map<String, Object> properties) {
        return this.entityEncodingManager.encodeData(data, format, name, properties);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.providers.EntityRESTProvider#formatAndOutputEntity(org.sakaiproject.entitybroker.EntityReference, java.lang.String, java.util.List, java.io.OutputStream, java.util.Map)
     */
    public void formatAndOutputEntity(EntityReference ref, String format,
            List<EntityData> entities, OutputStream outputStream, Map<String, Object> params) {
        this.entityEncodingManager.formatAndOutputEntity(ref, format, entities, outputStream, params);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.providers.EntityRESTProvider#handleCustomActionExecution(org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable, org.sakaiproject.entitybroker.EntityReference, java.lang.String, java.util.Map, java.io.OutputStream, org.sakaiproject.entitybroker.EntityView, java.util.Map)
     */
    public ActionReturn handleCustomActionExecution(ActionsExecutable actionProvider,
            EntityReference ref, String action, Map<String, Object> actionParams,
            OutputStream outputStream, EntityView view, Map<String, Object> searchParams) {
        return this.entityActionsManager.handleCustomActionExecution(actionProvider, ref, action, actionParams, outputStream, view, searchParams);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.providers.EntityRESTProvider#translateInputToEntity(org.sakaiproject.entitybroker.EntityReference, java.lang.String, java.io.InputStream, java.util.Map)
     */
    public Object translateInputToEntity(EntityReference ref, String format,
            InputStream inputStream, Map<String, Object> params) {
        return this.entityEncodingManager.translateInputToEntity(ref, format, inputStream, params);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.providers.EntityRESTProvider#handleEntityRequest(java.lang.String, java.lang.String, java.lang.String, java.util.Map, java.lang.Object)
     */
    public EntityResponse handleEntityRequest(String reference, String viewKey, String format,
            Map<String, String> params, Object entity) {
        return this.entityRequestHandler.fireEntityRequestInternal(reference, viewKey, format, params, entity);
    }

}
