package org.sakaiproject.progress.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Setter;
import lombok.Getter;
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
    @Column(name = "id", length = 36, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Getter @Setter
    private String id;

    @Type(type = "org.sakaiproject.springframework.orm.hibernate.type.InstantType")
    @Column(name = "date_created", nullable = false)
    @Getter
    private Instant dateCreated;

    @Type(type = "org.sakaiproject.springframework.orm.hibernate.type.InstantType")
    @Column(name = "date_edited")
    @Getter
    private Instant dateEdited;

    @Lob
    @Column(name = "config", length = 65535)
    @Getter @Setter
    private String configuration;

    @Column(name = "modified_by" length = 99)
    @Getter
    private String modifiedBy;


    public enum STATUS {
        UNLOCKED,   // 0
        LOCKED,     // 1
        DELETED     // 2
    }


}
