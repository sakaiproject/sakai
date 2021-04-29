/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.api.app.syllabus;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.BatchSize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "SAKAI_SYLLABUS_ITEM",
        indexes = {
                @Index(name = "syllabus_userId", columnList = "userId"),
                @Index(name = "syllabus_contextId", columnList = "contextId")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uniqueSyllabus", columnNames = {"userId", "contextId"})
        }
)
@Data
@EqualsAndHashCode(of = {"userId", "contextId", "redirectURL"})
@NoArgsConstructor
@ToString(of = {"surrogateKey", "userId", "contextId", "redirectURL", "lockId"})
public class SyllabusItem {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "syllabus_item_sequence")
    @SequenceGenerator(name = "syllabus_item_sequence", sequenceName = "SyllabusItemImpl_SEQ")
    private Long surrogateKey;

    @Version
    private Integer lockId;

    @Column(length = 36, nullable = false)
    private String userId;

    @Column(nullable = false)
    private String contextId;

    @Column(length = 512)
    private String redirectURL;

    @OneToMany(mappedBy = "syllabusItem", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @OrderBy("position ASC")
    private Set<SyllabusData> syllabi = new TreeSet<>();


    public SyllabusItem(String userId, String contextId, String redirectURL) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(contextId);

        this.userId = userId;
        this.contextId = contextId;
        this.redirectURL = redirectURL;
    }
}
