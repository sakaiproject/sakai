/**
 * $Id$
 * $URL$
 * TagSearchProvider.java - entity-broker - Apr 5, 2008 7:21:20 PM - azeckoski
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

import java.util.List;

import org.sakaiproject.entitybroker.entityprovider.capabilities.Taggable;


/**
 * Defines the methods necessary for searching for entities by tags (shared interface)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface TagSearchProvider {

   /**
    * Search for all entities with a set of tags (as defined by the {@link Taggable} interface)
    * 
    * @param tags a set of tags defined on these entities in the {@link Taggable} interface
    * @return a list of globally unique references to entities with these tags
    */
   public List<String> findEntityRefsByTags(String[] tags);

}
