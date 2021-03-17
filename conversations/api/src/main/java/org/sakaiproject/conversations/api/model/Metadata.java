package org.sakaiproject.conversations.api.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import java.time.Instant;

@Embeddable
public class Metadata {

    @Column(name = "CREATOR", length = 99)
    private String creator;

    @Column(name = "CREATED", nullable = false)
    private Instant created;

    @Column(name = "MODIFIER", length = 99)
    private String modifier;

    @Column(name = "MODIFIED")
    private Instant modified;
}
