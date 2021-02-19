package org.sakaiproject.conversations.api.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;

@Entity
@Table(name = "TOPICS")
@Getter
public class Topic {

    @Id
    @GeneratedValue
    @Column(name = "TOPIC_ID")
    private Long id;

    /**
     * This will hold a Sakai entity reference. It could be anything in Sakai.
     */
    @Column(name = "ABOUT", length = 255, nullable = false)
    private String about;

    @Column(name = "TITLE", length = 255, nullable = false)
    private String title;

    @Column(name = "MESSAGE", nullable = false)
    private String message;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @Column(name = "TYPE", length = 255, nullable = false)
    private String type;

    @Embedded
    private Metadata metadata;
}
