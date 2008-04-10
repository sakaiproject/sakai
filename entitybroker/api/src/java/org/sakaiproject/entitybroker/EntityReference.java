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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * The base class of all Entity references handled by the EntityBroker system. This provides the
 * minimal information of the entity prefix, which uniquely identifies the {@link EntityProvider}
 * responsible for handling the Entity. It also provides for all parsing methods for
 * entity references and can provide the complete set of path segments in a reference
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class EntityReference {

   public static final char SEPARATOR = '/';

   public static final String PREFIX = "prefix";
   public static final String ID = "id";

   public static final String TEMPLATEKEY = "+templateKey";

   /**
    * Defines the parse template for the "new" operation,
    * return a form for creating a new record,
    * typically /{prefix}/new
    */
   public static final String TEMPLATE_NEW  = "new";
   /**
    * Defines the parse template for the "list" operation,
    * return a list of all records,
    * typically /{prefix}
    */
   public static final String TEMPLATE_LIST = "list";
   /**
    * Defines the parse template for the "show" operation,
    * access a record OR POST operations related to a record,
    * typically /{prefix}/{id}
    */
   public static final String TEMPLATE_SHOW = "show";
   /**
    * Defines the parse template for the "edit" operation,
    * access the data to modify a record,
    * typically /{prefix}/{id}/edit
    */
   public static final String TEMPLATE_EDIT = "edit";

   /**
    * Defines the order that parse templates will be processed in and
    * the set of parse template types (keys) which must be defined,
    * the first one to match will be used when parsing in a path
    */
   public static String[] PARSE_TEMPLATE_KEYS = {
      TEMPLATE_EDIT,
      TEMPLATE_SHOW,
      TEMPLATE_NEW,
      TEMPLATE_LIST
   };

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
   private Map<String, String> parseTemplates;


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
      Map<String, String> segments = parseTemplate(reference, parseTemplates);

      if (segments.isEmpty()) {
         throw new IllegalArgumentException("Could not parse reference against any known templates: " + reference);
      }

      String templateKey = segments.remove(TEMPLATEKEY);
      if (templateKey == null) {
         throw new IllegalStateException("Invalid state of segments map, could not retrieve the templateKey");
      }
      populateInternals(templateKey, segments);
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
      checkTemplateKey(templateKey);
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
      checkPrefixId(prefix, id);
      this.prefix = prefix;
      this.id = id;

      // we know we are using the show template
      this.templateKey = TEMPLATE_SHOW;

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
    */
   public void loadParseTemplates(Map<String, String> templates) {
      if (parseTemplates == null) {
         parseTemplates = new HashMap<String, String>();
      }
      if (templates == null) {
         templates = new HashMap<String, String>();
      }
      for (int i = 0; i < PARSE_TEMPLATE_KEYS.length; i++) {
         String key = PARSE_TEMPLATE_KEYS[i];
         if (templates.containsKey(key)) {
            String template = templates.get(key);
            validateTemplate(template);
            parseTemplates.put(key, template);
         } else {
            // load a default template
            parseTemplates.put(key, getDefaultTemplate(key));
         }
      }
   }

   /**
    * @return the string version of this "show" entity reference,
    * example: /prefix if there is no id or /prefix/id if there is an id
    */
   @Override
   public String toString() {
      String ref = null;
      if (id == null || "".equals(id)) {
         ref = getReference(TEMPLATE_LIST);
      } else {
         ref = getReference(TEMPLATE_SHOW);
      }
      return ref;
   }

   /**
    * @param templateKey
    * @return
    */
   public String getReference(String templateKey) {
      checkTemplateKey(templateKey);
      if (parseTemplates.containsKey(templateKey)) {
         String template = parseTemplates.get(templateKey);
         return mergeTemplate(template, pathSegments);
      } else {
         throw new IllegalStateException("parseTemplates contains no template for key: " + templateKey);
      }
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
      checkTemplateKey(templateKey);
      return parseTemplates.get(templateKey);
   }


   // STATIC METHODS

   /**
    * Check if a templateKey is valid, if not then throws {@link IllegalArgumentException}
    * @param templateKey a key from the set of template keys {@link #PARSE_TEMPLATE_KEYS}
    */
   public static void checkTemplateKey(String templateKey) {
      boolean found = false;
      for (int i = 0; i < PARSE_TEMPLATE_KEYS.length; i++) {
         if (PARSE_TEMPLATE_KEYS[i].equals(templateKey)) {
            found = true;
            break;
         }
      }
      if (! found) {
         throw new IllegalArgumentException("Invalid parse template key: " + templateKey);
      }
   }

   /**
    * Takes a template and replaces the segment keys with the segment values,
    * keys should not have {} around them yet as these will be added around each key
    * in the segments map
    * 
    * @param template a parse template with {variables} in it
    * @param segments a map of all possible segment keys and values,
    * unused keys will be ignored
    * @return the template with replacement values filled in
    */
   public static String mergeTemplate(String template, Map<String, String> segments) {
      String reference = template;
      for (String key : segments.keySet()) {
         String keyBraces = "{"+key+"}";
         if (reference.contains(keyBraces)) {
            reference = reference.replace(keyBraces, segments.get(key));
         }
      }
      return reference;
   }

   /**
    * Get a default template for a specific template key
    * @param templateKey a key from the set of template keys {@link #PARSE_TEMPLATE_KEYS}
    * @return the template
    * @throws IllegalArgumentException if the template key is invalid
    */
   public static String getDefaultTemplate(String templateKey) {
      String template = null;
      if (TEMPLATE_EDIT.equals(templateKey)) {
         template = SEPARATOR + "{"+PREFIX+"}" + SEPARATOR + "{"+ID+"}" + SEPARATOR + "edit";
      } else if (TEMPLATE_LIST.equals(templateKey)) {
         template = SEPARATOR + "{"+PREFIX+"}";
      } else if (TEMPLATE_NEW.equals(templateKey)) {
         template = SEPARATOR + "{"+PREFIX+"}" + SEPARATOR + "new";
      } else if (TEMPLATE_SHOW.equals(templateKey)) {
         template = SEPARATOR + "{"+PREFIX+"}" + SEPARATOR + "{"+ID+"}";
      } else {
         throw new IllegalArgumentException("No default template available for this key: " + templateKey);
      }
      return template;
   }

   /**
    * Parse a string and attempt to match it to a template and then 
    * return the key of the matching template
    * in the map as {@value #TEMPLATEKEY} -> key, 
    * along with all the parsed out keys and values<br/>
    * 
    * @param input a string which we want to attempt to match to one of the templates
    * @param templates the templates (templateKey -> template) to attempt to match, can be a single template or multiples
    * @return a map containing the matching template, templateKey, and all the replacement 
    * variable names and values OR empty map if no templates matched
    */
   public static Map<String, String> parseTemplate(String input, Map<String, String> templates) {
      Map<String, String> segments = new HashMap<String, String>();
      List<String> vars = new ArrayList<String>();
      for (int i = 0; i < PARSE_TEMPLATE_KEYS.length; i++) {
         vars.clear();
         String templateKey = PARSE_TEMPLATE_KEYS[i];
         if (templates.containsKey(templateKey)) {
            StringBuilder regex = new StringBuilder();
            String template = templates.get(templateKey);
            String[] parts = template.split("[\\{\\}]");
            for (int j = 0; j < parts.length; j++) {
               String part = parts[j];
               if (j % 2 == 0) {
                  // odd parts are textual breaks
                  regex.append(part);
               } else {
                  // even parts are replacement vars
                  vars.add(part);
                  regex.append("(.+)");
               }
            }
            Pattern p = Pattern.compile(regex.toString());
            Matcher m = p.matcher(input);
            if (m.matches() 
                  && m.groupCount() == vars.size()) {
               for (int j = 0; j < m.groupCount(); j++) {
                  String subseq = m.group(j+1);
                  if (subseq != null) {
                     segments.put(vars.get(j), subseq);
                  }
               }
               segments.put(TEMPLATEKEY, templateKey);
               //segments.put(TEMPLATE, template);
               break;
            }
         }
      }      
      return segments;
   }

   private static String VALID_CHARS = "[A-Za-z0-9_-="+SEPARATOR+"]+";
   /**
    * Validate a template, if invalid then an exception is thrown
    * @param template a parse template
    */
   public static void validateTemplate(String template) {
      if (! template.replace("{", "").replace("}", "").matches(VALID_CHARS)) {
         throw new IllegalArgumentException("Template can only contain the special chars (/{})," +
         		"and the following [A-Za-z0-9_-=]");
      } else if (template.charAt(0) != SEPARATOR) {
         throw new IllegalArgumentException("Template must start with " + SEPARATOR);
      } else if (template.charAt(template.length()) == SEPARATOR) {
         throw new IllegalArgumentException("Template cannot end with " + SEPARATOR);
      } else if (template.indexOf("}{") != -1) {
         throw new IllegalArgumentException("Template replacement variables ({var}) " +
         		"cannot be next to each other, " +
         		"there must be something between each template variable");
      }
   }

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

   /**
    * Checks this prefix to see if it is valid format, throw exceptions if not
    * 
    * @param prefix
    */
   protected static void checkPrefixId(String prefix, String id) {
      if (prefix == null || id == null) {
         throw new IllegalArgumentException("prefix and id cannot be null to get entity reference");
      }
      if (prefix.equals("")) {
         throw new IllegalArgumentException(
               "prefix cannot be empty strings to get entity reference");
      }
      if (prefix.indexOf(EntityReference.SEPARATOR) != -1
            || id.indexOf(EntityReference.SEPARATOR) != -1) {
         throw new IllegalArgumentException("prefix and id cannot contain separator: "
               + EntityReference.SEPARATOR);
      }
   }

}
