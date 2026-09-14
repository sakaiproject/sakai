/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant.impl;

import java.util.Arrays;

import lombok.Getter;

/** A localized message produced while processing the participant wizard. */
@Getter
public class ParticipantMessage {

    public enum Severity {
        ERROR,
        WARNING,
        INFO
    }

    private final String code;
    private final Object[] args;
    private final Severity severity;

    public ParticipantMessage(String code, Object[] args, Severity severity) {
        this.code = code;
        this.args = args == null ? new Object[0] : Arrays.copyOf(args, args.length);
        this.severity = severity;
    }

    public ParticipantMessage(String code, Object arg, Severity severity) {
        this(code, new Object[] {arg}, severity);
    }
}
