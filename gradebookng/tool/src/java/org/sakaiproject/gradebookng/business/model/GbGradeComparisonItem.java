/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sakaiproject.gradebookng.business.model;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.grading.api.GradeDefinition;

import java.io.Serializable;
import java.util.Optional;

@Getter @Setter @NoArgsConstructor
public class GbGradeComparisonItem implements Serializable {
    
    transient String studentFirstName;
    transient String studentLastName;
    
    String studentDisplayName;
    
    transient String eid;
    Boolean isCurrentUser;
    
    String grade;
    String teacherComment;

    public GbGradeComparisonItem(GbStudentGradeInfo item) {
        this.eid = item.getStudentEid();
        this.studentFirstName = item.getStudentFirstName();
        this.studentLastName = item.getStudentLastName();
        GbGradeInfo gradeInfoEl = item.getGrades()
          .values()
          .stream()
          .findFirst()
          .orElseGet(() -> {
              GradeDefinition auxGd = new GradeDefinition();
              auxGd.setGrade("");
              return new GbGradeInfo(auxGd);
          });
        this.grade = gradeInfoEl.getGrade();
        this.teacherComment = Optional.ofNullable(gradeInfoEl.getGradeComment()).orElse("");
    }
}
