/**
 * $Id$
 * $URL$
 * OutputFormatable.java - entity-broker - Apr 8, 2008 12:17:21 PM - azeckoski
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
 * Convenience method to indicate that this type of entity can be 
 * resolved and formatted in the various types available internally
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface OutputFormatable extends EntityProvider, Resolvable, JSONable, XMLable {

   // this space intentionally left blank

}
