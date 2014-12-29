/**
 * $Id$
 * $URL$
 * HTMLable.java - entity-broker - Apr 6, 2008 7:37:54 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;

/**
 * These entities can be returned as certain output formats which are handled automatically<br/>
 * If you want to define the data that is returned instead of using the internal methods 
 * then use {@link OutputFormattable}<br/>
 * <br/>
 * <b>NOTE:</b> By default all entity view requests go through to the available access providers: 
 * {@link EntityViewAccessProvider} or {@link HttpServletAccessProvider}
 * <b>NOTE:</b> there is no internal handling of HTML, it will always redirect to the the available access provider
 * if there is one (if there is not one then the entity will be toStringed)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface Outputable extends EntityProvider, Formats {

   /**
    * Defines the output format types (extensions) handled by this provider<br/>
    * <b>NOTE:</b> In the case of an entity view the extension 
    * which goes on the end of an entity URL (after a ".") indicates the return type<br/>
    * <b>WARNING:</b> This combines with the access interface when http requests are
    * being processed, all requests will pass through to the {@link EntityViewAccessProvider}
    * if they are not handled
    * 
    * @return an array containing the extension formats (from {@link Formats}) handled <br/>
    * OR empty array to indicate all are handled (note that the internal formatter will throw exceptions when it cannot handle a type) <br/>
    * OR null to indicate none are handled (same as not implementing {@link AccessFormats}) <br/>
    * NOTE: use the constants (example: {@link #HTML}) or feel free to make up your own if you like
    */
   public String[] getHandledOutputFormats();

}
