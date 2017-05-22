/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.search.api;

import java.util.Set;

/**
 * Mix-in for {@link EntityContentProducer} implementations which expose the set of functions
 * (i.e. event keys) which should trigger content indexing using that producer. This is now the
 * preferred mechanism for content index event registration, as opposed to the historical approach
 * where content producers registered their own events directly with {@link SearchService}.
 * Going forward, content producer should register only with their owning {@link SearchIndexBuilder}
 * and let that component deal with narrowly targeted event registration using the functions
 * delcared by this interface.
 */
public interface EntityContentProducerEvents {

    Set<String> getTriggerFunctions();

}
