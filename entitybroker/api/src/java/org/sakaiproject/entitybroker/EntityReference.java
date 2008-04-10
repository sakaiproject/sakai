/**
 * $Id$
 * $URL$
 * AutoRegister.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 **/

package org.sakaiproject.entitybroker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.entitybroker.util.TemplateParseUtil.PreProcessedTemplate;
import org.sakaiproject.entitybroker.util.TemplateParseUtil.ProcessedTemplate;
import org.sakaiproject.entitybroker.util.TemplateParseUtil.Template;

/**
 * The base class of all Entity references handled by the EntityBroker system. This provides the
 * minimal information of the entity prefix, which uniquely identifies the {@link EntityProvider}
 * responsible for handling the Entity. It also provides for all parsing methods for
 * entity references and can provide the complete set of path segments in a reference
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EntityReference {

   public static final char SEPARATOR = TemplateParseUtil.SEPARATOR;

   public static final String PREFIX = TemplateParseUtil.PREFIX;
   public static final String ID = TemplateParseUtil.ID;

   /**
    * An entity prefix, should match with the prefix handled in an {@link EntityProvider}
    */
   public String prefix;

   /**
    * A local entity id, represents an entity uniquely in a tool/webapp, 
    * could match with the actual id of a model data object
    */
   public String id;

   /**
    * The templateKey for the current parsed reference
    */
   public String templateKey;

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


   // CONSTRUCTORS

   public EntityReference() {
      loadParseTemplates(null);
   }

   /**
    * Constructor which takes an entity reference
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional path segments
    */
   public EntityReference(String reference) {
      this();
      checkReference(reference);
      ProcessedTemplate parsed = TemplateParseUtil.parseTemplate(reference, anazlyzedTemplates);

      if (parsed == null) {
         throw new IllegalArgumentException("Could not parse reference against any known templates: " + reference);
      }

      String templateKey = parsed.templateKey;
      populateInternals(templateKey, new HashMap<String, String>(parsed.segmentValues) );
   }

   /**
    * Turn this templateKey and map of segments (key -> value pairs) into an entity
    * reference object
    * 
    * @param templateKey a key from the set of template keys {@link #PARSE_TEMPLATE_KEYS}
    * @param segments a map of keys (e.g. {@link #PREFIX}) to values
    */
   public EntityReference(String templateKey, Map<String, String> segments) {
      this();
      if (segments == null || segments.isEmpty()) {
         throw new IllegalArgumentException("segments map cannot be null or empty");
      }
      TemplateParseUtil.checkTemplateKey(templateKey);
      populateInternals(templateKey, segments);
   }

   /**
    * Populates the internal values based on the key and map of segments
    * @param templateKey a key from the set of template keys {@link #PARSE_TEMPLATE_KEYS}
    * @param segments
    */
   protected void populateInternals(String templateKey, Map<String, String> segments) {
      for (String key : segments.keySet()) {
         if (PREFIX.equals(key)) {
            this.prefix = segments.get(key);
         } else if (ID.equals(key)) {
            this.id = segments.get(key);
         }
      }
      this.templateKey = templateKey;
      this.pathSegments = new HashMap<String, String>();
      this.pathSegments.putAll(segments);
   }

   /**
    * Convenience constructor for when you know the prefix and/or id
    * and are using default parse templates
    * 
    * @param prefix the entity prefix, 
    * will be applied to the {@link #PREFIX} segment
    * @param id the local entity id (can be empty string if there is no id),
    * will be applied to the {@link #ID} segment
    */
   public EntityReference(String prefix, String id) {
      this();
      if (prefix == null || prefix.equals("") || id == null) {
         throw new IllegalArgumentException("prefix and id cannot be null (prefix cannot be empty) to get entity reference");
      }
      if (! prefix.matches(TemplateParseUtil.VALID_VAR_CHARS+"+") 
            || ! id.matches(TemplateParseUtil.VALID_VAR_CHARS+"+") ) {
         throw new IllegalArgumentException("prefix and id must contain only valid chars: " + TemplateParseUtil.VALID_VAR_CHARS);
      }

      this.prefix = prefix;
      if ("".equals(id)) { id = null; }
      this.id = id;

      // we know we are using the show template
      this.templateKey = TemplateParseUtil.TEMPLATE_SHOW;

      this.pathSegments = new HashMap<String, String>();
      this.pathSegments.put(PREFIX, prefix);
      if (id != null && ! "".equals(id)) {
         this.pathSegments.put(ID, id);
      }
   }


   // METHODS

   /**
    * Override this method if creating a custom EntityReference object
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
      String ref = null;
      if (id == null || "".equals(id)) {
         ref = getReference(TemplateParseUtil.TEMPLATE_LIST);
      } else {
         ref = getReference(TemplateParseUtil.TEMPLATE_SHOW);
      }
      return ref;
   }

   /**
    * Get a reference by merging a specific template with the data in this EB object
    * @param templateKey a key from the set of template keys {@link #PARSE_TEMPLATE_KEYS}
    * @return the reference that matches this
    */
   public String getReference(String templateKey) {
      String template = getParseTemplate(templateKey);
      if (template == null) {
         throw new IllegalStateException("parseTemplates contains no template for key: " + templateKey);
      }
      return TemplateParseUtil.mergeTemplate(template, pathSegments);
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
    * @param templateKey a key from the set of template keys {@link #PARSE_TEMPLATE_KEYS}
    * @return the template being used by this entity reference for this key or null if none found
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
    * @param reference
    * @return
    * @deprecated do not use this method anymore
    */
   public static String getPrefix(String reference) {
      return new EntityReference(reference).prefix;
   }

   /**
    * Check if a reference is basically valid
    * @param reference
    * @throws IllegalArgumentException if the reference is not even basically valid
    */
   protected static void checkReference(String reference) {
      if (reference == null 
            || "".equals(reference)
            || SEPARATOR != reference.charAt(0) )
      throw new IllegalArgumentException("Invalid entity reference for EntityBroker: "
            + reference + " - these begin with " + SEPARATOR + " and cannot be null");      
   }

}
