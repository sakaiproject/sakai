/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.poll.model;

public class PollRolePerms {
	
	public String rolename;
	public Boolean vote;
	public Boolean add;
	public Boolean deleteAny;
	public Boolean deleteOwn;
	public Boolean editAny;
	public Boolean editOwn;

	
		public PollRolePerms(String rolename) 
		{ 
			this.rolename = rolename; 
		}
		
		public PollRolePerms(String rolename,   Boolean vote, Boolean add,  Boolean deleteOwn, Boolean deleteAny, Boolean editOwn, Boolean editAny) {	
			this.rolename = rolename; 
			this.vote = vote; 
			this.add = add; 
			this.deleteAny = deleteAny; 
			this.deleteOwn = deleteOwn;
			this.editAny = editAny; 
			this.editOwn = editOwn; 
			
		}
		
		public PollRolePerms(String rolename, boolean vote, boolean add, boolean deleteOwn, boolean deleteAny, boolean editOwn, boolean editAny) {	
			this.rolename = rolename; 
			this.vote = Boolean.valueOf(vote); 
			this.add = Boolean.valueOf(add); 
			this.deleteAny = Boolean.valueOf(deleteAny); 
			this.deleteOwn = Boolean.valueOf(deleteOwn);
			this.editAny = Boolean.valueOf(editAny); 
			this.editOwn = Boolean.valueOf(editOwn); 
			
		}
		

}
