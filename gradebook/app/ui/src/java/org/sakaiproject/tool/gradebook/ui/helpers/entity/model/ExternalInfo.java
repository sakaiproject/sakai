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

import org.sakaiproject.service.gradebook.shared.Assignment;

import lombok.Getter;
import lombok.Setter;

/**
 * Encapsulates the external info for a {@link GradebookItem}, if any.
 */
public class ExternalInfo {
	
	@Getter
	@Setter
	private String externalId;
	
	@Getter
	@Setter
	private String externalApp;
	
	public ExternalInfo(Assignment assignment) {
		this.externalId = assignment.getExternalId();
		this.externalApp = assignment.getExternalAppName();
	}
	
}

