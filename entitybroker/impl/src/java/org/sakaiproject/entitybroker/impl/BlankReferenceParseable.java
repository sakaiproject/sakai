/**
 * $Id: EBlogic.java 1000 Apr 15, 2008 4:29:18 PM azeckoski $
 * $URL: https://source.sakaiproject.org/contrib $
 * EBlogic.java - entity-broker - Apr 15, 2008 4:29:18 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ReferenceParseable;

/** 
 * A dummy implementation of ReferenceParseable, to be used as a marker 
 * within maps for fast lookup.
 * 
 * @author Antranig Basman (amb26@ponder.org.uk)
 */
public class BlankReferenceParseable implements ReferenceParseable {

  public EntityReference getParsedExemplar() {
    return null;
  }

  public String getEntityPrefix() {
    return null;
  }

}
