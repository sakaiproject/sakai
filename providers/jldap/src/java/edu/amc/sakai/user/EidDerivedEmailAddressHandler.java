package edu.amc.sakai.user;

/**
 * Mixin interface for objects capable of calculating a user EID
 * from an email address. Commonly added to {@link LdapAttributeMapper}s
 * deployed against LDAP hosts which do not actually define email attributes
 * on user entries. 
 * 
 * @see EmailAddressDerivingLdapAttributeMapper
 * @author dmccallum
 *
 */
public interface EidDerivedEmailAddressHandler {

	/**
	 * Extract a user EID from the given email address.
	 * 
	 * @param email and email address. Not necessarily guaranteed to be non-null or
	 *   even contain valid email syntax
	 * @return an EID derived from the <code>email</code> argument
	 * @throws InvalidEmailAddressException if <code>email</code> cannot be processed
	 *   for any reason. Implementation should raise this exception in any situation
	 *   where it might otherwise return <code>null</code>
	 */
	String unpackEidFromAddress(String email) throws InvalidEmailAddressException;

}
