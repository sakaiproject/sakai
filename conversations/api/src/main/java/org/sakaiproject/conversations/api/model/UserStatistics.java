package org.sakaiproject.conversations.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import java.time.Instant;

import lombok.Getter;

@Entity
@Table(name = "CONV_USER_STATISTICS", indexes = @Index(columnList = "USER_ID"))
@Getter
public class UserStatistics {

    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "USER_ID", length = 99, nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TOPIC_ID")
    private Topic topic;

    @Column(name = "NUMBER_OF_POSTS")
    private Integer numberOfPosts = 0;

    @Column(name = "LAST_POST_DATE")
    private Instant lastPostDate;

    @Column(name = "NUMBER_OF_UPVOTES")
    private Integer numberOfUpvotes = 0;

    @Column(name = "NUMBER_OF_REACTIONS")
    private Integer numberOfReactions = 0;

    @Column(name = "NUMBER_OF_REPLIES")
    private Integer numberOfReplies = 0;

    @Column(name = "NUMBER_READ")
    private Integer numberRead = 0;

}
