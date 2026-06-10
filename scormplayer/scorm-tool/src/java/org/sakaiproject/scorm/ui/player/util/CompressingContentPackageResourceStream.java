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
import java.lang.ref.SoftReference;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.GZIPOutputStream;

import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

import org.sakaiproject.scorm.model.api.ContentPackageResource;

public class CompressingContentPackageResourceStream extends ContentPackageResourceStream
{
	private static final long serialVersionUID = 1L;

	/**
	 * Cache for compressed data, shared across requests. A new stream instance is created for
	 * every request, so an instance-level cache would never be hit; keying by resource path
	 * (invalidated by last-modified time) lets every launch of a package reuse the gzipped
	 * bytes. Values are SoftReferences so the cache shrinks under memory pressure.
	 */
	private static final ConcurrentMap<String, CacheEntry> CACHE = new ConcurrentHashMap<>();

	private final String cacheKey;

	public CompressingContentPackageResourceStream(ContentPackageResource resource)
	{
		super(resource);
		this.cacheKey = resource.getPath();
	}

	@Override
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		return new ByteArrayInputStream(getCompressedContent());
	}

	private byte[] getCompressedContent() throws ResourceStreamNotFoundException
	{
		Instant lastModified = lastModifiedTime();
		CacheEntry entry = cacheKey != null ? CACHE.get(cacheKey) : null;
		if (entry != null && entry.timeStamp.equals(lastModified))
		{
			byte[] cached = entry.bytes.get();
			if (cached != null)
			{
				return cached;
			}
		}

		try (InputStream stream = super.getInputStream())
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try (GZIPOutputStream zout = new GZIPOutputStream(out))
			{
				Streams.copy(stream, zout);
			}

			byte[] ret = out.toByteArray();
			if (cacheKey != null)
			{
				CACHE.put(cacheKey, new CacheEntry(lastModified, ret));
			}
			return ret;
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static final class CacheEntry
	{
		private final Instant timeStamp;
		private final SoftReference<byte[]> bytes;

		private CacheEntry(Instant timeStamp, byte[] bytes)
		{
			this.timeStamp = timeStamp;
			this.bytes = new SoftReference<>(bytes);
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
