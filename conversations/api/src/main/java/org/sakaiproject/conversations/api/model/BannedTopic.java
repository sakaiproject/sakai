package org.sakaiproject.conversations.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Getter;

@Entity
@Table(name = "CONV_BANNED", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "USER_ID", "TOPIC_ID" })
})
@Getter
public class BannedTopic {

    @Id
    @GeneratedValue
    @Column(name = "BANNED_ID")
    private Long id;

    @Column(name = "USER_ID", length = 99, nullable = false)
    private String userId;

    @Column(name = "TOPIC_ID", nullable = false)
    private String topicId;
}
