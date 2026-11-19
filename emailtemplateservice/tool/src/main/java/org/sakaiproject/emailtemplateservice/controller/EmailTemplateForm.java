/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.emailtemplateservice.controller;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.emailtemplateservice.api.model.EmailTemplate;

/** The editable fields accepted by the administration form, including partial updates. */
@Getter
@Setter
// The existing form sends this field, but the entity has always ignored it on input.
@JsonIgnoreProperties("from")
public class EmailTemplateForm {

    private String subject;
    private String key;
    private String locale;
    private String message;
    @JsonAlias("messagehtml")
    private String htmlMessage;

    public EmailTemplateForm(EmailTemplate template) {
        subject = template.getSubject();
        key = template.getKey();
        locale = template.getLocale();
        message = template.getMessage();
        htmlMessage = template.getHtmlMessage();
    }

    public EmailTemplate toTemplate(EmailTemplate original) {
        EmailTemplate template = new EmailTemplate();
        template.setId(original.getId());
        template.setOwner(original.getOwner());
        template.setVersion(original.getVersion());
        template.setLastModified(original.getLastModified());
        template.setFrom(original.getFrom());
        template.setDefaultType(original.getDefaultType());
        template.setSubject(subject);
        template.setKey(key);
        template.setLocale(locale);
        template.setMessage(message);
        template.setHtmlMessage(htmlMessage);
        return template;
    }
}
