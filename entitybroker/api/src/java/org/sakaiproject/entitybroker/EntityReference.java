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

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;

/**
 * The class of all Entity references handled by the EntityBroker system. This provides the
 * entity prefix, which uniquely identifies the {@link EntityProvider}
 * responsible for handling the Entity. It optionally provides the entity id which
 * uniquely identifies an entity locally within the prefix space. 
 * It also provides for all parsing methods for
 * entity references (always of the form /prefix or /prefix/id)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EntityReference {

   public static final char SEPARATOR = TemplateParseUtil.SEPARATOR;

   protected String originalReference;
   /**
    * This is a special method and should not normally be used,
    * use {@link #toString()} or {@link #getReference()}
    * @return the reference string used to create this object or
    * null if this was created using a constructor which does not supply a reference
    */
   public String getOriginalReference() {
      return originalReference;
   }
   protected void setOriginalReference(String reference) {
      checkReference(reference);
      this.originalReference = reference;
   }

   /**
    * An entity prefix, should match with the prefix handled in an {@link EntityProvider},
    * uniquely identifies an entity space or entity type
    */
   public String prefix;

   /**
    * A local entity id, represents an entity uniquely in a tool/webapp, 
    * could match with the actual id of a model data object,
    * this will be null if this reference refers to an entity space only
    */
   public String id;


   // CONSTRUCTORS

   public EntityReference() { }

   /**
    * Constructor which takes an entity reference,
    * this is the most common way to construct an entity reference object
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional path segments
    */
   public EntityReference(String reference) {
      this();
      checkReference(reference);
      originalReference = reference;
      prefix = findPrefix(reference);
      id = findId(reference);
   }

   /**
    * Convenience constructor for when you know the prefix and/or id
    * 
    * @param prefix the entity prefix
    * @param id the local entity id (can be empty string if there is no id)
    */
   public EntityReference(String prefix, String id) {
      this();
      checkPrefixId(prefix, id);

      this.prefix = prefix;
      if ("".equals(id)) { id = null; }
      this.id = id;
   }


   // METHODS

   /**
    * Get the string reference for this entity reference object,
    * same as calling {@link #toString()}
    * @return the full entity reference
    */
   public String getReference() {
      return this.toString();
   }

   /**
    * Get the space reference for this entity reference,
    * this ignored any id and only returns the reference to the entity space
    * @return the entity space reference (e.g. /myPrefix)
    */
   public String getSpaceReference() {
      return makeReference(true);
   }

   /**
    * @return the string version of this entity reference,
    * example: /prefix if there is no id or /prefix/id if there is an id
    */
   @Override
   public String toString() {
      return makeReference(false);
   }

   /**
    * Override this if you are making a new class to define your entity reference
    * 
    * @param spaceOnly if this is true then only return the entity space reference (e.g. /prefix),
    * otherwise return the full reference (e.g. /prefix/id)
    * @return an entity reference string
    */
   protected String makeReference(boolean spaceOnly) {
      if (prefix == null) {
         throw new IllegalStateException("prefix is null, cannot generate the string reference");
      }
      String ref = null;
      if (spaceOnly || id == null || "".equals(id)) {
         ref = SEPARATOR + prefix;
      } else {
         ref = SEPARATOR + prefix + SEPARATOR + id;
      }
      return ref;
   }

   // STATIC METHODS

   /**
    * Get the entity prefix based on a full entity reference
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional id
    * @return the entity prefix
    * @deprecated Do not use this anymore, use {@link #EntityReference(String, String)}
    */
   public static String getPrefix(String reference) {
      return findPrefix(reference);
   }

   /**
    * Get the entity prefix based on an entity reference
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional id
    * @return the entity prefix
    */
   protected static String findPrefix(String reference) {
      int spos = getSeparatorPos(reference, 1);
      return spos == -1 ? reference.substring(1) : reference.substring(1, spos);
   }

   /**
    * Get the local entity id based on a full entity reference
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional id
    * @return the local entity id or null if none can be found
    */
   protected static String findId(String reference) {
      String id = null;
      int spos = getSeparatorPos(reference, 1);
      if (spos != -1) {
         int spos2 = getSeparatorPos(reference, 2);
         id = spos2 == -1 ? reference.substring(spos + 1) : reference.substring(spos + 1, spos2);
      }
      return id;
   }

   /**
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional id
    * @param number this is the separator to get,
    * 0 would return the first one found, 1 would return the second
    * @return the location of the separator between the entity and the id or -1 if none found
    */
   protected static int getSeparatorPos(String reference, int number) {
      checkReference(reference);
      int position = 0;
      for (int i = 0; i < number; i++) {
         position = reference.indexOf(SEPARATOR, position+1);
         if (position < 0) {
            break;
         }
      }
      return position;
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
               + reference + " - these begin with /prefix, e.g. " + SEPARATOR + "myentity"
               + SEPARATOR + "3 OR " + SEPARATOR + "myentity");
   }

   /**
    * Checks this prefix and id to see if they are valid format, throw exceptions if they aren't
    */
   protected static void checkPrefixId(String prefix, String id) {
      if (prefix == null || prefix.equals("") || id == null) {
         throw new IllegalArgumentException("prefix and id cannot be null (prefix cannot be empty) to get entity reference");
      }
      if (! prefix.matches(TemplateParseUtil.VALID_VAR_CHARS+"+") 
            || ! id.matches(TemplateParseUtil.VALID_VAR_CHARS+"*") ) {
         throw new IllegalArgumentException("prefix and id must contain only valid chars: " + TemplateParseUtil.VALID_VAR_CHARS);
      }
   }

}
