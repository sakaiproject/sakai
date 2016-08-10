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
import java.util.List;

import org.sakaiproject.service.gradebook.shared.Assignment;

import lombok.Getter;
import lombok.Setter;

/**
 * GradebookItem encapsulates basic data about a gradebook item and the list of grades for that item
 */
public class GradebookItem {
	
	@Getter
	@Setter
	private long assignmentId;
	
	@Getter
	@Setter
	private String assignmentName;
	
	@Getter
	@Setter
	private List<StudentGrade> grades;
	
	public GradebookItem(Assignment assignment) {
		this.assignmentId = assignment.getId();
		this.assignmentName = assignment.getName();
		this.grades = new ArrayList<>();
	}
	
}

