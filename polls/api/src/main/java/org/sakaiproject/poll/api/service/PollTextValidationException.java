/*
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://opensource.org/licenses/ecl2.
 */
package org.sakaiproject.poll.api.service;

public class PollTextValidationException extends IllegalArgumentException {

    public PollTextValidationException(String message) {
        super(message);
    }
}
