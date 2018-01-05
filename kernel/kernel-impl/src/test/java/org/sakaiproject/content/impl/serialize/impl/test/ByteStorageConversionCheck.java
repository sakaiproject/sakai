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

package org.sakaiproject.content.impl.serialize.impl.test;

import java.util.Random;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Test;

import org.sakaiproject.util.ByteStorageConversion;

/**
 * @author ieb
 *
 */
@Slf4j
public class ByteStorageConversionCheck
{
	@Test
	public void test256Conversion() {
		byte[] bin = new byte[256];
		char[] cin = new char[256];
		byte[] bout = new byte[256];

		{
			int i = 0;
			for (byte bx = Byte.MIN_VALUE; bx <= Byte.MAX_VALUE && i < bin.length; bx++)
			{
				bin[i++] = bx;
			}
		}
		
		ByteStorageConversion.toChar(bin, 0, cin, 0, bin.length);
		ByteStorageConversion.toByte(cin, 0, bout, 0, cin.length);

		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		for (int i = 0; i < bin.length; i++)
		{
			sb.append("   Byte "+bin[i]+": Stored as int["+(int)cin[i]+"]  char["+cin[i]+"]\n");
		}
		log.info(sb.toString());
		
		for ( int i = 0; i < bin.length; i++ ) {
			Assert.assertEquals("Internal Byte conversion failed at "+bin[i]+"=>"+(int)cin[i]+"=>"+bout[i],bin[i],bout[i]);
		}
		log.info("Internal Byte conversion test Passed Ok");
		
	}

	@Test
	public void testMaxMinConversion() {
		byte[] bin = new byte[2];
		char[] cin = new char[2];
		byte[] bout = new byte[2];
		
		bin[0] = Byte.MIN_VALUE;
		bin[1] = Byte.MAX_VALUE;
		
		ByteStorageConversion.toChar(bin, 0, cin, 0, bin.length);
		ByteStorageConversion.toByte(cin, 0, bout, 0, cin.length);
		log.info("   Min Byte "+bin[0]+": Stored as int["+(int)cin[0]+"]  char["+cin[0]+"]\n");
		log.info("   Max Byte "+bin[1]+": Stored as int["+(int)cin[1]+"]  char["+cin[1]+"]\n");
		
		for ( int i = 0; i < bin.length; i++ ) {
			if ( bin[i] !=  bout[i] ) {
				log.warn("Internal Byte conversion failed at "+bin[i]+"=>"+(int)cin[i]+"=>"+bout[i]);
			}
		}
	}

	@Test
	public void testRandomConversion() {
		byte[] bin = new byte[102400];
		char[] cin = new char[102400];
		byte[] bout = new byte[102400];
		
		Random r = new Random();
		r.nextBytes(bin);
		
		ByteStorageConversion.toChar(bin, 0, cin, 0, bin.length);
		ByteStorageConversion.toByte(cin, 0, bout, 0, cin.length);
		
		for ( int i = 0; i < bin.length; i++ ) {
			if ( bin[i] !=  bout[i] ) {
				Assert.assertEquals("Internal Byte conversion failed at "+bin[i]+"=>"+(int)cin[i]+"=>"+bout[i],bin[i],bout[i]);
			}
		}
		log.info("Internal Byte (random set) conversion test Passed Ok");
	}
}
