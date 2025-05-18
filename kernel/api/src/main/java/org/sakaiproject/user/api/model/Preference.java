package org.sakaiproject.user.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "SAKAI_PREFERENCES")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Preference implements PersistableEntity<String> {

    @Id
    @Column(name = "PREFERENCES_ID", length = 99, nullable = false)
    @EqualsAndHashCode.Include
    private String id;

    @Lob
    @Column(name = "XML")
    private String xml;

    @Version
    @Column(name = "VERSION")
    private Long version;
}
