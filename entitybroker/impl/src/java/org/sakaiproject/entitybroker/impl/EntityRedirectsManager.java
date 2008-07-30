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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityURLRedirect;
import org.sakaiproject.entitybroker.entityprovider.capabilities.URLConfigDefinable;
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
    * the given prefix
    * @param prefix an entity prefix
    * @param incomingUrl the incoming URL to try to match
    * @return a URL redirect with the processed data in it OR null if no matches were found
    */
   public URLRedirect checkForTemplateMatch(String prefix, String incomingUrl) {
      URLRedirect togo = null;
      List<URLRedirect> redirects = getURLRedirects(prefix);
      if (redirects.size() > 0) {
         List<PreProcessedTemplate> preprocessed = new ArrayList<PreProcessedTemplate>();
         for (URLRedirect redirect : redirects) {
            preprocessed.add( redirect.preProcessedTemplate );
         }
         ProcessedTemplate processedTemplate;
         try {
            processedTemplate = TemplateParseUtil.parseTemplate(incomingUrl, preprocessed);
         } catch (IllegalArgumentException e) {
            processedTemplate = null;
         }
         if (processedTemplate != null) {
            for (URLRedirect redirect : redirects) {
               if (processedTemplate.template.equals(redirect.template)) {
                  togo = redirect.copy();
                  redirect.processedTemplate = processedTemplate;
                  break;
               }
            }
         }
      }
      return togo;
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
            String template = eurAnnote.template();
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
    * Validates the provided url templates in an entity provider and outputs the
    * url redirect objects as an array
    * @param configDefinable the entity provider
    * @return the array of url redirects
    */
   public static URLRedirect[] validateRedirectTemplates(URLConfigDefinable configDefinable) {
      List<URLRedirect> redirects = new ArrayList<URLRedirect>();
      Map<String, String> urlMappings = configDefinable.defineURLMappings();
      if (urlMappings == null || urlMappings.isEmpty()) {
         // this is ok then
      } else {
         for (Entry<String, String> entry : urlMappings.entrySet()) {
            String template = entry.getKey();
            String target = entry.getValue();
            // TODO make sure that we check the target vars match the template vars
            URLRedirect redirect = new URLRedirect(template, target);
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
   public static Class<?>[] validateParamTypes(Class<?>[] paramTypes) {
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
         if (redirect.targetTemplate == null 
               || redirect.methodName == null) {
            throw new IllegalArgumentException("url redirect targetTemplate or methodName must not be null");            
         }
         if (sb.length() > 0) {
            sb.append(", ");
         }
         String templateKey = "key" + templateKeys++;
         redirect.preProcessedTemplate.templateKey = templateKey;
         sb.append(redirect.template);
         urlRedirects.add(redirect);
      }
      entityRedirects.put(prefix, urlRedirects);
      log.info("Registered "+redirects.length+" url redirects for entity prefix ("+prefix+"): " + sb.toString());
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
