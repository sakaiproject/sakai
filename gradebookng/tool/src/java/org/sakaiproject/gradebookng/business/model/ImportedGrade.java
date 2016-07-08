package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * Created by chmaurer on 1/21/15.
 */
@Data
public class ImportedGrade implements Serializable {

	private String studentEid;
	private String studentUuid;
	private String studentName;

	private Map<String, ImportedGradeItem> gradeItemMap = new HashMap<String, ImportedGradeItem>();
}
