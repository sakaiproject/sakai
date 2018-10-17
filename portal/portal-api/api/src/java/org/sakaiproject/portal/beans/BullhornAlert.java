package org.sakaiproject.portal.beans;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import lombok.Data;

@Entity
@Table(name = "BULLHORN_ALERTS")
@Data
public class BullhornAlert {

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue
    private Long id;

    @Column(name = "ALERT_TYPE", length = 8, nullable = false)
    private String alertType;

    @Column(name = "FROM_USER", length = 99, nullable = false)
    private String fromUser;

    @Column(name = "TO_USER", length = 99, nullable = false)
    private String toUser;

    @Column(name = "EVENT", length = 32, nullable = false)
    private String event;

    @Column(name = "REF", length = 255, nullable = false)
    private String ref;

    @Column(name="TITLE", length=255)
    private String title;

    @Column(name="SITE_ID", length=99)
    private String siteId;

    @Column(name="URL", length=2048, nullable=false)
    private String url;

    @Column(name="EVENT_DATE", columnDefinition="DATETIME", nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventDate;

    @Transient
    private String fromDisplayName;

    @Transient
    private String siteTitle;
}
