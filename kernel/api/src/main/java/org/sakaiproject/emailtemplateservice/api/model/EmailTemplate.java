/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2006, 2007 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.emailtemplateservice.api.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Element;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * EmailTemplate is an email template, though it could actually be used for anything,
 * identified by a unique key and set to be locale specific if desired
 */
@Root
@Entity
@Table(name = "EMAIL_TEMPLATE_ITEM",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"TEMPLATE_KEY", "TEMPLATE_LOCALE"})},
        indexes = {@Index(name = "email_templ_owner", columnList = "OWNER"),
            @Index(name = "email_templ_key", columnList= "TEMPLATE_KEY")}
)
@Getter @Setter
public class EmailTemplate implements java.io.Serializable, PersistableEntity<Long> {

    public static final String DEFAULT_LOCALE = "default";
    private static final long serialVersionUID = -8697605573015358433L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "email_template_item_id_sequence")
    @SequenceGenerator(name = "email_template_item_id_sequence", sequenceName = "EMAILTEMPLATE_ITEM_SEQ")
    private Long id;

    @Column(name = "LAST_MODIFIED", nullable = false)
    private Date lastModified;

    @Element
    @Column(name = "TEMPLATE_KEY", nullable = false, length = 255)
    private String key;

    @Element(required = false)
    @Column(name = "TEMPLATE_LOCALE", length = 255)
    private String locale;

    @Element
    @Column(name = "OWNER", nullable = false, length = 255)
    private String owner;

    @Element
    @Lob
    @Column(name = "SUBJECT", nullable = false, length = 100000000)
    private String subject;

    @Element
    @Lob
    @Column(name = "MESSAGE", nullable = false, length = 100000000)
    private String message;

    @Element(required = false)
    @Lob
    @Column(name = "HTMLMESSAGE", length = 100000000)
    private String htmlMessage;

    @Element
    @Column(name = "VERSION")
    private Integer version;

    @Column(name = "emailfrom", length = 255)
    private String from;

    @Column(length = 255, unique = false)
    private String defaultType;

    /** default constructor */
    public EmailTemplate() {
    }

    public EmailTemplate(String key, String owner, String message) {

        if (this.lastModified == null) {
            this.lastModified = new Date();
        }
        this.owner = owner;
        this.message = message;
    }

    /** full constructor */
    public EmailTemplate(String key, String owner, String message, String defaultType, String locale) {

        if (this.lastModified == null) {
            this.lastModified = new Date();
        }
        this.key = key;
        this.locale = locale;
        this.owner = owner;
        this.message = message;
        this.defaultType = defaultType;
    }
}
