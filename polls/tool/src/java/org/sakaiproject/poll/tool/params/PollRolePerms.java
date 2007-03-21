package org.sakaiproject.poll.tool.params;

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
			this.vote = new Boolean(vote); 
			this.add = new Boolean(add); 
			this.deleteAny = new Boolean(deleteAny); 
			this.deleteOwn = new Boolean(deleteOwn);
			this.editAny = new Boolean(editAny); 
			this.editOwn = new Boolean(editOwn); 
			
		}
		

}
