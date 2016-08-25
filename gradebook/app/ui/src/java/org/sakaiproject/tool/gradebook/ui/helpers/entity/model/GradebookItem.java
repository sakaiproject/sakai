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

import org.sakaiproject.service.gradebook.shared.Assignment;

import lombok.Getter;
import lombok.Setter;

/**
 * Encapsulates basic data about a gradebook item and the list of grades for that item
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
	
	@Setter
	@Getter
	private Long categoryId;
	
	@Setter
	@Getter
	private boolean released;
	
	@Setter
	@Getter
	private boolean extraCredit;

	
	public GradebookItem(Assignment assignment) {
		this.id = assignment.getId();
		this.name = assignment.getName();
		this.points = assignment.getPoints();
		this.dueDate = assignment.getDueDate();
		if(assignment.isExternallyMaintained()) {
			this.externalInfo = new ExternalInfo(assignment);
		}
		this.grades = new ArrayList<>();
		this.categoryId = assignment.getCategoryId();
		this.released = assignment.isReleased();
		this.extraCredit = assignment.isExtraCredit();
	}
	
}

