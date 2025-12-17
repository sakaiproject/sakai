/**
 * $Id$
 * $URL$
 * BatchProvider.java - entity-broker - Jan 14, 2009 3:36:31 PM - azeckoski
 **********************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 **********************************************************************************/

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
