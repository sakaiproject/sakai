/**
 * EntityReference.java - created by antranig on 1 Jun 2007
 */

package org.sakaiproject.entitybroker;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * The base class of all Entity references handled by the EntityBroker system. This provides the
 * minimal information of the entity prefix, which uniquely identifies the {@link EntityProvider}
 * responsible for handling the Entity.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EntityReference {

   public static final char SEPARATOR = '/';

   /**
    * An entity prefix, should match with the prefix handled in an {@link EntityProvider}
    */
   public String prefix;

   public EntityReference() {
   }

   public EntityReference(String prefix) {
      checkPrefix(prefix);
      this.prefix = prefix;
   }

   /**
    * @param reference
    *           a globally unique reference to an entity, consists of the entity prefix and optional
    *           segments
    * @return the location of the separator between the entity and the id or -1 if none found
    */
   public static int getSeparatorPos(String reference) {
      if (reference == null || reference.length() == 0 || reference.charAt(0) != SEPARATOR) {
         throw new IllegalArgumentException("Invalid entity reference for EntityBroker: "
               + reference + " - these begin with /prefix, e.g. " + SEPARATOR + "myentity"
               + SEPARATOR + "3 OR " + SEPARATOR + "myentity");
      }
      return reference.indexOf(SEPARATOR, 1);
   }

   /**
    * Get the entity prefix based on a full entity reference
    * 
    * @param reference
    *           a globally unique reference to an entity, consists of the entity prefix and an
    *           optional suffix of unspecified size
    * @return the entity prefix
    */
   public static String getPrefix(String reference) {
      int spos = getSeparatorPos(reference);
      return spos == -1 ? reference.substring(1) : reference.substring(1, spos);
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   public String toString() {
      checkPrefix(prefix);
      return SEPARATOR + prefix;
   }

   /**
    * Checks this prefix to see if it is valid format, throw exceptions if not
    * 
    * @param prefix
    */
   private static void checkPrefix(String prefix) {
      if (prefix == null) {
         throw new IllegalArgumentException("prefix cannot be null to get entity reference");
      }
      if (prefix.equals("")) {
         throw new IllegalArgumentException(
               "prefix cannot be empty strings to get entity reference");
      }
      if (prefix.indexOf(EntityReference.SEPARATOR) != -1) {
         throw new IllegalArgumentException("prefix cannot contain separator: "
               + EntityReference.SEPARATOR);
      }
   }

}
