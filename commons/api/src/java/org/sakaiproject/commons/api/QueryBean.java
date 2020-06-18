/*************************************************************************************
 * Copyright 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.

 *************************************************************************************/

package org.sakaiproject.commons.api;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Builder;

/**
 * @author Adrian Fish (adrian.r.fish@gmail.com)
 */
@Getter
@Builder
public class QueryBean {

    @Builder.Default private String commonsId = "";
    @Builder.Default private String siteId = "";
    @Builder.Default private String embedder = "";
    @Builder.Default private boolean userSite = false;
    @Builder.Default private List<String> fromIds = new ArrayList<>();
    @Builder.Default private String callerId = "";
}
