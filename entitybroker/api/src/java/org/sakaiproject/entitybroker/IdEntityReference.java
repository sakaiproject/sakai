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
    * @deprecated use {@link #getId()} to get the id 
    */
   public String id;

   /**
    * @deprecated use {@link EntityReference#EntityReference(String)}
    */
   public IdEntityReference(String reference) {
      super(reference);
      this.id = getId();
   }

   /**
    * @deprecated use {@link EntityReference#EntityReference(String, String)}
    */
   public IdEntityReference(String prefix, String id) {
      super(prefix, id);
      this.id = getId();
   }

   /**
    * @deprecated do not use this method anymore, use the constructors for {@link EntityReference}
    */
   public static String getID(String reference) {
      return new EntityReference(reference).getId();
   }

}
