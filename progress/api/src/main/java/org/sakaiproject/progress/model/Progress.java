package org.sakaiproject.progress.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Instant;

/**
 * This is the Widget persistence POJO its mostly uses JPA annotations
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "PROGRESS")
@Data
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class Progress {

    @Id
    @Column(name = "ID", length = 36, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Type(type = "org.sakaiproject.springframework.orm.hibernate.type.InstantType")
    @Column(name = "CREATED_DATE", nullable = false)
    private Instant dateCreated;

    @Type(type = "org.sakaiproject.springframework.orm.hibernate.type.InstantType")
    @Column(name = "MODIFIED_DATE")
    private Instant dateModified;

    @Lob
    @Column(name = "CONFIGURATION", length = 65535)
    private String configuration;

    public enum STATUS {
        UNLOCKED,   // 0
        LOCKED,     // 1
        DELETED     // 2
    }
}
