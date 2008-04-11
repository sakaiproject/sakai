/**
 * $Id$
 * $URL$
 * RESTfulUrlRoutable.java - entity-broker - Apr 9, 2008 9:22:14 AM - azeckoski
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
 * This entity provider will allow for RESTful URLs to be handled by it (i.e be handled by the provider
 * rather than simply passing through to the {@link HttpServletAccessProvider} like most URLs). 
 * RESFful URLs are defined by this microformat standard:
 * <a href="http://microformats.org/wiki/rest/urls">http://microformats.org/wiki/rest/urls</a><br/>
 * If CRUD operations are defined then RESTful URL requests will be processed internally
 * (if the right capabilities have been implemented)<br/>
 * <b>NOTE:</b> Skipping this does not preclude the normal operation of entity URLs like:
 * /direct/prefix/id or /direct/prefix even though they match with RESTful semantics (not coincidently)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface RESTfulUrlHandleable extends EntityProvider {

   // This space left blank intentionally

}
