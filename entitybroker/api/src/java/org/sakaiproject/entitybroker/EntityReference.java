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
      prefix = getPrefix(reference);
      id = getID(reference);
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
    * @return the string version of this entity reference,
    * example: /prefix if there is no id or /prefix/id if there is an id
    */
   @Override
   public String toString() {
      if (prefix == null) {
         throw new IllegalStateException("prefix is null, cannot generate the string reference");
      }
      String ref = null;
      if (id == null || "".equals(id)) {
         ref = SEPARATOR + prefix;
      } else {
         ref = SEPARATOR + prefix + SEPARATOR + id;
      }
      return ref;
   }

   /**
    * Get the space reference for this entity reference,
    * this ignored any id and only returns the reference to the entity space
    * @return the entity space reference (e.g. /myPrefix)
    */
   public String getSpaceReference() {
      return SEPARATOR + prefix;
   }

   // STATIC METHODS

   /**
    * Get the entity prefix based on a full entity reference
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional id
    * @return the entity prefix
    */
   public static String getPrefix(String reference) {
      int spos = getSeparatorPos(reference);
      return spos == -1 ? reference.substring(1) : reference.substring(1, spos);
   }

   /**
    * Get the local entity id based on a full entity reference
    * 
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional id
    * @return the local entity id or null if none can be found
    */
   public static String getID(String reference) {
      String id = null;
      int spos = getSeparatorPos(reference);
      if (spos != -1) {
         int lspos = (reference.indexOf(EntityReference.SEPARATOR, spos + 1));
         id = lspos == -1 ? reference.substring(spos + 1) : reference.substring(spos + 1, lspos);
      }
      return id;
   }

   /**
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and optional id
    * @return the location of the separator between the entity and the id or -1 if none found
    */
   protected static int getSeparatorPos(String reference) {
      if (reference == null || reference.length() == 0 || reference.charAt(0) != SEPARATOR) {
         throw new IllegalArgumentException("Invalid entity reference for EntityBroker: "
               + reference + " - these begin with /prefix, e.g. " + SEPARATOR + "myentity"
               + SEPARATOR + "3 OR " + SEPARATOR + "myentity");
      }
      return reference.indexOf(SEPARATOR, 1);
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
