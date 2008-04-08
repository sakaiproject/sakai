/**
 * $Id$
 * $URL$
 * HTMLable.java - entity-broker - Apr 6, 2008 7:37:54 PM - azeckoski
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

import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * The entity can be returned as HTML and will automatically be handled using
 * the available {@link HttpServletAccessProviderManager}<br/>
 * If you want to define the HTML that is returned instead of allowing the redirect
 * to the servlet access provider then use {@link OutputHTMLdefineable}
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface OutputHTMLable extends EntityProvider {

   /**
    * the extension which goes on this entity URL (after a ".") to indicate the return should be HTML data,
    * no extension will do the same thing
    */
   public final String EXTENSION = "html";

   // this space intentionally left blank

}
