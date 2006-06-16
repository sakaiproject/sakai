/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.tool.podcasts;


public class podPermBean {
	private boolean [] maintainPerms = new boolean [4];
	private boolean [] accessPerms = new boolean [4];
	
	public podPermBean () {
		for (int i = 0; i < 4; i++) {
			maintainPerms[i]=false;
			accessPerms[i]=false;
		}
		
		maintainPerms[1] = accessPerms[1] = true;
	}
	
	public podPermBean (boolean [] mPerms, boolean [] aPerms) {
		for (int i = 0; i < 4; i++) {
			maintainPerms[i] = mPerms[i];
			accessPerms[i] = mPerms[i];
		}
	}
	
	public boolean getmNew () {
		return maintainPerms[0];
	}
	
	public void setmNew (boolean newPerm) {
	  maintainPerms[0] = newPerm;
	}

	public boolean getmRead () {
		return maintainPerms[1];
	}
	
	public void setmRead (boolean newPerm) {
	  maintainPerms[1] = newPerm;
	}
	
	public boolean getmRevise () {
		return maintainPerms[2];
	}
	
	public void setmRevise (boolean newPerm) {
	  maintainPerms[2] = newPerm;
	}
	
	public boolean getmDelete () {
		return maintainPerms[3];
	}
	
	public void setmDelete (boolean newPerm) {
	  maintainPerms[3] = newPerm;
	}
	
	public boolean getaNew () {
		return accessPerms[0];
	}
	
	public void setaNew (boolean newPerm) {
	  accessPerms[0] = newPerm;
	}
	
	public boolean getaRead () {
		return accessPerms[1];
	}
	
	public void setaRead (boolean newPerm) {
	  accessPerms[1] = newPerm;
	}
	
	public boolean getaRevise () {
		return maintainPerms[2];
	}
	
	public void setaRevise (boolean newPerm) {
	  accessPerms[2] = newPerm;
	}
	
	public boolean getaDelete () {
		return accessPerms[3];
	}
	
	public void setaDelete (boolean newPerm) {
	  accessPerms[3] = newPerm;
	}
	
	public String processPermChange() {
		return "cancel";
	}
	
	public String processPermCancel() {
		return "cancel";
	}

}