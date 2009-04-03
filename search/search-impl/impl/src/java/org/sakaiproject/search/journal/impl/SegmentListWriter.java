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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author ieb
 *
 */
public class SegmentListWriter
{
	private File out;
	private Log log = LogFactory.getLog(SegmentListWriter.class);

	public SegmentListWriter(File out) {
		this.out = out;
	}
	
	public void write(List<File> segments) throws IOException
	{
		
		if ( ! out.exists() && !out.getParentFile().exists() ) {
			out.getParentFile().mkdirs();
		}
		FileOutputStream fout = new FileOutputStream(out);
		DataOutputStream dout = new DataOutputStream(fout);
		dout.write(SegmentListStore.SEGMENT_LIST_SIGNATURE);
		dout.writeInt(SegmentListStore.VERSION_SIGNATURE);
		dout.writeInt(segments.size());
		StringBuilder sb = new StringBuilder();
		sb.append(segments.size()).append(" Segments \n");
		for (File segs : segments)
		{
			String s = segs.getAbsolutePath();
			sb.append("\t").append(s).append("\n");
			dout.writeUTF(s);
		}
		dout.close();
		fout.close();
		if ( log.isDebugEnabled() )
			log.debug("Saved: "+sb.toString());
	}

}
