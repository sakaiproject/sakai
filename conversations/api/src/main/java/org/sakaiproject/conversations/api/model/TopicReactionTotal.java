package org.sakaiproject.conversations.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@Table(name = "CONV_TOPIC_REACTION_TOTALS",
    indexes = { @Index(columnList = "TOPIC_ID") },
    uniqueConstraints = { @UniqueConstraint(name = "UniqueTopicReactionTotals", columnNames = { "TOPIC_ID", "REACTION" }) })
@Getter
@Setter
public class TopicReactionTotal implements PersistableEntity<Long> {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TOPIC_ID", nullable = false)
    private Topic topic;

    @Column(name = "REACTION", nullable = false)
    private Reaction reaction;

    @Column(name = "TOTAL", nullable = false)
    private Integer total;
}
