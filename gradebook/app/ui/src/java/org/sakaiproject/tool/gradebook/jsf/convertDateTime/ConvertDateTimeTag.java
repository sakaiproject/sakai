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

import javax.faces.webapp.ConverterTag;
import javax.servlet.jsp.PageContext;

/**
 * Simple tag that overrides the spec ConvertDateTimeTag and uses TimeZone.getDefault() as the
 * base timezone, rather than GMT.
 * *
 * Author:Louis Majanja <louis@media.berkeley.edu>
 * Date: Sep 15, 2006
 * Time: 12:27:22 PM
 */
public class ConvertDateTimeTag extends ConverterTag {

        /**
     * serial version id for correct serialisation versioning
     */
    private static final long serialVersionUID = 1L;


    public ConvertDateTimeTag()
    {
        setConverterId(org.sakaiproject.tool.gradebook.jsf.convertDateTime.DateTimeConverter.CONVERTER_ID);
    }


    public void setPageContext(PageContext context)
    {
        super.setPageContext(context);
        setConverterId(DateTimeConverter.CONVERTER_ID);
    }
}
