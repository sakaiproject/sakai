package org.sakaiproject.gradebookng.tool.panels;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.panel.Panel;
import org.sakaiproject.gradebookng.business.model.ImportedGrade;

import java.util.List;

/**
 * Created by chmaurer on 1/22/15.
 */
public class GradeImportConfirmationStep extends Panel {

    private static final Logger log = Logger.getLogger(GradeImportConfirmationStep.class);

    private List<ImportedGrade> grades;

    public GradeImportConfirmationStep(String id, List<ImportedGrade> grades) {
        super(id);
        this.grades = grades;

        log.info("Grades to import: " + grades.size());
    }
}
