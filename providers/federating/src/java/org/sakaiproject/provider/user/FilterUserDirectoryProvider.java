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


// package
package org.sakaiproject.provider.user;


// imports
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.user.api.ExternalUserSearchUDP;
import org.sakaiproject.user.api.AuthenticationIdUDP;
import org.sakaiproject.user.api.DisplayAdvisorUDP;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserFactory;
import org.sakaiproject.user.api.UsersShareEmailUDP;

/**
 * <p>
 * Filter User Directory Provider, calls a configure provider, and if that fails
 * calls the next provider in the chain. It does this for all methods. If the
 * response is a boolean, then true stop processing, false continues processon.
 * It is the reponsibility of the injected user directory provider to ignore
 * those calls that have nothing to do with it, either by reference to the
 * session, or by refernce to something in the environment or request. To Use,
 * add one or more of these beans to Spring, in a chain, marking the first one
 * in the chain as the 'official' userdirectory provider used by Sakai.
 * Construct the chain by setting the next FilterUserDirectorProvider to the
 * nextProvider and the real User Directory Provider to myProvider eg
 * 
 * <pre>
 *   
 *   &lt;bean
 *  		id=&quot;org.sakaiproject.user.api.UserDirectoryProvider&quot;
 *  		class=&quot;org.sakaiproject.provider.user.FilterUserDirectoryProvider&quot;
 *  		init-method=&quot;init&quot; destroy-method=&quot;destroy&quot; singleton=&quot;true&quot;&gt;
 *  		&lt;property name=&quot;myProvider&quot;&gt;
 *  			&lt;ref bean=&quot;org.sakaiproject.user.api.UserDirectoryProvider.provider1&quot; /&gt;
 *  		&lt;/property&gt;
 *  		&lt;property name=&quot;nextProvider&quot;&gt;
 *  			&lt;ref bean=&quot;org.sakaiproject.user.api.UserDirectoryProvider.chain1&quot; /&gt;
 *  		&lt;/property&gt;
 *  	&lt;/bean&gt;
 *  	&lt;bean
 *  		id=&quot;org.sakaiproject.user.api.UserDirectoryProvider.chain1&quot;
 *  		class=&quot;org.sakaiproject.provider.user.FilterUserDirectoryProvider&quot;
 *  		init-method=&quot;init&quot; destroy-method=&quot;destroy&quot; singleton=&quot;true&quot;&gt;
 *  		&lt;property name=&quot;myProvider&quot;&gt;
 *  			&lt;ref bean=&quot;org.sakaiproject.user.api.UserDirectoryProvider.provider2&quot; /&gt;
 *  		&lt;/property&gt;
 *  	&lt;/bean&gt;
 * </pre>
 * 
 * @author Ian Boston, Andrew Thornton, Daniel Parry, Raad
 * @version $Revision$
 */
@Slf4j
public class FilterUserDirectoryProvider implements UserDirectoryProvider, ExternalUserSearchUDP, UsersShareEmailUDP, AuthenticationIdUDP, DisplayAdvisorUDP
{

	private static ThreadLocal authenticatedProvider = new ThreadLocal();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/
	// The spring injected user provider we call
	/**
	 * The underlying UserDirectoryProvider
	 */
	private UserDirectoryProvider myProvider;
	/**
	 * The Next directory provider in the chain
	 */
	private UserDirectoryProvider nextProvider;
	
	private Long providerID = null;
	
	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		// Initialize the providerID as a random Long. SecureRandom is guaranteed to 
		// to give a separate id, however its not entirely thread safe, so I've reused
		// the thread local. It gets thrown away on the first auth attempt, so the
		// secure random wont hand around in production.
		
