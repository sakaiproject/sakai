/**********************************************************************************
* $URL$
* $Id$
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
 * users from an LDAP directory.
 * </p>
 * 
 * @author David Ross, Albany Medical College
 * @author Rishi Pande, Virginia Tech
 * @version $Revision$
 */

package edu.amc.sakai.user;

import java.util.Hashtable;import java.util.HashMap;
import java.util.Map;


import org.sakaiproject.service.framework.log.Logger;
import org.sakaiproject.service.legacy.user.UserDirectoryProvider;
import org.sakaiproject.service.legacy.user.UserEdit;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.LDAPSocketFactory;
import com.novell.ldap.LDAPConstraints;

// following imports are only needed if you are doing group membership -> sakai type matching (see section in getUser())
/*******
import com.novell.ldap.LDAPAttribute;

import com.novell.ldap.util.DN;

import com.novell.ldap.util.RDN;

import java.util.Vector;

import java.util.ListIterator;
*******/

public class JLDAPDirectoryProvider implements UserDirectoryProvider {
	private String ldapHost = ""; //address of ldap server
	private int ldapPort = 389; //port to connect to ldap server on
	private String keystoreLocation = ""; // keystore location (only needed for SSL connections)
	private String keystorePassword = ""; // keystore password (only needed for SSL connections)
	private String basePath = ""; //base path to start lookups on
	private boolean secureConnection = false; //whether or not we are using SSL
	private int operationTimeout = 5000; //default timeout for operations (in ms)
	/* Hashmap of attribute mappings */
	private HashMap attributeMappings = new HashMap();
	
	/* Hashtable of users that have successfully logged in...
	 * we pull their details from here instead of the directory on subsequent requests
	 * we will also expire their details after a default five minutes or so
	 */
	private Hashtable users = new Hashtable();
	
	/** The time to cache the user's data in the hashtable (in ms, defaults to 5 minutes) */
	private int m_cacheTimeMs = 5 * 60 * 1000;
	
	/* Dependency: logging service */  	
	protected Logger m_logger = null; 
    
	public JLDAPDirectoryProvider(){
		attributeMappings.put("login","cn");
		attributeMappings.put("firstName","givenName");
		attributeMappings.put("lastName","sn");
		attributeMappings.put("email","email");
		attributeMappings.put("groupMembership","groupMembership");
		attributeMappings.put("distinguishedName","dn");
	}

    public void setLogger(Logger service)  
    {           
    	m_logger = service;  
    }

    public void init()  
    {     
         try   {
           m_logger.info(this +".init()");               
           // set keystore location for SSL (if we are using it)
           if(isSecureConnection()){
           		System.setProperty("javax.net.ssl.trustStore", getKeystoreLocation());
           		System.setProperty("javax.net.ssl.trustStorePassword", getKeystorePassword());
           		LDAPSocketFactory ssf = new LDAPJSSESecureSocketFactory();
    			LDAPConnection.setSocketFactory(ssf);
           }
          }  
         catch (Throwable t) {m_logger.warn(this +".init(): ", t);}  
    }
    
    public void destroy() 
    {       
    	m_logger.info(this +".destroy()");   
    }
        
	public boolean authenticateUser(String userLogin, UserEdit edit, String password){
		m_logger.debug(this +".authenticateUser()"); 
		
		//create new ldap connection
		LDAPConnection conn = new LDAPConnection();	
		LDAPConstraints cons = new LDAPConstraints();
		
		cons.setTimeLimit(operationTimeout);
		
		conn.setConstraints(cons);


		//filter to find user
		String sFilter = (String)attributeMappings.get("login") + "=" + userLogin;
		
		//string to hold dn
		String thisDn = "";
		
		//string array of attribs to get from the directory
		String[] attrList = new String[] { (String)attributeMappings.get("distinguishedName") };
		//make sure password contains some value
		if(password.length() == 0){
			m_logger.info(this +".authenticateUser() returning false, blank password"); 
			return false;
		}
		
		try{
			//connect to ldap server
			conn.connect( ldapHost, ldapPort );
			
			//get entry from directory by email
			LDAPEntry userEntry = getEntryFromDirectory(sFilter,attrList,conn);
			thisDn = userEntry.getDN();
			 
			//attempt to bind to the directory... failure here means bad login/password
			conn.bind(LDAPConnection.LDAP_V3,
						thisDn,
						password.getBytes("UTF8"));
						/**
			 * remove any matching id's from the cache so it can be refreshed
			 * the next time that getUser() is called for this id
			 * nothing happens if it isn't there			 * 
			 */
			users.remove(userLogin);
			
			conn.disconnect();
			return true;
		}
		catch(Exception e)
		{
			 	m_logger.info(this+".authenticateUser() failed:"+e.toString());
				return false;
		}		
	}
	
