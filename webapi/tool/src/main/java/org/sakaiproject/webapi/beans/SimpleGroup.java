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

import java.util.Set;

import org.sakaiproject.site.api.Group;

public class SimpleGroup {

    public String id;
    public String reference;
    public String title;
    public Set<String> users;

    public SimpleGroup(Group g) {

        super();

        this.id = g.getId();
        this.reference = g.getReference();
        this.title = g.getTitle();
        this.users = g.getUsers();
    }
}
