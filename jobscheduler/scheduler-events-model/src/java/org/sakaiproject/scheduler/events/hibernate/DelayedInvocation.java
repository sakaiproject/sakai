/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.scheduler.events.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.GeneratedValue;

import java.util.Date;

/**
 * This is used to read migrate the delayed invocation out when migrating to storing the triggers in
 * quartz.
 */
@Entity
@Table(name = "SCHEDULER_DELAYED_INVOCATION")
public class DelayedInvocation {

    @Id
    @Column(name = "INVOCATION_ID", nullable = false, length = 36)
    @GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(name = "INVOCATION_TIME", nullable = false)
    private Date time;

    @Column(name = "COMPONENT", nullable = false, length = 2000)
    private String component;

    @Column(name = "CONTEXT", nullable = false, length = 2000)
    private String context;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
