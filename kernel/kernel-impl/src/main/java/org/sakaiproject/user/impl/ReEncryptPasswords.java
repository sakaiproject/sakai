/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-impl/src/main/java/org/sakaiproject/user/impl/OpenAuthnComponent.java $
 * $Id: OpenAuthnComponent.java 51317 2008-08-24 04:38:02Z csev@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008, 2009, 2010 Sakai Foundation
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

package org.sakaiproject.user.impl;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReEncryptPasswords {
	private static Properties props;

	/**
	 * Command line utility to re-encrypt all passwords in the database that use unsalted MD5.
	 * It should be run on the command line in the sakai.home folder with both the kernel-impl
	 * and SQL drvier jars on the classpath. 
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws Exception {

		props = new Properties();
		props.load(new FileInputStream(System.getProperty("sakai.properties", "sakai.properties")));
		String location = null;
		try {
			location = System.getProperty("local.properties", "local.properties");
			props.load(new FileInputStream(location));
		} catch (Exception e) {
			log.warn("Didn't load local.properties: "+ location);
		}
		try {
			location = System.getProperty("security.properties", "security.properties");
			props.load(new FileInputStream(location));
		} catch (Exception e) {
			log.warn("Didn't load security.properties: "+ location);
		}
		
		String url, username, password, driver;
		
		url = getOrBail("url@javax.sql.BaseDataSource");
		username = getOrBail("username@javax.sql.BaseDataSource");
		password = getOrBail("password@javax.sql.BaseDataSource");
		driver = getOrBail("driverClassName@javax.sql.BaseDataSource");

		Class.forName(driver);
		
		PasswordService pwdService = new PasswordService();
		
		Connection conn = DriverManager.getConnection(url, username, password);
		conn.setAutoCommit(false);
		PreparedStatement usersSt = conn.prepareStatement("SELECT USER_ID, PW FROM SAKAI_USER FOR UPDATE", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		ResultSet usersRs = usersSt.executeQuery();
		
		int total = 0, updated = 0;
		while (usersRs.next()) {
			total++;
			String currentPw = usersRs.getString("PW");
			String newPw = null;
			if (currentPw != null && currentPw.length() == 20) {
				newPw = PasswordService.MD5TRUNC_SALT_SHA256+ pwdService.encrypt(currentPw);
			} else if (currentPw != null && currentPw.length() == 24) {
				newPw = PasswordService.MD5_SALT_SHA256+ pwdService.encrypt(currentPw);
			}
			if (newPw != null) {
				usersRs.updateString("PW", newPw);
				usersRs.updateRow();
				updated++;
			}
		}
		conn.commit();
		log.info(" Users processed: "+ total+ " updated: "+ updated);
	}

	/**
	 * Get configuration from the properties or bail out.
	 * @param property The property to lookup.
	 * @return The value found.
	 * @throws java.lang.IllegalStateException If the property wasn't found.
	 */
	private static String getOrBail(String property) {
		String value = props.getProperty(property);
		if (value == null) {
			throw new IllegalStateException("Unable to find configuration for: "+ property);
		}
		return value;
	}

}
