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

import java.util.Date;

import org.sakaiproject.service.gradebook.shared.GradeDefinition;

import lombok.Getter;
import lombok.Setter;

/**
 * Encapsulates data about a specific grade for a student. It is always wrapped inside a {@link GradebookItem} to give it context.
 */
public class StudentGrade {

	@Getter
	@Setter
	private String userId;
	
	@Getter
	@Setter
	private String grade;
	
	@Getter
	@Setter
	private Date dateGraded;
	
	public StudentGrade(GradeDefinition def) {
		this.userId = def.getStudentUid();
		this.grade = def.getGrade();
		this.dateGraded = def.getDateRecorded();
	}
	
}

