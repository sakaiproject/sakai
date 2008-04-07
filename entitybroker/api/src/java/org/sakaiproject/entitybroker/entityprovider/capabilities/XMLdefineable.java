/**
 * $Id$
 * $URL$
 * XMLdefineable.java - entity-broker - Apr 6, 2008 7:48:37 PM - azeckoski
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

import org.sakaiproject.entitybroker.access.HttpServletAccessProvider;

/**
 * Allows this entity to define the XML data that is returned for it,
 * if you just want to use the internal methods to turn your entity into XML
 * then simply use {@link XMLable}
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface XMLdefineable extends XMLable, HttpServletAccessProvider {

   // this space intentionally left blank

}
