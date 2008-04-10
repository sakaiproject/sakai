/**
 * $Id$
 * $URL$
 * EntityView.java - entity-broker - Apr 10, 2008 6:26:47 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.entitybroker.util.TemplateParseUtil.PreProcessedTemplate;
import org.sakaiproject.entitybroker.util.TemplateParseUtil.ProcessedTemplate;
import org.sakaiproject.entitybroker.util.TemplateParseUtil.Template;


/**
 * Defines an entity view which can be controlled via templates,
 * views have unique view keys related to them
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EntityView {

   public static final char SEPARATOR = TemplateParseUtil.SEPARATOR;

   public static final String PREFIX = TemplateParseUtil.PREFIX;
   public static final String ID = TemplateParseUtil.ID;

   private String entityURL;

   private String extension;
   /**
    * @return the extension for this view if there is one,
    * this will be null if there was no extension in the original entityUrl
    */
   public String getExtension() {
      return extension;
   }

   private String viewKey;
   /**
    * @return the key which uniquely identifies the view we are associated with
    */
   public String getViewKey() {
      return viewKey;
   }

   private EntityReference entityReference;
   /**
    * @return the entity reference object which indicates which entity this view related to
    */
   public EntityReference getEntityReference() {
      return entityReference;
   }

   /**
    * Contains all path segments for this entity reference,
    * e.g. /prefix/id/thing would cause this to contain 3 segments
    * for the prefix, id, and thing as long as one of the parse templates
    * supported that path
    */
   private Map<String, String> pathSegments;

   /**
    * Contains the parsing templates for this entity reference
    */
   private List<Template> parseTemplates;
   /**
    * Cache the parsed templates for this EB
    */
   private List<PreProcessedTemplate> anazlyzedTemplates;

   public EntityView() {
      loadParseTemplates(null);
   }

   /**
    * Constructor which takes an entity URL path,
    * (should not include anything but the path, for example:
    * http://server/webapp/myprefix/edit/3/stuff.xml?system=down
    * would yield the path: /myprefix/edit/3/stuff.xml<br/>
    * <b>NOTE:</b> this is the most common way to construct an entity view object
    * 
    * @param entityURL a URL path which goes to a specific entity view,
    * consists of path segments defined by path templates and includes an option extension
    * and 
    */
   public EntityView(String entityURL) {
      this();
      this.entityURL = entityURL;
      checkEntityUrl(entityURL);
      ProcessedTemplate parsed = TemplateParseUtil.parseTemplate(entityURL, anazlyzedTemplates);

      if (parsed == null) {
         throw new IllegalArgumentException("Could not parse entityURL against any known templates: " + entityURL);
      }

      String viewKey = parsed.templateKey;
      populateInternals(viewKey, new HashMap<String, String>(parsed.segmentValues), null );
   }

   /**
    * Turn this viewKey and map of segments (key -> value pairs) into an entity view object
    * 
    * @param viewKey a key which uniquely identifies a view, 
    * from the set of template keys {@link #PARSE_TEMPLATE_KEYS}
    * @param segments a map of replaceable keys (e.g. {@link #PREFIX}) to values,
    * the replaceable variable names -> values (e.g. "prefix" -> "myPrefix"),
    * must contain at LEAST a key for the prefix (use constant {@link #PREFIX}) which is not set to null
    * @param extension an optional extension related to this view (e.g. xml), do not include the period,
    * leave this null for no extension
    */
   public EntityView(String viewKey, Map<String, String> segments, String extension) {
      this();
      if (segments == null || segments.isEmpty()) {
         throw new IllegalArgumentException("segments map cannot be null or empty");
      }
      TemplateParseUtil.checkTemplateKey(viewKey);
      populateInternals(viewKey, segments, extension);
   }

   /**
    * Populates the internal values based on the view key, map of segments, and extension
    */
   protected void populateInternals(String viewKey, Map<String, String> segments, String extension) {
      this.viewKey = viewKey;
      this.extension = extension;
      this.pathSegments = new HashMap<String, String>();
      this.pathSegments.putAll(segments);

      String prefix = null;
      String id = null;
      for (String key : segments.keySet()) {
         if (PREFIX.equals(key)) {
            prefix = segments.get(key);
         } else if (ID.equals(key)) {
            id = segments.get(key);
         }
      }
      if (prefix == null) {
         throw new IllegalArgumentException("Cannot identify an entity space for this view," +
         		"there appears to be no prefix that was parsed from the ");
      }
      this.entityReference = new EntityReference(prefix, id == null ? "" : id);
   }

   // METHODS

   /**
    * Override this method if creating a custom EntityReference object
    * 
    * @param segments a map of template constants -> parse templates,
    * the array which defines the set of template keys is {@link #PARSE_TEMPLATE_KEYS}<br/>
    * Rules for parse templates:
    * 1) "{","}", and {@link #SEPARATOR} are special characters and must be used as indicated only
    * 2) Must begin with a {@link #SEPARATOR}, must not end with a {@link #SEPARATOR}
    * 3) each {var} can only be used once in a template
    * 4) {var} can never touch each other (i.e /{var1}{var2}/{id} is invalid)
    * 5) each {var} can only have the chars from {@link TemplateParseUtil#VALID_VAR_CHARS}
    * 6) parse templates can only have the chars from {@link TemplateParseUtil#VALID_TEMPLATE_CHARS}
    * 7) Empty braces ({}) cannot appear in the template
    */
   public void loadParseTemplates(List<Template> templates) {
      if (parseTemplates == null) {
         parseTemplates = new ArrayList<Template>();
      }
      if (templates == null) {
         templates = new ArrayList<Template>();
      }
      for (int i = 0; i < TemplateParseUtil.PARSE_TEMPLATE_KEYS.length; i++) {
         String key = TemplateParseUtil.PARSE_TEMPLATE_KEYS[i];
         String template = null;
         for (Template t : templates) {
            if (key.equals(t.templateKey)) {
               template = t.template;
               break;
            }
         }
         if (template == null) {
            // load a default template
            template = TemplateParseUtil.getDefaultTemplate(key);
         }
         TemplateParseUtil.validateTemplate(template);
         parseTemplates.add( new Template(key, template) );
      }
      anazlyzedTemplates = TemplateParseUtil.preprocessTemplates(parseTemplates);
   }

   /**
    * @return the string version of this {@link TemplateParseUtil#TEMPLATE_SHOW} entity reference or 
    * the {@link TemplateParseUtil#TEMPLATE_LIST} one if there is no id,
    * example: /prefix if there is no id or /prefix/id if there is an id
    */
   @Override
   public String toString() {
      String ref = getEntityUrl(this.viewKey, this.extension);
      return ref;
   }

   /**
    * Get an entityUrl by merging a specific template with the data in this EB object
    * 
    * @param viewKey a key which uniquely identifies a view, 
    * from the set of template keys {@link #PARSE_TEMPLATE_KEYS}
    * @param extension an optional extension related to this view (e.g. xml), 
    * do not include the period, leave this null for no extension
    * @return the entityUrl which goes to this view
    * @throws IllegalArgumentException if there is not enough information 
    * in the path segments to generate the requested URL
    */
   public String getEntityUrl(String viewKey, String extension) {
      String template = getParseTemplate(viewKey);
      if (template == null) {
         throw new IllegalStateException("parseTemplates contains no template for key: " + viewKey);
      }
      return TemplateParseUtil.mergeTemplate(template, pathSegments);
   }

   /**
    * @return the original entity URL which was used to create this entity view,
    * includes the optional pieces from the URL
    */
   public String getOriginalEntityUrl() {
      String url = this.entityURL;
      if (url == null) {
         url = toString();
      }
      return url;
   }

   /**
    * Gets the parsed values of path segment variables
    * @param parseVariable a path segment variable (the thing that gets replaced in the parse template)
    * @return the value of this parse variable or null if no value exists
    */
   public String getPathSegment(String parseVariable) {
      return pathSegments.get(parseVariable);
   }

   /**
    * @param templateKey a key from the set of template keys {@link #PARSE_TEMPLATE_KEYS},
    * should match with the viewKey
    * @return the template being used by this entity view for this key or null if none found
    */
   public String getParseTemplate(String templateKey) {
      TemplateParseUtil.checkTemplateKey(templateKey);
      String template = null;
      for (Template t : parseTemplates) {
         if (templateKey.equals(t.templateKey)) {
            template = t.template;
         }
      }
      return template;
   }


   // STATIC METHODS

   /**
    * Check if an entityUrl is basically valid
    * @param entityUrl
    * @throws IllegalArgumentException if the entityUrl is not even basically valid
    */
   protected static void checkEntityUrl(String entityUrl) {
      if (entityUrl == null 
            || "".equals(entityUrl)
            || SEPARATOR != entityUrl.charAt(0) )
      throw new IllegalArgumentException("Invalid entity Url for EntityBroker: "
            + entityUrl + " - these begin with " + SEPARATOR + " and cannot be null");      
   }

}
