/**
 * $Id$
 * $URL$
 **************************************************************************
 * Copyright (c) 2011 The Sakai Foundation
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

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * This entity type can specify its own recursion depth limit.  It will
 * typically be implemented as a simple bean property, but there may be
 * scenarios where other information is used to determine an appropriate
 * limit.
 */
public interface DepthLimitable extends EntityProvider {

    /**
     * Retrieve the recursion depth limit for this provider.
     *
     * @returns the limit as specified for this entity type.
     */
    public int getMaxDepth();

}
