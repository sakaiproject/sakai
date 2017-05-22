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
package org.sakaiproject.service.gradebook.shared.exception;

import org.sakaiproject.service.gradebook.shared.GradebookException;

/**
 * Exception throws when a course grade override is found to be unmappable due to a change in the grading schema.
 */
public class UnmappableCourseGradeOverrideException extends GradebookException {

	private static final long serialVersionUID = 1L;

	public UnmappableCourseGradeOverrideException(String message) {
        super(message);
    }
}

