/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import java.util.zip.GZIPOutputStream;

import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.scorm.model.api.ContentPackageResource;

public class CompressingContentPackageResourceStream extends ContentPackageResourceStream
{
	private static final long serialVersionUID = 1L;

	/**
	 * Optional shared cache of gzipped bytes, owned by the Wicket application (see
	 * ScormWebApplication) and managed by the kernel MemoryService. Keys embed the resource's
	 * last-modified time, so a republished file simply gets a new key and stale entries age
	 * out under the cache's normal eviction policy. Transient: this stream lives for a single
	 * request and the cache must never be serialized with it.
	 */
	private final transient Cache<String, byte[]> compressedCache;

	private final String cacheKey;

	/** Holds the compressed bytes for this request so length() and getInputStream() compress only once. */
	private transient byte[] compressedContent;

	public CompressingContentPackageResourceStream(ContentPackageResource resource)
	{
		this(resource, null);
	}

	public CompressingContentPackageResourceStream(ContentPackageResource resource, Cache<String, byte[]> compressedCache)
	{
		super(resource);
		this.compressedCache = compressedCache;
		this.cacheKey = resource.getPath() + '|' + resource.getLastModified();
	}

	@Override
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		return new ByteArrayInputStream(getCompressedContent());
	}

	private byte[] getCompressedContent() throws ResourceStreamNotFoundException
	{
		if (compressedContent != null)
		{
			return compressedContent;
		}

		byte[] cached = compressedCache != null ? compressedCache.get(cacheKey) : null;
		if (cached != null)
		{
			compressedContent = cached;
			return cached;
		}

		try (InputStream stream = super.getInputStream())
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try (GZIPOutputStream zout = new GZIPOutputStream(out))
			{
				Streams.copy(stream, zout);
			}

			byte[] ret = out.toByteArray();
			if (compressedCache != null)
			{
				compressedCache.put(cacheKey, ret);
			}
			compressedContent = ret;
			return ret;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public Bytes length()
	{
		try
		{
			return Bytes.bytes(getCompressedContent().length);
		}
		catch (ResourceStreamNotFoundException e)
		{
			// No content, return null
			return Bytes.bytes(0);
		}
	}
}
