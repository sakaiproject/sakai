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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.springframework.data.PersistableEntity;

@Getter
@Setter
@Entity
@Table(name = "POLL_OPTION")
public class Option implements PersistableEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OPTION_ID")
    private Long optionId;

    @Column(name = "OPTION_POLL_ID", nullable = false)
    private Long pollId;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "OPTION_TEXT", nullable = false)
    private String text;

    @Transient
    private String status;

    @Column(name = "OPTION_UUID", nullable = false, length = 99)
    private String uuid;

    @Column(name = "DELETED", nullable = false)
    private Boolean deleted = Boolean.FALSE;

    @Column(name = "OPTION_ORDER", nullable = false)
    private Integer optionOrder;

    public Option() {
        // default constructor
    }

    public Option(Long optionId) {
        this.optionId = optionId;
    }

    @Override
    public Long getId() {
        return optionId;
    }
}
