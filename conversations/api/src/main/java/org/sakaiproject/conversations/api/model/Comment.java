package org.sakaiproject.conversations.api.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONV_COMMENTS", indexes = { @Index(columnList = "POST_ID"),
                                        @Index(columnList = "SITE_ID") })
@Getter
@Setter
public class Comment implements PersistableEntity<String> {

    @Id
    @Column(name = "COMMENT_ID", length = 36, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(name = "SITE_ID", length = 99, nullable = false)
    private String siteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_ID", nullable = false)
    private Post post;

    @Column(name = "MESSAGE", length = 255, nullable = false)
    private String message;

    @Column(name = "LOCKED")
    private Boolean locked = Boolean.FALSE;

    @Embedded
    private Metadata metadata;
}
