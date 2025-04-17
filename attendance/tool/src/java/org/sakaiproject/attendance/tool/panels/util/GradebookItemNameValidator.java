/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.tool.panels.util;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.sakaiproject.attendance.api.AttendanceGradebookProvider;
import org.sakaiproject.attendance.model.AttendanceSite;

/**
 * GradebookItemNameValidator is used to ensure the Gradebook Item Name is not all ready present in the gradebook
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public class GradebookItemNameValidator implements IValidator<String> {
    @SpringBean(name="org.sakaiproject.attendance.api.AttendanceGradebookProvider")
    private AttendanceGradebookProvider attendanceGradebookProvider;

    private String siteID;
    private Long aSID;
    private String oldValue;

    public GradebookItemNameValidator(AttendanceSite aS, String oldValue) {
        this.siteID = aS.getSiteID();
        this.aSID = aS.getId();
        this.oldValue = oldValue;
    }

    @Override
    public void validate(IValidatable<String> validatable) {
        Injector.get().inject(this);
        final String name = validatable.getValue();

        if(attendanceGradebookProvider.isAssessmentDefined(siteID, aSID)) {
            if(oldValue == null || !oldValue.equals(name)) {
                if(attendanceGradebookProvider.isGradebookAssignmentDefined(siteID, name)){
                    error(validatable, "gradebook.name.defined");
                }
            }
        } else {
            if(attendanceGradebookProvider.isGradebookAssignmentDefined(siteID, name)){
                error(validatable, "gradebook.name.defined");
            }
        }
    }

    private void error(IValidatable<String> validatable, String errorKey) {
        ValidationError error = new ValidationError();
        error.addKey(getClass().getSimpleName() + "." + errorKey);
        validatable.error(error);
    }
}
