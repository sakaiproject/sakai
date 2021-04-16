package org.sakaiproject.conversations.api.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "CONV_POSTS", indexes = @Index(name = "creator_index", columnList = "CREATOR"))
public class Post {

    @Id
    @Column(name = "POST_ID", length = 36, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TOPIC_ID")
    private Topic topic;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @Column(name = "MESSAGE", nullable = false)
    private String message;

    @Column(name = "DRAFT", nullable = false)
    private Boolean draft;

    @Column(name = "HIDDEN", nullable = false)
    private Boolean hidden;

    @Column(name = "UPVOTES")
    private Integer upvotes = 0;

    @Embedded
    private Metadata metadata;
}
