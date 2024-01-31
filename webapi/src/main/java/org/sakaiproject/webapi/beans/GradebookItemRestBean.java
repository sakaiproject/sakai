package org.sakaiproject.webapi.beans;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GradebookItemRestBean {

	private String id;

	private String name;

	private boolean disabled;
}