		SecureRandom sr = (SecureRandom)authenticatedProvider.get();
		if ( sr == null ) {
			sr = new SecureRandom();
			authenticatedProvider.set(sr);
		}
		providerID = new Long(sr.nextLong()); 
		try
		{
			log.info("init() FILTER as "+providerID);
		}
		catch (Throwable t)
		{
			log.info(".init(): ", t);
		}

	} // init

	/**
	 * Returns to uninitialized state. You can use this method to release resources thet your Service allocated when Turbine shuts down.
	 */
	public void destroy()
	{

		log.info("destroy()");

	} // destroy

	/**********************************************************************************************************************************************************************************************************************************************************
	 * UserDirectoryProvider implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct.
	 */
	public FilterUserDirectoryProvider()
	{
	} // Switch

	/**
	 * Access a user object. Update the object with the information found.
	 * 
	 * @param edit
	 *        The user object (id is set) to fill in.
	 * @return true if the user object was found and information updated, false if not.
	 */
	public boolean getUser(UserEdit edit)
	{
		if (log.isDebugEnabled()) {
			log.debug("FUDP: getUser(" + edit.getId() + " eid:" + edit.getEid()  + ") as "+providerID);
			log.debug("FUDP: doing myProvider.getUser() as " + providerID);
		}
		if (  myProvider.getUser(edit) ) {
			return true;
		} else if ( nextProvider != null ) {
			if (log.isDebugEnabled()) {
				log.debug("FUDP: doing nextProvider.getUser() as " + providerID);
			}
			return nextProvider.getUser(edit);
		}
		return false;

	} // getUser

	/**
	 * Access a collection of UserEdit objects; if the user is found, update the information, otherwise remove the UserEdit object from the collection.
	 * @param users The UserEdit objects (with id set) to fill in or remove.
	 */
	public void getUsers(Collection users)
	{
		if (log.isDebugEnabled()) {
			log.debug("getUsers() size()=" + users.size() + " as "+providerID);
		}
		if (nextProvider != null) {
			// We need to be clever and wrap the collection
			GetUsersCollectionWrapper wrapper = new GetUsersCollectionWrapper(users);
			if (log.isDebugEnabled()) {
				log.debug("using wrapper on " + myProvider + " as " + providerID);
			}
			myProvider.getUsers(wrapper.firstPassCollection());
			if (log.isDebugEnabled()) {
				log.debug("Passing myProvider removals collection to nextProvider size()=" + wrapper.secondPassCollection.size() + " as " + providerID);
				log.debug("using second wrapper on " + nextProvider);
			}
			
			nextProvider.getUsers(wrapper.secondPassCollection());
			if (log.isDebugEnabled()) {
				log.debug("Total number of removals from users collection=" + wrapper.realRemovals.size() + " as " + providerID);
				log.debug("Applying Changes");
			}
			wrapper.apply();			
			
		} else {
			myProvider.getUsers(users);
		}
	}

	private class GetUsersCollectionWrapper {
	  Collection inner;
	  
	  public GetUsersCollectionWrapper(Collection c) {
	    this.inner = c;
	  }
	  
	  ArrayList secondPassCollection = new ArrayList();
	  ArrayList realRemovals = new ArrayList();
	  
	  public Collection firstPassCollection() {
		  return createStoreRemovalsCollection(inner, secondPassCollection);
	  }
	  
	  public Collection secondPassCollection() {
		  return createStoreRemovalsCollection(secondPassCollection, realRemovals);
	  }
	  
	  
	  public Collection createStoreRemovalsCollection(final Collection originals, final Collection removals) {
		  return new Collection() {

			public boolean add(Object o)
			{
				throw new UnsupportedOperationException("UDP should not add to the collection passed into getUsers()");
			}

			public boolean addAll(Collection c)
			{
				throw new UnsupportedOperationException("UDP should not add to the collection passed into getUsers()");
			}

			public void clear()
			{
				for (Iterator it = originals.iterator(); it.hasNext();) {
					removals.add(it.next());
				}
			}

			public boolean contains(Object o)
			{
				return originals.contains(o) && !removals.contains(o);
			}

			public boolean containsAll(Collection col)
			{
				for (Iterator it = removals.iterator(); it.hasNext();) {
					if (col.contains(it.next())) return false;
				}
				
				return originals.containsAll(col);
			}

			public boolean isEmpty()
			{
				return originals.isEmpty() || removals.containsAll(originals);
			}

			public Iterator iterator()
			{
				return new Iterator() {

					Iterator internal = originals.iterator();
					Object next = internal.hasNext() ? internal.next() : null;
					Object current;
					
					public boolean hasNext()
					{
						return (next != null);
					}

					public Object next()
					{
						current = next;
						next = null;
						while (next == null && internal.hasNext()) { 
							next = internal.next();
							if (removals.contains(next)) {
								next = null;
							}
						}
						return current;
					}

					public void remove()
					{
						if (log.isDebugEnabled()) {
							User u = (User) current;
							log.debug("Removing object from internal collection :" + u.getEid());
						}
						if (originals.contains(current) && !removals.contains(current)) {
							removals.add(current);
						}
					}
					
				};
			}

			/*
			 * If remove is called for object o
			 */
			public boolean remove(Object o)
			{
				if (log.isDebugEnabled()) {
					User u = (User) o;
					log.debug("Removing object from internal collection :" + u.getEid());
				}

				if (originals.contains(o) && !removals.contains(o)) {
					removals.add(o);
					return true;
				}
				return false;
			}

			public boolean removeAll(Collection c)
			{
				boolean doneSomething = false;
				for (Iterator it = c.iterator(); it.hasNext() ;) {
					Object o = it.next();
					if (log.isDebugEnabled()) {
						User u = (User) o;
						log.debug("Removing object from internal collection :" + u.getEid());
					}

					if (originals.contains(o) && !removals.contains(o)) {
						removals.add(o);
						doneSomething = true;
					}					
				}
				return doneSomething;
			}

			public boolean retainAll(Collection c)
			{
				boolean doneSomething = false;
				for (Iterator it = originals.iterator(); it.hasNext();) {
					Object  o = it.next();
					
					if (log.isDebugEnabled()) {
						User u = (User) o;
						log.debug("Removing object from internal collection :" + u.getEid());
					}

					if (!c.contains(o) && !removals.contains(o)) {
						removals.add(o);
						doneSomething = true;
					}
				}
				return doneSomething;
			}

			public int size()
			{
				int i = 0;
				for (Iterator it = originals.iterator(); it.hasNext(); ) {
					if (!removals.contains(it.next())) {
						i++;
					}
				}
				return i;
			}

			public Object[] toArray()
			{
				return toList().toArray();
			}

			private ArrayList toList()
			{
				ArrayList l = new ArrayList();
				for (Iterator it = originals.iterator(); it.hasNext(); ) {
					Object o = it.next();
					if (!removals.contains(o)) {
						l.add(o);
					}
				}
				return l;
			}
			
			public Object[] toArray(Object[] a)
			{
				return toList().toArray(a);
			}
			
		  };
	  }
	  
	  public void apply() {
		  for (Iterator it = inner.iterator(); it.hasNext();) {
			  Object o = it.next();
				if (log.isDebugEnabled()) {
					User u = (User) o;
					log.debug("Actually removing object from collection :" + u.getEid());
				}

			  if (realRemovals.contains(o)) {
				  it.remove();
			  }
		  }
		  
	  }
	  
	}

	
	

	/**
	 * Find a user object who has this email address. Update the object with the information found.
	 * 
	 * @param email
	 *        The email address string.
	 * @return true if the user object was found and information updated, false if not.
	 */
	public boolean findUserByEmail(UserEdit edit, String email)
	{
		if (log.isDebugEnabled()) {
			log.debug("findUserByEmail() edit.getId()=" +edit.getId() +"  email=" + email + " as "+providerID);
		}

        if (  myProvider.findUserByEmail(edit,email) ) {
        		return true;
		} else if ( nextProvider != null ) {
			return nextProvider.findUserByEmail(edit,email);
		}
		return false;

	} // findUserByEmail

	/**
	 * Find all user objects which have this email address.
	 * 
	 * @param email
	 *        The email address string.
	 * @param factory
	 *        Use this factory's newUser() method to create all the UserEdit objects you populate and return in the return collection.
	 * @return Collection (UserEdit) of user objects that have this email address, or an empty Collection if there are none.
	 */
	public Collection findUsersByEmail(String email, UserFactory factory)
	{
		Collection rv = new Vector();
		if ( myProvider instanceof UsersShareEmailUDP ) {
			UsersShareEmailUDP ushare = (UsersShareEmailUDP) myProvider;
			if (log.isDebugEnabled()) {
				log.debug("myProvider Multiple lookup on "+email+" for "+ushare + " as " + providerID);
			}
			rv.addAll(ushare.findUsersByEmail(email,factory));
			if (log.isDebugEnabled()) {
				log.debug("myProvider - got "+rv.size()+" matches" + " as " + providerID);
			}
		} else {
			UserEdit edit = factory.newUser();
			if ( myProvider.findUserByEmail(edit,email) ) {
				if (log.isDebugEnabled()) {
					log.debug("myProvider - found user "+edit.getId()+" for "+email + " as " + providerID);
				}
				rv.add(edit);
			}
		}

		if ( nextProvider instanceof UsersShareEmailUDP) {
			UsersShareEmailUDP ushare = (UsersShareEmailUDP) nextProvider;
			if (log.isDebugEnabled()) {
				log.debug("nextProvider Multiple lookup on "+email+" for "+ushare + " as " + providerID);
			}
			rv.addAll(ushare.findUsersByEmail(email,factory));
			if (log.isDebugEnabled()) {
				log.debug("nextProvider - got "+rv.size()+" matches" + " as " + providerID);
			}
		} else if ( nextProvider != null ) {
			UserEdit edit = factory.newUser();
			if ( nextProvider.findUserByEmail(edit,email) ) {
				if (log.isDebugEnabled()) {
					log.debug("nextProvider - found user "+edit.getId()+" for "+email + " as " + providerID);
				}
				rv.add(edit);
			}
		}

		return rv;
	}

	/**
	 * Authenticate a user / password. If the user edit exists it may be modified, and will be stored if...
	 * 
	 * @param id
	 *        The user id.
	 * @param edit
	 *        The UserEdit matching the id to be authenticated (and updated) if we have one.
	 * @param password
	 *        The password.
	 * @return true if authenticated, false if not.
	 */
	public boolean authenticateUser(String userId, UserEdit edit, String password)
	{
		if (log.isDebugEnabled()) {
			log.debug("authenticateUser() userId=" + userId + " as "+providerID);
		}

		authenticatedProvider.set(null);
		if (  myProvider.authenticateUser(userId,edit,password) ) {
			authenticatedProvider.set(providerID);
			return true;
		} else if ( nextProvider != null ) {
			return nextProvider.authenticateUser(userId,edit,password);
		}
		return false;

	} // authenticateUser

	/**
	 * The UserDirectoryProvider used by this filter
	 * @return
	 */
	public UserDirectoryProvider getMyProvider() {
		return myProvider;
	}

	/**
	 * The UserDirectoryProvider used by this filter
	 * @param myProvider
	 */
	public void setMyProvider(UserDirectoryProvider myProvider) {
		this.myProvider = myProvider;
	}
	/**
	 * The Next Directory Provider in the chain
	 * @return
	 */
	public UserDirectoryProvider getNextProvider() {
		return nextProvider;
	}

	/**
	 * The Next Directory Provider in the chain
	 * @param nextDirectoryProvider
	 */
	public void setNextProvider(UserDirectoryProvider nextDirectoryProvider) {
		this.nextProvider = nextDirectoryProvider;
	}

	 /**
	  * {@inheritDoc}
	  */
	public boolean authenticateWithProviderFirst(String id)
	{
	   return false;
	}
	
	/**
	  * {@inheritDoc}
	 */
	public List<UserEdit> searchExternalUsers(String criteria, int first, int last, UserFactory factory) {
		
		List<UserEdit> users = new ArrayList<UserEdit>();
		
		if ( myProvider instanceof ExternalUserSearchUDP ) {
			ExternalUserSearchUDP extSearchUDP = (ExternalUserSearchUDP) myProvider;
			
			if (log.isDebugEnabled()) {
				log.debug("searchExternalUsers() criteria=" + criteria);
			}
			
			List<UserEdit> searchExternalUsers = extSearchUDP.searchExternalUsers(criteria, first, last, factory);
			if (searchExternalUsers != null) {
				users.addAll(searchExternalUsers);
			} else {
				// When something goes wrong with the search we want to pass this back up the stack.
				return null;
			}
		}
		
		if ( nextProvider instanceof ExternalUserSearchUDP) {
			ExternalUserSearchUDP extSearchUDP = (ExternalUserSearchUDP) nextProvider;
			
			if (log.isDebugEnabled()) {
				log.debug("nextProvider searchExternalUsers() criteria=" + criteria);
			}
			
			List<UserEdit> searchExternalUsers = extSearchUDP.searchExternalUsers(criteria, first, last, factory);
			if (searchExternalUsers != null) {
				users.addAll(searchExternalUsers);
			} else {
				// When something goes wrong with the search we want to pass this back up the stack.
				return null;
			}
		}

		return users;
	}

	public boolean getUserbyAid(String aid, UserEdit user) {
		if (myProvider instanceof AuthenticationIdUDP) {
			if (((AuthenticationIdUDP)myProvider).getUserbyAid(aid, user)) {
				return true;
			}
		}
		if (nextProvider instanceof AuthenticationIdUDP) {
			if (((AuthenticationIdUDP)nextProvider).getUserbyAid(aid, user)) {
				return true;
			}
		}
		return false;
	}

	public String getDisplayId(User user) {
		String displayId = null;
		if (myProvider instanceof DisplayAdvisorUDP) {
			displayId = ((DisplayAdvisorUDP)myProvider).getDisplayId(user); 
			if (displayId != null) {
				return displayId;
			}
		}
		if (nextProvider instanceof DisplayAdvisorUDP) {
			displayId = ((DisplayAdvisorUDP)nextProvider).getDisplayId(user); 
			if (displayId != null) {
				return displayId;
			}
		}
		return null;
	}

	public String getDisplayName(User user) {
		String displayName = null;
		if (myProvider instanceof DisplayAdvisorUDP) {
			displayName = ((DisplayAdvisorUDP)myProvider).getDisplayName(user);
			if (displayName != null) {
				return displayName;
			}
		}
		if (nextProvider instanceof DisplayAdvisorUDP) {
			displayName = ((DisplayAdvisorUDP)nextProvider).getDisplayName(user);
			if (displayName != null) {
				return displayName;
			}
		}
		return null;
	}


} // FilterUserDirectoryProvider




