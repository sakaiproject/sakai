package org.sakaiproject.microsoft.controller.auxiliar;

import lombok.Data;

@Data
public class MainSessionBean {
	private static final Integer DEFAULT_PAGE_SIZE = 50;
	
	private String sortBy = "status";
	private String sortOrder = "ASC";
	private Integer pageNum = 0;
	private Integer pageSize = DEFAULT_PAGE_SIZE;
	private String search;
}
