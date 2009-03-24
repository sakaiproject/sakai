/**
 * $Id$
 * $URL$
 * BatchProvider.java - entity-broker - Jan 14, 2009 3:36:31 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.rest.caps;

import org.sakaiproject.entitybroker.entityprovider.capabilities.DescribePropertiesable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;


/**
 * A provider interface for the batch handler
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface BatchProvider extends DescribePropertiesable, Outputable {
    // this left empty on purpose
}
