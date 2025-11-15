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

import lombok.Data;

@Data
public class DashboardRestBean {

    private String givenName;
    private String motd;
    private String title;
    private List<String> widgets;
    private Boolean editable;
    private List<String> widgetLayout;
    private String overview;
    private String programme;
    private Integer template = 1;
    private String image;
    private String courseTemplate1ThumbnailUrl;
    private String courseTemplate2ThumbnailUrl;
    private String courseTemplate3ThumbnailUrl;
    private String homeTemplate1ThumbnailUrl;
    private String homeTemplate2ThumbnailUrl;
    private String homeTemplate3ThumbnailUrl;
}
