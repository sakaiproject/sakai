/**********************************************************************************
*
* $Header: /cvs/sakai2/providers/openldap/src/java/es/udl/asic/user/OpenLdapDirectoryProvider.java,v 1.1 2005/06/03 11:54:21 csev.umich.edu Exp $
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

/**
 * <p>
 * An implementation of a Sakai UserDirectoryProvider that authenticates/retrieves 
 * users from an OpenLDAP directory.
 * </p>
 * 
 * @author ASIC - Udl
 * @version $Revision 1.0
 */

package es.udl.asic.user;

import java.util.Hashtable;
import java.util.HashMap;
import java.util.Map;


import org.sakaiproject.service.framework.log.Logger;
import org.sakaiproject.service.legacy.user.UserDirectoryProvider;
import org.sakaiproject.service.legacy.user.UserEdit;

//Necessary for OpenLdap

import javax.naming.*;
import javax.naming.directory.*;

public class OpenLdapDirectoryProvider implements UserDirectoryProvider {
	

	private String ldapHost = ""; //address of ldap server
	private int ldapPort = 389; //port to connect to ldap server on
	private String basePath = "";
	
	private Hashtable env = new Hashtable();
	 
    protected Logger m_logger = null; 
    
    public void setLogger(Logger service){           
    	m_logger = service;  
    }

