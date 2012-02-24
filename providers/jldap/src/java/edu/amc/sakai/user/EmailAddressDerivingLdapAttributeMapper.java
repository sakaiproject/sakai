/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package edu.amc.sakai.user;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.novell.ldap.LDAPEntry;

public class EmailAddressDerivingLdapAttributeMapper 
extends SimpleLdapAttributeMapper implements EidDerivedEmailAddressHandler {

	private Pattern addressPattern;
	private String defaultAddressDomain;
	
	/**
	 * Returns {@link #doUnpackEidFromAddress(String)} if the given 
	 * <code>email/code> string validates against 
	 * {@link #validateAddress(String)}. Otherwise throws a
	 * {@Link InvalidEmailAddressException}. Throws the same
	 * exception type if {@link #doUnpackEidFromAddress(String)} returns
	 * <code>null</code> or a whitespace string.
	 */
	public String unpackEidFromAddress(String address)
			throws InvalidEmailAddressException {
		boolean validated = validateAddress(address);
		if (!(validated)) {
			throw new InvalidEmailAddressException("Unable to unpack EID from email address [" + 
					address + "]. Expected pattern = [" + getAddressPattern() + "]");
		}
		String eid = StringUtils.trimToNull(doUnpackEidFromAddress(address));
		if ( eid == null ) {
			throw new InvalidEmailAddressException("Unpacked an empty EID from email address [" + 
					address + "].");
		}
		return eid;
	}
	
	/**
	 * Validate the given string against the {@link Pattern} configured
	 * by {@link #setAddressPattern(String)}. A null pattern has
	 * wildcard semantics.
	 * 
	 * @param address an email address to be validated against some pattern,
	 *   typically to verify that it belongs to an institutional domain
	 * @return
	 */
	protected boolean validateAddress(String address) {
		if ( addressPattern == null ) {
			return true;
		}
		return addressPattern.matcher(address).matches();
	}
	
	/**
	 * Calculates a user EID from an email address, assuming the address
	 * has already passed muster with {@link #validateAddress(String)}.
	 * This implementation strips all characters following the first
	 * occurance of '@' and returns the remaining characters.
	 *
	 * @param address a validated email address
	 * @return A user EID calculated from the given email address. This EID
	 *   is not itself validated in any way. May return <code>null</null> or
	 *   whitespace strings.
	 */
	protected String doUnpackEidFromAddress(String address) {
		int delimPos = address.indexOf("@");
		if ( delimPos < 0 ) {
			return address;
		}
		return address.substring(0, delimPos);
	}
	
	@Override
	public void mapLdapEntryOntoUserData(LDAPEntry ldapEntry, LdapUserData userData) {
		super.mapLdapEntryOntoUserData(ldapEntry, userData);
		if ( userData.getEmail() != null ) {
			return;
		}
		String derivedAddress = deriveAddressFromEid(userData.getEid());
		userData.setEmail(derivedAddress);
	}
	
	/**
	 * Calculate an email address from the given user EID. This implementation
	 * simply appends the "@" delim and the domain configured by
	 * {@link #setDefaultAddressDomain(String)}. If that value is <code>null</code>, 
	 * or the inbound eid is <code>null</code> or whitespace, simply returns 
	 * <code>null</code> 
	 *  
	 * @param string
	 * @return
	 */
	public String deriveAddressFromEid(String eid) {
		if ( defaultAddressDomain == null ) {
			return null;
		}
		eid = StringUtils.trimToNull(eid);
		if ( eid == null ) {
			return null;
		}
		return eid + "@" + defaultAddressDomain;
	}

	/**
	 * Access the current email address validation pattern, as
	 * assigned by {@link #setAddressPattern(String)}.
	 * 
	 * @return a string representation of the currently cached
	 *   {@link Pattern}. May return <code>null</code>
	 */
	public String getAddressPattern() {
		return addressPattern == null ? null : addressPattern.toString();
	}

	/**
	 * Assign the {@link Pattern} against which strings passed to
	 * {@link #unpackEidFromAddress(String)} must validate in order to 
	 * be processed by {@link #doUnpackEidFromAddress(String)}
	 * 
	 * @param addressPattern a string to compile into a {@link Pattern}.
	 *   Compilation errors are raised immediately without modifications
	 *   to any already-assigned pattern. <code>nulls</code> accepted and
	 *   have wildcard semantics.
	 */
	public void setAddressPattern(String addressPattern) {
		if ( addressPattern == null ) {
			this.addressPattern = null;
			return;
		}
		this.addressPattern = Pattern.compile(addressPattern);
	}
	
	/**
	 * See {@link #setDefaultAddressDomain(String)}
	 */
	public String getDefaultAddressDomain() {
		return defaultAddressDomain;
	}

	/**
	 * Assign the domain to be used when deriving email addresses from
	 * user EIDs. This is distinct from the pattern configured by 
	 * {@link #setAddressPattern(String)}, so there could in fact be
	 * many domains from which an EID could be unpacked, but only one
	 * domain in which EID-derived email addresses will be created.
	 * This behavior can technically be overridden in 
	 * {@link deriveEmailFromEid(String)}.
	 * 
	 * @param object some email domain. Not validated. May be <code>null</code>.	 
	 */
	public void setDefaultAddressDomain(String domain) {
		this.defaultAddressDomain = domain;
	}

	

}
