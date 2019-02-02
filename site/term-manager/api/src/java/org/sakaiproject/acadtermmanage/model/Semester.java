package org.sakaiproject.acadtermmanage.model;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.apache.commons.beanutils.BeanUtils;

import org.sakaiproject.coursemanagement.api.AcademicSession;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Semester implements Serializable {

	private static final long serialVersionUID = -5582248765432695478L;

	private String eid;
	private Date startDate;
	private Date endDate;
	private String title;
	private String description;	
	private boolean isCurrent;
	

	public static Semester createFromAcademicSession(AcademicSession as){
		Semester s = new Semester();
		s.eid = as.getEid();
		s.startDate = as.getStartDate();
		s.endDate = as.getEndDate();
		s.title = as.getTitle();
		s.description = as.getDescription();
		return s;
	}
	
	public static Semester createCopy(Semester otherSemester){
		Semester s = new Semester();
		try {
			BeanUtils.copyProperties(s, otherSemester);
		}
		catch (IllegalAccessException | InvocationTargetException e) {
			log.warn("Could copy properties", e);
		}

		return s;
	}
}
