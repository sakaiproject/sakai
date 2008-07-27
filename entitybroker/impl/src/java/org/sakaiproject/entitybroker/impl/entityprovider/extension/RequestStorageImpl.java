/**
 * $Id$
 * $URL$
 * RequestStorageImpl.java - entity-broker - Jul 24, 2008 2:26:09 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl.entityprovider.extension;

import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;


/**
 * Impl for the request store
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@SuppressWarnings("unchecked")
public class RequestStorageImpl implements RequestStorage {

   private ThreadLocal<ConcurrentHashMap<String, Object>> requestStore = new ThreadLocal<ConcurrentHashMap<String,Object>>();
   protected ConcurrentHashMap<String, Object> getInternalMap() {
      if (requestStore.get() == null) {
         requestStore.set( new ConcurrentHashMap<String, Object>() );
      }
      return requestStore.get();
   }

   private RequestGetter requestGetter;
   public void setRequestGetter(RequestGetter requestGetter) {
      this.requestGetter = requestGetter;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage#inRequest()
    */
   public boolean inRequest() {
      boolean in = false;
      if (requestGetter.getRequest() != null) {
         in = true;
      }
      return in;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage#getStorageMapCopy()
    */
   public Map<String, Object> getStorageMapCopy() {
      HashMap<String, Object> m = new HashMap<String, Object>();
      m.putAll( getRequestValues() ); // put in the request ones first
      m.putAll( getInternalMap() );
      return m;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage#getStoredValue(java.lang.String)
    */
   public Object getStoredValue(String key) {
      if (key == null) {
         throw new IllegalArgumentException("key must be non-null");
      }
      return getRequestValue(key);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage#setStoredValue(java.lang.String, java.lang.Object)
    */
   public void setStoredValue(String key, Object value) {
      if (key == null || value == null) {
         throw new IllegalArgumentException("key and value must be non-null");
      }
      for (int i = 0; i < ReservedKeys.values().length; i++) {
         String rKey = ReservedKeys.values()[i].name();
         if (rKey.equals(key)) {
            throw new IllegalArgumentException("Cannot change the value of a reserved key: " + rKey);
         }
      }
      setRequestValue(key, value);
   }

   /**
    * Resets the request storage and purges all stored values (has no effect on the data in the request)
    */
   public void reset() {
      getInternalMap().clear();
      HttpServletRequest request = requestGetter.getRequest();
      if (request != null) {
         Enumeration<String> aEnum = request.getAttributeNames();
         if (aEnum != null) {
            for (Enumeration<String> e = aEnum ; e.hasMoreElements() ;) {
               String key = e.nextElement();
               request.removeAttribute(key);
            }
         }
      }
   }

   /**
    * Allows use to set the value of a key directly, including reserved keys
    * @param key
    * @param value
    */
   public void setRequestValue(String key, Object value) {
      HttpServletRequest request = requestGetter.getRequest();
      if (request != null) {
         request.setAttribute(key, value);
      } else {
         getInternalMap().put(key, value);
      }
   }

   protected Object getRequestValue(String key) {
      Object value = getInternalMap().get(key);
      if (value == null) {
         value = getRequestValues().get(key);
      }
      if (value == null) {
         // perhaps get one of the reserved values
         if (ReservedKeys._locale.name().equals(key)) {
            // _locale
            value = Locale.getDefault();
         } else if (ReservedKeys._requestEntityReference.name().equals(key)) {
            // _requestEntityReference
            value = "/describe"; // default if not known
         } else if (ReservedKeys._requestActive.name().equals(key)) {
            // _requestActive
            if (requestGetter.getRequest() == null) {
               value = false;
            } else {
               value = true;
            }
         } else if (ReservedKeys._requestOrigin.name().equals(key)) {
            // _requestOrigin
            value = RequestOrigin.INTERNAL.name();
         }
      }
      return value;
   }

   protected Map<String, Object> getRequestValues() {
      HttpServletRequest request = requestGetter.getRequest();
      return getRequestValues(request);
   }

   public class EntryComparator implements Comparator<Entry<String, Object>> {
      public int compare(Entry<String, Object> o1, Entry<String, Object> o2) {
         return o1.getKey().compareTo(o2.getKey());
      }
   }

   // STATIC

   public static Map<String, Object> getRequestValues(HttpServletRequest request) {
      HashMap<String, Object> m = new HashMap<String, Object>();
      if (request != null) {
         Enumeration<String> headerEnum = request.getHeaderNames();
         if (headerEnum != null) {
            for (Enumeration<String> e = headerEnum ; e.hasMoreElements() ;) {
               String key = e.nextElement();
               Object value = request.getHeader(key);
               m.put(key, value);
            }
         }
         Map<String, String[]> pMap = request.getParameterMap();
         if (pMap != null) {
            for (Entry<String, String[]> param : pMap.entrySet()) {
               if (param.getValue().length > 1) {
                  m.put(param.getKey(), param.getValue());
               } else {
                  String value = "";
                  if (param.getValue().length == 1) {
                     value = param.getValue()[0];
                  }
                  m.put(param.getKey(), value);
               }
            }
         }
         Enumeration<String> aEnum = request.getAttributeNames();
         if (aEnum != null) {
            for (Enumeration<String> e = aEnum ; e.hasMoreElements() ;) {
               String key = e.nextElement();
               Object value = request.getAttribute(key);
               m.put(key, value);
            }
         }
         // encode a select set of values from the request
         m.put("_locale", request.getLocale());
         m.put("method", request.getMethod().toUpperCase());
         m.put("queryString", request.getQueryString() == null ? "" : request.getQueryString());
         m.put("pathInfo", request.getPathInfo() == null ? "" : request.getPathInfo());
      }
      return m;
   }

}
