/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant;

import org.sakaiproject.site.tool.helper.participant.impl.SiteAddParticipantHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Starts a fresh Add Participants operation when Site Info launches the helper. */
@Controller
public class ParticipantAddLaunchController {

    private final SiteAddParticipantHandler handler;

    public ParticipantAddLaunchController(SiteAddParticipantHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/")
    public String startNewOperation() {
        handler.startNewOperation();
        return "redirect:/add";
    }
}
