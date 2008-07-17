/**
 * $Id$
 * $URL$
 * ServerConfigEntityProvider.java - entity-broker - Jul 17, 2008 2:19:03 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;


/**
 * This provides access to the server configuration as entities,
 * output access only though, no setting of configuration
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class ServerConfigEntityProvider implements EntityProvider, Outputable, Resolvable, CollectionResolvable, AutoRegisterEntityProvider {

   public String[] includedStringSettings = new String[] {
         "portalPath",
         "version.service",
         "version.sakai",
         "locales",
         "force.url.secure",
         "skin.default",
         "skin.repo",
         "ui.institution",
         "ui.service"
      };

   public String[] includedBooleanSettings = new String[] {
         "auto.ddl",
         "display.users.present"
      };

   private ServerConfigurationService serverConfigurationService;
   public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
      this.serverConfigurationService = serverConfigurationService;
   }

   public static String PREFIX = "server-config";
   public String getEntityPrefix() {
      return PREFIX;
   }

   public Object getEntity(EntityReference ref) {
      if (ref.getId() == null) {
         return new EntitySession();
      }
      String name = ref.getId();
      Object value = getConfig(name);
      if (value == null) {
         throw new IllegalArgumentException("Cannot find server config setting with name: " + name);
      }
      EntityServerConfig esc = new EntityServerConfig(name, value, value.getClass().getName());
      return esc;
   }

   public List<?> getEntities(EntityReference ref, Search search) {
      List<EntityServerConfig> escs = new ArrayList<EntityServerConfig>();
      if (search != null 
            && ! search.isEmpty() 
            && search.getRestrictionByProperty("name") != null) {
         String name = (String) search.getRestrictionByProperty("name").value;
         Object value = getConfig(name);
         if (value != null) {
            EntityServerConfig esc = new EntityServerConfig(name, value, value.getClass().getName());
            escs.add(esc);
         }
      } else {
         Map<String, Object> known = getKnownSettings();
         for (Entry<String, Object> entry : known.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
               EntityServerConfig esc = new EntityServerConfig(name, value, value.getClass().getName());
               escs.add(esc);
            }
         }
      }
      Collections.sort(escs, new ESCComparator());
      return escs;
   }

   public String[] getHandledOutputFormats() {
      return new String[] { Formats.XML, Formats.JSON };
   }

   
   public Object getConfig(String name) {
      Object value = null;
      // check in the local group of settings first
      Map<String, Object> known = getKnownSettings();
      if (known.containsKey(name)) {
         value = known.get(name);
      } else {
         // now check the service
         value = serverConfigurationService.getString(name);
         if (value == null) {
            // could not find it so get the array instead
            value = serverConfigurationService.getStrings(name);
         } else {
            String sValue = (String) value;
            if (sValue.length() > 0) {
               // try to convert this to an integer first
               try {
                  Integer i = Integer.parseInt(sValue);
                  value = i.intValue();
               } catch (NumberFormatException e) {
                  // next try to convert it to a boolean
                  if (sValue.equalsIgnoreCase("true")) {
                     value = true;
                  } else if (sValue.equalsIgnoreCase("false")) {
                     value = false;
                  }
               }
               // otherwise leave it as a string
            }
         }
      }
      return value;
   }

   /**
    * Retrieves the known values in SCS which are not actually strings and properties
    * @return a map of name -> value
    */
   public Map<String, Object> getKnownSettings() {
      Map<String, Object> m = new HashMap<String, Object>();
      m.put("accessPath", serverConfigurationService.getAccessPath());
      m.put("accessUrl", serverConfigurationService.getAccessUrl());
      m.put("gatewaySiteId", serverConfigurationService.getGatewaySiteId());
      m.put("loggedOutUrl", serverConfigurationService.getLoggedOutUrl());
      m.put("portalUrl", serverConfigurationService.getPortalUrl());
      m.put("sakaiHomePath", serverConfigurationService.getSakaiHomePath());
      m.put("serverId", serverConfigurationService.getServerId());
      m.put("serverIdInstance", serverConfigurationService.getServerIdInstance());
      m.put("serverInstance", serverConfigurationService.getServerInstance());
      m.put("serverName", serverConfigurationService.getServerName());
      m.put("serverUrl", serverConfigurationService.getServerUrl());
      m.put("toolUrl", serverConfigurationService.getToolUrl());
      m.put("userHomeUrl", serverConfigurationService.getUserHomeUrl());
      // now we get the known String settings
      for (int i = 0; i < includedStringSettings.length; i++) {
         String name = includedStringSettings[i];
         String value = serverConfigurationService.getString(name);
         if (value != null) {
            m.put(name, value);
         }
      }
      // now get the known boolean settings
      for (int i = 0; i < includedBooleanSettings.length; i++) {
         String name = includedBooleanSettings[i];
         boolean value = serverConfigurationService.getBoolean(name, false);
         m.put(name, value);
      }
      return m;
   }

   public static class ESCComparator implements Comparator<EntityServerConfig> {
      public int compare(EntityServerConfig o1, EntityServerConfig o2) {
         return o1.getName().compareTo(o2.getName());
      }
   }
}
