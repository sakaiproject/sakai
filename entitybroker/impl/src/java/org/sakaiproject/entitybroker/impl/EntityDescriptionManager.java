/**
 * $Id$
 * $URL$
 * EntityDescriptionManager.java - entity-broker - Jul 22, 2008 12:18:48 PM - azeckoski
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityRequestHandler;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Deleteable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.DescribeDefineable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Updateable;
import org.sakaiproject.entitybroker.entityprovider.extension.CustomAction;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.impl.entityprovider.EntityPropertiesService;
import org.sakaiproject.entitybroker.impl.util.URLRedirect;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.entitybroker.util.reflect.ReflectUtil;


/**
 * This handles all the methods related to generating descriptions for entities,
 * html and xml currently supported
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityDescriptionManager {

   private static final String FIELD_KEY_PREFIX = "field.";
   private static final String REDIRECT_KEY_PREFIX = "redirect.";
   protected static String ACTION_KEY_PREFIX = "action.";
   protected static String DESCRIBE = EntityRequestHandler.DESCRIBE;
   protected static String SLASH_DESCRIBE = EntityRequestHandler.SLASH_DESCRIBE;
   protected static String FAKE_ID = EntityRequestHandler.FAKE_ID;

   protected static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
   protected static final String XHTML_HEADER = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" " +
   "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
   "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
   "<head>\n" +
   "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
   "  <title>Describe Entities</title>\n" +
   "</head>\n" +
   "<body>\n";
   protected static final String XHTML_FOOTER = "\n</body>\n</html>\n";

   
   private EntityProviderManager entityProviderManager;
   public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
      this.entityProviderManager = entityProviderManager;
   }

   private EntityPropertiesService entityProperties;
   public void setEntityProperties(EntityPropertiesService entityProperties) {
      this.entityProperties = entityProperties;
   }

   private EntityBrokerManager entityBrokerManager;
   public void setEntityBrokerManager(EntityBrokerManager entityBrokerManager) {
      this.entityBrokerManager = entityBrokerManager;
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
    * Generate a description of all entities in the system,
    * this is only available as XML and XHTML
    * 
    * @param format XML or HTML (default is HTML)
    * @return the description string for all known entities
    */
   public String makeDescribeAll(String format) {
      Map<String, List<Class<? extends EntityProvider>>> map = entityProviderManager.getRegisteredEntityCapabilities();
      String describeURL = entityBrokerManager.makeFullURL("") + SLASH_DESCRIBE;
      String output = "";
      if (Formats.XML.equals(format)) {
         // XML available in case someone wants to parse this in javascript or whatever
         StringBuilder sb = new StringBuilder();
         sb.append(XML_HEADER);
         sb.append("<describe>\n");
         sb.append("  <describeURL>" + describeURL + "</describeURL>\n");
         sb.append("  <prefixes>\n");
         ArrayList<String> prefixes = new ArrayList<String>(map.keySet());
         Collections.sort(prefixes);
         for (int i = 0; i < prefixes.size(); i++) {
            String prefix = prefixes.get(i);
            describeEntity(sb, prefix, FAKE_ID, format, false, map.get(prefix));
         }
         sb.append("  </prefixes>\n");
         sb.append("</describe>\n");
         output = sb.toString();
      } else {
         // just do HTML if not one of the handled ones
         Locale locale = entityProperties.getLocale();
         StringBuilder sb = new StringBuilder();
         sb.append(XML_HEADER);
         sb.append(XHTML_HEADER);
         sb.append("<h1><a href='"+ describeURL +"'>Describe all</a> registered entities"
               + makeFormatUrlHtml(describeURL, Formats.XML) +"</h1>\n");
         sb.append("  <i>RESTful URLs: <a href='http://microformats.org/wiki/rest/urls'>http://microformats.org/wiki/rest/urls</a></i><br/>\n");
         sb.append("  <h2>"+entityProperties.getProperty(DESCRIBE, "describe.all", locale)+" ("
               +entityProperties.getProperty(DESCRIBE, "describe.registered.entities", locale)+"): "
               +map.size()+"</h2>\n");
         ArrayList<String> prefixes = new ArrayList<String>(map.keySet());
         Collections.sort(prefixes);
         for (int i = 0; i < prefixes.size(); i++) {
            String prefix = prefixes.get(i);
            describeEntity(sb, prefix, FAKE_ID, format, false, map.get(prefix));
         }
         sb.append(XHTML_FOOTER);
         output = sb.toString();
      }
      return output;
   }

   /**
    * Generate a description of an entity type
    * 
    * @param prefix an entity prefix
    * @param id the entity id to use for generating URLs
    * @param format a format to output, HTML and XML supported
    * @return the description string
    * @throws IllegalArgumentException if the entity does not exist
    */
   public String makeDescribeEntity(String prefix, String id, String format) {
      if (entityProviderManager.getProviderByPrefix(prefix) == null) {
         throw new IllegalArgumentException("Invalid prefix ("+prefix+"), entity with that prefix does not exist");
      }
      StringBuilder sb = new StringBuilder();
      if (Formats.XML.equals(format)) {
         sb.append(XML_HEADER);
         describeEntity(sb, prefix, id, format, true, null);
      } else {
         // just do HTML if not one of the handled ones
         sb.append(XML_HEADER);
         sb.append(XHTML_HEADER);
         describeEntity(sb, prefix, id, format, true, null);
         sb.append(XHTML_FOOTER);
      }
      return sb.toString();
   }

   /**
    * This is reducing code duplication
    * @param sb
    * @param prefix
    * @param id
    * @param format
    * @param extra
    * @param caps
    * @return
    */
   protected String describeEntity(StringBuilder sb, String prefix, String id, String format, boolean extra, List<Class<? extends EntityProvider>> caps) {
      if (caps == null) {
         caps = entityProviderManager.getPrefixCapabilities(prefix);
      }
      String directUrl = entityBrokerManager.makeFullURL("");
      if (Formats.XML.equals(format)) {
         // XML available in case someone wants to parse this in javascript or whatever
         String describePrefixUrl = directUrl + "/" + prefix + SLASH_DESCRIBE;
         sb.append("    <prefix>\n");
         sb.append("      <prefix>" + prefix + "</prefix>\n");
         sb.append("      <describeURL>" + describePrefixUrl + "</describeURL>\n");
         String description = getEntityDescription(prefix, null);
         if (description != null) {
            sb.append("      <description>" + description + "</description>\n");            
         }
         if (extra) {
            // URLs
            EntityView ev = entityBrokerManager.makeEntityView(new EntityReference(prefix, id), null, null);
            if (caps.contains(CollectionResolvable.class)) {
               sb.append("      <collectionURL>" + ev.getEntityURL(EntityView.VIEW_LIST, null) + "</collectionURL>\n");
            }
            if (caps.contains(Createable.class)) {
               sb.append("      <createURL>" + ev.getEntityURL(EntityView.VIEW_NEW, null) + "</createURL>\n");
            }
            sb.append("      <showURL>" + ev.getEntityURL(EntityView.VIEW_SHOW, null) + "</showURL>\n");
            if (caps.contains(Updateable.class)) {
               sb.append("      <updateURL>" + ev.getEntityURL(EntityView.VIEW_EDIT, null) + "</updateURL>\n");
            }
            if (caps.contains(Deleteable.class)) {
               sb.append("      <deleteURL>" + ev.getEntityURL(EntityView.VIEW_DELETE, null) + "</deleteURL>\n");
            }
            // Custom Actions
            List<CustomAction> customActions = entityActionsManager.getCustomActions(prefix);
            if (! customActions.isEmpty()) {
               for (CustomAction customAction : customActions) {
                  sb.append("      <customActions>\n");
                  sb.append("        <customAction>\n");
                  sb.append("          <action>"+customAction.action+"</action>\n");
                  sb.append("          <url>"+directUrl+makeActionURL(ev, customAction)+"</url>\n");
                  if (customAction.viewKey == null) {
                     sb.append("          <method/>\n");
                     sb.append("          <viewKey/>\n");
                  } else {
                     sb.append("          <method>"+EntityView.translateViewKeyToMethod(customAction.viewKey)+"</method>\n");
                     sb.append("          <viewKey>"+customAction.viewKey+"</viewKey>\n");
                  }
                  String actionDesc = getEntityDescription(prefix, ACTION_KEY_PREFIX + customAction.action);
                  if (actionDesc != null) {
                     sb.append("          <description>"+actionDesc+"</description>\n");
                  }
                  sb.append("        </customAction>\n");
                  sb.append("      </customActions>\n");               
               }
            }
            // Formats
            String[] outputFormats = getFormats(prefix, true);
            sb.append("      <outputFormats>\n");
            for (int i = 0; i < outputFormats.length; i++) {
               sb.append("        <format>"+outputFormats[i]+"</format>\n");               
            }
            sb.append("      </outputFormats>\n");
            String[] inputFormats = getFormats(prefix, false);
            sb.append("      <inputFormats>\n");
            for (int i = 0; i < inputFormats.length; i++) {
               sb.append("        <format>"+inputFormats[i]+"</format>\n");               
            }
            sb.append("      </inputFormats>\n");
            // Resolvable Entity Info
            Object entity = getSampleEntityObject(prefix);
            if (entity != null) {
               sb.append("      <entityClass>\n");
               sb.append("        <class>"+ entity.getClass().getName() +"</class>\n");
               sb.append("        <fields>\n");
               Map<String, Class<?>> entityTypes = entityBrokerManager.getReflectUtil().getFieldTypes(entity.getClass());
               ArrayList<String> keys = new ArrayList<String>(entityTypes.keySet());
               Collections.sort(keys);
               for (String key : keys) {
                  Class<?> type = entityTypes.get(key);
                  sb.append("          <field>\n");
                  sb.append("            <name>"+ key +"</name>\n");
                  sb.append("            <type>"+ type.getName() +"</type>\n");
                  String fieldDesc = getEntityDescription(prefix, FIELD_KEY_PREFIX + key);
                  if (fieldDesc != null) {
                     sb.append("            <description>"+ fieldDesc +"</description>\n");
                  }
                  sb.append("          </field>\n");
               }
               sb.append("        </fields>\n");
               sb.append("      </entityClass>\n");
            }
            // Redirects
            List<URLRedirect> redirects = entityRedirectsManager.getURLRedirects(prefix);
            if (! redirects.isEmpty()) {
               sb.append("      <redirects>\n");
               for (int i = 0; i < redirects.size(); i++) {
                  URLRedirect redirect = redirects.get(i);
                  sb.append("        <redirect>\n");
                  sb.append("          <template>"+redirect.template+"</template>\n");
                  if (redirect.outgoingTemplate != null) {
                     sb.append("          <outgoingTemplate>"+redirect.outgoingTemplate+"</outgoingTemplate>\n");
                  }
                  if (redirect.methodName != null) {
                     sb.append("          <methodName>"+redirect.methodName+"</methodName>\n");
                  }
                  String redirectDesc = getEntityDescription(prefix, REDIRECT_KEY_PREFIX + redirect.template);
                  if (redirectDesc != null) {
                     sb.append("          <description>"+redirectDesc+"</description>\n");
                  }
                  sb.append("          <controllable>"+redirect.controllable+"</controllable>\n");
                  sb.append("          <order>"+i+"</order>\n");
                  sb.append("        </redirect>\n");
               }
               sb.append("      </redirects>\n");
            }
         }
         // now capabilities
         sb.append("      <capabilities>\n");
         for (Class<? extends EntityProvider> class1 : caps) {
            sb.append("        <capability>\n");
            sb.append("          <name>"+class1.getSimpleName()+"</name>\n");
            sb.append("          <type>"+class1.getName()+"</type>\n");
            if (extra) {
               String capabilityDescription = getEntityDescription(prefix, class1.getSimpleName());
               if (capabilityDescription != null) {
                  sb.append("          <description>" + capabilityDescription + "</description>\n");                  
               }
            }
            sb.append("        </capability>\n");
         }
         sb.append("      </capabilities>\n");
         sb.append("    </prefix>\n");
      } else {
         Locale locale = entityProperties.getLocale();
         // just do HTML if not one of the handled ones
         String describePrefixUrl = directUrl + "/" + prefix + SLASH_DESCRIBE;
         sb.append("    <h3><a href='"+describePrefixUrl+"'>"+prefix+"</a>"
               + makeFormatUrlHtml(describePrefixUrl, Formats.XML) +"</h3>\n");
         String description = getEntityDescription(prefix, null);
         if (description != null) {
            sb.append("      <div style='font-style: italics; padding-left:0.5em; padding-bottom:0.4em; width:90%;'>" + description + "</div>\n");
         }
         if (extra) {
            sb.append("      <div style='font-style: italics; padding-left:1em;'>" +
                  "RESTful URLs: <a href='http://microformats.org/wiki/rest/urls'>http://microformats.org/wiki/rest/urls</a></div>\n");
            String[] outputFormats = getFormats(prefix, true);
            // URLs
            EntityView ev = entityBrokerManager.makeEntityView(new EntityReference(prefix, id), null, null);
            String url = "";
            sb.append("      <h4 style='padding-left:0.5em;'>"+entityProperties.getProperty(DESCRIBE, "describe.entity.sample.urls", locale)
                  +" (_id='"+id+"') ["
                  +entityProperties.getProperty(DESCRIBE, "describe.entity.may.be.invalid", locale)+"]:</h4>\n");
            sb.append("        <ul>\n");
            if (caps.contains(CollectionResolvable.class)) {
               url = ev.getEntityURL(EntityView.VIEW_LIST, null);
               sb.append("          <li>"+entityProperties.getProperty(DESCRIBE, "describe.entity.collection.url", locale)+": <a href='"+ directUrl+url +"'>"+url+"<a/>"
                     + makeFormatsUrlHtml(directUrl+url, outputFormats) +"</li>\n");
            }
            if (caps.contains(Createable.class)) {
               url = ev.getEntityURL(EntityView.VIEW_NEW, null);
               sb.append("          <li>"+entityProperties.getProperty(DESCRIBE, "describe.entity.create.url", locale)+": <a href='"+ directUrl+url +"'>"+url+"<a/></li>\n");
            }
            url = ev.getEntityURL(EntityView.VIEW_SHOW, null);
            sb.append("          <li>"+entityProperties.getProperty(DESCRIBE, "describe.entity.show.url", locale)+": <a href='"+ directUrl+url +"'>"+url+"<a/>"
                  + makeFormatsUrlHtml(directUrl+url, outputFormats) +"</li>\n");
            if (caps.contains(Updateable.class)) {
               url = ev.getEntityURL(EntityView.VIEW_EDIT, null);
               sb.append("          <li>"+entityProperties.getProperty(DESCRIBE, "describe.entity.update.url", locale)+": <a href='"+ directUrl+url +"'>"+url+"<a/></li>\n");
            }
            if (caps.contains(Deleteable.class)) {
               url = ev.getEntityURL(EntityView.VIEW_DELETE, null);
               sb.append("          <li>"+entityProperties.getProperty(DESCRIBE, "describe.entity.delete.url", locale)+": <a href='"+ directUrl+url +"'>"+url+"<a/></li>\n");
            }
            sb.append("        </ul>\n");
            // Custom Actions
            List<CustomAction> customActions = entityActionsManager.getCustomActions(prefix);
            if (! customActions.isEmpty()) {
               sb.append("      <h4 style='padding-left:0.5em;'>"+entityProperties.getProperty(DESCRIBE, "describe.custom.actions", locale)+"</h4>\n");
               sb.append("      <div style='padding-left:1em;padding-bottom:1em;'>\n");
               for (CustomAction customAction : customActions) {
                  sb.append("        <div>\n");
                  sb.append("          <a style='font-weight:bold;' href='"+directUrl+makeActionURL(ev, customAction)+"'>"
                        +customAction.action+"</a> : " + "<span>"+makeCustomActionKeyMethodText(customAction)+"</span><br/>\n");
                  String actionDesc = getEntityDescription(prefix, ACTION_KEY_PREFIX + customAction.action);
                  if (actionDesc != null) {
                     sb.append("          <div style='font-style:italic;font-size:0.9em;padding-left:1.5em;'>"+actionDesc+"</div>\n");
                  }
                  sb.append("        </div>\n");
               }
               sb.append("      </div>\n");
            }
            // Formats
            sb.append("      <h4 style='padding-left:0.5em;'>"+entityProperties.getProperty(DESCRIBE, "describe.entity.output.formats", locale)+" : "+ makeFormatsString(outputFormats) +"</h4>\n");
            String[] inputFormats = getFormats(prefix, false);
            sb.append("      <h4 style='padding-left:0.5em;'>"+entityProperties.getProperty(DESCRIBE, "describe.entity.input.formats", locale)+" : "+ makeFormatsString(inputFormats) +"</h4>\n");
            // Resolvable Entity Info
            Object entity = getSampleEntityObject(prefix);
            if (entity != null) {
               sb.append("      <h4 style='padding-left:0.5em;'>"+entityProperties.getProperty(DESCRIBE, "describe.entity.class", locale)+" : "+ entity.getClass().getName() +"</h4>\n");
               sb.append("      <div style='padding-left:1em;padding-bottom:1em;'>\n");
               Map<String, Class<?>> entityTypes = entityBrokerManager.getReflectUtil().getFieldTypes(entity.getClass());
               ArrayList<String> keys = new ArrayList<String>(entityTypes.keySet());
               Collections.sort(keys);
               for (int i = 0; i < keys.size(); i++) {
                  String key = keys.get(i);
                  Class<?> type = entityTypes.get(key);
                  sb.append("        <div>\n");
                  sb.append("          <span>"+(i+1)+")</span> &nbsp; <span style='font-weight:bold;'>"+key
                        +"</span> :: <span>"+type.getName()+"</span><br/>\n");
                  String fieldDesc = getEntityDescription(prefix, FIELD_KEY_PREFIX + key);
                  if (fieldDesc != null) {
                     sb.append("          <div style='font-style:italic;font-size:0.9em;padding-left:1.5em;'>"+fieldDesc+"</div>\n");
                  }
                  sb.append("        </div>\n");
               }
               sb.append("      </div>\n");
            }
            // Redirects
            List<URLRedirect> redirects = entityRedirectsManager.getURLRedirects(prefix);
            if (! redirects.isEmpty()) {
               sb.append("      <h4 style='padding-left:0.5em;'>"+entityProperties.getProperty(DESCRIBE, "describe.url.redirects", locale)+"</h4>\n");
               sb.append("      <div style='padding-left:1em;padding-bottom:1em;'>\n");
               for (int i = 0; i < redirects.size(); i++) {
                  URLRedirect redirect = redirects.get(i);
                  sb.append("        <div>\n");
                  String target = replacePrefix(redirect.outgoingTemplate, prefix);
                  if (target == null) {
                     target = "<i>" + entityProperties.getProperty(DESCRIBE, "describe.url.redirects.no.outgoing", locale) + "</i>";
                  }
                  sb.append("          <span>"+(i+1)+")</span> &nbsp; "
                        + makeRedirectLink(replacePrefix(redirect.template, prefix), directUrl)
                        + " ==&gt; <span>"+target+"</span><br/>\n");
                  String redirectDesc = getEntityDescription(prefix, REDIRECT_KEY_PREFIX + redirect.template);
                  if (redirectDesc != null) {
                     sb.append("          <div style='font-style:italic;font-size:0.9em;padding-left:1.5em;'>"+redirectDesc+"</div>\n");
                  }
                  sb.append("        </div>\n");
               }
               sb.append("      </div>\n");
            }
         }
         // now capabilities
         sb.append("      <div style='font-size:1.1em; font-weight:bold; font-style:italic; padding-left:0.5em;'>"
               +entityProperties.getProperty(DESCRIBE, "describe.capabilities", locale)+": "+caps.size()+"</div>\n");
         sb.append("      <table width='95%' style='padding-left:1.5em;'>\n");
         sb.append("        <tr style='font-size:0.9em;'><th width='1%'></th><th width='14%'>"
               +entityProperties.getProperty(DESCRIBE, "describe.capabilities.name", locale)
               +"</th><th width='30%'>"
               +entityProperties.getProperty(DESCRIBE, "describe.capabilities.type", locale)
               +"</th>");
         if (extra) {   sb.append("<th width='55%'>"
               +entityProperties.getProperty(DESCRIBE, "describe.capabilities.description", locale)
               +"</th>"); }
         sb.append("</tr>\n");
         int counter = 1;
         for (Class<? extends EntityProvider> class1 : caps) {
            sb.append("        <tr style='font-size:0.9em;'><td>");
            sb.append(counter++);
            sb.append("</td><td>");
            sb.append(class1.getSimpleName());
            sb.append("</td><td>");
            sb.append(class1.getName());
            sb.append("</td><td>");
            if (extra) {
               String capabilityDescription = getEntityDescription(prefix, class1.getSimpleName());
               if (capabilityDescription != null) {
                  sb.append(capabilityDescription);
               }
            }
            sb.append("</td></tr>\n");
         }
         sb.append("      </table>\n");
      }
      return sb.toString();
   }

   /**
    * Replaces the {prefix} value in the template with the actual prefix,
    * allows nulls to pass through
    */
   protected String replacePrefix(String outgoingTemplate, String prefix) {
      if (outgoingTemplate != null) {
         outgoingTemplate = outgoingTemplate.replace(TemplateParseUtil.PREFIX_VARIABLE, prefix);
      }
      return outgoingTemplate;
   }

   /**
    * Turn a redirect template into html for a link if it has no variables in it,
    * otherwise output the text of the template in a span with bold
    */
   protected String makeRedirectLink(String redirect, String prefixURL) {
      String html = redirect;
      if (redirect.indexOf("{") > 0 && redirect.indexOf("}") > 0) {
         html = "<span style='font-weight:bold;'>"+redirect+"</span>";
      } else {
         html = "<a style='font-weight:bold;' href='"+prefixURL+redirect+"'>"+redirect+"</a>";
      }
      return html;
   }

   // DESCRIBE formatting utilities

   /**
    * @param customAction
    * @return a URL for triggering the custom action (without http://server/direct)
    */
   protected String makeActionURL(EntityView ev, CustomAction customAction) {
      String URL = ev.getEntityURL(customAction.viewKey, null) + EntityView.SEPARATOR + customAction.action;
      return URL;
   }

   /**
    * Create text to display for the CA key and method
    * @param customAction
    * @return
    */
   protected String makeCustomActionKeyMethodText(CustomAction customAction) {
      String togo = "*";
      if (customAction.viewKey != null) {
         togo = customAction.viewKey+" ("+EntityView.translateViewKeyToMethod(customAction.viewKey)+")";
      }
      return togo;
   }

   protected String[] getFormats(String prefix, boolean output) {
      String[] formats;
      try {
         if (output) {
            formats = entityProviderManager.getProviderByPrefixAndCapability(prefix, Outputable.class).getHandledOutputFormats();
         } else {
            formats = entityProviderManager.getProviderByPrefixAndCapability(prefix, Inputable.class).getHandledInputFormats();
         }
      } catch (NullPointerException e) {
         formats = new String[] {};
      }
      if (formats == null) {
         formats = new String[] {};
      }
      return formats;
   }

   protected String makeFormatsUrlHtml(String url, String[] formats) {
      StringBuilder sb = new StringBuilder();
      if (formats != null) {
         for (String format : formats) {
            sb.append( makeFormatUrlHtml(url, format) );
         }
      }
      return sb.toString();
   }

   protected String makeFormatsString(String[] formats) {
      String s = ReflectUtil.arrayToString( formats );
      if ("".equals(s)) {
         s = "<i>NONE</i>";
      }
      return s;
   }

   protected String makeFormatUrlHtml(String url, String format) {
      return " (<a href='"+url+"."+format+"'>"+format+"</a>)";
   }

   /**
    * Get the descriptions for an entity OR its capabilites OR custom actions
    * @param prefix an entity prefix
    * @param descriptionkey (optional) the key (simplename for capability, action.actionkey for actions)
    * @return the description (may be blank) OR null if there is none
    */
   protected String getEntityDescription(String prefix, String descriptionkey) {
      String value = null;
      Locale locale = entityProperties.getLocale();
      // get from EP first if possible
      DescribeDefineable describer = entityProviderManager.getProviderByPrefixAndCapability(prefix, DescribeDefineable.class);
      if (describer != null) {
         value = describer.getDescription(locale, descriptionkey);
      }
      // now from the default location if null
      if (value == null) {
         String key = prefix;
         if (descriptionkey != null) {
            // try simple name first
            key += "." + descriptionkey;
         }
         value = entityProperties.getProperty(prefix, key, locale);
      }
      if ("".equals(value)) {
         value = null;
      }
      return value;
   }

   /**
    * Safely get the sample entity object for descriptions
    */
   protected Object getSampleEntityObject(String prefix) {
      Object entity = null;
      try {
         Resolvable resolvable = entityProviderManager.getProviderByPrefixAndCapability(prefix, Resolvable.class);
         if (resolvable != null) {
            entity = resolvable.getEntity(new EntityReference(prefix, ""));
         }
      } catch (RuntimeException e) {
         entity = null;
      }
      if (entity == null) {
         try {
            Createable createable = entityProviderManager.getProviderByPrefixAndCapability(prefix, Createable.class);
            if (createable != null) {
               entity = createable.getSampleEntity();
            }
         } catch (RuntimeException e) {
            entity = null;
         }
      }
      return entity;
   }

}
