package org.sakaiproject.conversations.api.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONV_POSTS", indexes = { @Index(columnList = "TOPIC_ID"),
                                        @Index(columnList = "SITE_ID") })
@Getter
@Setter
public class Post implements PersistableEntity<String> {

    @Id
    @Column(name = "POST_ID", length = 36, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TOPIC_ID", nullable = false)
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_POST_ID")
    private Post parentPost;

    @Column(name = "SITE_ID", length = 99, nullable = false)
    private String siteId;

    @Lob
    @Column(name = "MESSAGE", nullable = false)
    private String message;

    @Column(name = "NUMBER_OF_COMMENTS")
    private Integer numberOfComments = 0;

    @Column(name = "DRAFT")
    private Boolean draft = Boolean.FALSE;

    @Column(name = "HIDDEN")
    private Boolean hidden = Boolean.FALSE;

    @Column(name = "LOCKED")
    private Boolean locked = Boolean.FALSE;

    @Column(name = "UPVOTES")
    private Integer upvotes = 0;

    @Column(name = "PRIVATE_POST")
    private Boolean privatePost = Boolean.FALSE;

    @Column(name = "ANONYMOUS")
    private Boolean anonymous = Boolean.FALSE;

    @Embedded
    private Metadata metadata;
}
