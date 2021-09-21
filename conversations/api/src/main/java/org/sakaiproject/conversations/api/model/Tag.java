package org.sakaiproject.conversations.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONV_TAGS", indexes = { @Index(columnList = "SITE_ID") })
@Getter
@Setter
public class Tag implements PersistableEntity<Long> {

    @Id
    @GeneratedValue
    @Column(name = "TAG_ID")
    private Long id;

    @Column(name = "SITE_ID", length = 99, nullable = false)
    private String siteId;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "LABEL", nullable = false)
    private String label;
}
