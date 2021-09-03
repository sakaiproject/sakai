package org.sakaiproject.conversations.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONV_STATUS", uniqueConstraints = { @UniqueConstraint(name = "UniqueConvStatus", columnNames = { "SITE_ID", "USER_ID" }) })
@Getter
@Setter
public class ConvStatus implements PersistableEntity<Long> {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "SITE_ID", length = 99, nullable = false)
    private String siteId;

    @Column(name = "USER_ID", length = 99, nullable = false)
    private String userId;

    @Column(name = "GUIDELINES_AGREED")
    private Boolean guidelinesAgreed = Boolean.FALSE;

    public ConvStatus() {}

    public ConvStatus(String siteId, String userId) {

        this.siteId = siteId;
        this.userId = userId;
    }
}
