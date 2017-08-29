/**
 * Copyright (c) 2003-2010 The Apereo Foundation
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

package org.sakaiproject.site.util;

/**
 * Inner class to put certain type of link into UI, that could be enabled or disabled by user actions
 * @author zqian
 *
 */
public class ActionLinkItem
{
	private String id = "";
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	private String label="";
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	private String link="";
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	
	private boolean toggleable = true;
	public boolean getToggleable() {
		return toggleable;
	}
	public void setToggleable(boolean toggleable) {
		this.toggleable = toggleable;
	}
	
	private boolean disabled = true;
	public boolean getDisabled() {
		return disabled;
	}
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public ActionLinkItem ()
	{
	}
	public ActionLinkItem(String id, String label, String link, boolean toggleable, boolean disabled)
	{
		this.id = id;
		this.label = label;
		this.link = link;
		this.toggleable = toggleable;
		this.disabled = disabled;
	}
}	// ActionLinkItem