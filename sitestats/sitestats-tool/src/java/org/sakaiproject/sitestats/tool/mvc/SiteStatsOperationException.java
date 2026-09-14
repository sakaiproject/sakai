/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.tool.mvc;

import lombok.Getter;

@Getter
public class SiteStatsOperationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String messageCode;

    public SiteStatsOperationException(String messageCode, String message) {
        super(message);
        this.messageCode = messageCode;
    }
}
