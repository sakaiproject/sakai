/**
 * $Id$
 * $URL$
 * Saveable.java - entity-broker - Apr 12, 2008 1:57:05 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * Convenience interface to indicates that an entity is can be saved,
 * i.e. it is creatable and updateable
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface Saveable extends EntityProvider, Createable, Updateable {

   // this space left blank intentionally

}