	public void destroyAuthentication() {
		// not sure what to do here
	}
	
	
	public boolean findUserByEmail(UserEdit edit, String email) {
		
		//	create new ldap connection
		LDAPConnection conn = new LDAPConnection();
		LDAPConstraints cons = new LDAPConstraints();
		
		cons.setTimeLimit(operationTimeout);
		
		conn.setConstraints(cons);

		
		// filter to find users
		String sFilter = (String)attributeMappings.get("email") + "=" + email;
				
		//string array of attribs to get from directory
		String[] attrList = new String[] {	(String)attributeMappings.get("login"),											(String)attributeMappings.get("email"),
											(String)attributeMappings.get("firstName"),
											(String)attributeMappings.get("lastName") };
											

		
		try{
			conn.connect( getLdapHost(), getLdapPort() );
			
			//get entry from directory by email
			LDAPEntry userEntry = getEntryFromDirectory(sFilter,attrList,conn);
		
			conn.disconnect();
			edit.setId(userEntry.getAttribute((String)attributeMappings.get("login")).getStringValue());
     		edit.setFirstName(userEntry.getAttribute((String)attributeMappings.get("firstName")).getStringValue());
     		edit.setLastName(userEntry.getAttribute((String)attributeMappings.get("lastName")).getStringValue());
     		edit.setEmail(userEntry.getAttribute((String)attributeMappings.get("email")).getStringValue());
			return true;
		}
		catch(Exception e)
			{
				return false;
			}
	}
	
	public boolean getUser(UserEdit edit) {
		//try to get user form in-memory hashtable
		UserData existingUser = (UserData)users.get(edit.getId());
		
		
		if(existingUser == null || (System.currentTimeMillis() - existingUser.getTimeStamp()) > m_cacheTimeMs){
			//first time logging in (or user expired from cache), get details from directory
			m_logger.debug(this +".getUser() from LDAP directory:"+edit.getId());
			
			LDAPConnection conn = new LDAPConnection();					
			LDAPConstraints cons = new LDAPConstraints();
			
			cons.setTimeLimit(operationTimeout);
			
			conn.setConstraints(cons);

			String[] attrList = new String[] {	(String)attributeMappings.get("login"),												(String)attributeMappings.get("email"),
												(String)attributeMappings.get("firstName"),
												(String)attributeMappings.get("lastName"),
												(String)attributeMappings.get("groupMembership")};
			
			String sFilter = (String)attributeMappings.get("login") + "=" + edit.getId();
			
			try{
				conn.connect( ldapHost, ldapPort );
				//get entry from directory by id
				LDAPEntry userEntry = getEntryFromDirectory(sFilter,attrList,conn);
				conn.disconnect();
		
				edit.setId(userEntry.getAttribute((String)attributeMappings.get("login")).getStringValue());
	     		edit.setFirstName(userEntry.getAttribute((String)attributeMappings.get("firstName")).getStringValue());

	     		edit.setLastName(userEntry.getAttribute((String)attributeMappings.get("lastName")).getStringValue());

	     		edit.setEmail(userEntry.getAttribute((String)attributeMappings.get("email")).getStringValue());
			

	     		String userTypeName = "";
	
				/****
				 * The following section of code is an example of using group memberships to control the sakai user's "type".
				 * It is commented-out by default.
				 * We simply see if the user is a member of any groups within a specified OU, and if they are we set their "type" equal to the name of the group.
				 * So, if the user is a member of "maintain", then that is their Sakai type.
				 * Adjust the new DN("ou=Sakai,ou=...") as fit to your directory structure

					LDAPAttribute gm = userEntry.getAttribute("groupmembership");
					//try to pull a user type out of the user's group memberships
					if(gm != null){
			     		String[] memberships = gm.getStringValueArray();			
		     			DN roleContainer = new DN("ou=Sakai,ou=...");
			
						for(int i = 0; i<memberships.length; i++){	     			
			     			DN group = new DN(memberships[i]);	     			
		    	 			if(group.getParent().equals(roleContainer)){
		     					Vector sections = group.getRDNs();
		     					ListIterator iter = sections.listIterator();
		     					while (iter.hasNext()) {
		     						RDN section = (RDN)iter.next();
		     						if(section.getType().equalsIgnoreCase("cn")){
			     						userTypeName = section.getValue();
			     					}
		    	 				}	     				      				
		     				}
			     		}
					}
	
				****/	
		
	     		edit.setType(userTypeName);
	     		
	     		UserData u = new UserData();
				u.setId(edit.getId());
	     		u.setFirstName(edit.getFirstName());
	     		u.setLastName(edit.getLastName());
	     		u.setEmail(edit.getEmail());
	     		u.setType(userTypeName);	     		
	     		// set the time we got this user from the directory
	     		u.setTimeStamp(System.currentTimeMillis());
	     		//place userData into memory
	     		users.put(edit.getId(),u);
				
				return true;
			}
			catch(Exception e)
				{
					m_logger.warn(this +".getUser() from LDAP directory exception"+e.getMessage());
					return false;
				}	
		}
		//user is in memory
		else{
			m_logger.debug(this +".getUser() from memory:"+existingUser.getId()+"("+existingUser.getType()+")");
			edit.setId(existingUser.getId());
			edit.setFirstName(existingUser.getFirstName());
			edit.setLastName(existingUser.getLastName());
			edit.setEmail(existingUser.getEmail());
			edit.setType(existingUser.getType());
			return true;			
		}	
	}
	
