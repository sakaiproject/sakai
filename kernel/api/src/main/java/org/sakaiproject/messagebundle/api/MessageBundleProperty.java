/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/branches/SAK-18678/api/src/main/java/org/sakaiproject/site/api/Site.java $
 * $Id: Site.java 81275 2010-08-14 09:24:56Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.sakaiproject.messagebundle.api;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table( name = "SAKAI_MESSAGE_BUNDLE",
        indexes = {
            @Index(name = "SMB_BASENAME_IDX", columnList = "BASENAME"),
            @Index(name = "SMB_MODULE_IDX", columnList = "MODULE_NAME"),
            @Index(name = "SMB_LOCALE_IDX", columnList = "LOCALE"),
            @Index(name = "SMB_PROPNAME_IDX", columnList = "PROP_NAME"),
            @Index(name = "SMB_SEARCH", columnList = "BASENAME, MODULE_NAME, LOCALE, PROP_NAME")
})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({
        @NamedQuery(name = "findProperty", query = "from MessageBundleProperty where baseName = :basename and moduleName = :module and propertyName = :name and locale = :locale"),
        @NamedQuery(name = "findPropertyWithNullValue", query = "from MessageBundleProperty where baseName = :basename and moduleName = :module and locale = :locale and value is not null"),
        @NamedQuery(name = "findLocales", query = "select distinct(locale) from MessageBundleProperty")
})

@Data
@NoArgsConstructor
public class MessageBundleProperty {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "message_bundle_property_sequence")
    @SequenceGenerator(name = "message_bundle_property_sequence", sequenceName = "SAKAI_MESSAGEBUNDLE_S")
    private Long id;

    @Column(name = "BASENAME", nullable = false)
    private String baseName;

    @Column(name = "MODULE_NAME", nullable = false)
    private String moduleName;

    @Column(name = "LOCALE", nullable = false)
    private String locale;

    @Column(name = "PROP_NAME", nullable = false)
    private String propertyName;

    @Lob
    @Column(name = "PROP_VALUE")
    private String value;

    @Lob
    @Column(name = "DEFAULT_VALUE")
    private String defaultValue;

    public MessageBundleProperty(String baseName, String moduleName, String locale, String propertyName) {
        this.baseName = baseName;
        this.moduleName = moduleName;
        this.locale = locale;
        this.propertyName = propertyName;
    }
}
