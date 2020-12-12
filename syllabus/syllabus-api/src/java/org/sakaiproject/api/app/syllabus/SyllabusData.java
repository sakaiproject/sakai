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

import java.util.Date;
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
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.BatchSize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "SAKAI_SYLLABUS_DATA",
        indexes = {
                @Index(name = "syllabus_position", columnList = "position_c"),
                @Index(name = "SYLLABUS_DATA_SURRO_I", columnList = "surrogateKey")
        }
)

@Data
@EqualsAndHashCode(of = "syllabusId")
@ToString(of = {"syllabusId", "syllabusItem", "asset", "position", "title", "view", "status", "emailNotification", "lockId"})
public class SyllabusData implements Comparable<SyllabusData> {
    public static final String ITEM_POSTED = "posted";
    public static final String ITEM_DRAFT = "draft";

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "syllabus_data_sequence")
    @SequenceGenerator(name = "syllabus_data_sequence", sequenceName = "SyllabusDataImpl_SEQ")
    private Long syllabusId;

    @Version
    private Integer lockId;

    @Lob
    @Column(length = 16777215)
    private String asset;

    @Column(length = 128)
    private String emailNotification;

    @Column(name = "LINK_CALENDAR")
    private Boolean linkCalendar = Boolean.FALSE;

    @Column(name = "position_c", nullable = false)
    private Integer position;

    @Column(name = "START_DATE")
    private Date startDate;

    @Column(name = "END_DATE")
    private Date endDate;

    @Column(length = 64)
    private String status;

    @Column(length = 256)
    private String title;

    @Column(name = "xview", length = 16)
    private String view;

    @Column(name = "CALENDAR_EVENT_ID_START", length = 99)
    private String calendarEventIdStartDate;

    @Column(name = "CALENDAR_EVENT_ID_END", length = 99)
    private String calendarEventIdEndDate;

    @OneToMany(mappedBy = "syllabusData", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private Set<SyllabusAttachment> attachments = new TreeSet<>();

    @ManyToOne
    @JoinColumn(name = "surrogateKey")
    private SyllabusItem syllabusItem;

    public int compareTo(SyllabusData data) {
        if (this.equals(data)) return 0;
        if (this.position == null) return -1;
        return this.position.compareTo(data.getPosition());
    }
}
