/**
 * $Id$
 * $URL$
 * EntityDescriptionManager.java - entity-broker - Jul 22, 2008 12:18:48 PM - azeckoski
 **************************************************************************
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
 */

package org.sakaiproject.entitybroker.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.azeckoski.reflectutils.ArrayUtils;
import org.azeckoski.reflectutils.ClassFields;
import org.azeckoski.reflectutils.ClassFields.FieldsFilter;
import org.azeckoski.reflectutils.ConstructorUtils;
import org.azeckoski.reflectutils.ReflectUtils;
import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.access.AccessFormats;
import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;
import org.sakaiproject.entitybroker.access.HttpServletAccessProvider;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderMethodStore;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityFieldRequired;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Deleteable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.DescribeDefineable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.DescribePropertiesable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Updateable;
import org.sakaiproject.entitybroker.entityprovider.extension.CustomAction;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.URLRedirect;
import org.sakaiproject.entitybroker.providers.EntityPropertiesService;
import org.sakaiproject.entitybroker.providers.EntityRequestHandler;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;

import lombok.extern.slf4j.Slf4j;


/**
 * This handles all the methods related to generating descriptions for entities,
 * html and xml currently supported
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
@SuppressWarnings("deprecation")
public class EntityDescriptionManager {

    private static final String INPUT_DESCRIBE_KEY = "input";
    private static final String OUTPUT_DESCRIBE_KEY = "output";
    private static final String VIEW_KEY_PREFIX = "view.";
    private static final String FIELD_KEY_PREFIX = "field.";
    private static final String REDIRECT_KEY_PREFIX = "redirect.";
    protected static final String ACTION_KEY_PREFIX = "action.";

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
    // include versions info in the footer now
    protected static final String XHTML_FOOTER = "<br/>\n<div style='width:100%;text-align:center;font-style:italic;font-size:0.9em;'>"
        + "REST:: <b>" + EntityHandlerImpl.APP_VERSION + "</b> SVN: " + EntityHandlerImpl.SVN_REVISION + " : " + EntityHandlerImpl.SVN_LAST_UPDATE 
        + "</div>\n"
        + "</body>\n</html>\n";

    protected EntityDescriptionManager() { }

    /**
     * Full constructor
     * @param entityViewAccessProviderManager
     * @param httpServletAccessProviderManager
     * @param entityProviderManager
     * @param entityProperties
     * @param entityBrokerManager
     * @param entityProviderMethodStore
     */
    public EntityDescriptionManager(
            EntityViewAccessProviderManager entityViewAccessProviderManager,
            HttpServletAccessProviderManager httpServletAccessProviderManager,
            EntityProviderManager entityProviderManager, EntityPropertiesService entityProperties,
            EntityBrokerManager entityBrokerManager,
            EntityProviderMethodStore entityProviderMethodStore) {
        super();
        this.entityViewAccessProviderManager = entityViewAccessProviderManager;
        this.httpServletAccessProviderManager = httpServletAccessProviderManager;
        this.entityProviderManager = entityProviderManager;
        this.entityProperties = entityProperties;
        this.entityBrokerManager = entityBrokerManager;
        this.entityProviderMethodStore = entityProviderMethodStore;
        init();
    }

    private EntityProvider describeEP = null;
    public void init() {
        log.info("EntityDescriptionManager: init()");
        // register the describe prefixes to load up descriptions
        describeEP = new DescribePropertiesable() {
            public String getEntityPrefix() {
                return EntityRequestHandler.DESCRIBE;
            }
            public String getBaseName() {
                return getEntityPrefix();
            }
            public ClassLoader getResourceClassLoader() {
                return EntityDescriptionManager.class.getClassLoader();
            }
        };
        entityProviderManager.registerEntityProvider(describeEP);
    }

    public void destroy() {
        log.info("EntityDescriptionManager: destroy()");
        // NOTE: do not try to unregister describe
//        if (describeEP != null) {
//            try {
//                entityProviderManager.unregisterEntityProvider(describeEP);
//            } catch (RuntimeException e) {
//                log.warn("EntityDescriptionManager: Unable to unregister the describe description provider: " + e);
//            }
//        }
    }

    private EntityViewAccessProviderManager entityViewAccessProviderManager;
    public void setEntityViewAccessProviderManager(
            EntityViewAccessProviderManager entityViewAccessProviderManager) {
        this.entityViewAccessProviderManager = entityViewAccessProviderManager;
    }

    private HttpServletAccessProviderManager httpServletAccessProviderManager;
    public void setHttpServletAccessProviderManager(
            HttpServletAccessProviderManager httpServletAccessProviderManager) {
        this.httpServletAccessProviderManager = httpServletAccessProviderManager;
    }

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

    private EntityProviderMethodStore entityProviderMethodStore;
    public void setEntityProviderMethodStore(EntityProviderMethodStore entityProviderMethodStore) {
        this.entityProviderMethodStore = entityProviderMethodStore;
    }


    /**
     * Generate a description of all entities in the system,
     * this is only available as XML and XHTML
     * 
     * @param format XML or HTML (default is HTML)
     * @param locale the locale to use for any translations
     * @return the description string for all known entities
     */
    public String makeDescribeAll(String format, Locale locale) {
        if (locale == null) {
            locale = entityProperties.getLocale();
        }
        Map<String, List<Class<? extends EntityProvider>>> map = entityProviderManager.getRegisteredEntityCapabilities();
        // take out the "describe" EP if it is in there
        map.remove(DESCRIBE);
        // now get to creating the descriptions
        String describeURL = entityBrokerManager.getServletContext() + SLASH_DESCRIBE;
        String output = "";
        if (Formats.XML.equals(format)) {
            // XML available in case someone wants to parse this in javascript or whatever
            StringBuilder sb = new StringBuilder(200);
            sb.append(XML_HEADER);
            sb.append("<describe>\n");
            sb.append("  <describeURL>" + describeURL + "</describeURL>\n");
            sb.append( makeXMLVersion() );
            sb.append("  <prefixes>\n");
            ArrayList<String> prefixes = new ArrayList<String>(map.keySet());
            Collections.sort(prefixes);
            for (int i = 0; i < prefixes.size(); i++) {
                String prefix = prefixes.get(i);
                describeEntity(sb, prefix, FAKE_ID, format, false, map.get(prefix), locale);
            }
            sb.append("  </prefixes>\n");
            sb.append("</describe>\n");
            output = sb.toString();
        } else {
            // just do HTML if not one of the handled ones
            StringBuilder sb = new StringBuilder(300);
            sb.append(XML_HEADER);
            sb.append(XHTML_HEADER);
            sb.append("<h1><a href='"+ describeURL +"'>"+entityProperties.getProperty(DESCRIBE, "describe.all", locale)+"</a> "
                    + entityProperties.getProperty(DESCRIBE, "describe.registered.entities", locale)
                    + makeFormatUrlHtml(describeURL, Formats.XML) +"</h1>\n");
            sb.append("  <i>RESTful URLs: <a href='http://microformats.org/wiki/rest/urls'>http://microformats.org/wiki/rest/urls</a></i><br/>\n");
            sb.append("  <h2>"+entityProperties.getProperty(DESCRIBE, "describe.all", locale)+" ("
                    +entityProperties.getProperty(DESCRIBE, "describe.registered.entities", locale)+"): "
                    +map.size()+"</h2>\n");
            sb.append("  <div style='font-style:italic;padding-bottom:0.5em;'>"+entityProperties.getProperty(DESCRIBE, "describe.general.notes", locale)+"</div>\n"); // notes
            sb.append("  <div style='font-style:italic;'>"+entityProperties.getProperty(DESCRIBE, "describe.searching", locale)+"</div>\n"); // searching
            ArrayList<String> prefixes = new ArrayList<String>(map.keySet());
            Collections.sort(prefixes);
            for (int i = 0; i < prefixes.size(); i++) {
                String prefix = prefixes.get(i);
                describeEntity(sb, prefix, FAKE_ID, format, false, map.get(prefix), locale);
            }
            sb.append(XHTML_FOOTER);
            output = sb.toString();
        }
        return output;
    }

    /**
     * @return the XML tags string that contains the XML version info
     */
    private String makeXMLVersion() {
        StringBuilder sb = new StringBuilder();
        sb.append("  <version>" + EntityHandlerImpl.APP_VERSION + "</version>\n");
        sb.append("  <svn>\n");
        sb.append("    <revision>"+EntityHandlerImpl.SVN_REVISION+"</revision>\n");
        sb.append("    <last-update>"+EntityHandlerImpl.SVN_LAST_UPDATE+"</last-update>\n");
        sb.append("  </svn>\n");
        return sb.toString();
    }


    /**
     * Generate a description of an entity type
     * 
     * @param prefix an entity prefix
     * @param id the entity id to use for generating URLs
     * @param format a format to output, HTML and XML supported
     * @param locale the locale to use for translations
     * @return the description string
     * @throws IllegalArgumentException if the entity does not exist
     */
    public String makeDescribeEntity(String prefix, String id, String format, Locale locale) {
        if (locale == null) {
            locale = entityProperties.getLocale();
        }
        if (entityProviderManager.getProviderByPrefix(prefix) == null) {
            throw new IllegalArgumentException("Invalid prefix ("+prefix+"), entity with that prefix does not exist");
        }
        StringBuilder sb = new StringBuilder(250);
        if (Formats.XML.equals(format)) {
            sb.append(XML_HEADER);
            describeEntity(sb, prefix, id, format, true, null, locale);
        } else {
            // just do HTML if not one of the handled ones
            sb.append(XML_HEADER);
            sb.append(XHTML_HEADER);
            describeEntity(sb, prefix, id, format, true, null, locale);
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
     * @param locale used for translations
     * @return the entity description
     */
    @SuppressWarnings("rawtypes")
    protected String describeEntity(StringBuilder sb, String prefix, String id, String format, boolean extra, List<Class<? extends EntityProvider>> caps, Locale locale) {
        if (caps == null) {
            caps = entityProviderManager.getPrefixCapabilities(prefix);
        }
        if (locale == null) {
            locale = entityProperties.getLocale();
        }
        String servletUrl = entityBrokerManager.getServletContext();
        if (Formats.XML.equals(format)) {
            // XML available in case someone wants to parse this in javascript or whatever
            String describePrefixUrl = servletUrl + "/" + prefix + SLASH_DESCRIBE;
            sb.append("    <prefix>\n");
            sb.append("      <prefix>" + prefix + "</prefix>\n");
            sb.append("      <describeURL>" + describePrefixUrl + "</describeURL>\n");
            String summary = getEntityDescription(prefix, null, locale);
            if (summary != null) {
                sb.append("      <summary>" + summary + "</summary>\n");            
            }
            String description = getEntityDescription(prefix, "description", locale);
            if (description != null) {
                sb.append("      <description>" + description + "</description>\n");            
            }
            if (extra) {
                // URLs
                EntityView ev = entityBrokerManager.makeEntityView(new EntityReference(prefix, id), null, null);
                if (caps.contains(CollectionResolvable.class)) {
                    sb.append("      <collectionURL>" + ev.getEntityURL(EntityView.VIEW_LIST, null) + "</collectionURL>\n");
                    String viewDesc = getEntityDescription(prefix, VIEW_KEY_PREFIX + EntityView.VIEW_LIST, locale);
                    if (viewDesc != null) {
                        sb.append("      <collectionDescription>"+viewDesc+"</collectionDescription>\n");
                    }
                }
                if (caps.contains(Createable.class)) {
                    sb.append("      <createURL>" + ev.getEntityURL(EntityView.VIEW_NEW, null) + "</createURL>\n");
                    String viewDesc = getEntityDescription(prefix, VIEW_KEY_PREFIX + EntityView.VIEW_NEW, locale);
                    if (viewDesc != null) {
                        sb.append("      <createDescription>"+viewDesc+"</createDescription>\n");
                    }
                }
                if (caps.contains(CoreEntityProvider.class) || caps.contains(Resolvable.class)) {
                    sb.append("      <showURL>" + ev.getEntityURL(EntityView.VIEW_SHOW, null) + "</showURL>\n");
                    String viewDesc = getEntityDescription(prefix, VIEW_KEY_PREFIX + EntityView.VIEW_SHOW, locale);
                    if (viewDesc != null) {
                        sb.append("      <showDescription>"+viewDesc+"</showDescription>\n");
                    }
                }
                if (caps.contains(Updateable.class)) {
                    sb.append("      <updateURL>" + ev.getEntityURL(EntityView.VIEW_EDIT, null) + "</updateURL>\n");
                    String viewDesc = getEntityDescription(prefix, VIEW_KEY_PREFIX + EntityView.VIEW_EDIT, locale);
                    if (viewDesc != null) {
                        sb.append("      <updateDescription>"+viewDesc+"</updateDescription>\n");
                    }
                }
                if (caps.contains(Deleteable.class)) {
                    sb.append("      <deleteURL>" + ev.getEntityURL(EntityView.VIEW_DELETE, null) + "</deleteURL>\n");
                    String viewDesc = getEntityDescription(prefix, VIEW_KEY_PREFIX + EntityView.VIEW_DELETE, locale);
                    if (viewDesc != null) {
                        sb.append("      <deleteDescription>"+viewDesc+"</deleteDescription>\n");
                    }
                }
                // Custom Actions
                List<CustomAction> customActions = entityProviderMethodStore.getCustomActions(prefix);
                if (! customActions.isEmpty()) {
                    for (CustomAction customAction : customActions) {
                        sb.append("      <customActions>\n");
                        sb.append("        <customAction>\n");
                        sb.append("          <action>"+customAction.action+"</action>\n");
                        sb.append("          <url>"+servletUrl+makeActionURL(ev, customAction)+"</url>\n");
                        if (customAction.viewKey == null || "".equals(customAction.viewKey)) {
                            sb.append("          <method/>\n");
                            sb.append("          <viewKey/>\n");
                        } else {
                            sb.append("          <method>"+EntityView.translateViewKeyToMethod(customAction.viewKey)+"</method>\n");
                            sb.append("          <viewKey>"+customAction.viewKey+"</viewKey>\n");
                        }
                        String actionDesc = getEntityDescription(prefix, ACTION_KEY_PREFIX + customAction.action, locale);
                        if (actionDesc != null) {
                            sb.append("          <description>"+actionDesc+"</description>\n");
                        }
                        sb.append("        </customAction>\n");
                        sb.append("      </customActions>\n");               
                    }
                }

                // Data and request handling
                // Formats
                String[] outputFormats = getFormats(prefix, true);
                sb.append("      <outputFormats>\n");
                if (outputFormats != null) {
                    if (outputFormats.length == 0) {
                        sb.append("        <format>*</format>\n");
                    } else {
                        for (int i = 0; i < outputFormats.length; i++) {
                            sb.append("        <format>"+outputFormats[i]+"</format>\n");
                        }
                    }
                }
                String outputDesc = getEntityDescription(prefix, OUTPUT_DESCRIBE_KEY, locale);
                if (outputDesc != null) {
                    sb.append("          <description>"+outputDesc+"</description>\n");
                }
                sb.append("       </outputFormats>\n");

                String[] inputFormats = getFormats(prefix, false);
                sb.append("      <inputFormats>\n");
                if (inputFormats != null) {
                    if (inputFormats.length == 0) {
                        sb.append("        <format>*</format>\n");
                    } else {
                        for (int i = 0; i < inputFormats.length; i++) {
                            sb.append("        <format>"+inputFormats[i]+"</format>\n");
                        }
                    }
                }
                String intputDesc = getEntityDescription(prefix, INPUT_DESCRIBE_KEY, locale);
                if (intputDesc != null) {
                    sb.append("          <description>"+intputDesc+"</description>\n");
                }
                sb.append("       </inputFormats>\n");

                EntityViewAccessProvider evap = entityViewAccessProviderManager.getProvider(prefix);
                // httpServletAccessProviderManager is deprecated and can be null
                HttpServletAccessProvider hsap = httpServletAccessProviderManager == null ? null : httpServletAccessProviderManager.getProvider(prefix);
                if (evap != null || hsap != null) {
                    sb.append("      <accessProvider>\n");
                    if (evap != null) {
                        sb.append("        <type>" + EntityViewAccessProvider.class.getSimpleName() + "</type>\n");
                        sb.append("        <implementor>" + evap.getClass().getName() + "</implementor>\n");
                        if (AccessFormats.class.isAssignableFrom(evap.getClass())) {
                            String[] accessFormats = ((AccessFormats)evap).getHandledAccessFormats();
                            sb.append("        <accessFormats>\n");
                            if (accessFormats != null) {
                                if (accessFormats.length == 0) {
                                    sb.append("          <format>*</format>\n");
                                } else {
                                    for (int i = 0; i < accessFormats.length; i++) {
                                        sb.append("          <format>"+accessFormats[i]+"</format>\n");
                                    }
                                }
                            }
                            sb.append("        </accessFormats>\n");
                        }
                    } else {
                        sb.append("        <type>" + HttpServletAccessProvider.class.getSimpleName() + "</type>\n");
                        sb.append("        <implementor>" + hsap.getClass().getName() + "</implementor>\n");
                    }
                    sb.append("      </accessProvider>\n");
                }

                // Resolvable Entity Info
                Object entity = entityBrokerManager.getSampleEntityObject(prefix, null);
                if (entity != null) {
                    Class<?> entityType = entity.getClass();
                    sb.append("      <entityClass>\n");
                    sb.append("        <class>"+ entityType.getName() +"</class>\n");
                    if (ConstructorUtils.isClassSimple(entityType)) {
                        sb.append("        <type>simple</type>\n");
                    } else if (ConstructorUtils.isClassCollection(entityType)) {
                        sb.append("        <type>collection</type>\n");
                    } else if (ConstructorUtils.isClassArray(entityType)) {
                        sb.append("        <type>array</type>\n");
                        sb.append("        <componentType>"+ArrayUtils.type((Object[])entity).getName()+"</componentType>\n");
                    } else if (ConstructorUtils.isClassMap(entityType)) {
                        sb.append("        <type>map</type>\n");
                        // get the types of the map keys if possible
                        Map m = (Map) entity;
                        if (m.size() > 0) {
                            Entry entry = (Entry) m.entrySet().iterator().next();
                            sb.append("        <keyType>"+(entry.getKey()==null?Object.class.getName():entry.getKey().getClass().getName())+"</keyType>\n");
                            sb.append("        <valueType>"+(entry.getValue()==null?Object.class.getName():entry.getValue().getClass().getName())+"</valueType>\n");
                        }
                    } else {
                        sb.append("        <type>bean</type>\n");
                        sb.append("        <fields>\n");
                        // get all the read and write fields from this object
                        Map<String, Class<?>> readTypes = ReflectUtils.getInstance().getFieldTypes(entity.getClass(), FieldsFilter.SERIALIZABLE);
                        Map<String, Class<?>> writeTypes = ReflectUtils.getInstance().getFieldTypes(entity.getClass(), FieldsFilter.WRITEABLE);
                        Map<String, Class<?>> entityTypes = new HashMap<String, Class<?>>(readTypes);
                        entityTypes.putAll(writeTypes);
                        ArrayList<String> keys = new ArrayList<String>(entityTypes.keySet());
                        Collections.sort(keys);
                        for (String key : keys) {
                            Class<?> type = entityTypes.get(key);
                            sb.append("          <field>\n");
                            sb.append("            <name>"+ key +"</name>\n");
                            sb.append("            <type>"+ type.getName() +"</type>\n");
                            sb.append("            <readable>"+ readTypes.containsKey(key) +"</readable>\n");
                            sb.append("            <writeable>"+ writeTypes.containsKey(key) +"</writeable>\n");
                            String fieldDesc = getEntityDescription(prefix, FIELD_KEY_PREFIX + key, locale);
                            if (fieldDesc != null) {
                                sb.append("            <description>"+ fieldDesc +"</description>\n");
                            }
                            sb.append("          </field>\n");
                        }
                        sb.append("        </fields>\n");
                    }
                    sb.append("      </entityClass>\n");
                }

                // Redirects
                List<URLRedirect> redirects = entityProviderMethodStore.getURLRedirects(prefix);
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
                        String redirectDesc = getEntityDescription(prefix, REDIRECT_KEY_PREFIX + redirect.template, locale);
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
                    String capabilityDescription = getEntityDescription(prefix, class1.getSimpleName(), locale);
                    if (capabilityDescription != null) {
                        sb.append("          <description>" + capabilityDescription + "</description>\n");                  
                    }
                }
                sb.append("        </capability>\n");
            }
            sb.append("      </capabilities>\n");
            sb.append("    </prefix>\n");
        } else {
            // just do HTML if not one of the handled ones
            String describePrefixUrl = servletUrl + "/" + prefix + SLASH_DESCRIBE;
            sb.append("    <h3 style='margin-bottom: 0.3em;'><a href='"+describePrefixUrl+"'>"+prefix+"</a>"
                    + makeFormatUrlHtml(describePrefixUrl, Formats.XML) +"</h3>\n");
            String summary = getEntityDescription(prefix, null, locale);
            if (summary != null) {
                sb.append("      <div style='padding-left:0.5em; padding-bottom:0.2em; width:90%;'>" + summary + "</div>\n");
            }
            if (extra) {
                String description = getEntityDescription(prefix, "description", locale);
                if (description != null) {
                    sb.append("      <div style='padding-left:1em; padding-bottom:0.4em; width:90%; font-size:0.9em;'>" + description + "</div>\n");
                }
                sb.append("      <div style='font-style: italic; padding-left:1em;'>" 
                        + "RESTful URLs: <a href='http://microformats.org/wiki/rest/urls'>http://microformats.org/wiki/rest/urls</a></div>\n");
                sb.append("      <div style='font-style: italic; padding-left:2em; font-size:0.9em;'>" + entityProperties.getProperty(DESCRIBE, "describe.response.codes", locale) + "</div>\n");

                String[] outputFormats = getFormats(prefix, true);

                // URLs
                EntityView ev = entityBrokerManager.makeEntityView(new EntityReference(prefix, id), null, null);
                String url = "";
                if (caps.contains(CollectionResolvable.class) 
                        || caps.contains(Createable.class)
                        || caps.contains(CoreEntityProvider.class) 
                        || caps.contains(Resolvable.class)
                        || caps.contains(Updateable.class)
                        || caps.contains(Deleteable.class)) {
                    sb.append("      <h4 style='padding-left:0.5em;margin-bottom:0.2em;'>"+entityProperties.getProperty(DESCRIBE, "describe.entity.sample.urls", locale)
                            +" (_id='"+id+"') ["
                            +entityProperties.getProperty(DESCRIBE, "describe.entity.may.be.invalid", locale)+"]:</h4>\n");
                    sb.append("      <div style='padding-left:1em;padding-bottom:1em;'>\n");
                    if (caps.contains(CollectionResolvable.class)) {
                        url = makeEntityURL(ev, EntityView.VIEW_LIST);
                        sb.append("        <div>\n");
                        sb.append("          <div>"+entityProperties.getProperty(DESCRIBE, "describe.entity.collection.url", locale)
                                +": GET <a href='"+ servletUrl+url +"'>"+url+"</a>"
                                + makeFormatsUrlHtml(servletUrl+url, outputFormats) 
                                + "</div>\n");
                        sb.append( generateMethodDetails(EntityView.VIEW_LIST, locale) );
                        String viewDesc = getEntityDescription(prefix, VIEW_KEY_PREFIX + EntityView.VIEW_LIST, locale);
                        if (viewDesc != null) {
                            sb.append("          <div style='font-style:italic;font-size:0.9em;padding-left:2em;'>"+viewDesc+"</div>\n");
                        }
                        sb.append("        </div>\n");
                    }
                    if (caps.contains(Createable.class)) {
                        url = makeEntityURL(ev, EntityView.VIEW_NEW);
                        sb.append("        <div>\n");
                        sb.append("          <div>"+entityProperties.getProperty(DESCRIBE, "describe.entity.create.url", locale)
                                +": POST <a href='"+ servletUrl+url +"'>"+url+"</a>"
                                + makeFormUrlHtml(servletUrl+url, outputFormats)
                                +"</div>\n");
                        sb.append( generateMethodDetails(EntityView.VIEW_NEW, locale) );
                        String viewDesc = getEntityDescription(prefix, VIEW_KEY_PREFIX + EntityView.VIEW_NEW, locale);
                        if (viewDesc != null) {
                            sb.append("          <div style='font-style:italic;font-size:0.9em;padding-left:2em;'>"+viewDesc+"</div>\n");
                        }
                        sb.append("        </div>\n");
                    }

                    if (caps.contains(CoreEntityProvider.class) || caps.contains(Resolvable.class)) {
                        url = makeEntityURL(ev, EntityView.VIEW_SHOW);
                        sb.append("        <div>\n");
                        sb.append("          <div>"+entityProperties.getProperty(DESCRIBE, "describe.entity.show.url", locale)
                                +": GET <a href='"+ servletUrl+url +"'>"+url+"</a>"
                                + makeFormatsUrlHtml(servletUrl+url, outputFormats) 
                                + "</div>\n");
                        sb.append( generateMethodDetails(EntityView.VIEW_SHOW, locale) );
                        String viewDesc = getEntityDescription(prefix, VIEW_KEY_PREFIX + EntityView.VIEW_SHOW, locale);
                        if (viewDesc != null) {
                            sb.append("          <div style='font-style:italic;font-size:0.9em;padding-left:2em;'>"+viewDesc+"</div>\n");
                        }
                        sb.append("        </div>\n");
                    }

                    if (caps.contains(Updateable.class)) {
                        url = makeEntityURL(ev, EntityView.VIEW_EDIT);
                        sb.append("        <div>\n");
                        sb.append("          <div>"+entityProperties.getProperty(DESCRIBE, "describe.entity.update.url", locale)
                                +": PUT <a href='"+ servletUrl+url +"'>"+url+"</a>"
                                + makeFormUrlHtml(servletUrl+url, outputFormats)
                                +"</div>\n");
                        sb.append( generateMethodDetails(EntityView.VIEW_EDIT, locale) );
                        String viewDesc = getEntityDescription(prefix, VIEW_KEY_PREFIX + EntityView.VIEW_EDIT, locale);
                        if (viewDesc != null) {
                            sb.append("          <div style='font-style:italic;font-size:0.9em;padding-left:2em;'>"+viewDesc+"</div>\n");
                        }
                        sb.append("        </div>\n");
                    }
                    if (caps.contains(Deleteable.class)) {
                        url = makeEntityURL(ev, EntityView.VIEW_DELETE);
                        sb.append("        <div>\n");
                        sb.append("          <div>"+entityProperties.getProperty(DESCRIBE, "describe.entity.delete.url", locale)
                                +": DELETE <a href='"+ servletUrl+url +"'>"+url+"</a>"
                                + makeFormUrlHtml(servletUrl+url, outputFormats)
                                +"</div>\n");
                        sb.append( generateMethodDetails(EntityView.VIEW_DELETE, locale) );
                        String viewDesc = getEntityDescription(prefix, VIEW_KEY_PREFIX + EntityView.VIEW_DELETE, locale);
                        if (viewDesc != null) {
                            sb.append("          <div style='font-style:italic;font-size:0.9em;padding-left:2em;'>"+viewDesc+"</div>\n");
                        }
                        sb.append("        </div>\n");
                    }
                    sb.append("      </div>\n");
                }

                // Custom Actions
                List<CustomAction> customActions = entityProviderMethodStore.getCustomActions(prefix);
                if (! customActions.isEmpty()) {
                    sb.append("      <h4 style='padding-left:0.5em;margin-bottom:0.2em;'>"+entityProperties.getProperty(DESCRIBE, "describe.custom.actions", locale)+"</h4>\n");
                    sb.append("      <div style='padding-left:1em;padding-bottom:1em;'>\n");
                    for (CustomAction customAction : customActions) {
                        sb.append("        <div>\n");
                        String actionURL = makeActionURL(ev, customAction);
                        String formatsHtml = "";
                        if (customAction.viewKey == null 
                                || EntityView.VIEW_LIST.equals(customAction.viewKey)
                                || EntityView.VIEW_SHOW.equals(customAction.viewKey)) {
                            formatsHtml = makeFormatsUrlHtml(servletUrl+actionURL, outputFormats);
                        }
                        sb.append("          <a style='font-weight:bold;' href='"+servletUrl+actionURL+"'>"
                                + customAction.action+"</a> : " 
                                + "<span>"+makeCustomActionKeyMethodText(customAction)+"</span> : "
                                + "<span>["+actionURL+"]</span> "
                                + formatsHtml
                                + "<br/>\n");
                        String actionDesc = getEntityDescription(prefix, ACTION_KEY_PREFIX + customAction.action, locale);
                        if (actionDesc != null) {
                            sb.append("          <div style='font-style:italic;font-size:0.9em;padding-left:1.5em;'>"+actionDesc+"</div>\n");
                        }
                        sb.append("        </div>\n");
                    }
                    sb.append("      </div>\n");
                }

                // Redirects
                List<URLRedirect> redirects = entityProviderMethodStore.getURLRedirects(prefix);
                if (! redirects.isEmpty()) {
                    sb.append("      <h4 style='padding-left:0.5em;margin-bottom:0.2em;'>"+entityProperties.getProperty(DESCRIBE, "describe.url.redirects", locale)+"</h4>\n");
                    sb.append("      <div style='padding-left:1em;padding-bottom:1em;'>\n");
                    for (int i = 0; i < redirects.size(); i++) {
                        URLRedirect redirect = redirects.get(i);
                        sb.append("        <div>\n");
                        String target = replacePrefix(redirect.outgoingTemplate, prefix);
                        if (target == null) {
                            target = "<i>" + entityProperties.getProperty(DESCRIBE, "describe.url.redirects.no.outgoing", locale) + "</i>";
                        }
                        sb.append("          <span>"+(i+1)+")</span> &nbsp; "
                                + makeRedirectLink(replacePrefix(redirect.template, prefix), servletUrl)
                                + " ==&gt; <span>"+target+"</span><br/>\n");
                        String redirectDesc = getEntityDescription(prefix, REDIRECT_KEY_PREFIX + redirect.template, locale);
                        if (redirectDesc != null) {
                            sb.append("          <div style='font-style:italic;font-size:0.9em;padding-left:1.5em;'>"+redirectDesc+"</div>\n");
                        }
                        sb.append("        </div>\n");
                    }
                    sb.append("      </div>\n");
                }

                // Resolvable Entity Info
                Object entity = entityBrokerManager.getSampleEntityObject(prefix, null);
                if (entity != null) {
                    Class<?> entityType = entity.getClass();
                    sb.append("      <h4 style='padding-left:0.5em;margin-bottom:0.2em;'>"+entityProperties.getProperty(DESCRIBE, "describe.entity.class", locale)+" : "+ entityType.getName() +"</h4>\n");
                    sb.append("      <div style='padding-left:1em;padding-bottom:1em;'>\n");
                    if (ConstructorUtils.isClassSimple(entityType)) {
                        sb.append( makeResolveType("simple", null, locale));
                    } else if (ConstructorUtils.isClassCollection(entityType)) {
                        sb.append( makeResolveType("collection", null, locale));
                    } else if (ConstructorUtils.isClassArray(entityType)) {
                        String cType = "Component Class: " + ArrayUtils.type((Object[])entity).getName();
                        sb.append( makeResolveType("array", cType, locale));
                    } else if (ConstructorUtils.isClassMap(entityType)) {
                        // get the types of the map keys if possible
                        String mapTypes = null;
                        Map m = (Map) entity;
                        if (m.size() > 0) {
                            Entry entry = (Entry) m.entrySet().iterator().next();
                            mapTypes = (entry.getKey()==null?Object.class.getName():entry.getKey().getClass().getName())
                            +" => "
                            +(entry.getValue()==null?Object.class.getName():entry.getValue().getClass().getName());
                        }
                        sb.append( makeResolveType("map", mapTypes, locale));
                    } else {
                        sb.append( makeResolveType("bean", null, locale));
                        sb.append("        <table width='80%' cellpadding='0' cellspacing='0'>\n");
                        sb.append("          <thead>\n");
                        sb.append("            <tr>\n");
                        sb.append("              <td width='1%'></td>\n");
                        sb.append("              <td>"+ entityProperties.getProperty(DESCRIBE, "describe.capabilities.name", locale) +"</td>\n");
                        sb.append("              <td>"+ entityProperties.getProperty(DESCRIBE, "describe.capabilities.type", locale) +"</td>\n");
                        sb.append("              <td>"+ entityProperties.getProperty(DESCRIBE, "describe.entity.field.status", locale) +"</td>\n");
                        sb.append("            </tr>\n");
                        sb.append("          </thead>\n");
                        sb.append("          <tbody>\n");
                        // get all the read and write fields from this object
                        ClassFields<?> cf = ReflectUtils.getInstance().analyzeClass(entity.getClass());
                        Map<String, Class<?>> readTypes = cf.getFieldTypes(FieldsFilter.SERIALIZABLE);
                        Map<String, Class<?>> writeTypes = cf.getFieldTypes(FieldsFilter.WRITEABLE);
                        HashSet<String> requiredFieldNames = new HashSet<String>(cf.getFieldNamesWithAnnotation(EntityFieldRequired.class));
                        Map<String, Class<?>> entityTypes = new HashMap<String, Class<?>>(readTypes);
                        entityTypes.putAll(writeTypes);
                        ArrayList<String> keys = new ArrayList<String>(entityTypes.keySet());
                        Collections.sort(keys);
                        for (int i = 0; i < keys.size(); i++) {
                            String fieldName = keys.get(i);
                            Class<?> type = entityTypes.get(fieldName);
                            String status = null;
                            String trStyle = "";
                            if (! readTypes.containsKey(fieldName)) {
                                // write only
                                status = entityProperties.getProperty(DESCRIBE, "describe.entity.field.write.only", locale);
                                trStyle = " style='color:blue;'";
                            } else if (! writeTypes.containsKey(fieldName)) {
                                // read only
                                status = entityProperties.getProperty(DESCRIBE, "describe.entity.field.read.only", locale);
                                trStyle = " style='color:red;'";
                            } else {
                                // read/write
                                status = entityProperties.getProperty(DESCRIBE, "describe.entity.field.read.write", locale);
                            }
                            boolean required = requiredFieldNames.contains(fieldName);
                            if (required) {
                                status = status + " <b style='color:red;'>* " + entityProperties.getProperty(DESCRIBE, "describe.entity.field.required", locale) + "</b>";
                            }
                            // get the printable type names
                            String typeName = type.getName();
                            if (String.class.getName().equals(typeName)) {
                                typeName = "string";
                            } else if (Boolean.class.getName().equals(typeName)) {
                                typeName = "boolean";
                            } else if (Integer.class.getName().equals(typeName)) {
                                typeName = "int";
                            } else if (Long.class.getName().equals(typeName)) {
                                typeName = "long";
                            }
                            sb.append("            <tr"+trStyle+"><td>"+(i+1)+")&nbsp;</td>"
                                    + "<td style='font-weight:bold;'>"+ fieldName +"</td>"
                                    + "<td>"+ typeName +"</td>"
                                    + "<td style='font-style:italic;'>"+status+"</td></tr>\n");
                            String fieldDesc = getEntityDescription(prefix, FIELD_KEY_PREFIX + fieldName, locale);
                            if (fieldDesc != null) {
                                sb.append("            <tr><td></td><td colspan='3' style='font-style:italic;font-size:0.9em;'>");
                                sb.append(fieldDesc);
                                sb.append("</td></tr>\n");
                            }
                        }
                        sb.append("          </tbody>\n");
                        sb.append("        </table>\n");
                    }
                    sb.append("      </div>\n");
                }

                // Data Handling
                sb.append("      <h4 style='padding-left:0.5em;margin-bottom:0.2em;'>"+entityProperties.getProperty(DESCRIBE, "describe.entity.data.handling", locale)+"</h4>\n");
                sb.append("      <div style='padding-left:1em;padding-bottom:1em;'>\n");
                sb.append("        <div>"+entityProperties.getProperty(DESCRIBE, "describe.entity.formats.output", locale)+" : "
                        + makeFormatsString(outputFormats, EntityEncodingManager.HANDLED_OUTPUT_FORMATS, locale) +"</div>\n");
                String outputDesc = getEntityDescription(prefix, OUTPUT_DESCRIBE_KEY, locale);
                if (outputDesc != null) {
                    sb.append("          <div style='font-style:italic;font-size:0.9em;padding-left:1.5em;'>"+outputDesc+"</div>\n");
                }
                String[] inputFormats = getFormats(prefix, false);
                sb.append("        <div>"+entityProperties.getProperty(DESCRIBE, "describe.entity.formats.input", locale)+" : "
                        + makeFormatsString(inputFormats, EntityEncodingManager.HANDLED_INPUT_FORMATS, locale) +"</div>\n");
                String intputDesc = getEntityDescription(prefix, INPUT_DESCRIBE_KEY, locale);
                if (intputDesc != null) {
                    sb.append("          <div style='font-style:italic;font-size:0.9em;padding-left:1.5em;'>"+intputDesc+"</div>\n");
                }

                EntityViewAccessProvider evap = entityViewAccessProviderManager.getProvider(prefix);
                if (evap != null) {
                    sb.append("        <div>"+entityProperties.getProperty(DESCRIBE, "describe.entity.data.access.provider", locale)+" : "+ EntityViewAccessProvider.class.getSimpleName() +"</div>\n");
                    if (AccessFormats.class.isAssignableFrom(evap.getClass())) {
                        String[] accessFormats = ((AccessFormats)evap).getHandledAccessFormats();
                        sb.append("        <div>"+entityProperties.getProperty(DESCRIBE, "describe.entity.formats.access", locale)+" : "
                                + makeFormatsString(accessFormats, null, locale) +"</div>\n");
                    }
                    sb.append("        <div>"+entityProperties.getProperty(DESCRIBE, "describe.entity.data.access.provider", locale)+" : "+ EntityViewAccessProvider.class.getSimpleName() +"</div>\n");
                } else if (httpServletAccessProviderManager != null 
                        && httpServletAccessProviderManager.getProvider(prefix) != null) {
                    sb.append("        <div>"+entityProperties.getProperty(DESCRIBE, "describe.entity.data.access.provider", locale)+" : "+ HttpServletAccessProvider.class.getSimpleName() +"</div>\n");
                } else {
                    sb.append("        <div style='font-style:italic;padding-left:1em'>"+entityProperties.getProperty(DESCRIBE, "describe.entity.data.access.provider.none", locale) +"</div>\n");
                }
                sb.append("      </div>\n");
            }

            // Capabilities
            if (extra) {
                sb.append("      <h4 style='padding-left:0.5em;margin-bottom:0.2em;'>"
                        +entityProperties.getProperty(DESCRIBE, "describe.capabilities", locale)+"</h4>\n");
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
                        String capabilityDescription = getEntityDescription(prefix, class1.getSimpleName(), locale);
                        if (capabilityDescription != null) {
                            sb.append(capabilityDescription);
                        }
                    }
                    sb.append("</td></tr>\n");
                }
                sb.append("      </table>\n");
            }
        }
        return sb.toString();
    }

    /**
     * Generates the details listing which shows the response types for a view method
     * @param methodType the view method (new, show, list, delete, edit)
     * @param locale the locale
     * @return the html string to place on the description page
     */
    protected String generateMethodDetails(String methodType, Locale locale) {
        return "          <div style='font-style:italic;font-size:0.9em;padding-left:1.5em;'>" 
        + entityProperties.getProperty(DESCRIBE, "describe.details.header", locale) 
        + " "
        + entityProperties.getProperty(DESCRIBE, "describe.entity."+methodType+".details", locale) 
        + "</div>\n";
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

    protected String makeResolveType(String typeName, String extra, Locale locale) {
        StringBuilder sb = new StringBuilder();
        sb.append("        <div><b>");
        sb.append( entityProperties.getProperty(DESCRIBE, "describe.capabilities.type", locale) );
        sb.append(" :: ");
        sb.append(typeName);
        sb.append("</b>");
        if (extra != null) {
            sb.append(" (");
            sb.append(extra);
            sb.append(") ");
        }
        sb.append("</div>\n");
        return sb.toString();
    }

    /**
     * @param ev the entity view
     * @param customAction the custom action
     * @return a URL for triggering the custom action (without http://server/direct)
     */
    protected String makeActionURL(EntityView ev, CustomAction customAction) {
        // switched to this since it is more correct
        String URL = EntityView.SEPARATOR + ev.getEntityReference().getPrefix() + EntityView.SEPARATOR + customAction.action;
        String viewKey = customAction.viewKey;
        if (viewKey != null 
                && (EntityView.VIEW_SHOW.equals(viewKey) || EntityView.VIEW_EDIT.equals(viewKey) || EntityView.VIEW_DELETE.equals(viewKey))) {
            URL = ev.getEntityURL(EntityView.VIEW_SHOW, null) + EntityView.SEPARATOR + customAction.action;
        }
        return URL;
    }

    /**
     * @param ev entity view
     * @param viewType the type of view
     * @return a URL for triggering the entity action (without http://server/direct)
     */
    protected String makeEntityURL(EntityView ev, String viewType) {
        if (viewType == null) {
            viewType = EntityView.VIEW_LIST;
        }
        if (! EntityView.VIEW_LIST.equals(viewType) && ! EntityView.VIEW_NEW.equals(viewType)) {
            viewType = EntityView.VIEW_SHOW;
        } else {
            viewType = EntityView.VIEW_LIST;
        }
        return ev.getEntityURL(viewType, null);
    }

    /**
     * Create text to display for the CA key and method
     * @param customAction
     * @return
     */
    protected String makeCustomActionKeyMethodText(CustomAction customAction) {
        String togo = "*";
        if (customAction.viewKey != null && ! "".equals(customAction.viewKey)) {
            togo = customAction.viewKey+" ("+EntityView.translateViewKeyToMethod(customAction.viewKey)+")";
        }
        return togo;
    }

    /**
     * @return all the format extensions handled by this, null if none handled, empty if all
     */
    protected String[] getFormats(String prefix, boolean output) {
        String[] formats = null;
        try {
            if (output) {
                formats = entityProviderManager.getProviderByPrefixAndCapability(prefix, Outputable.class).getHandledOutputFormats();
            } else {
                formats = entityProviderManager.getProviderByPrefixAndCapability(prefix, Inputable.class).getHandledInputFormats();
                if (formats != null) {
                    // strip out the FORM element if it was included
                    if (ArrayUtils.contains(formats, Formats.FORM)) {
                        ArrayList<String> l = new ArrayList<String>();
                        for (String format : formats) {
                            if (! Formats.FORM.equals(format)) {
                                l.add(format);
                            }
                        }
                        formats = l.toArray(new String[l.size()]);
                    }
                }
            }
        } catch (NullPointerException e) {
            formats = null;
        }
        EntityViewAccessProvider evap = entityViewAccessProviderManager.getProvider(prefix);
        if (evap != null) {
            if (AccessFormats.class.isAssignableFrom(evap.getClass())) {
                String[] accessFormats = ((AccessFormats)evap).getHandledAccessFormats();
                if (accessFormats != null) {
                    if (accessFormats.length > 0) {
                        if (formats == null) {
                            formats = accessFormats;
                        } else {
                            for (int i = 0; i < accessFormats.length; i++) {
                                if (! ReflectUtils.contains(formats, accessFormats[i])) {
                                    ReflectUtils.appendArray(formats, accessFormats[i]);
                                }
                            }
                        }
                    }
                }
            }
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

    protected String makeFormatsString(String[] formats, String[] extraFormats, Locale locale) {
        String s = "";
        if (locale == null) {
            locale = entityProperties.getLocale();
        }
        if (formats == null) {
            s = "<i>"+entityProperties.getProperty(DESCRIBE, "describe.entity.formats.none", locale)+"</i>";
        } else if (formats.length == 0) {
            // all
            String all = "*";
            if (extraFormats != null && extraFormats.length > 0) {
                all = makeArrayIntoString(formats) + ",*";
            }
            s = "<i>" + entityProperties.getProperty(DESCRIBE, "describe.entity.formats.all", locale) + " (" + all + ")</i>";
        } else {
            s = makeArrayIntoString( formats );
        }
        return s;
    }

    protected String makeFormUrlHtml(String url, String[] formats) {
        String form = "";
        if (formats != null) {
            if (ArrayUtils.contains(formats, Formats.FORM)) {
                form = makeFormatUrlHtml(url, Formats.FORM);
            }
        }
        return form;
    }

    protected String makeFormatUrlHtml(String url, String format) {
        return " (<a href='"+url+"."+format+"'>"+format+"</a>)";
    }

    protected String makeArrayIntoString(Object[] array) {
        StringBuilder result = new StringBuilder();
        if (array != null && array.length > 0) {
            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    result.append(", ");
                }
                if (array[i] != null) {
                    result.append(array[i].toString());
                }
            }
        }
        return result.toString();
    }

    /**
     * Get the descriptions for an entity OR its capabilities OR custom actions
     * @param prefix an entity prefix
     * @param descriptionkey (optional) the key (simplename for capability, action.actionkey for actions)
     * @param locale the Locale to use for translations
     * @return the description (may be blank) OR null if there is none
     */
    protected String getEntityDescription(String prefix, String descriptionkey, Locale locale) {
        String value = null;
        if (locale == null) {
            locale = entityProperties.getLocale();
        }
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

}
