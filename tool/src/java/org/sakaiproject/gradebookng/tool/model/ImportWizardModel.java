package org.sakaiproject.gradebookng.tool.model;

import lombok.Data;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.service.gradebook.shared.Assignment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model object used for the import wizard panels
 */
@Data
public class ImportWizardModel implements Serializable {

    /**
     * List of items that have been uploaded
     */
    private List<ProcessedGradeItem> processedGradeItems;

    /**
     * List of items that have been selected to import
     */
    private List<ProcessedGradeItem> selectedGradeItems;

    /**
     * Which step is the new gb item creation currently on
     */
    private int step;

    /**
     * How many total steps are in the new gb item creation portion of the wizard
     */
    private int totalSteps;

    private List<ProcessedGradeItem> gbItemsToCreate;
    private List<ProcessedGradeItem> itemsToCreate;
    private List<ProcessedGradeItem> itemsToUpdate;
    private List<Assignment> assignmentsToCreate = new ArrayList<Assignment>();

}
