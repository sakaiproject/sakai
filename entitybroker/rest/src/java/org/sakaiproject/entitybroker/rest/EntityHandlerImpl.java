/**
 * $Id$
 * $URL$
 * EntityHandler.java - entity-broker - Apr 6, 2008 9:03:03 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.azeckoski.reflectutils.ReflectUtils;
import org.azeckoski.reflectutils.exceptions.FieldnameNotFoundException;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.access.AccessFormats;
import org.sakaiproject.entitybroker.access.AccessViews;
import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;
import org.sakaiproject.entitybroker.access.HttpServletAccessProvider;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityLastModified;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Deleteable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.InputTranslatable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputFormattable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Redirectable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestHandler;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestInterceptor;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Updateable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.CustomAction;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetterWrite;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorageWrite;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityEncodingException;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.exception.FormatUnsupportedException;
import org.sakaiproject.entitybroker.providers.EntityRequestHandler;
import org.sakaiproject.entitybroker.util.ClassLoaderReporter;
import org.sakaiproject.entitybroker.util.EntityDataUtils;
import org.sakaiproject.entitybroker.util.EntityResponse;
import org.sakaiproject.entitybroker.util.http.HttpRESTUtils;
import org.sakaiproject.entitybroker.util.http.HttpRESTUtils.Method;
import org.sakaiproject.entitybroker.util.http.HttpResponse;
import org.sakaiproject.entitybroker.util.http.LazyResponseOutputStream;
import org.sakaiproject.entitybroker.util.request.RequestUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the handler for the EntityBroker system<br/>
 * This handles all the processing of incoming requests (http based) and includes
 * method to process the request data and ensure classloader safety
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@Slf4j
@SuppressWarnings("deprecation")
public class EntityHandlerImpl implements EntityRequestHandler {

    public static String APP_VERSION = "1.0.1";
    public static String SVN_REVISION = "$Revision$";
    public static String SVN_LAST_UPDATE = "$Date$";

    /**
     * Empty constructor
     */
    protected EntityHandlerImpl() { }

    /**
     * Full constructor
     */
    public EntityHandlerImpl(EntityProviderManager entityProviderManager,
            EntityBrokerManager entityBrokerManager, EntityEncodingManager entityEncodingManager,
            EntityDescriptionManager entityDescriptionManager,
            EntityViewAccessProviderManager entityViewAccessProviderManager,
            RequestGetterWrite requestGetter, EntityActionsManager entityActionsManager,
            EntityRedirectsManager entityRedirectsManager, EntityBatchHandler entityBatchHandler,
            RequestStorageWrite requestStorage) {
        super();
        this.entityProviderManager = entityProviderManager;
        this.entityBrokerManager = entityBrokerManager;
        this.entityEncodingManager = entityEncodingManager;
        this.entityDescriptionManager = entityDescriptionManager;
        this.entityViewAccessProviderManager = entityViewAccessProviderManager;
        this.requestGetter = requestGetter;
        this.entityActionsManager = entityActionsManager;
        this.entityRedirectsManager = entityRedirectsManager;
        this.requestStorage = requestStorage;
        setEntityBatchHandler(entityBatchHandler);
        init();
    }

    public void init() {
        log.info("EntityRequestHandler init complete");
    }

    private EntityProviderManager entityProviderManager;
    public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
        this.entityProviderManager = entityProviderManager;
    }

    private EntityBrokerManager entityBrokerManager;
    public void setEntityBrokerManager(EntityBrokerManager entityBrokerManager) {
        this.entityBrokerManager = entityBrokerManager;
    }

    private EntityEncodingManager entityEncodingManager;
    public void setEntityEncodingManager(EntityEncodingManager entityEncodingManager) {
        this.entityEncodingManager = entityEncodingManager;
    }

    private EntityDescriptionManager entityDescriptionManager;
    public void setEntityDescriptionManager(EntityDescriptionManager entityDescriptionManager) {
        this.entityDescriptionManager = entityDescriptionManager;
    }

    private HttpServletAccessProviderManager accessProviderManager;
    public void setAccessProviderManager(HttpServletAccessProviderManager accessProviderManager) {
        this.accessProviderManager = accessProviderManager;
    }

    private EntityViewAccessProviderManager entityViewAccessProviderManager;
    public void setEntityViewAccessProviderManager(
            EntityViewAccessProviderManager entityViewAccessProviderManager) {
        this.entityViewAccessProviderManager = entityViewAccessProviderManager;
    }

    private RequestGetterWrite requestGetter;
    public void setRequestGetter(RequestGetterWrite requestGetter) {
        this.requestGetter = requestGetter;
    }

    private EntityActionsManager entityActionsManager;
    public void setEntityActionsManager(EntityActionsManager entityActionsManager) {
        this.entityActionsManager = entityActionsManager;
    }

    private EntityRedirectsManager entityRedirectsManager;
    public void setEntityRedirectsManager(EntityRedirectsManager entityRedirectsManager) {
        this.entityRedirectsManager = entityRedirectsManager;
    }

    private EntityBatchHandler entityBatchHandler;
    public void setEntityBatchHandler(EntityBatchHandler entityBatchHandler) {
        this.entityBatchHandler = entityBatchHandler;
        // NOTE: this is somewhat tricky but it avoids issues related to circular dependencies
        this.entityBatchHandler.setEntityRequestHandler(this);
    }

    private RequestStorageWrite requestStorage;
    public void setRequestStorage(RequestStorageWrite requestStorage) {
        this.requestStorage = requestStorage;
    }


    // allow the servlet name to be more flexible
    private String servletContext;
    public String getServletContext() {
        if (this.servletContext == null) {
            return RequestUtils.getServletContext(null);
        }
        return this.servletContext;
    }
    public void setServletContext(String servletContext) {
        if (servletContext != null) {
            this.servletContext = servletContext;
            //log.info("Setting the REST servlet context to: " + servletContext);
            entityBrokerManager.setServletContext(servletContext);
        }
    }


    /**
     * If this param is set then the sakai session for the current request is set to this rather than establishing one,
     * will allow changing the session as well
     */

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityRequestHandler#handleEntityAccess(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public String handleEntityAccess(HttpServletRequest req, HttpServletResponse res, String path) {
        // set the servlet context if not set OR we know for sure we have a request object
        if (this.servletContext == null || req != null) {
            setServletContext( RequestUtils.getServletContext(req) );
        }

        // get the path info if not set
        if (req != null && path == null) {
            path = req.getPathInfo();
        }

        String handledReference = null;

        // special handling in case the session ID is sent in the request 
        // (allows setting up and reusing a session over and over without holding cookies)
        if (entityBrokerManager.getExternalIntegrationProvider() != null) {
            try {
                entityBrokerManager.getExternalIntegrationProvider().handleUserSessionKey(req);
            } catch (SecurityException se) {
            	throw new EntityException(se.getMessage(), path, HttpServletResponse.SC_UNAUTHORIZED);
            } catch (Exception e) {
                log.warn("EntityRequestHandler: External handleUserSessionKey method failed, continuing...: " + e);
            }
        }

        if (path == null || "".equals(path) || "/".equals(path)) {
            // SPECIAL handling for empty path
            res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            try {
                res.sendRedirect( res.encodeRedirectURL(getServletContext() + SLASH_DESCRIBE) );
            } catch (IOException e) {
                // should never happen
                throw new RuntimeException("Could not encode the redirect URL");
            }
            // immediate exit from redirect
            return "/";
        } else {
            // regular handling for direct URLs
            if ( (SLASH_DESCRIBE).equals(path) 
                    || path.startsWith(SLASH_DESCRIBE + EntityReference.PERIOD)) {
                // SPECIAL handling for the describe all URL
                String format = RequestUtils.findAndHandleFormat(req, res, Formats.HTML);
                String output = entityDescriptionManager.makeDescribeAll(format, req.getLocale()); // possibly get the locale from other places?
                res.setContentLength(output.getBytes().length);
                try {
                    res.getWriter().write(output);
                } catch (IOException e) {
                    // should never happen
                    throw new RuntimeException("Failed to put output into the response writer: " + e.getMessage(), e);
                }
                res.setStatus(HttpServletResponse.SC_OK);
                handledReference = EntityView.SEPARATOR+"";
            } else {
                // STANDARD processing for the incoming view
                EntityView view;
                try {
                    view = entityBrokerManager.parseEntityURL(path);
                } catch (IllegalArgumentException e) {
                    // FAILURE indicates we could not parse the reference
                    throw new EntityException("Could not parse entity path ("+path+"): " + e.getMessage(), path, HttpServletResponse.SC_BAD_REQUEST);
                }

                if (view == null) {
                    // FAILURE no provider for this entity prefix
                    throw new EntityException( "Could not parse the incoming path ("+path+") and no entity provider could be found to handle the prefix", 
                            path, HttpServletResponse.SC_NOT_IMPLEMENTED );
                } else if ( DESCRIBE.equals(view.getEntityReference().getId()) ) {
                    // SPECIAL handling for entity describe URLs
                    String format = RequestUtils.findAndHandleFormat(req, res, Formats.HTML);
                    String entityId = req.getParameter("_id");
                    if (entityId == null || "".equals(entityId)) {
                        entityId = FAKE_ID;
                    }
                    String output = entityDescriptionManager.makeDescribeEntity(view.getEntityReference().getPrefix(), entityId, format, req.getLocale());
                    res.setContentLength(output.getBytes().length);
                    try {
                        res.getWriter().write(output);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to put output into the response writer: " + e.getMessage(), e);
                    }
                    res.setStatus(HttpServletResponse.SC_OK);
                    handledReference = view.getEntityReference().getSpaceReference() + SLASH_DESCRIBE;

                } else {
                    // STANDARD reference successfully parsed
                    String prefix = view.getEntityReference().getPrefix();

                    // check for redirect
                    Redirectable urlConfigurable = entityProviderManager.getProviderByPrefixAndCapability(prefix, Redirectable.class);
                    if (urlConfigurable != null) {
                        // SPECIAL check for redirect
                        String redirectURL = entityRedirectsManager.checkForTemplateMatch(urlConfigurable, path, req.getQueryString());
                        if (redirectURL != null) {
                            // SPECIAL handling for redirect
                            if ("".equals(redirectURL)) {
                                // do nothing but return an empty response
                                res.setStatus(HttpServletResponse.SC_OK);
                            } else {
                                // do the redirect
                                log.info("EntityRequestHandler: Entity Redirect: redirecting from ("+path+") to ("+redirectURL+")");
                                RequestUtils.handleURLRedirect(redirectURL, true, req, res);
                            }
                            return EntityView.SEPARATOR + prefix; // exit here for redirects
                        }
                    }

                    // check for custom action
                    CustomAction customAction = entityActionsManager.getCustomAction(prefix, view.getPathSegment(1));
                    if (customAction == null) {
                        customAction = entityActionsManager.getCustomAction(prefix, view.getPathSegment(2));
                    }
                    if (customAction == null) {
                        // check to see if the entity exists
                        if (! entityBrokerManager.entityExists(view.getEntityReference()) ) {
                            // FAILURE invalid entity reference (entity does not exist)
                            throw new EntityException( "Attempted to access an entity URL path (" + path + ") for an entity ("
                                    + view.getEntityReference() + ") that does not exist", 
                                    view.getEntityReference()+"", HttpServletResponse.SC_NOT_FOUND );
                        }
                    } else {
                        // cleanup the entity reference, this has to be done because otherwise the custom action
                        // on collections appears to be the id of an entity in the collection
                        EntityReference cRef = view.getEntityReference();
                        if (cRef.getId() != null && cRef.getId().equalsIgnoreCase(customAction.action)) {
                            view.setEntityReference( new EntityReference(prefix, "") );
                        }
                    }
                    res.setStatus(HttpServletResponse.SC_OK); // default - other things can switch this later on

                    // store format in attribute
                    req.setAttribute("entity-format", view.getFormat());

                    // STANDARD initial processing complete
                    // wrap in try block so that request storage is always cleaned up
                    try {
                        // store the current request and response
                        requestGetter.setRequest(req);
                        requestGetter.setResponse(res);
                        // set the request variables
                        requestStorage.setRequestValue(RequestStorage.ReservedKeys._requestEntityReference.name(), view.getEntityReference().toString());
                        requestStorage.setRequestValue(RequestStorage.ReservedKeys._requestOrigin.name(), RequestStorage.RequestOrigin.REST.name());
                        requestStorage.setRequestValue(RequestStorage.ReservedKeys._requestActive.name(), true);

                        // handle the before interceptor
                        RequestInterceptor interceptor = (RequestInterceptor) entityProviderManager.getProviderByPrefixAndCapability(prefix, RequestInterceptor.class);
                        if (interceptor != null) {
                            interceptor.before(view, req, res);
                        }

                        if (BATCH.equals(prefix)) {
                            // special batch handling
                            // set the default format to JSON for batch handling
                            view.setExtension( RequestUtils.findAndHandleFormat(req, res, Formats.JSON) );
                            entityBatchHandler.handleBatch(view, req, res);
                        } else {
                            // ensure the format is set correctly for the response and the view
                            String format = RequestUtils.findAndHandleFormat(req, res, Formats.HTML);
                            view.setExtension( format );

                            // check for provider handling of this request
                            RequestHandler handler = (RequestHandler) entityProviderManager.getProviderByPrefixAndCapability(prefix, RequestHandler.class);
                            if (handler != null) {
                                // SPECIAL provider is handling this request
                                handleClassLoaderAccess(handler, req, res, view);
                            } else {
                                // STANDARD processing of the entity request internally start here
                                // try to handle the request internally if possible

                                // identify the type of request (input or output) and the action (will be encoded in the viewKey)
                                boolean output = RequestUtils.isRequestOutput(req, view);
                                setResponseHeaders(view, res, requestStorage.getStorageMapCopy(), null);

                                boolean handled = false;
                                // PROCESS CUSTOM ACTIONS
                                ActionReturn actionReturn = null;
                                if (customAction != null) {
                                    // SPECIAL handle the custom action
                                    ActionsExecutable actionProvider = entityProviderManager.getProviderByPrefixAndCapability(prefix, ActionsExecutable.class);
                                    if (actionProvider == null) {
                                        throw new EntityException( "The provider for prefix ("+prefix+") cannot handle custom actions", 
                                                view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST );
                                    }
                                    // make sure this request is a valid type for this action
                                    if (customAction.viewKey != null 
                                            && ! view.getViewKey().equals(customAction.viewKey)) {
                                        throw new EntityException( "Cannot execute custom action ("+customAction.action+") for request method " + req.getMethod()
                                                + ", The custom action view key ("+customAction.viewKey+") must match the request view key ("+view.getViewKey()+")", 
                                                view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST );
                                    }
                                    try {
                                        actionReturn = entityActionsManager.handleCustomActionRequest(actionProvider, view, customAction.action, req, res, 
                                                requestStorage.getStorageMapCopy(true, false, true, true) );
                                    } catch (SecurityException se) {
                                        // AJAX/WS type security exceptions are handled specially, no redirect
                                        throw new EntityException("Security exception handling request for view ("+view+"), "
                                                + "this is typically caused by the current user not having access to the "
                                                + "data requested or the user not being logged in at all :: message=" + se.getMessage(),
                                                view.getEntityReference()+"", HttpServletResponse.SC_FORBIDDEN);
                                    } catch (EntityNotFoundException e) {
                                        throw new EntityException( "Cannot execute custom action ("+customAction.action+"): Could not find entity ("+e.entityReference+"): " + e.getMessage(), 
                                                view.getEntityReference()+"", HttpServletResponse.SC_NOT_FOUND );
                                    } catch (FormatUnsupportedException e) {
                                        throw new EntityException( "Cannot execute custom action ("+customAction.action+"): Format not supported ("+e.format+"): " + e.getMessage(), 
                                                view.getEntityReference()+"", HttpServletResponse.SC_NOT_ACCEPTABLE );
                                    } catch (IllegalArgumentException e) {
                                        throw new EntityException( "Cannot execute custom action ("+customAction.action+"): Illegal arguments: " + e.getMessage(), 
                                                view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST );
                                    } catch (UnsupportedOperationException e) {
                                        throw new EntityException( "Cannot execute custom action ("+customAction.action+"): Could not execute action: " + e.getMessage(), 
                                                view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST );
                                    }
                                    if (actionReturn == null 
                                            || actionReturn.output != null) {
                                        // custom action processing complete
                                        /* actionReturn.output != null - this means that there is an 
                                         * outputstream set and that the encoding has been,
                                         * handled, however, the response status code should be set still
                                         */
                                        handled = true;
                                    } else {
                                        // if there are headers then set them now
                                        addResponseHeaders(res, actionReturn.getHeaders());
                                        // if the custom action returned entity data then we will encode it for output
                                        if (actionReturn.entitiesList == null
                                                && actionReturn.entityData == null) {
                                            handled = true;
                                        } else {
                                            // there is entity data to return
                                            output = true;
                                            handled = false;
                                            // populate the entity data
                                            if (actionReturn.entitiesList != null) {
                                                if (actionReturn.entitiesList.size() > 1) {
                                                    // correct the view key which should be used now
                                                    view.setViewKey(EntityView.VIEW_LIST);
                                                }
                                                entityBrokerManager.populateEntityData(actionReturn.entitiesList);
                                            } else if (actionReturn.entityData != null) {
                                                // correct the view key which should be used now
                                                view.setViewKey(EntityView.VIEW_SHOW);
                                                entityBrokerManager.populateEntityData( new EntityData[] {actionReturn.entityData} );
                                            }
                                        }
                                    }
                                }

                                boolean formatInvalidFailure = false;
                                if (!handled) {
                                    // INTERNAL PROCESSING OF REQUEST
                                    try {
                                        if (output) {
                                            // output request
                                            String viewKey = view.getViewKey();
                                            if (EntityView.VIEW_NEW.equals(viewKey)
                                                    || EntityView.VIEW_EDIT.equals(viewKey) 
                                                    || EntityView.VIEW_DELETE.equals(viewKey) ) {
                                                // request for the create/edit/delete entity forms
                                                handled = false; // if we handle this then switch this to true
                                                if (Formats.FORM.equals(format)) {
                                                    // generate new/edit/delete forms internally if the provider allows it
                                                    Outputable outputable = (Outputable) entityProviderManager.getProviderByPrefixAndCapability(prefix, Outputable.class);
                                                    if (outputable != null) {
                                                        String[] outputFormats = outputable.getHandledOutputFormats();
                                                        if (outputFormats != null && ReflectUtils.contains(outputFormats, Formats.FORM) ) {
                                                            // we are handling this type of format for this entity
                                                            RequestUtils.setResponseEncoding(format, res);
                                                            if (EntityView.Method.HEAD.name().equals(view.getMethod())) {
                                                                // HEADER only
                                                                res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                                                                handled = true;
                                                            } else {
                                                                // GET
                                                                String form = entityEncodingManager.encodeEntity(prefix, format, null, view);
                                                                // if the encoder returned something useful then we output it, if nothing comes back we pass on
                                                                if (form != null && form.length() > 0) {
                                                                    try {
                                                                        res.getWriter().print(form);
                                                                    } catch (IOException e) {
                                                                        throw new RuntimeException("Failed to get writer from response: " + view, e);
                                                                    }
                                                                    handled = true;
                                                                    setNoCacheHeaders(res);
                                                                    res.setStatus(HttpServletResponse.SC_OK);
                                                                }
                                                            }
                                                        } else {
                                                            // format type not handled
                                                            throw new FormatUnsupportedException("Outputable restriction (formats list) for " 
                                                                    + prefix + " does not allow form generation, add the FORM format ("
                                                                    +Formats.FORM+") to the list of allowed output formats to enable this",
                                                                    view.getEntityReference()+"", format);
                                                        }
                                                    }
                                                }
                                            } else {
                                                Outputable outputable = (Outputable) entityProviderManager.getProviderByPrefixAndCapability(prefix, Outputable.class);
                                                if (outputable != null) {
                                                    if (customAction != null) {
                                                        // override format from the custom action
                                                        if (actionReturn != null 
                                                                && actionReturn.format != null) {
                                                            format = actionReturn.format;
                                                        }
                                                    }
                                                    String[] outputFormats = outputable.getHandledOutputFormats();
                                                    if (outputFormats == null || ReflectUtils.contains(outputFormats, format) ) {
                                                        // we are handling this type of format for this entity
                                                        RequestUtils.setResponseEncoding(format, res);

                                                        EntityReference ref = view.getEntityReference();
                                                        // get the entities to output
                                                        List<EntityData> entities = null;
                                                        if (customAction != null 
                                                                && actionReturn != null) {
                                                            // get entities from a custom action
                                                            entities = actionReturn.entitiesList;
                                                            if (entities != null) {
                                                                // recode the collection
                                                                if (entities.size() > 0) {
                                                                    EntityData ed = entities.get(0);
                                                                    ref = new EntityReference(ed.getEntityRef().getPrefix(), "");
                                                                    view.setEntityReference( ref );
                                                                    view.setViewKey(EntityView.VIEW_LIST);
                                                                }
                                                            } else if (actionReturn.entityData != null) {
                                                                // this was a single object return so it should be encoded as such, thus we will recode the correct reference into the view
                                                                ArrayList<EntityData> eList = new ArrayList<EntityData>();
                                                                EntityData ed = actionReturn.entityData;
                                                                // set title if not set
                                                                if (! ed.isDisplayTitleSet()) {
                                                                    ed.setDisplayTitle(customAction.action);
                                                                }
                                                                // add to list
                                                                eList.add( ed );
                                                                entities = eList;
                                                                // make entity reference
                                                                ref = ed.getEntityRef();
                                                                if (ref == null) {
                                                                    ref = new EntityReference(prefix, customAction.action);
                                                                } else if (ref.getId() == null) {
                                                                    ref = new EntityReference(ref.getPrefix(), customAction.action);
                                                                }
                                                                view.setEntityReference( ref );
                                                                view.setViewKey(EntityView.VIEW_SHOW);
                                                            }
                                                        } else {
                                                            // get from a search
                                                            Search search = RequestUtils.makeSearchFromRequestParams(requestStorage.getStorageMapCopy(true, false, true, true)); // leave out headers));
                                                            entities = entityBrokerManager.getEntitiesData(ref, search, requestStorage.getStorageMapCopy());
                                                        }
                                                        // set the modified header (use the sole entity in the list if there is one only)
                                                        setLastModifiedHeaders(res, (entities != null && entities.size()==1 ? entities.get(0) : null), System.currentTimeMillis());

                                                        if (EntityView.Method.HEAD.name().equals(view.getMethod())) {
                                                            // HEADER only
                                                            res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                                                        } else {
                                                            // GET
                                                            OutputStream outputStream = new LazyResponseOutputStream(res);

                                                            /* try to use the provider formatter if one available,
                                                             * if it decided not to handle it or none is available then control passes to internal
                                                             */
                                                            try {
                                                                OutputFormattable formattable = (OutputFormattable) entityProviderManager.getProviderByPrefixAndCapability(prefix, OutputFormattable.class);
                                                                if (formattable != null) {
                                                                    // use provider's formatter
                                                                    formattable.formatOutput(ref, format, entities, requestStorage.getStorageMapCopy(), outputStream);
                                                                    handled = true;
                                                                }
                                                            } catch (FormatUnsupportedException e) {
                                                                // provider decided not to handle this format
                                                                handled = false;
                                                            }
                                                            if (!handled) {
                                                                // handle internally or fail
                                                                entityEncodingManager.internalOutputFormatter(ref, format, entities, requestStorage.getStorageMapCopy(), outputStream, view);
                                                                // SPECIAL CASE: FORM
                                                                if (Formats.FORM.equals(format)) {
                                                                    setNoCacheHeaders(res);
                                                                }
                                                            }
                                                            handled = true;
                                                            res.setStatus(HttpServletResponse.SC_OK);
                                                        }
                                                    } else {
                                                        // format type not handled
                                                        throw new FormatUnsupportedException("Outputable restriction (formats list) for " 
                                                                + prefix + " blocked handling this format ("+format+")",
                                                                view.getEntityReference()+"", format);
                                                    }
                                                }
                                            }
                                        } else {
                                            // input request
                                            if (EntityView.VIEW_DELETE.equals(view.getViewKey())) {
                                                // delete request
                                                Deleteable deleteable = (Deleteable) entityProviderManager.getProviderByPrefixAndCapability(prefix, Deleteable.class);
                                                if (deleteable != null) {
                                                    deleteable.deleteEntity(view.getEntityReference(), requestStorage.getStorageMapCopy());
                                                    res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                                                    handled = true;
                                                }
                                            } else {
                                                // save request
                                                Inputable inputable = (Inputable) entityProviderManager.getProviderByPrefixAndCapability(prefix, Inputable.class);
                                                if (inputable != null) {
                                                    String[] inputFormats = inputable.getHandledInputFormats();
                                                    if (inputFormats == null || ReflectUtils.contains(inputFormats, format) ) {
                                                        // we are handling this type of format for this entity
                                                        Object entity = null;
                                                        InputStream inputStream = null;
                                                        try {
                                                            inputStream = req.getInputStream();
                                                        } catch (IOException e) {
                                                            throw new RuntimeException("Failed to get output stream from response: " + view.getEntityReference(), e);
                                                        }

                                                        /* try to use the provider translator if one available,
                                                         * if it decided not to handle it or none is available then control passes to internal
                                                         */
                                                        try {
                                                            InputTranslatable translatable = (InputTranslatable) entityProviderManager.getProviderByPrefixAndCapability(prefix, InputTranslatable.class);
                                                            if (translatable != null) {
                                                                // use provider's translator
                                                                entity = translatable.translateFormattedData(view.getEntityReference(), 
                                                                        format, inputStream, requestStorage.getStorageMapCopy());
                                                                handled = true;
                                                            }
                                                        } catch (FormatUnsupportedException e) {
                                                            // provider decided not to handle this format
                                                            handled = false;
                                                        }
                                                        if (!handled) {
                                                            // use internal translators or fail
                                                            entity = entityEncodingManager.internalInputTranslator(view.getEntityReference(), 
                                                                    format, inputStream, req);
                                                        }

                                                        if (entity == null) {
                                                            // FAILURE input could not be translated into an entity object
                                                            handled = false;
                                                            throw new EntityException("Unable to save entity ("+view.getEntityReference()+") with format ("
                                                                    +format+"), translated entity object was null", 
                                                                    view.toString(), HttpServletResponse.SC_BAD_REQUEST);
                                                        } else {
                                                            // setup all the headers for the response
                                                            if (EntityView.VIEW_NEW.equals(view.getViewKey())) {
                                                                Createable createable = entityProviderManager.getProviderByPrefixAndCapability(prefix, Createable.class);
                                                                if (createable == null) {
                                                                    throw new EntityException("Unable to create new entity ("+view+"), "
                                                                            +Createable.class.getName()+" is not implemented for this entity type ("+prefix+")", 
                                                                            view+"", HttpServletResponse.SC_NOT_IMPLEMENTED);
                                                                }
                                                                String createdId = createable.createEntity(view.getEntityReference(), entity, requestStorage.getStorageMapCopy());
                                                                if (createdId == null || "".equals(createdId)) {
                                                                    throw new IllegalStateException("Could not get the createdId from the newly created entity for ("+view+"), please ensure the provider is returning a non-null and non-empty value from the create method, if the item was not created then an exception should have been thrown");
                                                                }
                                                                view.setEntityReference( new EntityReference(prefix, createdId) ); // update the entity view
                                                                res.setHeader(EntityRequestHandler.HEADER_ENTITY_ID, createdId);
                                                                res.setStatus(HttpServletResponse.SC_CREATED);
                                                                // added the id to the response to make it easier on Nico
                                                                try {
                                                                    OutputStream outputStream = res.getOutputStream();
                                                                    outputStream.write( createdId.getBytes() );
                                                                } catch (IOException e) {
                                                                    // oh well, no id in the output
                                                                } catch (RuntimeException e) {
                                                                    // oh well, no id in the output
                                                                }
                                                            } else if (EntityView.VIEW_EDIT.equals(view.getViewKey())) {
                                                                Updateable updateable = entityProviderManager.getProviderByPrefixAndCapability(prefix, Updateable.class);
                                                                if (updateable == null) {
                                                                    throw new EntityException("Unable to create new entity ("+view+"), "
                                                                            +Updateable.class.getName()+" is not implemented for this entity type ("+prefix+")", 
                                                                            view+"", HttpServletResponse.SC_NOT_IMPLEMENTED);
                                                                }
                                                                updateable.updateEntity(view.getEntityReference(), entity, requestStorage.getStorageMapCopy());
                                                                res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                                                            } else {
                                                                // FAILURE not delete, edit, or new
                                                                throw new EntityException("Unable to handle entity input ("+view.getEntityReference()+"), " +
                                                                        "action was not understood: " + view.getViewKey(), 
                                                                        view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST);
                                                            }
                                                            // return the location of this updated or created entity (without any extension)
                                                            res.setHeader(EntityRequestHandler.HEADER_ENTITY_URL, view.getEntityURL() );
                                                            res.setHeader(EntityRequestHandler.HEADER_ENTITY_REFERENCE, view.getEntityReference().toString() );
                                                            handled = true;
                                                        }
                                                    } else {
                                                        // format type not handled
                                                        throw new FormatUnsupportedException("Inputable restriction for " 
                                                                + prefix + " blocked handling this format ("+format+")",
                                                                view.getEntityReference()+"", format);
                                                    }
                                                }
                                            }
                                        }
                                    } catch (FormatUnsupportedException e) {
                                        // this format could not be handled internally so we will pass it to the access provider, nothing else to do here
                                        formatInvalidFailure = true;
                                        handled = false;
                                    } catch (SecurityException se) {
                                        // AJAX/WS type security exceptions are handled specially, no redirect
                                        throw new EntityException("Security exception handling request for view ("+view+"), "
                                                + "this is typically caused by the current user not having access to the "
                                                + "data requested or the user not being logged in at all :: message=" + se.getMessage(),
                                                view.getEntityReference()+"", HttpServletResponse.SC_FORBIDDEN);
                                    } catch (EntityEncodingException e) {
                                        // translate EEE into EE - internal server error
                                        throw new EntityException("EntityEncodingException: Unable to handle " + (output ? "output" : "input") + " request for format  "+view.getFormat()+" for this path (" 
                                                + path + ") for prefix (" + prefix + ") for entity (" + view.getEntityReference() + "), request url (" + view.getOriginalEntityUrl() + "): " + e.getMessage(),
                                                view.getEntityReference()+"", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);        
                                    } catch (IllegalArgumentException e) {
                                        // translate IAE into EE - bad request
                                        throw new EntityException("IllegalArgumentException: Unable to handle " + (output ? "output" : "input") + " request for format  "+view.getFormat()+" for this path (" 
                                                + path + ") for prefix (" + prefix + ") for entity (" + view.getEntityReference() + "), request url (" + view.getOriginalEntityUrl() + "): " + e.getMessage(),
                                                view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST);        
                                    } catch (IllegalStateException e) {
                                        // translate ISE into EE - internal server error
                                        throw new EntityException("IllegalStateException: Unable to handle " + (output ? "output" : "input") + " request for format  "+view.getFormat()+" for this path (" 
                                                + path + ") for prefix (" + prefix + ") for entity (" + view.getEntityReference() + "), request url (" + view.getOriginalEntityUrl() + "): " + e.getMessage(),
                                                view.getEntityReference()+"", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                    }
                                }

                                if (! handled) {
                                    // default handling, send to the access provider if there is one (if none this will throw EntityException)
                                    try {
                                        boolean accessProviderExists = handleAccessProvider(view, req, res);
                                        if (!accessProviderExists) {
                                            if (formatInvalidFailure) {
                                                // trigger the format 
                                                throw new FormatUnsupportedException("Nothing (AP and internal) available to handle the requested format", view.getEntityReference()+"", view.getFormat());
                                            }
                                            String message = "Access Provider: Attempted to access an entity URL path ("
                                                + view + ") using method ("+view.getMethod()+") for an entity (" + view.getEntityReference() 
                                                + ") and view ("+view.getViewKey()+") when there is no " 
                                                + "access provider to handle the request for prefix (" + view.getEntityReference().getPrefix() + ")";
                                            throw new EntityException( message, view.toString(), HttpServletResponse.SC_METHOD_NOT_ALLOWED );
                                        }
                                    } catch (FormatUnsupportedException e) {
                                        // TODO add in the methods "allowed" header?
                                        throw new EntityException( "AccessProvider: Method/Format unsupported: Will not handle " + (output ? "output" : "input") + " request for format  "+view.getFormat()+" for this path (" 
                                                + path + ") for prefix (" + prefix + ") for entity (" + view.getEntityReference() + "), request url (" + view.getOriginalEntityUrl() + ")",
                                                view.getEntityReference()+"", HttpServletResponse.SC_NOT_ACCEPTABLE );
                                    }
                                }
                            }
                        }
                        handledReference = view.getEntityReference().toString();
                        requestStorage.setRequestValue(RequestStorage.ReservedKeys._requestEntityReference.name(), handledReference);

                        // handle the after interceptor
                        if (interceptor != null) {
                            interceptor.after(view, req, res);
                        }
                    } finally {
                        // clear the request data no matter what happens
                        requestStorage.reset();
                        requestGetter.setRequest(null);
                        requestGetter.setResponse(null);
                    }
                }
            }
        }
        return handledReference;
    }


    /**
     * @see EntityBroker#fireEntityRequest(String, String, String, Map, Object)
     */
    public EntityResponse fireEntityRequestInternal(String reference, String viewKey, String format, Map<String, String> params, Object entity) {
        if (reference == null) {
            throw new IllegalArgumentException("reference must not be null");
        }
        // convert the reference/key/format into a URL
        EntityReference ref = new EntityReference(reference);
        EntityView ev = new EntityView();
        ev.setEntityReference( ref );
        if (viewKey != null 
                && ! "".equals(viewKey)) {
            ev.setViewKey(viewKey);
        }
        if (format != null 
                && ! "".equals(format)) {
            ev.setExtension(format);
        }
        String URL = ev.toString();
        // get the right method to use
        Method method = Method.GET;
        if (EntityView.VIEW_DELETE.equals(ev.getViewKey())) {
            method = Method.DELETE;
        } else if (EntityView.VIEW_EDIT.equals(ev.getViewKey())) {
            method = Method.PUT;
        } else if (EntityView.VIEW_NEW.equals(ev.getViewKey())) {
            method = Method.POST;
        } else {
            method = Method.GET;
        }
        // handle entity if one was included
        Object data = null;
        if (entity != null) {
            String prefix = ref.getPrefix();
            Inputable inputable = entityProviderManager.getProviderByPrefixAndCapability(prefix, Inputable.class);
            if (inputable == null) {
                throw new IllegalArgumentException("This entity ("+ref+") is not Inputable so there is no reason to provide "
                        + "a non-null entity, you should leave the entity null when firing requests to this entity");
            }
            Outputable outputable = entityProviderManager.getProviderByPrefixAndCapability(prefix, Outputable.class);
            if (outputable == null) {
                throw new IllegalArgumentException("This entity ("+ref+") is not AccessFormats so there is no reason to provide "
                        + "a non-null entity, you should leave the entity null when firing requests to this entity");
            } else {
                // handle outputing the entity data
                List<EntityData> entities = new ArrayList<EntityData>();
                entities.add( EntityDataUtils.makeEntityData(ref, entity) );
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                entityEncodingManager.formatAndOutputEntity(ref, format, entities, output, null);
                data = new ByteArrayInputStream(output.toByteArray());
            }
        }
        HttpResponse httpResponse = HttpRESTUtils.fireRequest(URL, method, params, data, true);
        // translate response to correct kind
        EntityResponse response = new EntityResponse(httpResponse.getResponseCode(), 
                httpResponse.getResponseMessage(), httpResponse.getResponseBody(), httpResponse.getResponseHeaders());
        return response;
    }


    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.EntityRequestHandler#handleEntityError(javax.servlet.http.HttpServletRequest, java.lang.Throwable)
     */
    public String handleEntityError(HttpServletRequest req, Throwable error) {
        String msg = "Failure processing entity request ("+req.getPathInfo()+"): " + error.getMessage();
        if (entityBrokerManager.getExternalIntegrationProvider() != null) {
            try {
                msg = entityBrokerManager.getExternalIntegrationProvider().handleEntityError(req, error);
            } catch (UnsupportedOperationException e) {
                // nothing to do here, this is OK
            } catch (Exception e) {
                log.warn("EntityRequestHandler: External handleEntityError method failed, using default instead: " + e);
            }
        }
        return msg;
    }


    /**
     * Will choose whichever access provider is currently available to handle the request
     * @return true if there is an access provider, false otherwise
     */
    private boolean handleAccessProvider(EntityView view, HttpServletRequest req, HttpServletResponse res) {
        // no special handling so send on to the standard access provider if one can be found
        EntityViewAccessProvider evAccessProvider = entityViewAccessProviderManager.getProvider(view.getEntityReference().getPrefix());
        if (evAccessProvider == null) {
            if (accessProviderManager != null) {
                // try the old type access provider then
                HttpServletAccessProvider httpAccessProvider = accessProviderManager.getProvider(view.getEntityReference().getPrefix());
                if (httpAccessProvider == null) {
                    return false;
                } else {
                    // classloader protection START
                    ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
                    try {
                        Object thing = httpAccessProvider;
                        ClassLoader newClassLoader = thing.getClass().getClassLoader();
                        // check to see if this access provider reports the correct classloader
                        if (thing instanceof ClassLoaderReporter) {
                            newClassLoader = ((ClassLoaderReporter) thing).getSuitableClassLoader();
                        }
                        Thread.currentThread().setContextClassLoader(newClassLoader);
                        // send request to the access provider which will route it on to the correct entity world
                        httpAccessProvider.handleAccess(req, res, view.getEntityReference());
                    } finally {
                        Thread.currentThread().setContextClassLoader(currentClassLoader);
                    }
                    // classloader protection END
                }
            }
        } else {
            // check if this view key is specifically disallowed
            if (AccessViews.class.isAssignableFrom(evAccessProvider.getClass())) {
                String[] entityViewKeys = ((AccessViews)evAccessProvider).getHandledEntityViews();
                if (entityViewKeys != null && ! ReflectUtils.contains(entityViewKeys, view.getViewKey()) ) {
                    throw new EntityException("Access provider for " + view.getEntityReference().getPrefix() 
                            + " will not handle this view ("+view.getViewKey()+"): " + view,
                            view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST);
                }
            }
            // check if this format is specifically disallowed
            if (AccessFormats.class.isAssignableFrom(evAccessProvider.getClass())) {
                String[] accessFormats = ((AccessFormats)evAccessProvider).getHandledAccessFormats();
                if (accessFormats != null && ! ReflectUtils.contains(accessFormats, view.getFormat()) ) {
                    throw new FormatUnsupportedException("Access provider for " + view.getEntityReference().getPrefix() 
                            + " will not handle this format ("+view.getFormat()+")",
                            view.getEntityReference()+"", view.getFormat());
                }
            }
            handleClassLoaderAccess(evAccessProvider, req, res, view);
        }
        return true;
    }

    /**
     * Wrap this in an appropriate classloader before handling the request to ensure we
     * do not get ugly classloader failures
     */
    private void handleClassLoaderAccess(EntityViewAccessProvider accessProvider,
            HttpServletRequest req, HttpServletResponse res, EntityView view) {
        // START classloader protection
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Object classloaderIndicator = accessProvider;
            ClassLoader newClassLoader = classloaderIndicator.getClass().getClassLoader();
            // check to see if this access provider reports the correct classloader
            if (classloaderIndicator instanceof ClassLoaderReporter) {
                newClassLoader = ((ClassLoaderReporter) classloaderIndicator).getSuitableClassLoader();
            }
            Thread.currentThread().setContextClassLoader(newClassLoader);
            // START run in classloader
            accessProvider.handleAccess(view, req, res);
            // END run in classloader
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
        // END classloader protection
    }

    /**
     * Force a response to be set for no caching,
     * can be run after other headers are set
     * @param res the response
     */
    protected void setNoCacheHeaders(HttpServletResponse res) {
        long currentTime = System.currentTimeMillis();
        res.setDateHeader(ActionReturn.Header.DATE.toString(), currentTime);
        res.setDateHeader(ActionReturn.Header.EXPIRES.toString(), currentTime + 1000);

        res.setHeader(ActionReturn.Header.CACHE_CONTROL.toString(), "must-revalidate");
        res.addHeader(ActionReturn.Header.CACHE_CONTROL.toString(), "private");
        res.addHeader(ActionReturn.Header.CACHE_CONTROL.toString(), "no-store");
        res.addHeader(ActionReturn.Header.CACHE_CONTROL.toString(), "max-age=0");
        res.addHeader(ActionReturn.Header.CACHE_CONTROL.toString(), "s-maxage=0");
    }

    /**
     * Correctly sets up the basic headers for every response,
     * allows setting caching to be disabled by using the nocache or no-cache param
     * @param view
     * @param res
     * @param params
     * @param headers any headers to add on
     */
    protected void setResponseHeaders(EntityView view, HttpServletResponse res, Map<String, Object> params, Map<String, String> headers) {
        boolean noCache = false;
        long currentTime = System.currentTimeMillis();
        long lastModified = currentTime;
        if (params != null) {
            if (params.containsKey("no-cache") || params.containsKey("nocache")) {
                noCache = true;
            }
            String key = "last-modified";
            if (params.containsKey(key)) {
                try {
                    lastModified = ((Long) params.get(key)).longValue();
                } catch (Exception e) {
                    // nothing to do here but use the default time
                    lastModified = currentTime;
                }
            }
        }
        setLastModifiedHeaders(res, null, lastModified);

        // set the cache headers
        res.setDateHeader(ActionReturn.Header.DATE.toString(), currentTime);
        res.setDateHeader(ActionReturn.Header.EXPIRES.toString(), currentTime + 600000);

        if (noCache) {
            res.setHeader(ActionReturn.Header.CACHE_CONTROL.toString(), "must-revalidate");
            res.addHeader(ActionReturn.Header.CACHE_CONTROL.toString(), "private");
            res.addHeader(ActionReturn.Header.CACHE_CONTROL.toString(), "no-store");
            res.setDateHeader(ActionReturn.Header.EXPIRES.toString(), currentTime + 1000);
            res.addHeader(ActionReturn.Header.CACHE_CONTROL.toString(), "max-age=0");
            res.addHeader(ActionReturn.Header.CACHE_CONTROL.toString(), "s-maxage=0");
        } else {
            // response.addHeader("Cache-Control", "must-revalidate");
            res.setHeader(ActionReturn.Header.CACHE_CONTROL.toString(), "public");
            res.addHeader(ActionReturn.Header.CACHE_CONTROL.toString(), "max-age=600");
            res.addHeader(ActionReturn.Header.CACHE_CONTROL.toString(), "s-maxage=600");
        }

        // set the EB specific headers
        String prefix = view.getEntityReference().getPrefix();
        EntityProvider provider = entityProviderManager.getProviderByPrefix(prefix);
        res.setHeader("x-entity-prefix", prefix);
        res.setHeader("x-entity-reference", view.getEntityReference().toString());
        res.setHeader("x-entity-url", view.getEntityURL());
        res.setHeader("x-entity-format", view.getFormat());

        // set Sakai sdata compliant headers
        res.setHeader("x-sdata-handler", provider == null ? EntityBroker.class.getName() : provider.getClass().getName());
        res.setHeader("x-sdata-url", view.getOriginalEntityUrl());

        // add in any extra headers last
        addResponseHeaders(res, headers);
    }

    /**
     * Adds in headers to the response as needed
     * @param res
     * @param headers
     */
    protected void addResponseHeaders(HttpServletResponse res, Map<String, String> headers) {
        // add in any extra headers last
        if (headers != null && ! headers.isEmpty()) {
            for (Entry<String, String> entry : headers.entrySet()) {
                res.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * @param res the response
     * @param lastModifiedTime the time to use if none is found any other way
     * @param ed (optional) some entity data if available
     */
    protected void setLastModifiedHeaders(HttpServletResponse res, EntityData ed, long lastModifiedTime) {
        long lastModified = System.currentTimeMillis();
        if (ed != null) {
            // try to get from props first
            boolean found = false;
            Object lm = ed.getEntityProperties().get("lastModified");
            if (lm != null) {
                Long l = makeLastModified(lm);
                if (l != null) {
                    lastModified = l.longValue();
                    found = true;
                }
            }
            if (!found) {
                if (ed.getData() != null) {
                    // look for the annotation on the entity
                    try {
                        lm = ReflectUtils.getInstance().getFieldValue(ed.getData(), "lastModified", EntityLastModified.class);
                        Long l = makeLastModified(lm);
                        if (l != null) {
                            lastModified = l.longValue();
                            found = true;
                        }
                    } catch (FieldnameNotFoundException e1) {
                        // nothing to do here
                    }
                }
            }
        } else {
            lastModified = lastModifiedTime;
        }
        // ETag or Last-Modified
        res.setDateHeader(ActionReturn.Header.LAST_MODIFIED.toString(), lastModified);
        String currentEtag = String.valueOf(lastModified);
        res.setHeader(ActionReturn.Header.ETAG.toString(), currentEtag);
    }

    /**
     * Make a last modified long from this object if possible OR return null
     * @param lm any object that might be the last modified date
     * @return the long OR null if it cannot be converted
     */
    private Long makeLastModified(Object lm) {
        Long lastModified = null;
        if (lm != null) {
            Class<?> c = lm.getClass();
            if (Date.class.isAssignableFrom(c)) {
                lastModified = ((Date)lm).getTime();
            } else if (Long.class.isAssignableFrom(c)) {
                lastModified = ((Long)lm);
            } else if (String.class.isAssignableFrom(c)) {
                try {
                    lastModified = new Long((String)lm);
                } catch (NumberFormatException e) {
                    // nothing to do here
                }
            } else {
                log.warn("EntityRequestHandler: Unknown type returned for 'lastModified' (not Date, Long, String): " + lm.getClass() + ", using the default value of current time instead");
            }
        }
        return lastModified;
    }

}
