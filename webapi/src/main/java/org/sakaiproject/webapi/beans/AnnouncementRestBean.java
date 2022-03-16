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

import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Site;

import org.springframework.hateoas.Link;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnouncementRestBean {

    private String id;
    private String siteId;
    private String siteTitle;
    private String subject;
    private String author;
    private String access;
    private boolean hasAttachment;
    private long date;
    private Long release;
    private Long retract;
    private Link link;

    public AnnouncementRestBean(Site site, AnnouncementMessage am, String url, String access) {
        id = am.getId();
        siteId = site.getId();
        siteTitle = site.getTitle();
        this.access = access;
        AnnouncementMessageHeader header = am.getAnnouncementHeader();
        subject = header.getSubject();
        author = header.getFrom().getDisplayName();
        date = header.getInstant().toEpochMilli();
        hasAttachment = !header.getAttachments().isEmpty();
        link = Link.of(url);
        ResourceProperties resourceProperties = am.getProperties();
        try {
            release = resourceProperties.getInstantProperty(AnnouncementService.RELEASE_DATE).toEpochMilli();
            date = release;
        } catch (EntityPropertyTypeException | EntityPropertyNotDefinedException e) { /*No action needed*/ }
        try {
            retract = resourceProperties.getInstantProperty(AnnouncementService.RETRACT_DATE).toEpochMilli();
        } catch (EntityPropertyTypeException | EntityPropertyNotDefinedException e) { /*No action needed*/ }
    }
}
