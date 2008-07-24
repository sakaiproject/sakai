/**
 * $Id$
 * $URL$
 * EntityHandler.java - entity-broker - Apr 6, 2008 9:03:03 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;
import org.sakaiproject.entitybroker.access.HttpServletAccessProvider;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Deleteable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.InputTranslatable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputFormattable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestHandler;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestInterceptor;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestGetterImpl;
import org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestStorageImpl;
import org.sakaiproject.entitybroker.util.ClassLoaderReporter;
import org.sakaiproject.entitybroker.util.EntityResponse;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.entitybroker.util.http.HttpRESTUtils;
import org.sakaiproject.entitybroker.util.http.HttpResponse;
import org.sakaiproject.entitybroker.util.http.HttpRESTUtils.Method;
import org.sakaiproject.entitybroker.util.reflect.ReflectUtil;

/**
 * Common implementation of the handler for the EntityBroker system. This should be used in
 * preference to the EntityBroker directly by implementation classes part of the EntityBroker
 * scheme, rather than the user-facing EntityBroker directly.
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
@SuppressWarnings("deprecation")
public class EntityHandlerImpl implements EntityRequestHandler {

   protected static final String DIRECT = "/direct";
   protected static final String POST_METHOD = "_method";

   private static Log log = LogFactory.getLog(EntityHandlerImpl.class);

   // must be the same as the string in DirectServlet with the same name
   private static final String ORIGINAL_METHOD = "_originalMethod";

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

      // special handling for empty path
      if (path == null || "".equals(path) || "/".equals(path)) {
         try {
            res.sendRedirect( res.encodeRedirectURL(DIRECT + SLASH_DESCRIBE) );
         } catch (IOException e) {
            // this is not going to ever happen
            throw new RuntimeException("Could not encode the redirect URL");
         }
      } else {
         // regular handling for direct URLs
         if ( (SLASH_DESCRIBE).equals(path) 
               || path.startsWith(SLASH_DESCRIBE + EntityReference.PERIOD)) {
            // handling for the describe all URL
            String format = TemplateParseUtil.findExtension(path)[2];
            if (format == null) {
               format = Formats.HTML;
            }
            setResponseEncoding(format, res);
            String output = entityDescriptionManager.makeDescribeAll(format);
            try {
               res.getWriter().write(output);
            } catch (IOException e) {
               throw new RuntimeException("Failed to put output into the response writer: " + e.getMessage(), e);
            }
            res.setStatus(HttpServletResponse.SC_OK);
            handledReference = EntityView.SEPARATOR+"";
         } else {

            EntityView view;
            try {
               view = entityBrokerManager.parseEntityURL(path);
            } catch (IllegalArgumentException e) {
               // indicates we could not parse the reference
               throw new EntityException("Could not parse entity path ("+path+"): " + e.getMessage(), path, HttpServletResponse.SC_BAD_REQUEST);
            }

            if (view == null) {
               // no provider for this entity prefix
               throw new EntityException( "No entity provider could be found to handle the prefix in this path: " + path, 
                     path, HttpServletResponse.SC_NOT_IMPLEMENTED );
            } else if ( DESCRIBE.equals(view.getEntityReference().getId()) ) {
               // Special handling for entity describe URLs
               String format = TemplateParseUtil.findExtension(path)[2];
               if (format == null) {
                  format = Formats.HTML;
               }
               setResponseEncoding(format, res);
               String entityId = req.getParameter("_id");
               if (entityId == null || "".equals(entityId)) {
                  entityId = FAKE_ID;
               }
               String output = entityDescriptionManager.makeDescribeEntity(view.getEntityReference().getPrefix(), entityId, format);
               try {
                  res.getWriter().write(output);
               } catch (IOException e) {
                  throw new RuntimeException("Failed to put output into the response writer: " + e.getMessage(), e);
               }
               res.setStatus(HttpServletResponse.SC_OK);
               handledReference = view.getEntityReference().getSpaceReference() + SLASH_DESCRIBE;
            } else if (! entityBrokerManager.entityExists(view.getEntityReference()) ) {
               // invalid entity reference (entity does not exist)
               throw new EntityException( "Attempted to access an entity URL path (" + path + ") for an entity ("
                     + view.getEntityReference() + ") that does not exist", 
                     view.getEntityReference()+"", HttpServletResponse.SC_NOT_FOUND );
            } else {
               // reference successfully parsed
               String prefix = view.getEntityReference().getPrefix();
               res.setStatus(HttpServletResponse.SC_OK); // other things can switch this later on

               // check for extensions
               if (view.getExtension() == null) {
                  view.setExtension(Formats.HTML); // update the view
               }
               req.setAttribute("extension", view.getExtension());

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
                  // provider is handling this request
                  handleClassLoaderAccess(handler, req, res, view);
               } else {
                  // handle the request internally if possible

                  // identify the type of request (input or output) and the action (will be encoded in the viewKey)
                  boolean output = false;
                  String method = req.getMethod() == null ? "GET" : req.getMethod().toUpperCase().trim();
                  // this fails because the original post gets lost therefore we are giving up on this for now
//                // check to see if the original method value was set
//                if (req.getAttribute(ORIGINAL_METHOD) != null) {
//                method = (String) req.getAttribute(ORIGINAL_METHOD);
//                }
                  if ("GET".equals(method)) {
                     output = true;
                  } else {
                     // identify the action based on the method type or "_method" attribute
                     if ("DELETE".equals(method)) {
                        view.setViewKey(EntityView.VIEW_DELETE);
                     } else if ("PUT".equals(method)) {
                        view.setViewKey(EntityView.VIEW_EDIT);
                     } else if ("POST".equals(method)) {
                        String _method = req.getParameter(POST_METHOD);
                        if (_method == null) {
                           // this better be a create request
                           view.setViewKey(EntityView.VIEW_NEW);
                        } else {
                           _method = _method.toUpperCase().trim();
                           if ("DELETE".equals(_method)) {
                              view.setViewKey(EntityView.VIEW_DELETE);
                           } else if ("PUT".equals(_method)) {
                              view.setViewKey(EntityView.VIEW_EDIT);
                           } else {
                              throw new EntityException("Unable to handle POST request with _method, unknown method (only PUT/DELETE allowed): " + _method, 
                                    view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST);                        
                           }
                        }
                     } else {
                        throw new EntityException("Unable to handle request method, unknown method (only GET/POST/PUT/DELETE allowed): " + method, 
                              view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST);
                     }

                     // check that the request is valid (edit and delete require an entity id)
                     if ( (EntityView.VIEW_EDIT.equals(view.getViewKey()) || EntityView.VIEW_DELETE.equals(view.getViewKey()) )
                           && view.getEntityReference().getId() == null) {
                        throw new EntityException("Unable to handle entity ("+prefix+") edit or delete request without entity id, url=" 
                              + view.getOriginalEntityUrl(), 
                              view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST);
                     }
                  }

                  boolean handled = false;
                  try {
                     if (output) {
                        // output request
                        Outputable outputable = (Outputable) entityProviderManager.getProviderByPrefixAndCapability(prefix, Outputable.class);
                        if (outputable != null) {
                           if ( ReflectUtil.contains(outputable.getHandledOutputFormats(), view.getExtension()) ) {
                              // we are handling this type of format for this entity
                              String format = view.getExtension();
                              setResponseEncoding(format, res);

                              // get the entities to output
                              Search search = makeSearchFromRequest(req);
                              List<?> entities = entityBrokerManager.fetchEntityList(view.getEntityReference(), search);
                              OutputStream outputStream = null;
                              try {
                                 outputStream = res.getOutputStream();
                              } catch (IOException e) {
                                 throw new RuntimeException("Failed to get output stream from response: " + view.getEntityReference(), e);
                              }

                              OutputFormattable formattable = (OutputFormattable) entityProviderManager.getProviderByPrefixAndCapability(prefix, OutputFormattable.class);
                              if (formattable == null) {
                                 // handle internally or fail
                                 entityEncodingManager.internalOutputFormatter(view.getEntityReference(), view.getExtension(), entities, outputStream, view);
                              } else {
                                 // use provider's formatter
                                 formattable.formatOutput(view.getEntityReference(), view.getExtension(), entities, outputStream);
                              }
                              res.setStatus(HttpServletResponse.SC_OK);
                              handled = true;
                           } else {
                              // will not handle this format type
                              throw new EntityException( "Will not handle output request for format  "+view.getExtension()+" for this path (" 
                                    + path + ") for prefix (" + prefix + ") for entity (" + view.getEntityReference() + ")", 
                                    view.getEntityReference()+"", HttpServletResponse.SC_METHOD_NOT_ALLOWED );
                           }
                        }
                     } else {
                        // input request
                        if (EntityView.VIEW_DELETE.equals(view.getViewKey())) {
                           // delete request
                           Deleteable deleteable = (Deleteable) entityProviderManager.getProviderByPrefixAndCapability(prefix, Deleteable.class);
                           if (deleteable != null) {
                              deleteable.deleteEntity(view.getEntityReference());
                              res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                              handled = true;
                           }
                        } else {
                           // save request
                           Inputable inputable = (Inputable) entityProviderManager.getProviderByPrefixAndCapability(prefix, Inputable.class);
                           if (inputable != null) {
                              if ( ReflectUtil.contains(inputable.getHandledInputFormats(), view.getExtension()) ) {
                                 // we are handling this type of format for this entity
                                 Object entity = null;
                                 InputStream inputStream = null;
                                 try {
                                    inputStream = req.getInputStream();
                                 } catch (IOException e) {
                                    throw new RuntimeException("Failed to get output stream from response: " + view.getEntityReference(), e);
                                 }

                                 InputTranslatable translatable = (InputTranslatable) entityProviderManager.getProviderByPrefixAndCapability(prefix, InputTranslatable.class);
                                 if (translatable == null) {
                                    // use internal translators or fail
                                    entity = entityEncodingManager.internalInputTranslator(view.getEntityReference(), 
                                          view.getExtension(), inputStream, req);
                                 } else {
                                    // use provider's translator
                                    entity = translatable.translateFormattedData(view.getEntityReference(), 
                                          view.getExtension(), inputStream);
                                 }

                                 if (entity == null) {
                                    throw new EntityException("Unable to save entity ("+view.getEntityReference()+"), entity object was null", 
                                          view.toString(), HttpServletResponse.SC_BAD_REQUEST);
                                 } else {
                                    if (EntityView.VIEW_NEW.equals(view.getViewKey())) {
                                       String createdId = inputable.createEntity(view.getEntityReference(), entity);
                                       view.setEntityReference( new EntityReference(prefix, createdId) ); // update the entity view
                                       res.setHeader(EntityRequestHandler.HEADER_ENTITY_ID, createdId);
                                       res.setStatus(HttpServletResponse.SC_CREATED);
                                    } else if (EntityView.VIEW_EDIT.equals(view.getViewKey())) {
                                       inputable.updateEntity(view.getEntityReference(), entity);
                                       res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                                    } else {
                                       throw new EntityException("Unable to handle entity input ("+view.getEntityReference()+"), " +
                                             "action was not understood: " + view.getViewKey(), 
                                             view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST);
                                    }
                                    // return the location of this updated or created entity (without any extension)
                                    res.setHeader(EntityRequestHandler.HEADER_ENTITY_URL, view.getEntityURL(EntityView.VIEW_SHOW, null));
                                    res.setHeader(EntityRequestHandler.HEADER_ENTITY_REFERENCE, view.getEntityReference().toString());
                                    handled = true;
                                 }
                              } else {
                                 // will not handle this format type
                                 throw new EntityException( "Will not handle input request for format  "+view.getExtension()+" for this path (" 
                                       + path + ") for prefix (" + prefix + ") for entity (" + view.getEntityReference().toString() + ")", 
                                       view.getEntityReference()+"", HttpServletResponse.SC_METHOD_NOT_ALLOWED );
                              }
                           }
                        }
                     }
                  } catch (IllegalArgumentException e) {
                     // translate IAE into EE - bad request
                     throw new EntityException("IllegalArgumentException: Unable to handle " + (output ? "output" : "input") + " request url ("
                           + view.getOriginalEntityUrl()+"), " + e.getMessage(),
                           view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST);        
                  } catch (IllegalStateException e) {
                     // translate ISE into EE - internal server error
                     throw new EntityException("IllegalStateException: Unable to handle " + (output ? "output" : "input") + " request url ("
                           + view.getOriginalEntityUrl()+"), " + e.getMessage(),
                           view.getEntityReference()+"", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                  }

                  if (! handled) {
                     // default handling, send to the access provider if there is one
                     handleAccessProvider(view, req, res);
                  }
               }
               handledReference = view.getEntityReference().toString();
               requestStorage.setRequestValue(RequestStorage.ReservedKeys._requestEntityReference.name(), handledReference);

               // handle the after interceptor
               if (interceptor != null) {
                  interceptor.after(view, req, res);
               }

               // clear the request data
               ((RequestGetterImpl) requestGetter).setRequest(null);
               ((RequestGetterImpl) requestGetter).setResponse(null);
               requestStorage.reset();
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
            throw new IllegalArgumentException("This entity ("+ref+") is not Outputable so there is no reason to provide "
               + "a non-null entity, you should leave the entity null when firing requests to this entity");
         } else {
            String[] formats = outputable.getHandledOutputFormats();
            if ( ReflectUtil.contains(formats, format) ) {
               List<Object> entities = new ArrayList<Object>();
               entities.add(entity);
               // need to make sure the reference has an id set
               ref = new EntityReference(ref.getPrefix(), ref.getId() == null ? "new" : ref.getId());
               // setup the output stream
               ByteArrayOutputStream output = new ByteArrayOutputStream();
               OutputFormattable formattable = entityProviderManager.getProviderByPrefixAndCapability(prefix, OutputFormattable.class);
               if (formattable == null) {
                  // handle internally or fail
                  entityEncodingManager.internalOutputFormatter(ref, format, entities, output, null);
               } else {
                  // use provider's formatter
                  formattable.formatOutput(ref, format, entities, output);
               }
               data = new ByteArrayInputStream(output.toByteArray());
            } else {
               throw new IllegalArgumentException("This entity ("+reference+") is not outputable in this format ("+format+")," +
                     " only the following formats are supported: " + ReflectUtil.arrayToString(formats));
            }
         }
      }
      HttpResponse httpResponse = HttpRESTUtils.fireRequest(URL, method, params, data, true);
      // translate response to correct kind
      EntityResponse response = new EntityResponse(httpResponse.getResponseCode(), 
            httpResponse.getResponseMessage(), httpResponse.getResponseBody(), httpResponse.getResponseHeaders());
      return response;
   }

   /**
    * This looks at search parameters and returns anything it finds in the
    * request parameters that can be put into the search,
    * supports the page params
    * 
    * @param req a servlet request
    * @return a search filter object
    */
   @SuppressWarnings("unchecked")
   public Search makeSearchFromRequest(HttpServletRequest req) {
      Search search = new Search();
      int page = -1;
      int limit = -1;
      try {
         if (req != null) {
            Map<String, String[]> params = req.getParameterMap();
            if (params != null) {
               for (String key : params.keySet()) {
                  if (POST_METHOD.equals(key) 
                        || ORIGINAL_METHOD.equals(key)) {
                     // skip the method
                     continue;
                  }
                  Object value = null;
                  String[] values = req.getParameterValues(key);
                  if (values == null) {
                     // in theory this should not happen
                     continue;
                  } else if (values.length > 1) {
                     value = values;
                  } else if (values.length == 1) {
                     value = values[0];
                     // get paging values out if possible
                     if ("_limit".equals(key) 
                           || "_perpage".equals(key)
                           || ":perpage".equals(key)) {
                        try {
                           limit = Integer.valueOf(value.toString()).intValue();
                           search.setLimit(limit);
                        } catch (NumberFormatException e) {
                           log.warn("Invalid non-number passed in for _limit/_perpage param: " + value, e);
                        }
                     } else if ("_start".equals(key)) {
                        try {
                           int start = Integer.valueOf(value.toString()).intValue();
                           search.setStart(start);
                        } catch (NumberFormatException e) {
                           log.warn("Invalid non-number passed in for '_start' param: " + value, e);
                        }
                     } else if ("_page".equals(key)
                           || ":page".equals(key)) {
                        try {
                           page = Integer.valueOf(value.toString()).intValue();
                        } catch (NumberFormatException e) {
                           log.warn("Invalid non-number passed in for '_page' param: " + value, e);
                        }
                     }
                  }
                  search.addRestriction( new Restriction(key, value) );
               }
            }
         }
      } catch (Exception e) {
         // failed to translate the request to a search, not really much to do here
         log.warn("Could not translate entity request into search params: " + e.getMessage(), e);
      }
      // translate page into start/limit
      if (page > -1) {
         if (limit <= -1) {
            limit = 10; // set to a default value
            search.setLimit(limit);
            log.warn("page is set without a limit per page, setting per page limit to default value of 10");
         }
         search.setStart( page * limit );
      }
      return search;
   }



   /**
    * This will set the response mime type correctly based on the format constant,
    * also sets the response encoding to UTF_8
    * @param format the format constant, example {@link Formats#XML}
    * @param res the current outgoing response
    */
   protected void setResponseEncoding(String format, HttpServletResponse res) {
      String encoding;
      if (Formats.XML.equals(format)) {
         encoding = Formats.XML_MIME_TYPE;
      } else if (Formats.HTML.equals(format)) {
         encoding = Formats.HTML_MIME_TYPE;
      } else if (Formats.JSON.equals(format)) {
         encoding = Formats.JSON_MIME_TYPE;
      } else if (Formats.RSS.equals(format)) {
         encoding = Formats.RSS_MIME_TYPE;                        
      } else if (Formats.ATOM.equals(format)) {
         encoding = Formats.ATOM_MIME_TYPE;                        
      } else {
         encoding = Formats.TXT_MIME_TYPE;
      }
      res.setContentType(encoding);
      res.setCharacterEncoding(Formats.UTF_8);
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

}
