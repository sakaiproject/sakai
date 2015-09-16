package org.sakaiproject.lti.entityprovider;

import java.util.Properties;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class LTIListEntity {
        private List<Map<String,Object>> list = null;
}
