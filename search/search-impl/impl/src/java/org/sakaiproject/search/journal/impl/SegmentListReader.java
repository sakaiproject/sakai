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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ieb
 *
 */

public class SegmentListReader
{
	private File in;
	public SegmentListReader( File in ) {
		this.in = in;
	}
	public List<File> read() throws IOException
	{
		List<File> segments = new ArrayList<File>();
		if ( !in.exists() ) {
			return segments;
		}
		FileInputStream fout = new FileInputStream(in);
		DataInputStream din = new DataInputStream(fout);
		byte[] sig = new byte[SegmentListStore.SEGMENT_LIST_SIGNATURE.length];
		din.read(sig);
		for (int i = 0; i < sig.length; i++)
		{
			if (sig[i] != SegmentListStore.SEGMENT_LIST_SIGNATURE[i])
			{
				throw new IOException(
						"Segment List file is corrupt, please remove segments and recover from journal");
			}
		}
		int version = din.readInt();
		if (version == SegmentListStore.VERSION_SIGNATURE)
		{
			int n = din.readInt();
			for (int i = 0; i < n; i++)
			{
				segments.add(new File(din.readUTF()));
			}
		}
		else
		{
			throw new IOException(
					"Segment List savePoint not recognised, please remove segments and recover from journal ");
		}
		din.close();
		din.close();
		return segments;

	}

}
