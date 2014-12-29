/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package edu.amc.sakai.user;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.apache.commons.lang.StringUtils;

/**
 * Not a stub per-se, so much as a {@link BaseResourcePropertiesEdit} extension
 * with enhancements for testing equality, outputting a meaningful String
 * representation, and initializing state from a set of default and
 * override {@link Properties}.
 * 
 * 
 * @author dmccallum@unicon.net
 *
 */
public class ResourcePropertiesEditStub extends BaseResourcePropertiesEdit {

	private static final long serialVersionUID = 1L;
	
	public ResourcePropertiesEditStub() {
		super();
	}
	
	public ResourcePropertiesEditStub(Properties defaultConfig, Properties configOverrides) {
		super();
		if ( defaultConfig != null && !(defaultConfig.isEmpty()) ) {
			for ( Enumeration i = defaultConfig.propertyNames() ; i.hasMoreElements() ; ) {
				String propertyName = (String)i.nextElement();
				String propertyValue = StringUtils.trimToNull((String)defaultConfig.getProperty(propertyName));
				if ( propertyValue == null ) {
					continue;
				}
				String[] propertyValues = propertyValue.split(";");
				if ( propertyValues.length > 1 ) {
					for ( String splitPropertyValue : propertyValues ) {
						super.addPropertyToList(propertyName, splitPropertyValue);
					}
				} else {
					super.addProperty(propertyName, propertyValue);
				}
			}
		}
		
		if ( configOverrides != null && !(configOverrides.isEmpty()) ) {
			// slightly different... configOverrides are treated as complete
			// overwrites of existing values.
			for ( Enumeration i = configOverrides.propertyNames() ; i.hasMoreElements() ; ) {
				String propertyName = (String)i.nextElement();
				super.removeProperty(propertyName);
				String propertyValue = StringUtils.trimToNull((String)configOverrides.getProperty(propertyName));
				String[] propertyValues = propertyValue.split(";");
				if ( propertyValues.length > 1 ) {
					for ( String splitPropertyValue : propertyValues ) {
						super.addPropertyToList(propertyName, splitPropertyValue);
					}
				} else {
					super.addProperty(propertyName, propertyValue);
				}
			}
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for ( Iterator names = getPropertyNames(); names.hasNext();  ) {
			String name = (String)names.next();
			sb.append(name + "=" + this.getPropertyFormatted(name) + "; ");
		}
		
		return sb.toString();
	}
	
	/** Pure hack, but BaseResourceProperties doesn't have a meaningful impl */
	public boolean equals(Object o) {
		
		if ( o == this ) {
			return true;
		}
		
		if ( o == null ) {
			return false;
		}
		
        if ( !(o instanceof ResourcePropertiesEdit) ) {
            return false;
        }
    
        ResourcePropertiesEdit otherProps = (ResourcePropertiesEdit)o;
        
        int cnt = 0;
        Iterator namesInOther = otherProps.getPropertyNames();
        while ( namesInOther.hasNext() ) {

            cnt++;
            String nameInOther = (String)namesInOther.next();
            Object valueInOther = otherProps.get(nameInOther);

            Object valueInThis = get(nameInOther);
            
            if ( valueInThis == valueInOther ) {
            	continue;
            }
            
            if ( valueInThis == null || valueInOther == null ) {
                return false;
            }
            
            if ( !(valueInThis.equals(valueInOther)) ) {
                return false;
            }
                     
        }

        if ( m_props.size() != cnt ) {
            return false;
        }

        return true;
    
    }
	
}
