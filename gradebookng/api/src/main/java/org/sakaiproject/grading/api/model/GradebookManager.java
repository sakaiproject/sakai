package org.sakaiproject.grading.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.sakaiproject.springframework.data.PersistableEntity;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Table(name = "GB_MGR_T")
public class GradebookManager implements PersistableEntity<String> {

    public GradebookManager(String siteId) {
        this.id = siteId;
    }

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "SITE_ID", length = 99, nullable = false)
    private String id;

    @ElementCollection
    @CollectionTable(name = "GB_MGR_GROUPS_T",
                     joinColumns = @JoinColumn(name = "SITE_ID"),
                     indexes = @Index(columnList = "SITE_ID"))
    @Fetch(FetchMode.SUBSELECT)
    @Column(name = "GROUP_ID", length = 99)
    private Set<String> groups = new HashSet<>();

    @OneToMany(mappedBy = "gradebookManager")
    private List<Gradebook> gradebooks = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "GB_MGR_MAPPING_T",
            joinColumns = @JoinColumn(name = "SITE_ID"),
            indexes = {
                    @Index(columnList = "SITE_ID"),
                    @Index(columnList = "CONTEXT_ID")
            }
    )
    @MapKeyColumn(name = "CONTEXT_ID", length = 99)
    @Column(name = "GRADEBOOK_ID", length = 99)
    private Map<String, String> contextMapping = new HashMap<>();

    @Enumerated(value = EnumType.STRING)
    @Column(name = "ACCESS_TYPE", nullable = false)
    private Access typeOfAccess = Access.SITE;

    public enum Access {
        SITE,
        GROUP
    }
}
