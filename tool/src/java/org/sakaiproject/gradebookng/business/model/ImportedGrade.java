package org.sakaiproject.gradebookng.business.model;

import lombok.Data;
import org.sakaiproject.entity.api.ResourceProperties;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chmaurer on 1/21/15.
 */
@Data
public class ImportedGrade implements Serializable {

    private String studentId;
    private String studentName;

    private List<ImportedGradeItem> gradeItems;

    private ResourceProperties properties;

    /**
     * Convenience method to get the list of properties as a map;
     * @return
     */
    public Map<String,String> propertiesToMap() {
        Map<String,String> m = new LinkedHashMap<String, String>();
        Iterator<String> iter = properties.getPropertyNames();
        while(iter.hasNext()){
            String prop = iter.next();
            m.put(prop, properties.getProperty(prop));

        }
        return m;

    }
}
