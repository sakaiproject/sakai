package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chmaurer on 1/21/15.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ImportedGradeItem implements Serializable {

	private String gradeItemName;
	private String gradeItemComment;
	private String gradeItemScore;

}
