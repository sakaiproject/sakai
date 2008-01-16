/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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
/*
 * This code borrows substantially from the Apache Wicket nested class
 *  	org.apache.wicket.markup.html.CompressedPackageResource$CompressingResourceStream
 * authored by Janne Hietam&auml;ki
 * 
 * The original license for that class is pasted below:
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.ui.player.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.zip.GZIPOutputStream;

import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;
import org.sakaiproject.scorm.model.api.ContentPackageResource;

public class CompressingContentPackageResourceStream extends ContentPackageResourceStream {

	private static final long serialVersionUID = 1L;

	/** Cache for compressed data */
	private SoftReference cache = new SoftReference(null);

	/** Timestamp of the cache */
	private Time timeStamp = null;
	
	
	public CompressingContentPackageResourceStream(ContentPackageResource resource) {
		super(resource);
	}
	
	public InputStream getInputStream() throws ResourceStreamNotFoundException {
		return new ByteArrayInputStream(getCompressedContent());
	}

	private byte[] getCompressedContent() throws ResourceStreamNotFoundException {
		InputStream stream = super.getInputStream();
		try {
			byte ret[] = (byte[])cache.get();
			if (ret != null && timeStamp != null)
			{
				if (timeStamp.equals(lastModifiedTime()))
				{
					return ret;
				}
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			GZIPOutputStream zout = new GZIPOutputStream(out);
			Streams.copy(stream, zout);
			zout.close();
			stream.close();
			ret = out.toByteArray();
			timeStamp = lastModifiedTime();
			cache = new SoftReference(ret);
			return ret;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

	}
	
}
