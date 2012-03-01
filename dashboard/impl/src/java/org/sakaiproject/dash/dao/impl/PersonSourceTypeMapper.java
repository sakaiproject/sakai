/********************************************************************************** 
 * $URL$ 
 * $Id$ 
 *********************************************************************************** 
 * 
 * Copyright (c) 2011 The Sakai Foundation 
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.osedu.org/licenses/ECL-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **********************************************************************************/ 

package org.sakaiproject.dash.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sakaiproject.dash.model.ItemType;
import org.sakaiproject.dash.model.Person;
import org.sakaiproject.dash.model.PersonSourceType;
import org.sakaiproject.dash.model.SourceType;
import org.springframework.jdbc.core.RowMapper;

public class PersonSourceTypeMapper implements RowMapper
{

	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		ItemType itemType = ItemType.fromInteger(rs.getInt("pc_itemtype"));
		Person person = (Person) new PersonMapper().mapRow(rs, rowNum);
		SourceType sourceType = (SourceType) new SourceTypeMapper().mapRow(rs, rowNum);
		PersonSourceType personSourceType = new PersonSourceType(itemType, person, sourceType);
		return personSourceType;
	}
	
}
