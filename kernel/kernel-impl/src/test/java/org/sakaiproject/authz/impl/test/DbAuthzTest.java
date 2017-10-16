/**
 * Copyright (c) 2003-2015 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.authz.impl.test;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.authz.impl.DbAuthzGroupSqlDefault;
import org.sakaiproject.authz.impl.DbAuthzGroupSqlOracle;
import org.sakaiproject.test.SakaiKernelTestBase;


/**
 * This just checks that the split method for oracle in clauses works.
 * @author Juanjo Merono
 *
 */
public class DbAuthzTest extends SakaiKernelTestBase {
	
	private DbAuthzGroupSqlOracle sqlOracle;
	private String defaultQuery;
	private String inClauseRegExp;
	private int [] tests = new int[]{1,3,15,111,998,999,1000,1001,1997,1998,1999,2000,2001,2011,2997,2998,2999,3000,3001};

	@Before
	public void setUp() {
		sqlOracle = new DbAuthzGroupSqlOracle();
		defaultQuery = (new DbAuthzGroupSqlDefault()).getSelectRealmIdSql(null);
		inClauseRegExp = "and \\(SR.REALM_ID in \\((\\?\\,)*\\?\\)( or SR.REALM_ID in \\((\\?\\,)*\\?\\))*\\)";
	}

	@Test
	public void testNull() {
		Assert.assertTrue("Testing oracle in split with null collection.",defaultQuery.equals(sqlOracle.getSelectRealmIdSql(null)));
	}

	@Test
	public void testEmpty() {
		Assert.assertTrue("Testing oracle in split with empty collection.",defaultQuery.equals(sqlOracle.getSelectRealmIdSql(new HashSet<String>())));
	}

	private Set<String> getSet(int size) {
		HashSet<String> s = new HashSet<String>();
		for (int i=0; i<size; i++) {
			s.add(""+i);
		}
		return s;
	}
	
	private boolean testElement(int size) {
		Set<String> set = getSet(1);
		String finalQuery = sqlOracle.getSelectRealmIdSql(set);
		finalQuery = finalQuery.substring(defaultQuery.length());
		// The where clause is correct and has collection size ? on it.
		return finalQuery.matches(inClauseRegExp) && (finalQuery.length()-finalQuery.replaceAll("\\?", "").length())==set.size();
	}
	
	@Test
	public void testElements() {
		for (int k=0; k<tests.length; k++) {
			Assert.assertTrue("Testing oracle in split with "+tests[k]+" elements.",testElement(tests[k]));
		}
	}

}
