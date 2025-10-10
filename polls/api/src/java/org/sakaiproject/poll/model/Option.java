/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation
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

package org.sakaiproject.poll.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.sakaiproject.springframework.data.PersistableEntity;

@Data
@Entity
@Table(name = "POLL_OPTION")
public class Option implements PersistableEntity<Long> {

    @Id
    @Column(name = "OPTION_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "poll_option_sequence")
    @SequenceGenerator(name = "poll_option_sequence", sequenceName = "POLL_OPTION_ID_SEQ")
    private Long optionId;

    @Column(name = "OPTION_POLL_ID", nullable = false)
    private Long pollId;

    @Lob
    @Column(name = "OPTION_TEXT", nullable = false)
    private String text;

    private String status;

    @Column(name = "OPTION_UUID", length = 255, nullable = false)
    private String uuid;

    @Column(name = "DELETED", nullable = false)
    private Boolean deleted = Boolean.FALSE;

    @Column(name = "OPTION_ORDER", nullable = false)
    private Integer optionOrder;

    public Option() {}

    public Option(Long oId) {
        this.optionId = oId;
    }

    // PersistableEntity implementation
    @Override
    public Long getId() {
        return optionId;
    }
}
