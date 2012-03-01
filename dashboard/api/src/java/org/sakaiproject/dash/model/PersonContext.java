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

package org.sakaiproject.dash.model;

/**
 * 
 *
 */
public class PersonContext {
	
	protected long id;
	protected ItemType itemType;
	protected Person person;
	protected Context context;
	
	/**
	 * 
	 */
	public PersonContext() {
		super();
	}

	/**
	 * @param id
	 * @param person
	 * @param context
	 */
	public PersonContext(ItemType itemType, Person person, Context context) {
		super();
		this.itemType = itemType;
		this.person = person;
		this.context = context;
	}
	/**
	 * @param id
	 * @param person
	 * @param context
	 */
	public PersonContext(long id, ItemType itemType, Person person, Context context) {
		super();
		this.id = id;
		this.itemType = itemType;
		this.person = person;
		this.context = context;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @return the itemType
	 */
	public ItemType getItemType() {
		return itemType;
	}

	/**
	 * @return the person
	 */
	public Person getPerson() {
		return person;
	}

	/**
	 * @return the context
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @param itemType the itemType to set
	 */
	public void setItemType(ItemType itemType) {
		this.itemType = itemType;
	}

	/**
	 * @param person the person to set
	 */
	public void setPerson(Person person) {
		this.person = person;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(Context context) {
		this.context = context;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PersonContext [id=");
		builder.append(id);
		builder.append(", itemType=");
		builder.append(itemType.getName());
		builder.append(", person=");
		builder.append(person);
		builder.append(", context=");
		builder.append(context);
		builder.append("]");
		return builder.toString();
	}
	
}
