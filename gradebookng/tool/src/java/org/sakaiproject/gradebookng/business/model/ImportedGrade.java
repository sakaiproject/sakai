package org.sakaiproject.gradebookng.business.model;

import lombok.Data;
import org.sakaiproject.entity.api.ResourceProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

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
