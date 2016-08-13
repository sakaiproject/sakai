/* 
 * Copyright (c) Orchestral Developments Ltd and the Orion Health group of companies (2001 - 2016).
 * 
 * This document is copyright. Except for the purpose of fair reviewing, no part
 * of this publication may be reproduced or transmitted in any form or by any
 * means, electronic or mechanical, including photocopying, recording, or any
 * information storage and retrieval system, without permission in writing from
 * the publisher. Infringers of copyright render themselves liable for
 * prosecution.
 */
package org.sakaiproject.tool.gradebook.ui.helpers.entity.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.service.gradebook.shared.Assignment;

import lombok.Getter;
import lombok.Setter;

/**
 * GradebookItem encapsulates basic data about a gradebook item and the list of grades for that item
 */
public class GradebookItem {
	
	@Getter
	@Setter
	private long id;
	
	@Getter
	@Setter
	private String name;
	
	@Getter
	@Setter
	private Double points;
	
	@Getter
	@Setter
	private Date dueDate;
	
	@Getter
	@Setter
	private ExternalInfo externalInfo;
	
	@Getter
	@Setter
	private List<StudentGrade> grades;
	
	public GradebookItem(Assignment assignment) {
		this.id = assignment.getId();
		this.name = assignment.getName();
		this.points = assignment.getPoints();
		this.dueDate = assignment.getDueDate();
		if(StringUtils.isNotBlank(assignment.getExternalId())) {
			this.externalInfo = new ExternalInfo(assignment);
		}
		this.grades = new ArrayList<>();
		
		assignment.get
	}
	
}

