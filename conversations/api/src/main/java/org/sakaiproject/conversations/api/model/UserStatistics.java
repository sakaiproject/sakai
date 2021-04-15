package org.sakaiproject.conversations.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;

@Entity
@Table(name = "CONV_USER_STATISTICS", indexes = @Index(name = "user_index", columnList = "USER_ID"))
@Getter
public class UserStatistics {

    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "USER_ID", length = 99, nullable = false)
    private String userId;

    @Column(name = "NUMBER_OF_TOPICS")
    private Integer numberOfTopics = 0;

    @Column(name = "NUMBER_OF_POSTS")
    private Integer numberOfPosts = 0;

    @Column(name = "NUMBER_OF_UPVOTES")
    private Integer numberOfUpvotes = 0;

    @Column(name = "NUMBER_OF_REPLIES")
    private Integer numberOfReplies = 0;
}
