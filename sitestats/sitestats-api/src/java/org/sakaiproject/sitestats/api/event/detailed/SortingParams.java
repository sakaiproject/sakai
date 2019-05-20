/**
 * Copyright (c) 2006-2019 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.api.event.detailed;

/**
 * Immutable class to hold parameters for sorting in detailed events queries
 *
 * @author bjones86
 */
public final class SortingParams
{
    public final String sortProp;
    public final boolean asc;

    /**
     * Constructor requiring all parameters
     *
     * @param sortProp the property to sort on
     * @param asc sorting order, true = asc, false = desc
     */
    public SortingParams( String sortProp, boolean asc )
    {
        this.sortProp = sortProp;
        this.asc = asc;
    }
}
