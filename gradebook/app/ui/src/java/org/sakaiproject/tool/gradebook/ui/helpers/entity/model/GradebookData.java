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

import org.sakaiproject.site.api.Site;

import lombok.Getter;
import lombok.Setter;

/**
 * This is a wrapper class for the return data format from the batch grade entity provider
 */
public class GradebookData {

	@Getter
	@Setter
	private String siteId;
	
	@Getter
	@Setter
	private String siteTitle;
	
	@Getter
	@Setter
	private List<Category> categories;
	
	@Getter
	@Setter
	private List<GradebookItem> gradeItems;
	
	public GradebookData(Site site) {
		this.siteId = site.getId();
		this.siteTitle = site.getTitle();
		this.gradeItems = new ArrayList<>();
		this.categories = new ArrayList<>();
	}
	
}

