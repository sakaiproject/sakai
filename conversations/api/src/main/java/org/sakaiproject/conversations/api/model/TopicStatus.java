package org.sakaiproject.conversations.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONV_TOPIC_STATUS",
    uniqueConstraints = { @UniqueConstraint(name = "UniqueTopicStatus", columnNames = { "TOPIC_ID", "USER_ID" }) },
    indexes = { @Index(columnList = "TOPIC_ID, USER_ID") })
@Getter
@Setter
public class TopicStatus implements PersistableEntity<Long> {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "SITE_ID", nullable = false)
    private String siteId;

    @Column(name = "TOPIC_ID", nullable = false)
    private String topicId;

    @Column(name = "USER_ID", length = 99, nullable = false)
    private String userId;

    @Column(name = "BOOKMARKED")
    private Boolean bookmarked = Boolean.FALSE;

    @Column(name = "UNREAD")
    private Integer unread = 0;

    @Column(name = "VIEWED")
    private Boolean viewed = Boolean.FALSE;

    public TopicStatus() {
    }

    public TopicStatus(String siteId, String topicId, String userId) {

        this.siteId = siteId;
        this.topicId = topicId;
        this.userId = userId;
    }
}
