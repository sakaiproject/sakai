/**
 * Copyright (c) 2003-2019 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.messaging.api;

import org.sakaiproject.messaging.api.model.UserNotification;

import java.time.Instant;

public class UserNotificationTransferBean {

    public String from;
    public String to;
    public String event;
    public String ref;
    public String title;
    public String siteId;
    public String url;
    public Instant eventDate;
    public boolean viewed;
    public String tool;
    public boolean broadcast;

    public String fromDisplayName;
    public String siteTitle;
    public String formattedEventDate;

    public static UserNotificationTransferBean of(UserNotification un) {

        UserNotificationTransferBean bean = new UserNotificationTransferBean();
        bean.from = un.getFromUser();
        bean.to = un.getToUser();
        bean.event = un.getEvent();
        bean.ref = un.getRef();
        bean.title = un.getTitle();
        bean.siteId = un.getSiteId();
        bean.url = un.getUrl();
        bean.eventDate = un.getEventDate();
        bean.viewed = un.getViewed();
        bean.tool = un.getTool();
        bean.broadcast = un.getBroadcast();
        return bean;
    }

    public static UserNotificationTransferBean of(UserNotificationTransferBean from) {

        UserNotificationTransferBean to = new UserNotificationTransferBean();
        to.from = from.from;
        to.to = from.to;
        to.event = from.event;
        to.ref = from.ref;
        to.title = from.title;
        to.siteId = from.siteId;
        to.url = from.url;
        to.eventDate = from.eventDate;
        to.viewed = from.viewed;
        to.tool = from.tool;
        to.broadcast = from.broadcast;
        to.fromDisplayName = from.fromDisplayName;
        to.siteTitle = from.siteTitle;
        to.formattedEventDate = from.formattedEventDate;
        return to;
    }

}
