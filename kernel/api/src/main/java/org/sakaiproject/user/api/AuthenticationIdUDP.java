package org.sakaiproject.user.api;

/**
 * Interface a provider should implement if the authentication ID doesn't map to the EID.
 *
 * If you are performing authentication in the provider then you may want to look at
 * {@link org.sakaiproject.user.api.AuthenticatedUserProvider}.
 */
public interface AuthenticationIdUDP {

	/**
	 * Find a user by their authentication ID.
	 * @param aid The ID used to find the user by.
	 * @param user A blank user object onto which the details of the user can be loaded.
	 * @return <code>true</code> if a valid user was found and loaded.
	 */
	public boolean getUserbyAid(String aid, UserEdit user);

}
