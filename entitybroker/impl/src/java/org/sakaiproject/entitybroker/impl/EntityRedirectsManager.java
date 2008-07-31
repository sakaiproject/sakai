/**
 * $Id$
 * $URL$
 * EntityRedirectsManager.java - entity-broker - Jul 26, 2008 9:58:00 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityURLRedirect;
import org.sakaiproject.entitybroker.entityprovider.capabilities.URLConfigControllable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.URLConfigDefinable;
import org.sakaiproject.entitybroker.entityprovider.extension.TemplateMap;
import org.sakaiproject.entitybroker.impl.util.URLRedirect;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.entitybroker.util.TemplateParseUtil.PreProcessedTemplate;
import org.sakaiproject.entitybroker.util.TemplateParseUtil.ProcessedTemplate;
import org.sakaiproject.entitybroker.util.reflect.ReflectUtil;


/**
 * Handles everything related the URL redirects handling and processing
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityRedirectsManager {

   private static Log log = LogFactory.getLog(EntityRedirectsManager.class);

   private Map<String, List<URLRedirect>> entityRedirects = new ConcurrentHashMap<String, List<URLRedirect>>();

   /**
    * Do a check to see if the current incoming URL is a match to any of the redirect templates for
    * the given entity provider
    * @param entityProvider an entity provider
    * @param incomingUrl the incoming URL to try to match
    * @return the URL to redirect to OR null if no matches were found
    */
   public String checkForTemplateMatch(EntityProvider entityProvider, String incomingURL) {
      String prefix = entityProvider.getEntityPrefix();
      String targetURL = null;
      List<URLRedirect> redirects = getURLRedirects(prefix);
      if (redirects.size() > 0) {
         List<PreProcessedTemplate> preprocessed = new ArrayList<PreProcessedTemplate>();
         for (URLRedirect redirect : redirects) {
            preprocessed.add( redirect.preProcessedTemplate );
         }
         ProcessedTemplate processedTemplate;
         try {
            processedTemplate = TemplateParseUtil.parseTemplate(incomingURL, preprocessed);
         } catch (IllegalArgumentException e) {
            processedTemplate = null;
         }
         if (processedTemplate != null) {
            URLRedirect redirect = null;
            for (URLRedirect urlRedirect : redirects) {
               if (processedTemplate.template.equals(urlRedirect.template)) {
                  // found the matching urlRedirect
                  redirect = urlRedirect;
                  break;
               }
            }

            if (redirect == null) {
               // TODO should this be a warning instead?
               throw new IllegalStateException("Failed to find a matching redirect for the matched template ("+processedTemplate.template+") for the incoming URL ("+incomingURL+")");
            } else {
               Map<String, String> segmentValues = new HashMap<String, String>( processedTemplate.segmentValues );
               segmentValues.put(TemplateParseUtil.PREFIX, prefix);
               if (redirect.controllable) {
                  // call the executable
                  if (URLConfigControllable.class.isAssignableFrom(entityProvider.getClass())) {
                     targetURL = ((URLConfigControllable)entityProvider).handleRedirects(processedTemplate.template, 
                           incomingURL, 
                           processedTemplate.variableNames.toArray(new String[processedTemplate.variableNames.size()]), 
                           segmentValues);
                  } else {
                     throw new IllegalStateException("Invalid URL Redirect Object, marked as controllable when this entity broker does not have the capability: " + URLConfigControllable.class);
                  }
               } else if (redirect.methodName != null) {
                  // call the redirect method
                  Object result = null;
                  Method method = null;
                  try {
                     method = entityProvider.getClass().getMethod(redirect.methodName, redirect.methodArgTypes);
                  } catch (SecurityException e1) {
                     throw new RuntimeException("Fatal error trying to get URL redirect method: " + redirect, e1);
                  } catch (NoSuchMethodException e1) {
                     throw new RuntimeException("Fatal error trying to get URL redirect method: " + redirect, e1);
                  }
                  Object[] args = new Object[redirect.methodArgTypes.length];
                  for (int i = 0; i < redirect.methodArgTypes.length; i++) {
                     Class<?> argType = redirect.methodArgTypes[i];
                     if (String.class.equals(argType)) {
                        args[i] = incomingURL;
                     } else if (String[].class.equals(argType)) {
                        args[i] = processedTemplate.variableNames.toArray(new String[processedTemplate.variableNames.size()]);
                     } else if (Map.class.equals(argType)) {
                        args[i] = segmentValues;
                     } else {
                        throw new IllegalStateException("URL redirect method ("+redirect+") contains an invalid methodArgTypes, " +
                              "only valid types allowed: " + ReflectUtil.arrayToString(validParamTypes));
                     }
                  }
                  try {
                     result = method.invoke(entityProvider, args);
                  } catch (IllegalArgumentException e) {
                     throw new RuntimeException("Fatal error trying to execute URL redirect method: " + redirect, e);
                  } catch (IllegalAccessException e) {
                     throw new RuntimeException("Fatal error trying to execute URL redirect method: " + redirect, e);
                  } catch (InvocationTargetException e) {
                     if (e.getCause() != null) {
                        if (e.getCause().getClass().isAssignableFrom(IllegalArgumentException.class)) {
                           throw new IllegalArgumentException(e.getCause().getMessage() + " (rethrown)", e.getCause());
                        } else if (e.getCause().getClass().isAssignableFrom(IllegalStateException.class)) {
                           throw new IllegalStateException(e.getCause().getMessage() + " (rethrown)", e.getCause());
                        }
                     }
                     throw new RuntimeException("Fatal error trying to execute URL redirect method: " + redirect, e);
                  }
                  if (result != null) {
                     targetURL = result.toString();
                  } else {
                     targetURL = null;
                  }
               } else if (redirect.outgoingTemplate != null) {
                  // handle the straight processing
                  try {
                     targetURL = TemplateParseUtil.mergeTemplate(redirect.outgoingTemplate, segmentValues);
                  } catch (IllegalArgumentException e) {
                     targetURL = null;
                     log.warn("Unable to merge target template ("+redirect.outgoingTemplate+") with available variables: " + e.getMessage());
                  }
               } else {
                  // should never get here
                  throw new IllegalStateException("Invalid URL Redirect Object, could not determine operation: " + redirect);
               }
            }
         }
      }
      if (targetURL != null && targetURL.length() > 0) {
         // fix up the outgoing URL if needed (must end up non-relative)
         if (targetURL.charAt(0) == TemplateParseUtil.SEPARATOR 
               || targetURL.startsWith("http:") || targetURL.startsWith("https:")) {
            // leave it as is
         } else {
            targetURL = TemplateParseUtil.DIRECT_PREFIX + TemplateParseUtil.SEPARATOR + targetURL;
         }
      }
      return targetURL;
   }

   /**
    * Looks for redirect methods in the given entity provider
    * @param entityProvider an entity provider
    * @return an array of redirect objects
    * @throws IllegalArgumentException if the methods are setup incorrectly
    */
   public URLRedirect[] findURLRedirectMethods(EntityProvider entityProvider) {
      ArrayList<URLRedirect> redirects = new ArrayList<URLRedirect>();
      Method[] methods = entityProvider.getClass().getMethods();
      for (Method method : methods) {
         if (method.isAnnotationPresent(EntityURLRedirect.class)) {
            EntityURLRedirect eurAnnote = method.getAnnotation(EntityURLRedirect.class);
            String template = eurAnnote.value();
            if (null == template || "".equals(template)) {
               throw new IllegalArgumentException("there is no template set for the annotation: " + EntityURLRedirect.class);
            }
            URLRedirect redirect = new URLRedirect(template, method.getName(), validateParamTypes(method.getParameterTypes()));
            redirects.add(redirect);
         }
      }
      return redirects.toArray(new URLRedirect[redirects.size()]);
   }

   /**
    * Validates the provided URL templates in an entity provider and outputs the
    * URL redirect objects as an array
    * @param configDefinable the entity provider
    * @return the array of URL redirects
    */
   public static URLRedirect[] validateDefineableTemplates(URLConfigDefinable configDefinable) {
      List<URLRedirect> redirects = new ArrayList<URLRedirect>();
      TemplateMap[] urlMappings = configDefinable.defineURLMappings();
      if (urlMappings == null || urlMappings.length == 0) {
         // this is ok then, or is it?
         log.warn("URLConfigDefinable: no templates defined for url redirect");
      } else {
         for (TemplateMap templateMap : urlMappings) {
            String incomingTemplate = templateMap.getIncomingTemplate();
            String outgoingTemplate = templateMap.getOutgoingTemplate();
            URLRedirect redirect = new URLRedirect(incomingTemplate, outgoingTemplate);
            // make sure that we check the target vars match the template vars
            List<String> incomingVars = new ArrayList<String>( redirect.preProcessedTemplate.variableNames );
            incomingVars.add(TemplateParseUtil.PREFIX);
            List<String> outgoingVars = redirect.outgoingPreProcessedTemplate.variableNames;
            if (incomingVars.containsAll(outgoingVars)) {
               // all is ok
               redirects.add(redirect);
            } else {
               throw new IllegalArgumentException("Outgoing template ("+outgoingTemplate+") has variables which do not occur in "
               		+ "incoming template ("+incomingTemplate+") and " + TemplateParseUtil.PREFIX 
               		+ ", please make sure your outgoing template only includes variables"
               		+ " which can be found in the incoming template and " + TemplateParseUtil.PREFIX);
            }
         }
      }
      return redirects.toArray(new URLRedirect[redirects.size()]);
   }

   /**
    * Execute this validate and get the templates so they can be registered
    * @param configControllable the entity provider
    * @return the array of URL redirects
    */
   public static URLRedirect[] validateControllableTemplates(URLConfigControllable configControllable) {
      List<URLRedirect> redirects = new ArrayList<URLRedirect>();
      String[] templates = configControllable.defineHandledTemplatePatterns();
      if (templates == null || templates.length == 0) {
         throw new IllegalArgumentException("URLConfigControllable: invalid defineHandledTemplatePatterns: " +
         		"this should return a non-empty array of templates or the capability should not be used");
      } else {
         for (String template : templates) {
            URLRedirect redirect = new URLRedirect(template);
            redirects.add(redirect);
         }
      }
      return redirects.toArray(new URLRedirect[redirects.size()]);
   }

   protected static Class<?>[] validParamTypes = {
      String.class,
      String[].class,
      Map.class
   };

   /**
    * Validates the parameter types on a method to make sure they are valid
    * @param paramTypes an array of parameter types
    * @return the new valid array of param types
    * @throws IllegalArgumentException if the param types are invalid
    */
   protected static Class<?>[] validateParamTypes(Class<?>[] paramTypes) {
      Class<?>[] validParams = new Class<?>[paramTypes.length];
      for (int i = 0; i < paramTypes.length; i++) {
         boolean found = false;
         Class<?> paramType = paramTypes[i];
         for (int j = 0; j < validParamTypes.length; j++) {
            if (validParamTypes[j].isAssignableFrom(paramType)) {
               validParams[i] = validParamTypes[j];
               found = true;
            }
         }
         if (!found) {
            throw new IllegalArgumentException("Invalid redirect method: param type is not allowed: " + paramType.getName() 
                  + " : valid types include: " + ReflectUtil.arrayToString(validParamTypes));
         }
      }
      return validParams;
   }

   /**
    * Add all URL redirects to the following prefix,
    * maintains any existing ones
    * @param prefix an entity prefix
    * @param redirects an array of redirects
    * @throws IllegalArgumentException if any of the URL redirects are invalid
    */
   public void addURLRedirects(String prefix, URLRedirect[] redirects) {
      if (redirects != null && redirects.length > 0) {
         ArrayList<URLRedirect> urlRedirects = new ArrayList<URLRedirect>();
         int templateKeys = 0;
         if (entityRedirects.containsKey(prefix)) {
            List<URLRedirect> current = entityRedirects.get(prefix);
            urlRedirects.addAll(current);
            templateKeys += urlRedirects.size();
         }
         StringBuilder sb = new StringBuilder();
         for (URLRedirect redirect : redirects) {
            if (redirect == null || redirect.template == null || "".equals(redirect.template)) {
               throw new IllegalArgumentException("url redirect and pattern template must not be null");
            }
            if (redirect.outgoingTemplate == null 
                  && redirect.methodName == null
                  && redirect.controllable == false) {
               throw new IllegalArgumentException("url redirect targetTemplate or methodName must not be null");            
            }
            if (sb.length() > 0) {
               sb.append(", ");
            }
            if (urlRedirects.contains(redirect)) {
               throw new IllegalArgumentException("Duplicate redirect template definition: " +
               		"The redirect set already contains this template: " + redirect.template + ", it cannot contain 2 identical templates");
            }
            urlRedirects.add(redirect);
            sb.append(redirect.template);
         }
         entityRedirects.put(prefix, urlRedirects);
         log.info("Registered "+redirects.length+" url redirects for entity prefix ("+prefix+"): " + sb.toString());
      }
   }

   /**
    * Remove any and all redirects for this prefix
    * @param prefix an entity prefix
    */
   public void removeURLRedirects(String prefix) {
      entityRedirects.remove(prefix);
   }

   /**
    * Get the list of all redirects for this prefix
    * @param prefix the entity prefix
    * @return a list of url redirects, may be empty if there are none
    */
   public List<URLRedirect> getURLRedirects(String prefix) {
      List<URLRedirect> redirects = new ArrayList<URLRedirect>();
      if (entityRedirects.containsKey(prefix)) {
         redirects.addAll( entityRedirects.get(prefix) );
      }
      return redirects;
   }

}
