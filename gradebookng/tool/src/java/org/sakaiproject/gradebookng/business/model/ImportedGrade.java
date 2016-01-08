package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.sakaiproject.entity.api.ResourceProperties;

import lombok.Data;

/**
 * Created by chmaurer on 1/21/15.
 */
@Data
public class ImportedGrade implements Serializable {

	private String studentEid;
	private String studentUuid;
	private String studentName;

	private Map<String, ImportedGradeItem> gradeItemMap = new HashMap<String, ImportedGradeItem>();

	private ResourceProperties properties;

	/**
	 * Convenience method to get the list of properties as a map;
	 * 
	 * @return
	 */
	public Map<String, String> propertiesToMap() {
		final Map<String, String> m = new LinkedHashMap<String, String>();
		final Iterator<String> iter = this.properties.getPropertyNames();
		while (iter.hasNext()) {
			final String prop = iter.next();
			m.put(prop, this.properties.getProperty(prop));

		}
		return m;

	}
}
