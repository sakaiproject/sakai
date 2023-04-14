/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.beans;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class DashboardRestBean {

    private String givenName;
    private String motd;
    private String title;
    private String worksiteSetupUrl;
    private List<String> widgets;
    private Boolean editable;
    private List<String> layout;
    private String overview;
    private String programme;
    private Integer template;
    private Map<String, List<String>> defaultWidgetLayouts;
    private String image;
}
