/**
 * IdEntityReference.java - created by antranig on 14 May 2007
 */

package org.sakaiproject.entitybroker;

/**
 * Represents a parsed form of a simple entity reference, as accepted to the {@link EntityBroker}
 * API. These are of the form /prefix/id - this parser will accept overlong references with
 * additional path segments, which will be ignored.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class IdEntityReference extends EntityReference {

   /**
    * A local entity id, represent an entity uniquely in a tool/webapp, could match with the actual
    * id of a model data object
    */
   public String id;

   /**
    * Constructor which takes an entity reference
    * 
    * @param reference
    *           a globally unique reference to an entity, consists of the entity prefix and the
    *           local id
    */
   public IdEntityReference(String reference) {
      prefix = getPrefix(reference);
      id = getID(reference);
   }

   /**
    * Full constructor
    * 
    * @param prefix
    *           the entity prefix
    * @param id
    *           the local entity id
    */
   public IdEntityReference(String prefix, String id) {
      checkPrefixId(prefix, id);
      this.prefix = prefix;
      this.id = id;
   }

   /**
    * Get the full entity reference (only works if id and prefix are set)
    * 
    * @return the full entity reference or throw error if there is not enough information (prefix
    *         and id) to build it
    */
   public String toString() {
      checkPrefixId(prefix, id);
      return EntityReference.SEPARATOR + prefix + EntityReference.SEPARATOR + id;
   }

   // STATIC METHODS

   /**
    * Get the local entity id based on a full entity reference
    * 
    * @param reference
    *           a globally unique reference to an entity, consists of the entity prefix and the
    *           local id
    * @return the local entity id
    */
   public static String getID(String reference) {
      int spos = getSeparatorPos(reference);
      if (spos == -1) {
         throw new IllegalArgumentException(
               "cannot find id, separator position cannot be found, reference appears to have no id");
      }
      int lspos = (reference.indexOf(EntityReference.SEPARATOR, spos + 1));
      return lspos == -1 ? reference.substring(spos + 1) : reference.substring(spos + 1, lspos);
   }

   /**
    * Checks this prefix and id to see if they are valid format, throw exceptions if they aren't
    * 
    * @param prefix
    * @param id
    */
   private static void checkPrefixId(String prefix, String id) {
      if (prefix == null || id == null) {
         throw new IllegalArgumentException("prefix and id cannot be null to get entity reference");
      }
      if (prefix.equals("") || id.equals("")) {
         throw new IllegalArgumentException(
               "prefix and id cannot be empty strings to get entity reference");
      }
      if (prefix.indexOf(EntityReference.SEPARATOR) != -1
            || id.indexOf(EntityReference.SEPARATOR) != -1) {
         throw new IllegalArgumentException("prefix and id cannot contain separator: "
               + EntityReference.SEPARATOR);
      }
   }

}
