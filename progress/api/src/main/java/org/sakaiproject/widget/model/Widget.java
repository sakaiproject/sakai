package org.sakaiproject.widget.model;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

/**
 * This is the Widget persistence POJO its mostly uses JPA annotations
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "WIDGET")
@Data
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class Widget {

    @Id
    @Column(name = "ID", length = 36, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "CONTEXT", length = 99, nullable = false)
    private String context;

    @Enumerated
    @Column(name = "STATUS", nullable = false)
    private STATUS status = STATUS.UNLOCKED;

    @Type(type = "org.sakaiproject.springframework.orm.hibernate.type.InstantType")
    @Column(name = "CREATED_DATE", nullable = false)
    private Instant dateCreated;

    @Type(type = "org.sakaiproject.springframework.orm.hibernate.type.InstantType")
    @Column(name = "MODIFIED_DATE")
    private Instant dateModified;

    @Type(type = "org.sakaiproject.springframework.orm.hibernate.type.InstantType")
    @Column(name = "EXPIRATION_DATE")
    private Instant dateExpired;

    @Lob
    @Column(name = "DESCRIPTION", length = 65535)
    private String description;

    public enum STATUS {
        UNLOCKED,   // 0
        LOCKED,     // 1
        DELETED     // 2
    }
}
