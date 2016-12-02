package org.sakaiproject.scheduler.events.hibernate;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.sakaiproject.api.app.scheduler.events.TriggerEvent;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "scheduler_trigger_events")
@NamedQueries({
        @NamedQuery(name = "purgeEventsBefore", query = "delete from TriggerEventHibernateImpl as evt where evt.time < :before")
})

@EqualsAndHashCode(of = "id")
@Getter
@Setter
@ToString
public class TriggerEventHibernateImpl implements TriggerEvent
{
    @Id
    @Column(name = "uuid", length = 36)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(name = "eventType", nullable = false)
    @Enumerated(EnumType.STRING)
    private TRIGGER_EVENT_TYPE eventType;

    @Column(name = "jobName", nullable = false)
    private String jobName;

    @Column(name = "triggerName")
    public String triggerName;

    @Column(name = "eventTime", nullable = false)
    @OrderColumn(name = "schdulr_trggr_vnts_eventTime", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    @Column(name = "message")
    @Lob
    private String message;

    @Column(name = "serverId")
    private String serverId;
}
