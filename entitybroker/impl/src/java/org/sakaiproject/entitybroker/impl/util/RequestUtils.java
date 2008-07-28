/**
 * $Id$
 * $URL$
 * RequestUtils.java - entity-broker - Jul 28, 2008 7:41:28 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl.util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.EntityRequestHandler;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;


/**
 * Contains a set of static utility methods for working with requests
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class RequestUtils {

   private static Log log = LogFactory.getLog(RequestUtils.class);

   /**
    * This looks at search parameters and returns anything it finds in the
    * request parameters that can be put into the search,
    * supports the page params
    * 
    * @param req a servlet request
    * @return a search filter object
    */
   @SuppressWarnings("unchecked")
   public static Search makeSearchFromRequest(HttpServletRequest req) {
      Search search = new Search();
      int page = -1;
      int limit = -1;
      try {
         if (req != null) {
            Map<String, String[]> params = req.getParameterMap();
            if (params != null) {
               for (String key : params.keySet()) {
                  if (EntityRequestHandler.COMPENSATE_METHOD.equals(key) ) {
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
   public static void setResponseEncoding(String format, HttpServletResponse res) {
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

}
