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

package org.sakaiproject.attendance.tool.panels;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.tool.dataproviders.AttendanceStatusProvider;

/**
 * AttendanceRecordFormHeaderPanel is a panel which is used to display the header for a table of AttendanceRecords
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class AttendanceRecordFormHeaderPanel extends BasePanel {
    private static final long serialVersionUID = 1L;

    public AttendanceRecordFormHeaderPanel(String id) {
        super(id);

        add(createStatusHeader());
    }

    private WebMarkupContainer createStatusHeader() {
        WebMarkupContainer status = new WebMarkupContainer("status");

        AttendanceStatusProvider attendanceStatusProvider = new AttendanceStatusProvider(attendanceLogic.getCurrentAttendanceSite(), AttendanceStatusProvider.ACTIVE);
        DataView<AttendanceStatus> statusHeaders = new DataView<AttendanceStatus>("status-headers", attendanceStatusProvider) {
            @Override
            protected void populateItem(Item<AttendanceStatus> item) {
                item.add(new Label("header-status-name", getStatusString(item.getModelObject().getStatus())));
            }
        };
        status.add(statusHeaders);

        return status;
    }
}
