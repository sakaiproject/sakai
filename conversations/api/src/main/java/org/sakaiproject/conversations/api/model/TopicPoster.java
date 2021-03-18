package org.sakaiproject.conversations.api.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Getter;

@Entity
@Table(name = "CONV_TOPIC_POSTERS", uniqueConstraints = { @UniqueConstraint(columnNames = { "TOPIC_ID", "POSTER" }) })
@Getter
public class TopicPoster {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "TOPIC_ID", nullable = false)
    private Topic topic;

    @Column(name = "USER_ID", length = 99, nullable = false)
    private String userId;

    @Column(name = "NUMBER_OF_POSTS", nullable = false)
    private Integer numberOfPosts;

    @Column(name = "LAST_POST", nullable = false)
    private Instant lastPost;

    @Column(name = "POSTS_READ", nullable = false)
    private Integer postsRead;
}
