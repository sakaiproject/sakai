/**********************************************************************************
 * $URL: $
 * $Id: $
 * **********************************************************************************
 * <p>
 * Author: David P. Bauer, dbauer1@udayton.edu
 * <p>
 * Copyright (c) 2016 University of Dayton
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.opensource.org/licenses/ECL-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool;

public class SimpleChecklistItemImpl implements SimpleChecklistItem {

    private long id; // Basic ID
    private String name; // Name to be displayed
    private long link; // ID of item if linked, -1 otherwise

    public SimpleChecklistItemImpl() {
        name = "";
    }

    public SimpleChecklistItemImpl(long id, String name, Long link) {
        this.id = id;
        this.name = name;
        this.setLink(link);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLink() {
        return link;
    }

    public void setLink(Long link) {
        if(link == null) {
            link = -1L;
        }
        this.link = link;
    }

}