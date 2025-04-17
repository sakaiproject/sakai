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

package org.sakaiproject.attendance.tool.util;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class AttendanceFeedbackPanel extends FeedbackPanel {
    public static final long serialVersionUID = 1L;

    public AttendanceFeedbackPanel(final String id) {
        super(id);

        setOutputMarkupId(true);
    }

    @Override
    protected Component newMessageDisplayComponent(final String id, final FeedbackMessage message) {
        final Component newMessageDisplayComponent = super.newMessageDisplayComponent(id, message);

        if(message.getLevel() == FeedbackMessage.ERROR ||
                message.getLevel() == FeedbackMessage.DEBUG ||
                message.getLevel() == FeedbackMessage.FATAL ||
                message.getLevel() == FeedbackMessage.WARNING){
            add(AttributeModifier.replace("class", "attendanceAlertMessage"));
        } else if(message.getLevel() == FeedbackMessage.INFO ||
                message.getLevel() == FeedbackMessage.SUCCESS){
            add(AttributeModifier.replace("class", "attendanceMessageSuccess"));
        }

        return newMessageDisplayComponent;
    }

    public void clear() {
        getFeedbackMessages().clear();
        this.add(AttributeModifier.remove("class"));
    }
}