	public boolean updateUserAfterAuthentication() {
		return false;
	}
	
	public boolean userExists(String id) {
		UserData existingUser = (UserData)users.get(id);
		
		if(existingUser != null){
			return true;
		}
		LDAPConnection conn = new LDAPConnection();
		String sFilter = (String)attributeMappings.get("login") + "=" + id;

		String thisDn = "";
		String[] attrList = new String[] { (String)attributeMappings.get("distinguishedName") };
		try{
			conn.connect( ldapHost, ldapPort );
			//this will fail if user does not exist	
			LDAPEntry userEntry = getEntryFromDirectory(sFilter,attrList,conn);			
			conn.disconnect();
		}
		catch(Exception e)
			{
			return false;	
			}		
		return true;
	}
	
	//search the directory to get an entry
	private LDAPEntry getEntryFromDirectory(String searchFilter, String[] attribs, LDAPConnection conn)
		throws LDAPException
	{
		LDAPEntry nextEntry = null;
		LDAPSearchConstraints cons = new LDAPSearchConstraints();
		cons.setDereference(LDAPSearchConstraints.DEREF_ALWAYS);		cons.setTimeLimit(operationTimeout);
		
		LDAPSearchResults searchResults =
			conn.search(getBasePath(),
					LDAPConnection.SCOPE_SUB,
					searchFilter,
					attribs,
			        false,
					cons);
		
		if(searchResults.hasMore()){
            nextEntry = searchResults.next();            
		 }
		return nextEntry;
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
	 * @return Returns the secureConnection.
	 */
	public boolean isSecureConnection() {
		return secureConnection;
	}
	/**
	 * @param secureConnection The secureConnection to set.
	 */
	public void setSecureConnection(boolean secureConnection) {
		this.secureConnection = secureConnection;
	}
	
	/**
	 * @return Returns the keystoreLocation.
	 */
	public String getKeystoreLocation() {
		return keystoreLocation;
	}
	/**
	 * @param keystoreLocation The keystoreLocation to set.
	 */
	public void setKeystoreLocation(String keystoreLocation) {
		this.keystoreLocation = keystoreLocation;
	}
	/**
	 * @return Returns the keystorePassword.
	 */
	public String getKeystorePassword() {
		return keystorePassword;
	}
	/**
	 * @param keystorePassword The keystorePassword to set.
	 */
	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
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
	class UserData{
		String id;
		String firstName;
		String lastName;
		String email;
		String type;
		long timeStamp;
		/**
		 * @return Returns the email.
		 */
		public String getEmail() {
			return email;
		}
		/**
		 * @param email The email to set.
		 */
		public void setEmail(String email) {
			this.email = email;
		}
		/**
		 * @return Returns the firstName.
		 */
		public String getFirstName() {
			return firstName;
		}
		/**
		 * @param firstName The firstName to set.
		 */
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}
		/**
		 * @return Returns the id.
		 */
		public String getId() {
			return id;
		}
		/**
		 * @param id The id to set.
		 */
		public void setId(String id) {
			this.id = id;
		}
		/**
		 * @return Returns the lastName.
		 */
		public String getLastName() {
			return lastName;
		}
		/**
		 * @param lastName The lastName to set.
		 */
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
		/**
		 * @return Returns the type.
		 */
		public String getType() {
			return type;
		}
		/**
		 * @param type The type to set.
		 */
		public void setType(String type) {
			this.type = type;
		}
		/**
		 * @return Returns the timeStamp.
		 */
		public long getTimeStamp() {
			return timeStamp;
		}
		/**
		 * @param timeStamp The timeStamp to set.
		 */
		public void setTimeStamp(long timeStamp) {
			this.timeStamp = timeStamp;
		}
	}

	/**	 * @return Returns the m_cacheTimeMs.
	 */
	public int getCacheTTL() {
		return m_cacheTimeMs;
	}
	/**
	 * @param timeMs The m_cacheTimeMs to set.
	 */
	public void setCacheTTL(int timeMs) {
		m_cacheTimeMs = timeMs;
	}
	/**
	 * @return Returns the attributeMappings.
	 */
	public Map getAttributeMappings() {
		return attributeMappings;
	}
	/**
	 * @param attributeMappings The attributeMappings to set.
	 */
	public void setAttributeMappings(Map attributeMappings) {
		this.attributeMappings = (HashMap)attributeMappings;
	}
	/**
	 * @return Returns the operationTimeout.
	 */
	public int getOperationTimeout() {
		return operationTimeout;
	}
	/**
	 * @param operationTimeout The operationTimeout to set.
	 */
	public void setOperationTimeout(int operationTimeout) {
		this.operationTimeout = operationTimeout;
	}
}

