package org.sakaiproject.gradebookng.business.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.sakaiproject.service.gradebook.shared.Assignment;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by chmaurer on 3/3/15.
 * 
 * @Deprecated. use shared Assignment instead as that is just a DTO( where as the tool Assignment is the persistent)
 */
@NoArgsConstructor
@Data
@Deprecated 
public class GbAssignment implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private Long id;
    private Double points;
    private Date dueDate;
    private boolean counted;
    private boolean externallyMaintained;
    private String externalId;
    private String externalAppName;
    private boolean released;
    private String categoryName;
    private Double weight;
    private boolean ungraded;
    private boolean extraCredit;
    private boolean categoryExtraCredit;

    public GbAssignment(Assignment assignment) {
        this.categoryExtraCredit = assignment.isCategoryExtraCredit();
        this.categoryName = assignment.getCategoryName();
        this.counted = assignment.isCounted();
        this.dueDate = assignment.getDueDate();
        this.externalAppName = assignment.getExternalAppName();
        this.externalId = assignment.getExternalId();
        this.externallyMaintained = assignment.isExternallyMaintained();
        this.extraCredit = assignment.getExtraCredit();
        this.id = assignment.getId();
        this.name = assignment.getName();
        this.points = assignment.getPoints();
        this.released = assignment.isReleased();
        this.ungraded = assignment.getUngraded();
        this.weight = assignment.getWeight();
    }

    public Assignment convert2Assignment() {
        Assignment assignment = new Assignment();
        assignment.setCategoryExtraCredit(categoryExtraCredit);
        assignment.setCategoryName(categoryName);
        assignment.setCounted(counted);
        assignment.setDueDate(dueDate);
        assignment.setExternalAppName(externalAppName);
        assignment.setExternalId(externalId);
        assignment.setExternallyMaintained(externallyMaintained);
        assignment.setExtraCredit(extraCredit);
        assignment.setId(id);
        assignment.setName(name);
        assignment.setPoints(points);
        assignment.setReleased(released);
        assignment.setUngraded(ungraded);
        assignment.setWeight(weight);

        return assignment;
    }
}
