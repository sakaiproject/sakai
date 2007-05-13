/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006,2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.poll.model;

import java.util.Stack;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



public class Option implements Entity{

	
	private Long pollId;
	private Long id;
	private String text;
	private String status;
	private String UUId;
	
	public Option(){
		
	}	
	public Option(Long oId){
		this.id = oId;
	}
	
	public void setOptionId(Long value) {
		// TODO Auto-generated method stub
		id = value;
	}

	public Long getOptionId() {
		
		return id;
	}

	public void setOptionText(String option) {
		
		text = option;

	}

	public String getOptionText() {
		
		return text;
	}

	public Long getPollId(){
		return pollId;
	}
	
	public void setPollId(Long pollid) {
		this.pollId = pollid;
	}
	
	public void setStatus(String s) {
		this.status = s;
	}
	
	public String getStatus() {
		return this.status;
	}
	public ResourceProperties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getReference() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getReference(String rootProperty) {
		return ServerConfigurationService.getAccessUrl() + "/poll/" + this.getPollId() + Entity.SEPARATOR +"option"+ Entity.SEPARATOR + this.getId();
	}
	public String getUrl() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getUrl(String rootProperty) {
		// TODO Auto-generated method stub
		return null;
	}
	public Element toXml(Document doc, Stack stack) {
		Element option = doc.createElement("option");

		if (stack.isEmpty())
		{
			doc.appendChild(option);
		}
		else
		{
			((Element) stack.peek()).appendChild(option);
		}

		stack.push(option);

		option.setAttribute("id", getId());
		option.setAttribute("optionid", getOptionId().toString());
		option.setAttribute("title", getOptionText());
		stack.pop();

		return option;
	}
	public String getId() {
		// TODO Auto-generated method stub
		return UUId;
	}
	public void setId(String id) {
		UUId = id;
	}
}
