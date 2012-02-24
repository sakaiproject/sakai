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

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Implements <code>User</code> EID "validation" by checking
 * for matches in a configurable blacklist, entries in which
 * are treated as regular expressions.
 * 
 * @author dmccallum
 *
 */
public class RegexpBlacklistEidValidator implements EidValidator {

	private Collection<Pattern> eidBlacklist;
	private int regexpFlags;
	
	/**
	 * Requires minimally valid and non-blacklisted EIDs. This
	 * is tested by calling {@link #isMinimallyValidEid(String)}
	 * and {@link #isBlackListedEid(String)}. Theoretically, then,
	 * a subclass could override {@link #isMinimallyValidEid(String)}
	 * to, for example, allow any non-null EID but allow any
	 * EID not described by the current blacklist.
	 */
	public boolean isSearchableEid(String eid) {
		return isMinimallyValidEid(eid) &&
		  !(isBlackListedEid(eid));
	}
	
	/**
	 * As implemented requires that the given ID be non-null
	 * and non-whitespace.
	 * 
	 * @param eid and EID to test
	 * @return <code>true<code> unless the given String is
	 *   null or entirely whitespace
	 */
	protected boolean isMinimallyValidEid(String eid) {
		return StringUtils.isNotBlank(eid);
	}

	/**
	 * Encapsulates the logic for actually checking a user EID
	 * against the configured blacklist. If no blacklist is
	 * configured, will return <code>false</code>
	 * 
	 * @return <code>true</code> if the eid matches a configured
	 *   blacklist pattern. <code>false</code> otherwise (e.g.
	 *   if no configured blacklist).
	 */
	protected boolean isBlackListedEid(String eid) {
		if ( eidBlacklist == null || eidBlacklist.isEmpty() ) {
			return false;
		}
		for ( Pattern pattern : eidBlacklist ) {
			if ( pattern.matcher(eid).matches() ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Access the String representation of blacklisted User EID
	 * regexps configured on this object. The returned collection
	 * may or may not be equivalent to the collection passed to
	 * {@link #setEidBlacklist(Collection)}
	 * 
	 * @return
	 */
	public Collection<String> getEidBlacklist() {
		return eidBlacklistAsStrings();
	}

	private Collection<String> eidBlacklistAsStrings() {
		if ( eidBlacklist == null || eidBlacklist.isEmpty() ) {
			return new HashSet<String>(0);
		}
		HashSet<String> patternStrings = new HashSet<String>(eidBlacklist.size());
		for ( Pattern pattern : eidBlacklist ) {
			patternStrings.add(pattern.pattern());
		}
		return patternStrings;
	}

	/**
	 * Converts the given collection of Strings into a collection
	 * of {@Link Pattern}s and caches the latter for evaluation
	 * by {@link #isSearchableEid(String)}. Configure {link Pattern}
	 * evaluation flags with {@link #setRegexpFlags(int)}.
	 * 
	 * @param eidBlacklist a collection of Strings to be compiled
	 *   into {@link Patterns}. May be <code>null</code>, which
	 *   will have the same semantics as an empty collection.
	 */
	public void setEidBlacklist(Collection<String> eidBlacklist) {
		this.eidBlacklist = eidBlacklistAsPatterns(eidBlacklist);
	}

	private Collection<Pattern> eidBlacklistAsPatterns(
			Collection<String> eidBlacklistStrings) {
		if ( eidBlacklistStrings == null || eidBlacklistStrings.isEmpty() ) {
			return new HashSet<Pattern>(0);
		}
		HashSet<Pattern> patterns = new HashSet<Pattern>(eidBlacklistStrings.size());
		for ( String patternString : eidBlacklistStrings ) {
			Pattern pattern = Pattern.compile(patternString, regexpFlags);
			patterns.add(pattern);
		}
		return patterns;
	}

	/**
	 * Access the configured set of {@link Pattern} matching
	 * flags. Defaults to zero.
	 * 
	 * @see Pattern#compile(String, int)
	 * @return a bitmask of {@link Pattern} matching flags
	 */
	public int getRegexpFlags() {
		return regexpFlags;
	}

	/**
	 * Assign a bitmask for {@link Pattern} matching behaviors.
	 * Be sure to set this property prior to invoking
	 * {@link #setEidBlacklist(Collection)}. The cached {@link Patterns}
	 * will <code>not</code> be recompiled as a side-effect of
	 * invoking this method.
	 * 
	 * @param regexpFlags
	 */
	public void setRegexpFlags(int regexpFlags) {
		this.regexpFlags = regexpFlags;
	}
	
}
