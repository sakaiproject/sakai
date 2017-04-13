package org.sakaiproject.scheduler.events.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * This is used to read migrate the delayed invocation out when migrating to storing the triggers in
 * quartz.
 */
@Entity
@Table(name = "SCHEDULER_DELAYED_INVOCATION")
public class DelayedInvocation {

    @Id
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
