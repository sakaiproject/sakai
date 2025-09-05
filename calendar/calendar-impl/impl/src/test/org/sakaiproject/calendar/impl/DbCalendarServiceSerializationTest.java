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

package org.sakaiproject.calendar.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import junit.framework.TestCase;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.calendar.impl.DbCalendarService.DbStorage;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;

/**
 * @author ieb
 *
 */
@Slf4j
public class DbCalendarServiceSerializationTest extends TestCase
{
	private SqlService sqlService;
	private Entity container;
	private EntityManager entityManager;
	private Map<String, Object> services;
	private TimeService timeService;
	public DbCalendarServiceSerializationTest(String name)
	{
		super(name);
	}

	/**
	 * @throws java.lang.Exception
	 */
	protected void setUp() throws Exception
	{
		entityManager = new EntityManager() {

			public boolean checkReference(String ref)
			{
				return false;
			}

			@Override
			public EntityProducer getEntityProducer(String reference, Reference target)
			{
				return null;
			}

			public List getEntityProducers()
			{
				return new ArrayList();
			}

			public Reference newReference(String refString)
			{
				return null;
			}

			public Reference newReference(Reference copyMe)
			{
				return null;
			}

			public List newReferenceList()
			{
				return new ArrayList();
			}

			public List newReferenceList(List copyMe)
			{
				return new ArrayList(copyMe);
			}

			public void registerEntityProducer(EntityProducer manager, String referenceRoot)
			{
			}
			
		};
		sqlService = new SqlService(){

			public Connection borrowConnection() throws SQLException
			{
				return null;
			}

			public void dbCancel(Connection conn)
			{
			}

			public Long dbInsert(Connection callerConnection, String sql, Object[] fields, String autoColumn)
			{
				return null;
			}

			public Long dbInsert(Connection callerConnection, String sql, Object[] fields, String autoColumn, InputStream last, int lastLength)
			{
				return null;
			}

			public List dbRead(String sql)
			{
				return null;
			}

			public List dbRead(String sql, Object[] fields, SqlReader reader)
			{
				return null;
			}

			public List dbRead(Connection conn, String sql, Object[] fields, SqlReader reader)
			{
				return null;
			}

			public void dbReadBinary(String sql, Object[] fields, byte[] value)
			{
			}

			public void dbReadBinary(Connection conn, String sql, Object[] fields, byte[] value)
			{
			}

			public InputStream dbReadBinary(String sql, Object[] fields, boolean big) throws ServerOverloadException
			{
				return null;
			}

			public void dbReadBlobAndUpdate(String sql, byte[] content)
			{
			}

			public Connection dbReadLock(String sql, StringBuilder field)
			{
				return null;
			}

			public void dbUpdateCommit(String sql, Object[] fields, String var, Connection conn)
			{
			}

			public boolean dbWrite(String sql)
			{
				return false;
			}

			public boolean dbWrite(String sql, String var)
			{
				return false;
			}

			public boolean dbWrite(String sql, Object[] fields)
			{
				return false;
			}

			public boolean dbWrite(Connection connection, String sql, Object[] fields)
			{
				return false;
			}

			public boolean dbWrite(String sql, Object[] fields, String lastField)
			{
				return false;
			}

			public boolean dbWriteBatch(Connection connection, String sql, List<Object[]> fieldsList)
			{
				return false;
			}

			public boolean dbWriteBinary(String sql, Object[] fields, byte[] var, int offset, int len)
			{
				return false;
			}

			public boolean dbWriteFailQuiet(Connection connection, String sql, Object[] fields)
			{
				return false;
			}

			public void ddl(ClassLoader loader, String resource)
			{
			}

			public String getBooleanConstant(boolean value)
			{
				return null;
			}

			public GregorianCalendar getCal()
			{
				return null;
			}

			public Long getNextSequence(String tableName, Connection conn)
			{
				return null;
			}

			public String getVendor()
			{
				return "mysql";
			}

			public void returnConnection(Connection conn)
			{
			}

			public boolean transact(Runnable callback, String tag)
			{
				return false;
			}

			public Connection dbReadLock(String sql, SqlReader reader)
			{
				return null;
			}

			public int dbWriteCount(String sql, Object[] fields, String lastField, Connection callerConnection, boolean failQuiet) 

			{
				return -1;
			}
			public int dbWriteCount(String sql, Object[] fields, String lastField, Connection callerConnection, int failQuiet) 

			{
				return -1;
			}

			public boolean dbWriteBinaryStream(String sql, Object[] fields, java.io.InputStream binaryStream, long streamLength)
			{
				return false;
			}

			public boolean dbWriteBinaryStream(Connection connection, String sql, Object[] fields, java.io.InputStream binaryStream, long streamLength)
			{
				return false;
			}

			
		};
		timeService = new TimeService() {

			public boolean clearLocalTimeZone(String userId)
			{
				return false;
			}

			public boolean different(Time a, Time b)
			{
				return false;
			}

			public GregorianCalendar getCalendar(TimeZone zone, int year, int month, int day, int hour, int min, int sec, int ms)
			{
				return null;
			}

			public TimeZone getLocalTimeZone()
			{
				return null;
			}

			@Override
			public TimeZone getLocalTimeZone(String userId) {
				return null;
			}

			public Time newTime()
			{
				return null;
			}

			public Time newTime(long value)
			{
				return null;
			}

			public Time newTime(GregorianCalendar cal)
			{
				return null;
			}

			public TimeBreakdown newTimeBreakdown(int year, int month, int day, int hour, int minute, int second, int millisecond)
			{
				return null;
			}

			public Time newTimeGmt(String value)
			{
				return null;
			}

			public Time newTimeGmt(int year, int month, int day, int hour, int minute, int second, int millisecond)
			{
				return null;
			}

			public Time newTimeGmt(TimeBreakdown breakdown)
			{
				return null;
			}

			public Time newTimeLocal(int year, int month, int day, int hour, int minute, int second, int millisecond)
			{
				return null;
			}

			public Time newTimeLocal(TimeBreakdown breakdown)
			{
				return null;
			}

			public TimeRange newTimeRange(Time start, Time end, boolean startIncluded, boolean endIncluded)
			{
				return null;
			}

			public TimeRange newTimeRange(String value)
			{
				return null;
			}

			public TimeRange newTimeRange(Time startAndEnd)
			{
				return null;
			}

			public TimeRange newTimeRange(long start, long duration)
			{
				return null;
			}

			public TimeRange newTimeRange(Time start, Time end)
			{
				return null;
			}

			public TimeRange newTimeRange(Instant start, Instant end)
			{
				return null;
			}

			public int dbWriteCount(String sql, Object[] fields, String lastField, Connection callerConnection, boolean failQuiet)
			{
				return -1;
			}
			public int dbWriteCount(String sql, Object[] fields, String lastField, Connection callerConnection, int failQuiet)
			{
				return -1;
			}

			@Override
			public String dateFormat(Date date, Locale locale, int df) {
				return null;
			}

			@Override
			public String dateTimeFormat(Date date, Locale locale, int df) {
				return null;
			}

			@Override
			public String dateTimeFormat(Instant instant, FormatStyle dateStyle, FormatStyle timeStyle) {
				return null;
			}

			@Override
			public String shortLocalizedTimestamp(Instant instant, TimeZone timezone, Locale locale) {
				return null;
			}

			@Override
			public String shortLocalizedTimestamp(Instant instant, Locale locale) {
				return null;
			}

			@Override
			public String shortLocalizedDate(LocalDate date, Locale locale) {
				return null;
			}

			@Override
			public String shortPreciseLocalizedTimestamp(Instant instant, TimeZone timezone, Locale locale) {
				return null;
			}

			@Override
			public String shortPreciseLocalizedTimestamp(Instant instant, Locale locale) {
				return null;
			}

			@Override
			public String timeFormat(Date date, Locale locale, int format) {
				return null;
			}

			@Override
			public String dayOfWeekFormat(Date date, Locale locale, int format) {
				return null;
			}

			@Override
			public Date parseISODateInUserTimezone(String dateString) {
				return null;
			}

			@Override
			public String dateFromUtcToUserTimeZone(String utcDate, boolean formatted) {
				return null;
			}

			@Override
			public LocalDateTime dateFromUserTimeZoneToUtc(String zonedDate) {
				return null;
			}
		};
		services = new HashMap<String,Object>();
		services.put("sqlservice", sqlService);
		services.put("timeservice",timeService);
		services.put("entitymanager",entityManager);
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	protected void tearDown() throws Exception
	{
	}
	
	
	public void testConstruct() {
		DbCalendarService dbCal = new DbCalendarService();
		
	}
	public void testSerialize() throws IOException {
		DbCalendarService dbCal = new DbCalendarService();
		dbCal.setSqlService(sqlService);
		dbCal.setEntityManager(entityManager);
		dbCal.setServices(services);
		DbStorage s = (DbStorage) dbCal.newStorage();
		for(int i = 0; i < 100; i++ ) {
			InputStream ins = this.getClass().getResourceAsStream("testSerialize_"+i);
			if ( ins == null ) {
				break;
			}
			BufferedReader instream = new BufferedReader(new InputStreamReader(ins));
			StringBuilder sb = new StringBuilder();
			String line = instream.readLine();
			while ( line != null ) {
				sb.append(line).append("\n");
				line = instream.readLine();
			}
			instream.close();
			log.info("Xml is ["+sb.toString()+"]");
			Entity e = s.readContainerTest(sb.toString());
			assertNotNull(e);
			if ( container != null ) {
				container = e;
			}
			
		}
		for(int i = 0; i < 100; i++ ) {
			InputStream ins = this.getClass().getResourceAsStream("testSerializeEvent_"+i);
			if ( ins == null ) {
				break;
			}
			BufferedReader instream = new BufferedReader(new InputStreamReader(ins));
			StringBuilder sb = new StringBuilder();
			String line = instream.readLine();
			while ( line != null ) {
				sb.append(line).append("\n");
				line = instream.readLine();
			}
			instream.close();
			log.info("Xml is ["+sb.toString()+"]");
			Entity e = s.readResourceTest(container, sb.toString());
			assertNotNull(e);
		}
	}

}
