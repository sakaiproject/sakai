package org.sakaiproject.conversations.api.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import java.time.Instant;

@Embeddable
public class Metadata {

    @Column(name = "AUTHOR", length = 99)
    private String author;

    @Column(name = "CREATED", nullable = false)
    private Instant created;

    @Column(name = "LAST_MODIFIED")
    private Instant lastModified;
}
