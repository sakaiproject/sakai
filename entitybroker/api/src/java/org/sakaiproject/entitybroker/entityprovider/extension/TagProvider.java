/**
 * $Id$
 * $URL$
 * TagProvider.java - entity-broker - Apr 5, 2008 7:19:14 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.entityprovider.extension;

import java.util.Set;


/**
 * Defines the methods related to tagging entities (shared between interfaces)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface TagProvider {

   /**
    * Get the set of tags which are associated with this entity
    * 
    * @param reference a globally unique reference to an entity
    * @return a set of the tags which are associated with this entity
    * @throws UnsupportedOperationException if this reference cannot be tagged
    */
   public Set<String> getTags(String reference);

   /**
    * Sets the tags which are associated with this entity,
    * this overwrites any current tags and makes the input
    * tags the only current tags for this entity
    * 
    * @param reference a globally unique reference to an entity
    * @param tags a set of the tags to associate with this entity, 
    * setting this to an empty set will remove all tags from this entity
    * @throws UnsupportedOperationException if this reference cannot be tagged
    */
   public void setTags(String reference, String[] tags);

}
