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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.journal.impl;

import java.io.File;
import java.util.List;

/**
 * @author ieb
 */
public class SegmentListStore
{

	public static final int VERSION_SIGNATURE = 1;

	public static final byte[] SEGMENT_LIST_SIGNATURE = { 'S', 'E', 'G', 'L', 'I', 'S',
			'T' };

	public static void main(String[] argv)
	{
		try
		{
			File f = new File(argv[0]);
			System.out.println("Reading Segment List " + f.getPath());
			SegmentListReader sr = new SegmentListReader(f);
			List<File> files = sr.read();
			System.err.println("Contains " + files.size() + " segments(s)");
			for (File sf : files)
			{
				System.out.println(sf.getPath());
			}
			System.out.println("Done");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.err.println("Failed " + ex.getMessage());
		}
	}

}
