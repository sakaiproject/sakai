/*******************************************************************************
 * Copyright (c) 2006, 2008 The Sakai Foundation, The MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.sakaiproject.tool.gradebook.jsf.convertDateTime;

import java.util.TimeZone;


/**
 * Simple converter that overrides the spec DateTimeConverter and uses TimeZone.getDefault() as the
 * base timezone, rather than GMT.
 *
 * Author:Louis Majanja <louis@media.berkeley.edu>
 * Date: Sep 15, 2006
 * Time: 12:16:10 PM
 */
public class DateTimeConverter extends javax.faces.convert.DateTimeConverter {

    public static final String CONVERTER_ID =  org.sakaiproject.tool.gradebook.jsf.convertDateTime.DateTimeConverter.class.getName();

    public DateTimeConverter()
    {
        setTimeZone(TimeZone.getDefault());
    }

}
