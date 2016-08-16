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

}
