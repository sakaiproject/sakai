/*
 * Copyright (c) 2016, The Apereo Foundation
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
 *
 */

package org.sakaiproject.tool.assessment.data.dao.assessment;

import lombok.*;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;


import java.io.Serializable;
import java.util.Date;

/**
 * Created by Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtendedTime implements Serializable {
    private static final    long                    serialVersionUID = 1L;

    private                 Long                    id;
    private                 AssessmentBaseIfc       assessment;
    private                 PublishedAssessmentIfc  pubAssessment;
    private                 String                  user;
    private                 String                  group;
    private                 Date                    startDate;
    private                 Date                    dueDate;
    private                 Date                    retractDate;
    private                 Integer                 timeHours;
    private                 Integer                 timeMinutes;

    public          ExtendedTime        (AssessmentBaseIfc ass) {
        this.assessment = ass;
    }

    public          ExtendedTime        (PublishedAssessmentIfc pub) {
        this.pubAssessment = pub;
    }

    public          ExtendedTime        (ExtendedTime source) {
        this(source.id, source.assessment, source.pubAssessment, source.user, source.group, source.startDate, source.dueDate, source.retractDate, source.timeHours, source.timeMinutes);
    }

    public Long     getAssessmentId     () {
        if(assessment == null) {
            return null;
        }
        return assessment.getAssessmentBaseId();
    }

    public Long     getPubAssessmentId  () {
        if(pubAssessment == null) {
            return null;
        }
        return pubAssessment.getPublishedAssessmentId();
    }

    /**
     * Sync the dates up to the dates in AssessmentAccessControlIfc
     */
    public void syncDates() {
        AssessmentAccessControlIfc ac = null;
        if (assessment != null) {
            ac = assessment.getAssessmentAccessControl();
        }
        else if (pubAssessment != null) {
            ac = pubAssessment.getAssessmentAccessControl();
        }

        if (ac == null) {
            return;
        }

        if (this.getDueDate() == null) {
            this.setDueDate(ac.getDueDate());
        }
        if (this.getStartDate() == null) {
            this.setStartDate(ac.getStartDate());
        }
        if (this.getRetractDate() == null && ac.getLateHandling() == AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION) {
            this.setRetractDate(ac.getRetractDate());
        }
    }

    @Override
    public boolean  equals              (final Object obj) {
        if(obj == this) return true;  // test for reference equality
        if(obj == null) return false; // test for null
        if(obj instanceof ExtendedTime) {
            final ExtendedTime other = (ExtendedTime) obj;
            return new EqualsBuilder()
                    .append(getAssessmentId(), other.getAssessmentId())
                    .append(getPubAssessmentId(), other.getPubAssessmentId())
                    .append(user, other.user)
                    .append(group, other.group)
                    .append(startDate, other.startDate)
                    .append(dueDate, other.dueDate)
                    .append(retractDate, other.retractDate)
                    .append(timeHours, other.timeHours)
                    .append(timeMinutes, other.timeMinutes)
                    .isEquals();
        } else{
            return false;
        }
    }

    @Override
    public int      hashCode            () {
        return new HashCodeBuilder()
                .append(getAssessmentId())
                .append(getPubAssessmentId())
                .append(user)
                .append(group)
                .append(startDate)
                .append(dueDate)
                .append(retractDate)
                .append(timeHours)
                .append(timeMinutes)
                .toHashCode();
    }
}
