/**
 * $Id$
 * $URL$
 * TemplateParseUtil.java - entity-broker - Apr 10, 2008 9:57:29 AM - azeckoski
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

package org.sakaiproject.entitybroker.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;

/**
 * Utility class to handle the URL template parsing (entity template parsing)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TemplateParseUtil {

   public static final char SEPARATOR = EntityView.SEPARATOR;
   public static final char PERIOD = EntityView.PERIOD;
   public static final String BRACES = "[\\{\\}]";

   /**
    * The entity prefix marker (Example value: "myprefix")
    */
   public static final String PREFIX = EntityView.PREFIX;
   /**
    * The entity ID marker (Example value: "123")
    */
   public static final String ID = EntityView.ID;
   /**
    * The entity extension (format) marker (Example value: "xml")
    */
   public static final String EXTENSION = "extension";
   /**
    * The extension with a period in front marker (Example value: ".xml")
    */
   public static final String DOT_EXTENSION = "dot-extension";
   /**
    * The value in the query string (without a leading ?), '' if non available (Example value: "auto=true")
    */
   public static final String QUERY_STRING = "query-string";
   /**
    * The value in the query string (with a leading ?), '' if non available (Example value: "?auto=true")
    */
   public static final String QUESTION_QUERY_STRING = "question-query-string";
   public static final String PREFIX_VARIABLE = "{"+PREFIX+"}";
   public static final String TEMPLATE_PREFIX = SEPARATOR + PREFIX_VARIABLE;
   public static final String DIRECT_PREFIX = EntityView.DIRECT_PREFIX;
   public static final String DIRECT_PREFIX_SLASH = DIRECT_PREFIX + SEPARATOR;


   /**
    * Defines the valid chars for a replacement variable
    */
   public static final String VALID_VAR_CHARS = "[A-Za-z0-9\\\\(\\\\)\\+\\*\\.\\-_=,:;!~@% ]";
   /**
    * Defines the valid chars for a parser input (e.g. entity reference)
    */
   public static final String VALID_INPUT_CHARS = "[A-Za-z0-9\\\\(\\\\)\\+\\*\\.\\-_=,:;!~@% "+SEPARATOR+"]";
   /**
    * Defines the valid chars for a template
    */
   public static final String VALID_TEMPLATE_CHARS = "[A-Za-z0-9\\\\(\\\\)\\+\\*\\.\\-_=,:;&!~@%"+SEPARATOR+"\\{\\}]";
   /**
    * Defines the valid template chars for an outgoing template (allows ?)
    */
   public static final String VALID_TEMPLATE_CHARS_OUTGOING = "[A-Za-z0-9\\\\(\\\\)\\+\\*\\.\\-_=,:;&!~@%"+SEPARATOR+"\\{\\}\\?]";


   /**
    * Defines the parse template for the "list" operation,
    * return a list of all records,
    * typically /{prefix}
    */
   public static final String TEMPLATE_LIST = EntityView.VIEW_LIST;
   /**
    * Defines the parse template for the "show" operation,
    * access a record OR POST operations related to a record,
    * typically /{prefix}/{id}
    */
   public static final String TEMPLATE_SHOW = EntityView.VIEW_SHOW;
   /**
    * Defines the parse template for the "new" operation,
    * return a form for creating a new record,
    * typically /{prefix}/new
    */
   public static final String TEMPLATE_NEW  = EntityView.VIEW_NEW;
   /**
    * Defines the parse template for the "edit" operation,
    * access the data to modify a record,
    * typically /{prefix}/{id}/edit
    */
   public static final String TEMPLATE_EDIT = EntityView.VIEW_EDIT;
   /**
    * Defines the parse template for the "delete" operation,
    * access the data to remove a record,
    * typically /{prefix}/{id}/delete
    */
   public static final String TEMPLATE_DELETE = EntityView.VIEW_DELETE;

   /**
    * Defines the order that parse templates will be processed in and
    * the set of parse template types (keys) which must be defined,
    * the first one to match will be used when parsing in a path
    */
   public static final String[] PARSE_TEMPLATE_KEYS = {
      TEMPLATE_EDIT,
      TEMPLATE_DELETE,
      TEMPLATE_NEW,
      TEMPLATE_SHOW,
      TEMPLATE_LIST
   };

   /**
    * Stores the preloaded default templates
    */
   public static final List<Template> defaultTemplates;
   /**
    * Stores the preloaded processed default templates
    */
   public static final List<PreProcessedTemplate> defaultPreprocessedTemplates;
   /**
    * Contains a set of all the common extensions
    */
   public static Set<String> commonExtensions = new HashSet<String>(20);

   // static initializer
   static {
      defaultTemplates = new ArrayList<Template>();
      // this load order should match the array above
      defaultTemplates.add( new Template(TEMPLATE_EDIT, TEMPLATE_PREFIX + SEPARATOR + "{"+ID+"}" + SEPARATOR + "edit") );
      defaultTemplates.add( new Template(TEMPLATE_DELETE, TEMPLATE_PREFIX + SEPARATOR + "{"+ID+"}" + SEPARATOR + "delete") );
      defaultTemplates.add( new Template(TEMPLATE_NEW,  TEMPLATE_PREFIX + SEPARATOR + "new") );
      defaultTemplates.add( new Template(TEMPLATE_SHOW, TEMPLATE_PREFIX + SEPARATOR + "{"+ID+"}") );
      defaultTemplates.add( new Template(TEMPLATE_LIST, TEMPLATE_PREFIX) );

      defaultPreprocessedTemplates = preprocessTemplates(defaultTemplates);

      // populate the list of common extensions
      Collections.addAll(commonExtensions, Formats.JSON_EXTENSIONS);
      Collections.addAll(commonExtensions, Formats.XML_EXTENSIONS);
      Collections.addAll(commonExtensions, Formats.HTML_EXTENSIONS);
      Collections.addAll(commonExtensions, Formats.FORM_EXTENSIONS);
      Collections.addAll(commonExtensions, Formats.JSONP_EXTENSIONS);
      Collections.addAll(commonExtensions, Formats.ATOM_EXTENSIONS);
      Collections.addAll(commonExtensions, Formats.RSS_EXTENSIONS);
      // also image extensions and other common ones
      commonExtensions.add("png");
      commonExtensions.add("jpg");
      commonExtensions.add("gif");
      commonExtensions.add("jpeg");
      commonExtensions.add("csv");
   }

   /**
    * Check if a templateKey is valid, if not then throws {@link IllegalArgumentException}
    * @param templateKey a key from the set of template keys {@link #PARSE_TEMPLATE_KEYS}
    */
   public static void validateTemplateKey(String templateKey) {
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
    * Get a default template for a specific template key
    * @param templateKey a key from the set of template keys {@link #PARSE_TEMPLATE_KEYS}
    * @return the template
    * @throws IllegalArgumentException if the template key is invalid
    */
   public static String getDefaultTemplate(String templateKey) {
      String template = null;
      for (Template t : defaultTemplates) {
         if (t.templateKey.equals(templateKey)) {
            template = t.template;
         }
      }
      if (template == null) {
         throw new IllegalArgumentException("No default template available for this key: " + templateKey);
      }
      return template;
   }

   /**
    * Validate a template, if invalid then an exception is thrown
    * @param template a parse template
    */
   public static void validateTemplate(String template) {
      if (template == null || "".equals(template)) {
         throw new IllegalArgumentException("Template cannot be null or empty string");
      } else if (template.charAt(0) != SEPARATOR) {
         throw new IllegalArgumentException("Template ("+template+") must start with " + SEPARATOR);
      } else if (template.charAt(template.length()-1) == SEPARATOR) {
         throw new IllegalArgumentException("Template ("+template+") cannot end with " + SEPARATOR);
      } else if (! template.startsWith(TEMPLATE_PREFIX)) {
         throw new IllegalArgumentException("Template ("+template+") must start with: " + TEMPLATE_PREFIX 
               + " :: that is SEPARATOR + \"{\"+PREFIX+\"}\"");
      } else if (template.indexOf("}{") != -1) {
         throw new IllegalArgumentException("Template ("+template+") replacement variables ({var}) " +
               "cannot be next to each other, " +
               "there must be something between each template variable");
      } else if (template.indexOf("{}") != -1) {
         throw new IllegalArgumentException("Template ("+template+") replacement variables ({var}) " +
               "cannot be empty ({}), there must be a value between them");
      } else if (! template.matches(VALID_TEMPLATE_CHARS+"+")) {
         // take out {} and check if the template uses valid chars
         throw new IllegalArgumentException("Template ("+template+") can only contain the following (not counting []): " + VALID_TEMPLATE_CHARS);
      }
   }

   /**
    * Validates an outgoing template to make sure it is valid
    * @param template an outgoing template,
    * if starts with / then it will be used as is and redirected to,
    * otherwise it will have the direct URL prefixed and will be forwarded
    * @return the template which should be completely valid
    */
   public static String validateOutgoingTemplate(String template) {
      String validTemplate = template;
      if (template == null || "".equals(template)) {
         throw new IllegalArgumentException("Template cannot be null or empty string");
      } else if (template.indexOf("{}") != -1) {
         throw new IllegalArgumentException("Template ("+template+") replacement variables ({var}) " +
               "cannot be empty ({}), there must be a value between them");
      } else if (! template.matches(VALID_TEMPLATE_CHARS_OUTGOING+"+")) {
         // take out {} and check if the template uses valid chars
         throw new IllegalArgumentException("Template ("+template+") can only contain the following (not counting []): " + VALID_TEMPLATE_CHARS_OUTGOING);
      }
      if (template.startsWith(PREFIX_VARIABLE)) {
         validTemplate = DIRECT_PREFIX + SEPARATOR + template;
      } else if (template.startsWith(TEMPLATE_PREFIX)) {
         validTemplate = DIRECT_PREFIX + template;
      } else if (SEPARATOR != template.charAt(0)) {
         // assume the user wants /direct/ added to the URL
         validTemplate = DIRECT_PREFIX + SEPARATOR + template;
      }
      return validTemplate;
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
    * @throws IllegalArgumentException if all template variables cannot be replaced or template is empty/null
    */
   public static String mergeTemplate(String template, Map<String, String> segments) {
      if (template == null || "".equals(template) || segments == null) {
         throw new IllegalArgumentException("Cannot operate on null template/segments, template must not be empty");
      }

      int vars = 0;
      char[] chars = template.toCharArray();
      for (int i = 0; i < chars.length; i++) {
         if (chars[i] == '{') {
            vars++;
         }
      }
      String reference = template;
      int replacements = 0;
      for (Entry<String, String> es : segments.entrySet()) {
         String keyBraces = "{"+es.getKey()+"}";
         if (reference.contains(keyBraces)) {
            reference = reference.replace(keyBraces, es.getValue());
            replacements++;
         }
      }
      if (replacements != vars) {
         throw new IllegalArgumentException("Failed merge, could not replace all variables ("+vars
               +") in the template, only replaced " + replacements);
      }

      return reference;
   }


   /**
    * Find the extension from a string and return the string without the extension and the extension,
    * an extension is a period (".") followed by any number of non-periods,
    * the original input is returned as the 0th item in the array <br/>
    * returned array contains 3 strings: <br/>
    * 0 = the original input string <br/>
    * 1 = the string without the extension or the original if it has none <br/>
    * 2 = the extension OR null if there is no extension <br/>
    * 
    * @param input any string
    * @return an array with the string without the extension or the original if it has none in position 1
    * and the extension in the position 2 (or null if no extension), position 0 holds the original input string
    */
   public static String[] findExtension(String input) {
      // regex pattern: ".*(\\.[^.]+|$)"
      String stripped = input;
      String extension = null;
      if (input != null) {
         int extensionLoc = input.lastIndexOf(PERIOD, input.length());
         if (extensionLoc == 0) {
             // starts with a period so no extension, do nothing
         } else {
             int sepLoc = input.lastIndexOf(SEPARATOR, input.length());
             if (extensionLoc > 0 
                     && sepLoc < extensionLoc) {
                stripped = input.substring(0, extensionLoc);
                if ( (input.length() - 1) > extensionLoc) {
                   extension = input.substring(extensionLoc + 1);
                   // we only consider it an extension if we recognize the type
                   if (!commonExtensions.contains(extension)) {
                       stripped = input;
                       extension = null;
                   }
                }
             }
         }
      }
      return new String[] {input, stripped, extension};
   }

   /**
    * Parse a string and attempt to match it to a template and then 
    * return the match information along with all the parsed out keys and values<br/>
    * 
    * @param input a string which we want to attempt to match to one of the templates
    * @param preprocessed the analyzed templates to attempt to match in the order they should attempt the match, 
    * can be a single template or multiples, use {@link #preprocessTemplates(List)} to create this
    * (recommend caching the preprocessed templates to avoid reprocessing them over and over)
    * @return a the processed template analysis object OR null if no matches
    */
   public static ProcessedTemplate parseTemplate(String input, List<PreProcessedTemplate> preprocessed) {
      if (preprocessed == null) {
         preprocessed = defaultPreprocessedTemplates;
      }
      if (input == null || "".equals(input)) {
         throw new IllegalArgumentException("input cannot be null or empty");
      }
      if (! input.matches(VALID_INPUT_CHARS+"+")) {
         throw new IllegalArgumentException("input must consist of the following chars only (not counting []): " + VALID_INPUT_CHARS);         
      }
      ProcessedTemplate analysis = null;
      Map<String, String> segments = new HashMap<String, String>();
      // strip off the extension if there is one
      String[] ext = findExtension(input);
      input = ext[1];
      String extension = ext[2];
      // try to get matches
      for (PreProcessedTemplate ppt : preprocessed) {
         segments.clear();
         String regex = ppt.regex + "(?:/"+VALID_INPUT_CHARS+"+|$)"; // match extras if there are any (greedy match)
         Pattern p = Pattern.compile(regex);
         Matcher m = p.matcher(input);
         if ( m.matches() ) {
            if ( m.groupCount() == ppt.variableNames.size() ) {
               for (int j = 0; j < m.groupCount(); j++) {
                  String subseq = m.group(j+1); // ignore first group, it is the whole pattern
                  if (subseq != null) {
                     segments.put(ppt.variableNames.get(j), subseq);
                  }
               }
               // fill in the analysis object
               analysis = new ProcessedTemplate(ppt.templateKey, ppt.template, regex, 
                     new ArrayList<String>(ppt.variableNames), 
                     new HashMap<String, String>(segments), extension);
               break;
            }
         }
      }

      if (analysis == null) {
         // no matches so should we die?
      }
      return analysis;
   }

   /**
    * Process the templates before attempting to match them,
    * this is here so we can reduce the load of reprocessing the same templates over and over
    * @param templates the templates to attempt to preprocess, can be a single template or multiples
    * @return the list of preprocessed templates (in the same order as input)
    */
   public static List<PreProcessedTemplate> preprocessTemplates(List<Template> templates) {
      if (templates == null) {
         templates = defaultTemplates;
      }
      List<PreProcessedTemplate> analyzedTemplates = new ArrayList<PreProcessedTemplate>();
      for (Template t : templates) {
         analyzedTemplates.add( preprocessTemplate(t) );
      }      
      return analyzedTemplates;
   }

   /**
    * process a template into a preprocessed template which can be cached
    * @param t the template
    * @return the preprocessed template
    */
   public static PreProcessedTemplate preprocessTemplate(Template t) {
      if (t.incoming) {
         TemplateParseUtil.validateTemplate(t.template);
      } else {
         t.template = TemplateParseUtil.validateOutgoingTemplate(t.template);
      }
      List<String> vars = new ArrayList<String>();
      StringBuilder regex = new StringBuilder();
      String[] parts = t.template.split(BRACES);
      for (int j = 0; j < parts.length; j++) {
         String part = parts[j];
         if (j % 2 == 0) {
            // odd parts are textual breaks
            // check for regex chars and escape them "[A-Za-z0-9\\\\(\\\\)\\.\\-_.=,:;&!~"+SEPARATOR+"\\{\\}\\?]"
            regex.append(part.replace("?", "\\?").replace(".", "\\.").replace("*", "\\*").replace("+", "\\+")
                  .replace("-", "\\-").replace("(", "\\\\(").replace(")", "\\\\)"));
         } else {
            // even parts are replacement vars
            vars.add(part);
            regex.append("(");
            regex.append(VALID_VAR_CHARS);
            regex.append("+)");
         }
      }
      return new PreProcessedTemplate(t.templateKey, 
            t.template, regex.toString(), new ArrayList<String>(vars));
   }

   /**
    * Represents a parseable template (which is basically a key and the template string),
    * the array which defines the set of template keys is {@link #PARSE_TEMPLATE_KEYS}<br/>
    * Rules for parse templates:
    * 1) "{","}", and {@link #SEPARATOR} are special characters and must be used as indicated only
    * 2) Must begin with a {@link #SEPARATOR}, must not end with a {@link #SEPARATOR}
    * 3) must begin with "/{prefix}" (use the {@link #SEPARATOR} and {@link #PREFIX} constants)
    * 3) each {var} can only be used once in a template
    * 4) {var} can never touch each other (i.e /{var1}{var2}/{id} is invalid)
    * 5) each {var} can only have the chars from {@link TemplateParseUtil#VALID_VAR_CHARS}
    * 6) parse templates can only have the chars from {@link TemplateParseUtil#VALID_TEMPLATE_CHARS}
    * 7) Empty braces ({}) cannot appear in the template
    * 
    * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
    */
   public static class Template {
      /**
       * the template key, from the set of template keys {@link #PARSE_TEMPLATE_KEYS},
       * or make one up for your own templates, should be unique for this set of templates
       */
      public String templateKey;
      /**
       * the template itself
       */
      public String template;
      /**
       * indicates the template is an incoming template if true, outgoing template if false
       */
      public boolean incoming = true;

      /**
       * Used to create a template for loading, defaults to an incoming template
       * @param templateKey template identifier, from the set of template keys {@link #PARSE_TEMPLATE_KEYS},
       * must be unique for this set of templates
       * @param template the parseable template
       */
      public Template(String templateKey, String template) {
         if (templateKey == null || "".equals(templateKey)) {
            templateKey = UUID.randomUUID().toString();
         }
         this.templateKey = templateKey;
         this.template = template;
      }

      /**
       * Used to create a template for loading
       * @param templateKey template identifier, from the set of template keys {@link #PARSE_TEMPLATE_KEYS},
       * must be unique for this set of templates
       * @param template the parseable template
       * @param incoming if true then this is an incoming template, otherwise it is an outgoing one
       */
      public Template(String templateKey, String template, boolean incoming) {
         this(templateKey, template);
         this.incoming = incoming;
      }

      @Override
      public boolean equals(Object obj) {
         if (null == obj) return false;
         if (!(obj instanceof Template)) return false;
         else {
            Template castObj = (Template) obj;
            if (null == this.templateKey || null == castObj.templateKey) return false;
            else return (
                  this.templateKey.equals(castObj.templateKey)
            );
         }
      }

      @Override
      public int hashCode() {
         if (null == this.templateKey) return super.hashCode();
         String hashStr = this.getClass().getName() + ":" + this.templateKey.hashCode();
         return hashStr.hashCode();
      }
   }

   /**
    * Contains the data for templates,
    * each template must have a template key and the template itself
    * 
    * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
    */
   public static class PreProcessedTemplate extends Template {
      /**
       * The regular expression to match this template exactly 
       */
      public String regex;
      /**
       * The list of variable names found in this template
       */
      public List<String> variableNames;

      protected PreProcessedTemplate(String templateKey, String template, String regex, List<String> variableNames) {
         super(templateKey, template);
         this.regex = regex;
         this.variableNames = variableNames;
      }
   }

   /**
    * Contains the processed template with the values from the processed input string
    * that was determined to be related to this template
    * 
    * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
    */
   public static class ProcessedTemplate extends PreProcessedTemplate {
      /**
       * The list of segment values (variableName -> matched value),
       * this will be filled in by the {@link TemplateParseUtil#parseTemplate(String, Map)} method
       * and will be null otherwise
       */
      public Map<String, String> segmentValues;
      /**
       * The extension found while processing the input string,
       * null if none could be found
       */
      public String extension;

      public ProcessedTemplate(String templateKey, String template, String regex,
            List<String> variableNames, Map<String, String> segmentValues, String extension) {
         super(templateKey, template, regex, variableNames);
         this.segmentValues = segmentValues;
         this.extension = extension;
      }
   }

}
