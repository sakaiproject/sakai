/**
 * $Id$
 * $URL$
 * EntityHandler.java - entity-broker - Apr 6, 2008 9:03:03 AM - azeckoski
 **************************************************************************
 * Copyright 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityRequestHandler;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.access.AccessFormats;
import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;
import org.sakaiproject.entitybroker.access.HttpServletAccessProvider;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityLastModified;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Deleteable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.InputTranslatable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputFormattable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestHandler;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestInterceptor;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Redirectable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.CustomAction;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.FormatUnsupportedException;
import org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestGetterImpl;
import org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestStorageImpl;
import org.sakaiproject.entitybroker.impl.util.RequestUtils;
import org.sakaiproject.entitybroker.util.ClassLoaderReporter;
import org.sakaiproject.entitybroker.util.EntityDataUtils;
import org.sakaiproject.entitybroker.util.EntityResponse;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.entitybroker.util.http.HttpRESTUtils;
import org.sakaiproject.entitybroker.util.http.HttpResponse;
import org.sakaiproject.entitybroker.util.http.HttpRESTUtils.Method;
import org.sakaiproject.entitybroker.util.reflect.ReflectUtil;
import org.sakaiproject.entitybroker.util.reflect.exception.FieldnameNotFoundException;

/**
 * Implementation of the handler for the EntityBroker system<br/>
 * This handles all the processing of incoming requests (http based) and includes
 * method to process the request data and ensure classloader safety
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@SuppressWarnings("deprecation")
public class EntityHandlerImpl implements EntityRequestHandler {

   private static final String HEADER_EXPIRES = "Expires";

   private static final String HEADER_DATE = "Date";

   private static final String HEADER_ETAG = "ETag";

   private static final String HEADER_LAST_MODIFIED = "Last-Modified";

   private static final String HEADER_CACHE_CONTROL = "Cache-Control";

   protected static final String DIRECT = TemplateParseUtil.DIRECT_PREFIX;

   private static Log log = LogFactory.getLog(EntityHandlerImpl.class);

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

   private RequestGetter requestGetter;
   public void setRequestGetter(RequestGetter requestGetter) {
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

   /**
    * This has to be the impl, we ONLY use the impl specific methods
    */
   private RequestStorageImpl requestStorage;
   public void setRequestStorage(RequestStorageImpl requestStorage) {
      this.requestStorage = requestStorage;
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.EntityRequestHandler#handleEntityAccess(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   public String handleEntityAccess(HttpServletRequest req, HttpServletResponse res, String path) {
      // get the path info if not set
      if (path == null) {
         path = req.getPathInfo();
      }

      String handledReference = null;

      if (path == null || "".equals(path) || "/".equals(path)) {
         // SPECIAL handling for empty path
         res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
         try {
            res.sendRedirect( res.encodeRedirectURL(DIRECT + SLASH_DESCRIBE) );
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
            String format = TemplateParseUtil.findExtension(path)[2];
            if (format == null) {
               format = Formats.HTML;
            }
            RequestUtils.setResponseEncoding(format, res);
            String output = entityDescriptionManager.makeDescribeAll(format);
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
               String format = TemplateParseUtil.findExtension(path)[2];
               if (format == null) {
                  format = Formats.HTML;
               }
               RequestUtils.setResponseEncoding(format, res);
               String entityId = req.getParameter("_id");
               if (entityId == null || "".equals(entityId)) {
                  entityId = FAKE_ID;
               }
               String output = entityDescriptionManager.makeDescribeEntity(view.getEntityReference().getPrefix(), entityId, format);
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
                        log.info("Entity Redirect: redirecting from ("+path+") to ("+redirectURL+")");
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
                  if (cRef.getId().equals(customAction.action)) {
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
                  ((RequestGetterImpl) requestGetter).setRequest(req);
                  ((RequestGetterImpl) requestGetter).setResponse(res);
                  // set the request variables
                  requestStorage.setRequestValue(RequestStorage.ReservedKeys._requestEntityReference.name(), view.getEntityReference().toString());
                  requestStorage.setRequestValue(RequestStorage.ReservedKeys._requestOrigin.name(), RequestStorage.RequestOrigin.REST.name());
                  requestStorage.setRequestValue(RequestStorage.ReservedKeys._requestActive.name(), true);

                  // handle the before interceptor
                  RequestInterceptor interceptor = (RequestInterceptor) entityProviderManager.getProviderByPrefixAndCapability(prefix, RequestInterceptor.class);
                  if (interceptor != null) {
                     interceptor.before(view, req, res);
                  }

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
                     setResponseHeaders(view, res, null);

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
                           actionReturn = entityActionsManager.handleCustomActionRequest(actionProvider, view, customAction.action, req, res);
                        } catch (IllegalArgumentException e) {
                           throw new EntityException( "Cannot execute custom action ("+customAction.action+"): Illegal arguments: " + e.getMessage(), 
                                 view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST );
                        } catch (UnsupportedOperationException e) {
                           throw new EntityException( "Cannot execute custom action ("+customAction.action+"): Invalid action: " + e.getMessage(), 
                                 view.getEntityReference()+"", HttpServletResponse.SC_METHOD_NOT_ALLOWED );
                        }
                        if (actionReturn == null 
                              || actionReturn.output != null) {
                           // custom action processing complete
                           res.setStatus(HttpServletResponse.SC_OK);
                           handled = true;
                        } else {
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
                                 entityBrokerManager.populateEntityData(actionReturn.entitiesList);
                              } else if (actionReturn.entityData != null) {
                                 entityBrokerManager.populateEntityData( new EntityData[] {actionReturn.entityData} );
                              }
                           }
                        }
                     }

                     if (!handled) {
                        // INTERNAL PROCESSING OF REQUEST
                        try {
                           String format = view.getFormat();
                           if (output) {
                              // output request
                              Outputable outputable = (Outputable) entityProviderManager.getProviderByPrefixAndCapability(prefix, Outputable.class);
                              if (outputable != null) {
                                 if (customAction != null) {
                                    // override format from the custom action
                                    if (actionReturn.format != null) {
                                       format = actionReturn.format;
                                    }
                                 }
                                 String[] outputFormats = outputable.getHandledOutputFormats();
                                 if (outputFormats == null || ReflectUtil.contains(outputFormats, format) ) {
                                    // we are handling this type of format for this entity
                                    RequestUtils.setResponseEncoding(format, res);

                                    // get the entities to output
                                    List<EntityData> entities = null;
                                    if (customAction != null) {
                                       // get entities from a custom action
                                       entities = actionReturn.entitiesList;
                                       if (entities == null 
                                             && actionReturn.entityData != null) {
                                          ArrayList<EntityData> eList = new ArrayList<EntityData>();
                                          EntityData ed = actionReturn.entityData;
                                          eList.add( ed );
                                          entities = eList;
                                          // this was a single object return so it should be encoded as such, thus we will recode the correct reference into the view
                                          EntityReference edRef = ed.getEntityReference();
                                          if (edRef.getId() == null) {
                                             edRef = new EntityReference(edRef.getPrefix(), customAction.action);
                                          }
                                          view.setEntityReference( edRef );
                                          view.setViewKey(EntityView.VIEW_SHOW);
                                       }
                                    } else {
                                       // get from a search
                                       Search search = RequestUtils.makeSearchFromRequest(req);
                                       entities = entityBrokerManager.getEntitiesData(view.getEntityReference(), search, null);
                                    }
                                    // set the modifed header (use the sole entity in the list if there is one only)
                                    setLastModifiedHeaders(res, (entities != null && entities.size()==1 ? entities.get(0) : null), System.currentTimeMillis());

                                    if (EntityView.Method.HEAD.name().equals(view.getMethod())) {
                                       // HEADER only
                                       res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                                    } else {
                                       // GET
                                       OutputStream outputStream = null;
                                       try {
                                          outputStream = res.getOutputStream();
                                       } catch (IOException e) {
                                          throw new RuntimeException("Failed to get output stream from response: " + view.getEntityReference(), e);
                                       }

                                       /* try to use the provider formatter if one available,
                                        * if it decided not to handle it or none is available then control passes to internal
                                        */
                                       try {
                                          OutputFormattable formattable = (OutputFormattable) entityProviderManager.getProviderByPrefixAndCapability(prefix, OutputFormattable.class);
                                          if (formattable != null) {
                                             // use provider's formatter
                                             formattable.formatOutput(view.getEntityReference(), format, entities, null, outputStream);
                                             handled = true;
                                          }
                                       } catch (FormatUnsupportedException e) {
                                          // provider decided not to handle this format
                                          handled = false;
                                       }
                                       if (!handled) {
                                          // handle internally or fail
                                          entityEncodingManager.internalOutputFormatter(view.getEntityReference(), format, entities, outputStream, view, null);
                                       }
                                       handled = true;
                                       res.setStatus(HttpServletResponse.SC_OK);
                                    }
                                 } else {
                                    // format type not handled
                                    throw new FormatUnsupportedException("Outputable restriction for " 
                                          + prefix + " blocked handling this format ("+format+")",
                                          view.getEntityReference()+"", format);
                                 }
                              }
                           } else {
                              // input request
                              if (EntityView.VIEW_DELETE.equals(view.getViewKey())) {
                                 // delete request
                                 Deleteable deleteable = (Deleteable) entityProviderManager.getProviderByPrefixAndCapability(prefix, Deleteable.class);
                                 if (deleteable != null) {
                                    deleteable.deleteEntity(view.getEntityReference(), null);
                                    res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                                    handled = true;
                                 }
                              } else {
                                 // save request
                                 Inputable inputable = (Inputable) entityProviderManager.getProviderByPrefixAndCapability(prefix, Inputable.class);
                                 if (inputable != null) {
                                    String[] inputFormats = inputable.getHandledInputFormats();
                                    if (inputFormats == null || ReflectUtil.contains(inputFormats, format) ) {
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
                                                   format, inputStream, null);
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
                                             String createdId = inputable.createEntity(view.getEntityReference(), entity, null);
                                             view.setEntityReference( new EntityReference(prefix, createdId) ); // update the entity view
                                             res.setHeader(EntityRequestHandler.HEADER_ENTITY_ID, createdId);
                                             res.setStatus(HttpServletResponse.SC_CREATED);
                                          } else if (EntityView.VIEW_EDIT.equals(view.getViewKey())) {
                                             inputable.updateEntity(view.getEntityReference(), entity, null);
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
                           handled = false;
                        } catch (IllegalArgumentException e) {
                           // translate IAE into EE - bad request
                           throw new EntityException("Internal Handler: IllegalArgumentException: Unable to handle " + (output ? "output" : "input") + " request for format  "+view.getFormat()+" for this path (" 
                                 + path + ") for prefix (" + prefix + ") for entity (" + view.getEntityReference() + "), request url (" + view.getOriginalEntityUrl() + "): " + e.getMessage(),
                                 view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST);        
                        } catch (IllegalStateException e) {
                           // translate ISE into EE - internal server error
                           throw new EntityException("Internal Handler: IllegalStateException: Unable to handle " + (output ? "output" : "input") + " request for format  "+view.getFormat()+" for this path (" 
                                 + path + ") for prefix (" + prefix + ") for entity (" + view.getEntityReference() + "), request url (" + view.getOriginalEntityUrl() + "): " + e.getMessage(),
                                 view.getEntityReference()+"", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        }
                     }

                     if (! handled) {
                        // default handling, send to the access provider if there is one (if none this will throw EntityException)
                        try {
                           handleAccessProvider(view, req, res);
                        } catch (FormatUnsupportedException e) {
                           throw new EntityException( "AccessProvider: Format unsupported: Will not handle " + (output ? "output" : "input") + " request for format  "+view.getFormat()+" for this path (" 
                                 + path + ") for prefix (" + prefix + ") for entity (" + view.getEntityReference() + "), request url (" + view.getOriginalEntityUrl() + ")",
                                 view.getEntityReference()+"", HttpServletResponse.SC_METHOD_NOT_ALLOWED );
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
                  ((RequestGetterImpl) requestGetter).setRequest(null);
                  ((RequestGetterImpl) requestGetter).setResponse(null);
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




   /**
    * Will choose whichever access provider is currently available to handle the request
    * @throws EntityException if there is none
    */
   @SuppressWarnings("deprecation")
   private void handleAccessProvider(EntityView view, HttpServletRequest req, HttpServletResponse res) {
      // no special handling so send on to the standard access provider if one can be found
      EntityViewAccessProvider evAccessProvider = entityViewAccessProviderManager.getProvider(view.getEntityReference().getPrefix());
      if (evAccessProvider == null) {
         // try the old type access provider then
         HttpServletAccessProvider httpAccessProvider = accessProviderManager.getProvider(view.getEntityReference().getPrefix());
         if (httpAccessProvider == null) {
            String message = "Attempted to access an entity URL path ("
               + view.toString() + ") for an entity (" + view.toString() + ") when there is no " 
               + "access provider to handle the request for prefix (" + view.getEntityReference().getPrefix() + ")";
            throw new EntityException( message, view.toString(), HttpServletResponse.SC_METHOD_NOT_ALLOWED );
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
      } else {
         if (AccessFormats.class.isAssignableFrom(evAccessProvider.getClass())) {
            String[] accessFormats = ((AccessFormats)evAccessProvider).getHandledAccessFormats();
            if (accessFormats != null && ! ReflectUtil.contains(accessFormats, view.getFormat()) ) {
               throw new FormatUnsupportedException("Access provider for " + view.getEntityReference().getPrefix() 
                     + " will not handle this format ("+view.getFormat()+")",
                     view.getEntityReference()+"", view.getFormat());
            }
         }
         handleClassLoaderAccess(evAccessProvider, req, res, view);
      }
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
    * Correctly sets up the basic headers for every response
    * @param view
    * @param res
    * @param params
    */
   protected void setResponseHeaders(EntityView view, HttpServletResponse res, Map<String, Object> params) {
      boolean noCache = false;
      long currentTime = System.currentTimeMillis();
      long lastModified = currentTime;
      if (params != null) {
         String key = "no-cache";
         if (params.containsKey(key)) {
            try {
               noCache = ((Boolean) params.get(key)).booleanValue();
            } catch (Exception e) {
               // in case the value is not there or null
               noCache = false;
            }
         }
         key = "last-modified";
         if (params.containsKey(key)) {
            try {
               lastModified = ((Long) params.get(key)).longValue();
            } catch (Exception e) {
               // nothing to do here but use the default time
            }
         }
      }
      setLastModifiedHeaders(res, null, lastModified);

      // set the cache headers
      if (noCache) {
         res.addHeader(HEADER_CACHE_CONTROL, "must-revalidate");
         res.addHeader(HEADER_CACHE_CONTROL, "private");
         res.addHeader(HEADER_CACHE_CONTROL, "no-store");
      } else {
         // response.addHeader("Cache-Control", "must-revalidate");
         res.addHeader(HEADER_CACHE_CONTROL, "public");
      }
      res.addHeader(HEADER_CACHE_CONTROL, "max-age=600");
      res.addHeader(HEADER_CACHE_CONTROL, "s-maxage=600");
      res.setDateHeader(HEADER_DATE, currentTime);
      res.setDateHeader(HEADER_EXPIRES, currentTime + 600000);

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
            if (ed.getEntity() != null) {
               // look for the annotation on the entity
               try {
                  lm = entityBrokerManager.getReflectUtil().getFieldValue(ed.getEntity(), "lastModified", EntityLastModified.class);
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
      res.setDateHeader(HEADER_LAST_MODIFIED, lastModified);
      String currentEtag = String.valueOf(lastModified);
      res.setHeader(HEADER_ETAG, currentEtag);
   }

   private Long makeLastModified(Object lm) {
      Long lastModified = null;
      if (Date.class.isAssignableFrom(lm.getClass())) {
         lastModified = ((Date)lm).getTime();
      } else if (Long.class.isAssignableFrom(lm.getClass())) {
         lastModified = ((Long)lm);
      } else if (String.class.isAssignableFrom(lm.getClass())) {
         try {
            lastModified = new Long((String)lm);
         } catch (NumberFormatException e) {
            // nothing to do here
         }
      } else {
         log.warn("Unknown type returned for 'lastModified' (not Date, Long, String): " + lm.getClass() + ", using the default value of current time instead");
      }
      return lastModified;
   }

}
