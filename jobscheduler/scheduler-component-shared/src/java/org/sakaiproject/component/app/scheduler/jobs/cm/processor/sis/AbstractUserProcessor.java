/**
 * Copyright (c) 2003 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.component.app.scheduler.jobs.cm.processor.sis;

import lombok.Setter;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.scheduler.jobs.cm.processor.BaseCsvFileProcessor;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.user.api.UserDirectoryService;

abstract class AbstractUserProcessor extends BaseCsvFileProcessor {
    @Setter
    protected UserDirectoryService userDirectoryService;
    @Setter
    protected EmailService emailService;
    @Setter
    protected EmailTemplateService emailTemplateService;
    @Setter
    protected ServerConfigurationService serverConfigurationService;

    @Setter
    protected boolean userEmailNotification;
    @Setter
    protected boolean generatePassword;
    @Setter
    protected boolean updateAllowed;
    @Setter
    protected boolean updatePassword;
}
