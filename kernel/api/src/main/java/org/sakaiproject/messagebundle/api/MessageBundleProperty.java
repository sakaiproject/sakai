/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/branches/SAK-18678/api/src/main/java/org/sakaiproject/site/api/Site.java $
 * $Id: Site.java 81275 2010-08-14 09:24:56Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.messagebundle.api;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MessageBundleProperty {
    private Long id;
    private String baseName;
    private String moduleName;
    private String locale;
    private String propertyName;
    private String value;
    private String defaultValue;

    public MessageBundleProperty(String baseName, String moduleName, String locale, String propertyName) {
        this.baseName = baseName;
        this.moduleName = moduleName;
        this.locale = locale;
        this.propertyName = propertyName;
    }
}
