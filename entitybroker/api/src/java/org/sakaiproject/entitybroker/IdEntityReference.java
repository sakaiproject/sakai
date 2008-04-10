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


/**
 * Represents a parsed form of a simple entity reference, as accepted to the {@link EntityBroker}
 * API. These are of the form /prefix/id - this parser will accept overlong references with
 * additional path segments, which will be ignored.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @deprecated Use {@link EntityReference} directly, this will be removed eventually
 */
public class IdEntityReference extends EntityReference {

   /**
    * Constructor which takes an entity reference
    * @param reference a globally unique reference to an entity, 
    * consists of the entity prefix and the local id
    */
   public IdEntityReference(String reference) {
      super(reference);
   }

   /**
    * Full constructor
    * @param prefix the entity prefix
    * @param id the local entity id
    */
   public IdEntityReference(String prefix, String id) {
      super(prefix, id);
   }

   /**
    * @param reference
    * @return
    * @deprecated do not use this method anymore
    */
   public static String getID(String reference) {
      return new EntityReference(reference).id;
   }

}
