/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package uk.ac.cam.caret.sakai.rwiki.component.service.impl.test;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasEdit;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;

import uk.ac.cam.caret.sakai.rwiki.component.service.impl.RWikiObjectServiceImpl;
import uk.ac.cam.caret.sakai.rwiki.model.RWikiCurrentObjectImpl;
import uk.ac.cam.caret.sakai.rwiki.model.RWikiHistoryObjectImpl;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiCurrentObjectDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiHistoryObjectDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiCurrentObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiHistoryObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import junit.framework.TestCase;

public class RWikiObjectServiceImplTest extends TestCase {

	RWikiObjectServiceImpl rwosi = null;
	RWikiCurrentObjectImpl rwco = new RWikiCurrentObjectImpl();
	
	public RWikiObjectServiceImplTest(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();
		rwosi = new RWikiObjectServiceImpl();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testExtractReferencesEmptyConstructor() {
		assertNotNull(rwosi);
	}
	
	// can deal with page with no references
	public void testExtractReferencesExtractReferencesEmpty() {
		rwco = new RWikiCurrentObjectImpl();
		HashSet hs = new HashSet();
		StringBuffer sb = rwosi.extractReferences(rwco, hs);
		assert(sb.length() == 2);
	}

	// can deal with page with one references
	public void testExtractReferencesExtractReferencesOneReference() {
		rwco = new RWikiCurrentObjectImpl();
		HashSet<String> hs = new HashSet<String>();
		hs.add("oneReference");
		StringBuffer sb = rwosi.extractReferences(rwco, hs);
		assertEquals("one reference string",16,sb.length());
	}
	
	// can deal with page with multiple references
	public void testExtractReferencesExtractReferencesTwoReference() {
		rwco = new RWikiCurrentObjectImpl();
		HashSet<String> hs = new HashSet<String>();
		hs.add("oneReference");
		hs.add("twoReference");
		StringBuffer sb = rwosi.extractReferences(rwco, hs);
		assertEquals("two reference strings",30,sb.length());
		
	}
	
	// can deal with page with too many references
        public void testExtractReferencesExtractReferencesTooLong() {
                StringBuffer sb;
                rwco = new RWikiCurrentObjectImpl();
                HashSet<String> hs = new HashSet<String>();

                hs.add(longString(3990));
                sb = rwosi.extractReferences(rwco, hs);
                assertEquals("two reference strings pass",3994,sb.length());

                hs.add("A");
                sb = rwosi.extractReferences(rwco, hs);
                assertEquals("two reference strings pass",3997,sb.length());

                hs.add("twoReference");
                sb = rwosi.extractReferences(rwco, hs);
                assertEquals("three reference strings last one too long",3997,sb.length());


        }

        public void testHardDeleteRemovesPagesAndHistory()
        {
                RWikiCurrentObjectImpl page = new RWikiCurrentObjectImpl();
                page.setId("page-id");
                page.setName("/site/siteId/page");
                page.setRealm("/site/siteId");

                RWikiHistoryObjectImpl historyOne = new RWikiHistoryObjectImpl();
                historyOne.setId("hist-1");
                historyOne.setRwikiobjectid(page.getId());

                RWikiHistoryObjectImpl historyTwo = new RWikiHistoryObjectImpl();
                historyTwo.setId("hist-2");
                historyTwo.setRwikiobjectid(page.getId());

                List<RWikiObject> pages = new ArrayList<RWikiObject>();
                pages.add(page);

                Map<String, List<RWikiHistoryObject>> historyMap = new HashMap<String, List<RWikiHistoryObject>>();
                historyMap.put(page.getId(), Arrays.<RWikiHistoryObject>asList(historyOne, historyTwo));

                RecordingSqlService sqlService = new RecordingSqlService();
                RecordingAliasService aliasService = new RecordingAliasService();
                StubCurrentObjectDao currentDao = new StubCurrentObjectDao(pages);
                StubHistoryObjectDao historyDao = new StubHistoryObjectDao(historyMap);

                rwosi.setRWikiCurrentObjectDao(currentDao);
                rwosi.setRWikiHistoryObjectDao(historyDao);
                rwosi.setAliasService(aliasService);
                rwosi.setSqlService(sqlService);

                rwosi.hardDelete("siteId");

                assertEquals(1, aliasService.removedTargets.size());
                assertEquals(rwosi.createReference(page.getName()), aliasService.removedTargets.get(0));

                assertTrue(sqlService.executedSql.contains("delete from rwikicurrentcontent where rwikiid = ?"));
                assertTrue(sqlService.executedSql.contains("delete from rwikipagegroups where rwikiobjectid = ?"));
                assertTrue(sqlService.executedSql.contains("delete from rwikiobject where id = ?"));
                assertTrue(sqlService.executedSql.contains("delete from rwikihistory where id = ?"));
                assertTrue(sqlService.executedSql.contains("delete from rwikihistory where rwikiobjectid = ?"));

                List<List<Object>> currentContentParams = sqlService.parametersForSql("delete from rwikicurrentcontent where rwikiid = ?");
                assertEquals(1, currentContentParams.size());
                assertTrue(currentContentParams.get(0).contains(page.getId()));

                List<List<Object>> historyDeletes = sqlService.parametersForSql("delete from rwikihistory where id = ?");
                assertEquals(2, historyDeletes.size());
                List<Object> flattened = new ArrayList<Object>();
                for (List<Object> values : historyDeletes)
                {
                        flattened.addAll(values);
                }
                assertTrue(flattened.contains("hist-1"));
                assertTrue(flattened.contains("hist-2"));

                assertEquals(Collections.singletonList("rwiki hard delete siteId"), sqlService.transactionTags);
        }

        private static class RecordingSqlService extends SqlServiceAdapter
        {
                final List<String> executedSql = new ArrayList<String>();

                final List<List<Object>> recordedParameters = new ArrayList<List<Object>>();

                final List<String> transactionTags = new ArrayList<String>();

                @Override
                public boolean transact(Runnable callback, String tag)
                {
                        transactionTags.add(tag);
                        callback.run();
                        return true;
                }

                @Override
                public boolean dbWrite(String sql)
                {
                        executedSql.add(sql);
                        recordedParameters.add(Collections.<Object>emptyList());
                        return true;
                }

                @Override
                public boolean dbWrite(String sql, Object[] fields)
                {
                        executedSql.add(sql);
                        List<Object> values = fields == null ? Collections.<Object>emptyList() : Arrays.asList(fields);
                        recordedParameters.add(values);
                        return true;
                }

                List<List<Object>> parametersForSql(String sql)
                {
                        List<List<Object>> matches = new ArrayList<List<Object>>();
                        for (int i = 0; i < executedSql.size(); i++)
                        {
                                if (executedSql.get(i).equals(sql))
                                {
                                        matches.add(recordedParameters.get(i));
                                }
                        }
                        return matches;
                }
        }

        private static class RecordingAliasService implements AliasService
        {
                final List<String> removedTargets = new ArrayList<String>();

                public boolean allowSetAlias(String alias, String target)
                {
                        return true;
                }

                public void setAlias(String alias, String target) throws IdUsedException, IdInvalidException, PermissionException
                {
                }

                public boolean allowRemoveAlias(String alias)
                {
                        return true;
                }

                public void removeAlias(String alias) throws IdUnusedException, PermissionException, InUseException
                {
                }

                public boolean allowRemoveTargetAliases(String target)
                {
                        return true;
                }

                public void removeTargetAliases(String target) throws PermissionException
                {
                        removedTargets.add(target);
                }

                public String getTarget(String alias) throws IdUnusedException
                {
                        return null;
                }

                public List<Alias> getAliases(String target)
                {
                        return Collections.<Alias>emptyList();
                }

                public List<Alias> getAliases(String target, int first, int last)
                {
                        return Collections.<Alias>emptyList();
                }

                public List<Alias> getAliases(int first, int last)
                {
                        return Collections.<Alias>emptyList();
                }

                public int countAliases()
                {
                        return 0;
                }

                public List<Alias> searchAliases(String criteria, int first, int last)
                {
                        return Collections.<Alias>emptyList();
                }

                public int countSearchAliases(String criteria)
                {
                        return 0;
                }

                public String aliasReference(String id)
                {
                        return null;
                }

                public boolean allowAdd()
                {
                        return false;
                }

                public AliasEdit add(String id) throws IdInvalidException, IdUsedException, PermissionException
                {
                        throw new UnsupportedOperationException();
                }

                public boolean allowEdit(String id)
                {
                        return false;
                }

                public AliasEdit edit(String id) throws IdUnusedException, PermissionException, InUseException
                {
                        throw new UnsupportedOperationException();
                }

                public void commit(AliasEdit edit)
                {
                }

                public void cancel(AliasEdit edit)
                {
                }

                public void remove(AliasEdit edit) throws PermissionException
                {
                }
        }

        private abstract static class SqlServiceAdapter implements SqlService
        {
                public Connection borrowConnection() throws SQLException
                {
                        throw new UnsupportedOperationException();
                }

                public void returnConnection(Connection conn)
                {
                }

                public List<String> dbRead(String sql)
                {
                        return Collections.emptyList();
                }

                public <T> List<T> dbRead(String sql, Object[] fields, SqlReader<T> reader)
                {
                        return Collections.emptyList();
                }

                public <T> List<T> dbRead(Connection conn, String sql, Object[] fields, SqlReader<T> reader)
                {
                        return Collections.emptyList();
                }

                public void dbReadBinary(String sql, Object[] fields, byte[] value)
                {
                }

                public void dbReadBinary(Connection conn, String sql, Object[] fields, byte[] value)
                {
                }

                public InputStream dbReadBinary(String sql, Object[] fields, boolean big) throws ServerOverloadException
                {
                        throw new UnsupportedOperationException();
                }

                public Long dbInsert(Connection callerConnection, String sql, Object[] fields, String autoColumn)
                {
                        throw new UnsupportedOperationException();
                }

                public Long dbInsert(Connection callerConnection, String sql, Object[] fields, String autoColumn, InputStream last, int lastLength)
                {
                        throw new UnsupportedOperationException();
                }

                public boolean dbWrite(String sql)
                {
                        return false;
                }

                public boolean dbWrite(String sql, String var)
                {
                        throw new UnsupportedOperationException();
                }

                public boolean dbWriteBinary(String sql, Object[] fields, byte[] var, int offset, int len)
                {
                        throw new UnsupportedOperationException();
                }

                public boolean dbWrite(String sql, Object[] fields)
                {
                        return false;
                }

                public boolean dbWrite(Connection connection, String sql, Object[] fields)
                {
                        throw new UnsupportedOperationException();
                }

                public boolean dbWriteBatch(Connection connection, String sql, List<Object[]> fieldsList)
                {
                        throw new UnsupportedOperationException();
                }

                public boolean dbWriteFailQuiet(Connection connection, String sql, Object[] fields)
                {
                        throw new UnsupportedOperationException();
                }

                public boolean dbWrite(String sql, Object[] fields, String lastField)
                {
                        throw new UnsupportedOperationException();
                }

                public void dbReadBlobAndUpdate(String sql, byte[] content)
                {
                        throw new UnsupportedOperationException();
                }

                public Connection dbReadLock(String sql, StringBuilder field)
                {
                        throw new UnsupportedOperationException();
                }

                public void dbUpdateCommit(String sql, Object[] fields, String var, Connection conn)
                {
                        throw new UnsupportedOperationException();
                }

                public void dbCancel(Connection conn)
                {
                        throw new UnsupportedOperationException();
                }

                public String getVendor()
                {
                        return ""; //$NON-NLS-1$
                }

                public void ddl(ClassLoader loader, String resource)
                {
                }

                public Long getNextSequence(String tableName, Connection conn)
                {
                        return null;
                }

                public String getBooleanConstant(boolean value)
                {
                        return Boolean.toString(value);
                }

                public Connection dbReadLock(String sql, SqlReader reader)
                {
                        throw new UnsupportedOperationException();
                }

                public int dbWriteCount(String sql, Object[] fields, String lastField, Connection callerConnection, boolean failQuiet)
                {
                        throw new UnsupportedOperationException();
                }

                public int dbWriteCount(String sql, Object[] fields, String lastField, Connection callerConnection, int failQuiet)
                {
                        throw new UnsupportedOperationException();
                }

                public boolean transact(Runnable callback, String tag)
                {
                        throw new UnsupportedOperationException();
                }
        }

        private static class StubCurrentObjectDao implements RWikiCurrentObjectDao
        {
                private final List<RWikiObject> pages;

                StubCurrentObjectDao(List<RWikiObject> pages)
                {
                        this.pages = pages;
                }

                public List getAll()
                {
                        return Collections.emptyList();
                }

                public void updateObject(RWikiObject rwo)
                {
                }

                public RWikiCurrentObject findByGlobalName(String name)
                {
                        return null;
                }

                public List findByGlobalNameAndContents(String criteria, String user, String realm)
                {
                        return Collections.emptyList();
                }

                public void update(RWikiCurrentObject rwo, RWikiHistoryObject rwho)
                {
                }

                public RWikiCurrentObject createRWikiObject(String name, String realm)
                {
                        return null;
                }

                public List findChangedSince(java.util.Date since, String realm)
                {
                        return Collections.emptyList();
                }

                public List findReferencingPages(String name)
                {
                        return Collections.emptyList();
                }

                public RWikiCurrentObject getRWikiCurrentObject(RWikiObject reference)
                {
                        return null;
                }

                public boolean exists(String name)
                {
                        return false;
                }

                public int getPageCount(String group)
                {
                        return 0;
                }

                public List findRWikiSubPages(String globalParentPageName)
                {
                        return pages;
                }

                public RWikiObject findLastRWikiSubPage(String globalParentPageName)
                {
                        return null;
                }

                public List findAllChangedSince(java.util.Date time, String basepath)
                {
                        return Collections.emptyList();
                }

                public List findAllPageNames()
                {
                        return Collections.emptyList();
                }

                public Object proxyObject(Object o)
                {
                        return o;
                }
        }

        private static class StubHistoryObjectDao implements RWikiHistoryObjectDao
        {
                private final Map<String, List<RWikiHistoryObject>> histories;

                StubHistoryObjectDao(Map<String, List<RWikiHistoryObject>> histories)
                {
                        this.histories = histories;
                }

                public void update(RWikiHistoryObject rwo)
                {
                }

                public RWikiHistoryObject createRWikiHistoryObject(RWikiCurrentObject rwo)
                {
                        return null;
                }

                public RWikiHistoryObject getRWikiHistoryObject(RWikiObject rwo, int revision)
                {
                        return null;
                }

                public List findRWikiHistoryObjects(RWikiObject reference)
                {
                        List<RWikiHistoryObject> list = histories.get(reference.getId());
                        if (list == null)
                        {
                                return null;
                        }
                        return new ArrayList<RWikiHistoryObject>(list);
                }

                public List findRWikiHistoryObjectsInReverse(RWikiObject reference)
                {
                        return Collections.emptyList();
                }

                public List getAll()
                {
                        return Collections.emptyList();
                }

                public void updateObject(RWikiObject rwo)
                {
                }
        }

        // make an arbitrarily long string
        String longString(int size) {
                StringBuffer sb  = new StringBuffer();
                int i = 0;
		while(i++ < size) {
			sb.append("X");
		}
		return sb.toString();
	}

}
