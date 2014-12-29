/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.entity.api;

import java.io.Serializable;

/**
 * <p>
 * Summary is a Map that represents an RSS feed item
 * </p>
 */
public interface Summary extends Serializable
{
	/** Property for Publication date (RFC822) */
        static final String PROP_PUBDATE  = "RSS:pubdate";

	/** Property for Title */
        static final String PROP_TITLE  = "RSS:title";

	/** Property for Description */
        static final String PROP_DESCRIPTION  = "RSS:description";

	/** Property for Link */
        static final String PROP_LINK  = "RSS:link";
}
