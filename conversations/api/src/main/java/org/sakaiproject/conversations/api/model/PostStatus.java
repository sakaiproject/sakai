package org.sakaiproject.conversations.api.model;

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
@Table(name = "CONV_POST_STATUS", uniqueConstraints = { @UniqueConstraint(columnNames = { "POST_ID", "POSTER" }) })
@Getter
public class PostStatus {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "POST_ID", nullable = false)
    private Post post;

    @Column(name = "POSTER", length = 99, nullable = false)
    private String poster;

    @Column(name = "VIEWED")
    private Boolean viewed;

    @Column(name = "LIKED")
    private Boolean liked;

    @Column(name = "FAVOURITED")
    private Boolean favourited;
}
