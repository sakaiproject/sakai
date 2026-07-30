/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.tool.mvc;

import lombok.Getter;

@Getter
public class InvalidReportConfigurationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String messageCode;

    public InvalidReportConfigurationException(String messageCode) {
        super("Invalid report configuration: " + messageCode);
        this.messageCode = messageCode;
    }
}
