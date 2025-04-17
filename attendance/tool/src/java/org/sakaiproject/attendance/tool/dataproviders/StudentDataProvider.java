/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.tool.dataproviders;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.attendance.tool.models.DetachableUserModel;
import org.sakaiproject.user.api.User;

import java.util.Collections;
import java.util.List;

/**
 * A StudentDataProvider
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class StudentDataProvider extends BaseProvider<User> {
    private String groupId;

    public StudentDataProvider() {
        super();
    }

    /**
     * Constructor with data supplied
     *
     * @param l, list of Users
     */
    public StudentDataProvider(List<User> l) {
        super();

        if(l != null && !l.isEmpty()) {
            this.list = l;
        }
    }

    public StudentDataProvider(String groupId) {
        super();

        this.groupId = groupId;
    }

    protected List<User> getData() {
        if(this.list == null) {
            if(groupId == null) {
                this.list = sakaiProxy.getCurrentSiteMembership();
            } else {
                this.list = sakaiProxy.getGroupMembershipForCurrentSite(groupId);
            }
            Collections.reverse(this.list);
        }
        return this.list;
    }

    @Override
    public IModel<User> model(User object){
        return new DetachableUserModel(object);
    }
}
