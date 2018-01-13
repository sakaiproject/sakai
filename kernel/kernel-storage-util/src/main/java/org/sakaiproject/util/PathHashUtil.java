/**********************************************************************************
 * $HeadURL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/PathHashUtil.java $
 * $Id: PathHashUtil.java 101656 2011-12-12 22:40:28Z aaronz@vt.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 Sakai Foundation
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
import java.security.NoSuchAlgorithmException;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * A utility class to generate a SHA1 hash based on a full path to a resource/entity.
 * @deprecated unused as of 12 Dec 2011, planned for removal after 2.9
 */
@Slf4j
public class PathHashUtil 
{
    private static char[] encode = { '0', '1', '2', '3', '4', '5', '6', '7',
                    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private static ThreadLocal<MessageDigest> digest = new ThreadLocal<MessageDigest>();
    
    /**
     * create a SHA1 hash of the path
     *
     * @param nodePath
     * @param encode
     * @return some SHA1 hash value possibly
     * @throws NoSuchAlgorithmException
     */
    public static String hash(String nodePath)
    {
    	MessageDigest mdigest  = (MessageDigest) digest.get();
    	if ( mdigest == null ) 
    	{
    		try
    		{
    			mdigest = MessageDigest.getInstance("SHA1");
                digest.set(mdigest);
    		}
    		catch (NoSuchAlgorithmException e)
    		{
    		    throw new RuntimeException("Failed to find SHA1 message digest: " + e, e);
    		}
    	}
    	byte[] b = mdigest.digest(nodePath.getBytes());
    	char[] c = new char[b.length * 2];
    	for (int i = 0; i < b.length; i++)
    	{
    		c[i * 2] = encode[b[i]&0x0f];
    		c[i * 2 + 1] = encode[(b[i]>>4)&0x0f];
    	}
    	String encoded =  new String(c);
    	log.debug("Encoded "+nodePath+" as "+encoded);
    	return encoded;
    }
}
