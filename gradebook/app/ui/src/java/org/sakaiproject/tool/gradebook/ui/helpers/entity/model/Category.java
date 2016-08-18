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

import org.sakaiproject.service.gradebook.shared.CategoryDefinition;

import lombok.Getter;
import lombok.Setter;

/**
 * Encapsulates category info for a gradebook. It is always wrapped inside {@link GradebookData} to give it context.
 */
public class Category {
	
	@Getter
	@Setter
	private long id;
	
	@Getter
	@Setter
	private String name;
	
	@Getter
	@Setter
	private Double weight;
	
	public Category(CategoryDefinition categoryDefinition) {
		this.id = categoryDefinition.getId();
		this.name = categoryDefinition.getName();
		this.weight = categoryDefinition.getWeight();
	}
	
}

