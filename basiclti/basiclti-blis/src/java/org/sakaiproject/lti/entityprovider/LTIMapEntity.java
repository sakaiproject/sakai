package org.sakaiproject.lti.entityprovider;

import java.util.Properties;
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
public class LTIMapEntity {
        private Map<String,Object> map = null;
}
