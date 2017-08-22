/**
 * Copyright (c) 2005-2011 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import java.io.Serializable;

import org.sakaiproject.tool.assessment.ui.bean.util.Validator;

public class ItemBarBean 
implements Serializable
{
/**
 * 
 */
private static final long serialVersionUID = 1L;
private String itemText;
private String columnHeight;
private String numStudentsText;


public ItemBarBean(){
	this.itemText = null;
	this.columnHeight = null;
}
public void setItemText(String itemText){
	this.itemText = itemText;
}
public void setColumnHeight(String columnHeight){
	this.columnHeight =columnHeight;
}
public String getItemText(){
	return Validator.check(itemText, "N/A");
}
public String getColumnHeight(){
	return Validator.check(columnHeight, "N/A");
}

public void setNumStudentsText(String text){
	this.numStudentsText = text;
}
public String getNumStudentsText(){
	return this.numStudentsText;
}


}