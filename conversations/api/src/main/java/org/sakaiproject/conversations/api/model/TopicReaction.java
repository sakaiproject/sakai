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

import org.sakaiproject.conversations.api.Reaction;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONV_TOPIC_REACTIONS",
    uniqueConstraints = { @UniqueConstraint(name = "UniqueTopicReactions", columnNames = { "TOPIC_ID", "USER_ID", "REACTION" }) },
    indexes = { @Index(columnList = "TOPIC_ID, USER_ID"), @Index(columnList = "TOPIC_ID") })
@Getter
@Setter
public class TopicReaction implements PersistableEntity<Long> {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "TOPIC_ID", nullable = false)
    private Topic topic;

    @Column(name = "USER_ID", length = 99, nullable = false)
    private String userId;

    @Column(name = "REACTION", nullable = false)
    private Reaction reaction;

    @Column(name = "REACTION_STATE")
    private Boolean state = Boolean.FALSE;
}
