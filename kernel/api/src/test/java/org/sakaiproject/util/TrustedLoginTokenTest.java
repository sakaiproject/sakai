/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.util;

import java.security.MessageDigest;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Test;

@Slf4j
public class TrustedLoginTokenTest
{
	@Test
	public void testDecode() throws Exception
	{
		String user = "usersid";
		String secret = "HowManyBottlesAreThereOnTheWall";
		TrustedLoginFilter tlf = new TrustedLoginFilter();
		tlf.setSharedSecret(secret);
		Random ran = new Random();
		String data = user + ";" + ran.nextLong();
		String hash = tlf.byteArrayToHexStr(MessageDigest.getInstance("SHA1").digest(
				(secret + ";" + data).getBytes("UTF-8")));
		String token = hash + ";" + data;
		long start = System.currentTimeMillis();
		
		for (int i = 0; i < 1000; i++)
		{
			String decodedUser = tlf.decodeToken(token);
			
			Assert.assertEquals("Users dont match ", user, decodedUser);
		}
		long end = System.currentTimeMillis();
		double t = 1.0*(end - start) / 1000.0;
		log.info("Per call " + t + " ms");
	}
}
