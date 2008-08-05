/**
 * $Id$
 * $URL$
 * TestInterfaceFour.java - entity-broker - May 5, 2008 1:29:50 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.util.reflect;

/**
 * Test interface that extends 4 others
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface TestInterfaceFour extends TestInterfaceOne, Runnable, Cloneable, Readable {

}