    public void init(){     
    	try{
    		m_logger.info(this +".init()");               
    		}  
    	catch (Throwable t){
    		m_logger.warn(this +".init(): ", t);
    	}
		
    	env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL,getLdapHost() + ":" + getLdapPort());
		env.put(Context.SECURITY_AUTHENTICATION,"simple");
		env.put(Context.SECURITY_CREDENTIALS,"secret");
    }
    
    public void destroy(){ 
    	m_logger.info(this +".destroy()");   
    }
        
	public boolean authenticateUser(String userLogin, UserEdit edit, String password){
		Hashtable env = new Hashtable();
		InitialDirContext ctx;
	 	
		String INIT_CTX="com.sun.jndi.ldap.LdapCtxFactory";
		String MY_HOST= getLdapHost() +":" + getLdapPort();
		String cn;
		boolean returnVal=false;		
		
		if (!password.equals("")){

			env.put(Context.INITIAL_CONTEXT_FACTORY,INIT_CTX);
			env.put(Context.PROVIDER_URL,MY_HOST);
			env.put(Context.SECURITY_AUTHENTICATION,"simple");
			env.put(Context.SECURITY_CREDENTIALS,"secret");
			
			String[] returnAttribute = {"ou"};
	        SearchControls srchControls = new SearchControls();
	        srchControls.setReturningAttributes(returnAttribute);
	        srchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			
			String searchFilter = "(&(objectclass=person)(uid="+userLogin+"))";
			
	        try{                    
		          ctx = new InitialDirContext(env);
			      NamingEnumeration answer = ctx.search(getBasePath(),searchFilter, srchControls);
			      String trobat="false";

			      while (answer.hasMore() && trobat.equals("false")){
  				      	
   				      	SearchResult sr = (SearchResult)answer.next();
   				      	String dn=sr.getName().toString()+","+getBasePath();

	                //Second binding
	                Hashtable authEnv = new Hashtable();
	                try{
	                	authEnv.put(Context.INITIAL_CONTEXT_FACTORY,INIT_CTX);
	                	authEnv.put(Context.PROVIDER_URL,MY_HOST);
	                	authEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
	                	authEnv.put(Context.SECURITY_PRINCIPAL,sr.getName()+","+ getBasePath());
	                	authEnv.put(Context.SECURITY_CREDENTIALS,password);
	                	try{ 
	                		DirContext authContext = new InitialDirContext(authEnv);  
	                		returnVal=true;	
	                		trobat="true";
	                		authContext.close();
	                	}catch(AuthenticationException ae){	
	                		m_logger.info("Access forbidden");
	                		}
					    
					 } catch (NamingException namEx) {
	                                    m_logger.info(this + "User doesn't exist");
	                                    returnVal=false;
	                                    namEx.printStackTrace();
	                   }
	               } 
			      if (trobat.equals("false")) returnVal=false;
				
	        	}
	            catch(NamingException namEx){
	            	namEx.printStackTrace();
	                returnVal=false;
	                }
	           }
		return returnVal;
	}
	
	public void destroyAuthentication() {
	}
	
	public boolean findUserByEmail(UserEdit edit, String email) {
		
		env.put(Context.SECURITY_PRINCIPAL,"");
		env.put(Context.SECURITY_CREDENTIALS,"");
		String filter = "(&(objectclass=person)(mail="+email+"))";
		return getUserInf(edit,filter);
	}
	
	public boolean getUser(UserEdit edit) {
				
		if (!userExists(edit.getId()))
			return false;

		env.put(Context.SECURITY_PRINCIPAL,"");
		env.put(Context.SECURITY_CREDENTIALS,"");
		String filter = "(&(objectclass=person)(uid="+edit.getId()+"))";
		return getUserInf(edit,filter);
	}		
		
	public boolean updateUserAfterAuthentication() {
		return false;
	}
	
	public boolean userExists(String id) {
		env.put(Context.SECURITY_AUTHENTICATION,"simple");
		env.put(Context.SECURITY_CREDENTIALS,"secret");
		
		try
		{
			DirContext ctx = new InitialDirContext(env);

			/* Setup subtree scope to tell LDAP to recursively descend directory structure 
			during searches. */
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			/*Setup the directory entry attributes we want to search for. In this case it
			 is the user's ID.*/

			String filter = "(&(objectclass=person)(uid="+id+"))";

			/* Execute the search, starting at the directory level of Users */
			
			NamingEnumeration hits = ctx.search(getBasePath(), filter, searchControls);

			/* All we need to know is if there were any hits at all. */
			
			if(hits.hasMore()){
				hits.close();
				ctx.close();
				return true;
			}else{
				hits.close();
				ctx.close();
				return false;
			}
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}	
	}
	
	private boolean getUserInf(UserEdit edit,String filter){

		String id = null;
		String firstName = null;
		String lastName = null;
		String employeenumber =null;
		String email = null;
		try
		{
			DirContext ctx = new InitialDirContext(env);

			// Setup subtree scope to tell LDAP to recursively descend directory structure
			// during searches.
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			// We want the user's id, first name and last name ...
			searchControls.setReturningAttributes(new String[] {"uid","givenName","sn"});

			// Execute the search, starting at the directory level of Users
			NamingEnumeration results = ctx.search(getBasePath(), filter, searchControls);

			while(results.hasMore())
			{
				SearchResult result = (SearchResult) results.next();
				String dn=result.getName().toString()+","+getBasePath();
				Attributes attrs=ctx.getAttributes(dn);
				id = attrs.get("uid").get().toString();
				String cn=attrs.get("cn").get().toString();
				firstName=cn.substring(0,cn.indexOf(" "));
				lastName=cn.substring(cn.indexOf(" "));
				email=attrs.get("mail").get().toString();
			}

			results.close();
			ctx.close();
		}catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}

		edit.setId(id);
		edit.setFirstName(firstName);
		edit.setLastName(lastName);
		edit.setEmail(email);
		return true;
	}
		
	/**
	 * @return Returns the ldapHost.
	 */
	
	public String getLdapHost() {
		return ldapHost;
	}
	/**
	 * @param ldapHost The ldapHost to set.
	 */
	public void setLdapHost(String ldapHost) {
		this.ldapHost = ldapHost;
	}
	/**
	 * @return Returns the ldapPort.
	 */
	public int getLdapPort() {
		return ldapPort;
	}
	/**
	 * @param ldapPort The ldapPort to set.
	 */
	public void setLdapPort(int ldapPort) {
		this.ldapPort = ldapPort;
	}
	
	/**
	 * @return Returns the basePath.
	 */
	public String getBasePath() {
		return basePath;
	}
	
	/**
	 * @param basePath The basePath to set.
	 */
	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
	
	//helper class for storing user data in the hashtable cache
	
		
}


