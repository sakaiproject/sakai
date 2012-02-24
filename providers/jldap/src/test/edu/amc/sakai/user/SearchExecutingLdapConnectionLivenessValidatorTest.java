/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

import java.text.MessageFormat;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;

public class SearchExecutingLdapConnectionLivenessValidatorTest extends MockObjectTestCase {

	private static final String UNIQUE_SEARCH_FILTER_TERM = "TESTING";
	private SearchExecutingLdapConnectionLivenessValidator validator;
	private Mock mockConn;
	private LDAPConnection conn;
	private Mock mockSearchResults;
	private LDAPSearchResults searchResults;
	private Mock mockLdapEntry;
	private LDAPEntry ldapEntry;
	
	
	protected void setUp() {
		validator = new SearchExecutingLdapConnectionLivenessValidator() {
			// we need this to be a predictable value
			protected String generateUniqueToken() {
				return UNIQUE_SEARCH_FILTER_TERM;
			}
		};
		validator.init();
		mockConn = mock(PooledLDAPConnection.class, "mockConn");
        conn = (PooledLDAPConnection)mockConn.proxy();
        mockSearchResults = mock(LDAPSearchResults.class, "mockResults");
        searchResults = (LDAPSearchResults) mockSearchResults.proxy();
        mockLdapEntry = mock(LDAPEntry.class, "mockEntry");
        ldapEntry = (LDAPEntry)mockLdapEntry.proxy();
	}
	
	public void testIssuesLivenessSearch() {
		expectStandardSearch();
		mockSearchResults.expects(once()).method("hasMore").after(mockConn, "search").
			will(returnValue(true));
		mockSearchResults.expects(once()).method("next").after("hasMore").will(returnValue(ldapEntry));
		assertTrue(validator.isConnectionAlive(conn));
	}
	
	public void testUniqueSearchFilterTermIncludesHostName() {
		final String EXPECTED_TERM = UNIQUE_SEARCH_FILTER_TERM + "-" + validator.getHostName();
		assertEquals(EXPECTED_TERM, validator.generateUniqueSearchFilterTerm());
	}
	
	/**
	 * Identical to {@link #testIssuesLivenessSearch()}, but expects
	 * search to return no results.
	 */
	public void testLivenessTestConvertsEmptySearchResultsToFalseReturnValue() {
		expectStandardSearch();
		mockSearchResults.expects(once()).method("hasMore").after(mockConn, "search").
			will(returnValue(false));
		
		assertFalse(validator.isConnectionAlive(conn));
	}

	/**
	 * Not entirely sure that this could actually happen in the wild.
	 */
	public void testLivenessTestConvertsNullLDAPEntryToFalseReturnValue() {
		expectStandardSearch();
		mockSearchResults.expects(once()).method("hasMore").after(mockConn, "search").
			will(returnValue(true));
		mockSearchResults.expects(once()).method("next").after("hasMore").will(returnValue(null));
		assertFalse(validator.isConnectionAlive(conn));
	}
	
	/**
	 * Same as {@link #testIssuesLivenessSearchConvertsEmptySearchResultsToFalseReturnValue()}, but
	 * verifies that exceptional searches are handled properly.
	 */
	public void testTransformsSearchExceptionToFalseReturnValue() {
		expectStandardSearchToThrowLDAPException();
		assertFalse(validator.isConnectionAlive(conn));
	}
	
	public void testTransformsSearchResultsIterationExceptionToFalseReturnValue() {
		expectStandardSearch();
		mockSearchResults.expects(once()).method("hasMore").after(mockConn, "search").
			will(returnValue(true));
		mockSearchResults.expects(once()).method("next").after("hasMore").will(throwException(new LDAPException()));
		assertFalse(validator.isConnectionAlive(conn));
	}
	
	public void testInterpolatesUniqueTermInFormattedSearchFilter() {
		String rawSearchFilter = validator.getSearchFilter();
		
		// relies on setUp() having overriden generateUniqueSearchFilterTerm()
		// to return a predictable value
		String expectedFormattedSearchFilter =
			MessageFormat.format(rawSearchFilter, 
					validator.generateUniqueSearchFilterTerm());
		
		assertEquals(expectedFormattedSearchFilter, validator.formatSearchFilter());
		
	}

	private void expectStandardSearch() {
		final String BASE_DN = "some-dn";
		final LDAPSearchConstraints expectedConstraints = validator.getSearchConstraints();
		String expectedFilterString = validator.formatSearchFilter();
		validator.setBaseDn(BASE_DN);
		mockConn.expects(once()).method("search").
			with(new Constraint[] { eq(BASE_DN), 
					eq(LDAPConnection.SCOPE_BASE),
					eq(expectedFilterString),
					eq(new String[] {validator.getSearchAttributeName()}),
					eq(false),
					eq(expectedConstraints)}).will(returnValue(searchResults));
	}
	
	private void expectStandardSearchToThrowLDAPException() {
		final String BASE_DN = "some-dn";
		final LDAPSearchConstraints expectedConstraints = validator.getSearchConstraints();
		String expectedFilterString = validator.formatSearchFilter();
		validator.setBaseDn(BASE_DN);
		mockConn.expects(once()).method("search").
			with(new Constraint[] { eq(BASE_DN), 
					eq(LDAPConnection.SCOPE_BASE),
					eq(expectedFilterString),
					eq(new String[] {validator.getSearchAttributeName()}),
					eq(false),
					eq(expectedConstraints)}).will(throwException(new LDAPException()));
	}

	
	
}
