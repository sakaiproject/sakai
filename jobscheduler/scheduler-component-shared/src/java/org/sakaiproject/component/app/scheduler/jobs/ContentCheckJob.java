/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.component.app.scheduler.jobs;

import java.io.IOException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.bind.DatatypeConverter;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.iterators.IteratorChain;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.NullOutputStream;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.exception.ServerOverloadException;

/**
 * <p>
 * This class iterates through all the items in content hosting checking that the files on disk are correct
 * and they are the correct size. It would be nice if we could also have a checksum to compare but Sakai
 * doesn't currently store that.
 * </p>
 * <p>
 * This was written as we suspect that the network file-store that we use might have dropped some of the files.
 * It does a depth first search of the content hosting service. Reporting is done through the logging framework.
 * </p>
 * <p>
 * Be very careful if you change this class that you don't introduce any methods in ContentHostingService
 * that use thread local caches as on long running threads these won't get cleared and on a large install will
 * crash the JVM.
 * </p>
 *
 * @author Matthew Buckett
 */
@Slf4j
public class ContentCheckJob implements Job {

	public static final String ALGORITHM = "MD5";

	private ContentHostingService chs;

	public void setChs(ContentHostingService chs) {
		this.chs = chs;
	}

	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new JobExecutionException("Can't get digest for "+ ALGORITHM);
		}
		String[] types = {
				ResourceType.TYPE_HTML, ResourceType.MIME_TYPE_TEXT, ResourceType.TYPE_UPLOAD
		};
		IteratorChain allFiles = new IteratorChain();
		for (String type : types) {
			Iterator<ContentResource> resourceIterator = new ContentHostingIterator<ContentResource>(type);
			allFiles.addIterator(resourceIterator);
		}
		// Now check all the files.
		ContentResourceChecker checker = new ContentResourceChecker(allFiles, digest);
		checker.check();
	}

	/**
	 * This class does the checking of resources that the size on disk matches the size in the DB.
	 */
	class ContentResourceChecker {

		private Iterator<ContentResource> resourceIterator;
		private MessageDigest digest;

		public ContentResourceChecker(Iterator<ContentResource> resourceIterator, MessageDigest digest) {
			this.resourceIterator = resourceIterator;
			this.digest = digest;
		}

		public void check() {
			long count = 0;
			long bad = 0;
			long overload = 0;
			long io = 0;
			log.info("Checking resources DB/filesystem are in sync.");
			while(resourceIterator.hasNext()) {
				ContentResource resource = resourceIterator.next();
				// This should be redundant but it's a long running job that I don't want to fail.
				if (resource == null) {
					log.warn("Got null resource, skipping.");
					continue;
				}
				count++;
				if (log.isDebugEnabled()) {
					log.debug("Starting to look at: "+ resource.getId());
				}
				long reportedLength = resource.getContentLength();
				CountingInputStream is = null;
				DigestOutputStream os = null;
				try {
					digest.reset();
					is =  new CountingInputStream(resource.streamContent());
					os = new DigestOutputStream(new NullOutputStream(), digest);
					IOUtils.copy(is, os);
					long readLength = is.getByteCount();
					// Check if it's good.
					if (reportedLength != readLength) {
						bad++;
						byte[] digestBytes = digest.digest();
						String digestString = DatatypeConverter.printHexBinary(digestBytes);
						log.warn(String.format(
								"Length mismatch for: %s stored length: %d read length %d %s digest: %s",
								resource.getId(), reportedLength, readLength, ALGORITHM, digestString));
					}
				} catch (ServerOverloadException e) {
					log.error(String.format("Failed to read: %s because %s", resource.getId(), e.getMessage()));
					overload++;
				} catch (IOException e) {
					log.error(String.format("IO problem with: %s because %s", resource.getId(), e.getMessage()));
					io++;
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
							log.warn("Failed to close inputstream for: "+ resource.getId());
						}
					}
					if (os != null) {
						try {
							os.close();
						} catch (IOException e) {
							log.warn("Failed to close outputstream for: "+ resource.getId());
						}
					}
				}
			}
			log.info(String.format("Looked at %d resources (%d bad, %d overloads, %d io problems).",
					count,  bad, overload, io));
		}
	}

	/**
	 * This iterator works it's way through content hosting. It does this using methods that don't
	 * use the thread local caches so we don't have to worry about clearing them.
	 * @param <T> The type we get back from ContentHostingService.
	 */
	class ContentHostingIterator<T extends ContentEntity> implements Iterator<T> {

		private int page = 0;
		private int pageSize = 256;
		private String type;
		// The iterator on the paged list we got back from the CHS.
		private Iterator<T> it;
		// The next element to return.
		private T next;

		public ContentHostingIterator(String type, int pageSize) {
			this.type = type;
			this.pageSize = pageSize;
			loadNext();
		}

		public ContentHostingIterator(String type) {
			this(type, 256);
		}

		@SuppressWarnings("unchecked")
		private void loadNext() {
			next = null;
			if (it == null || !it.hasNext()) {
				Collection resources = chs.getResourcesOfType(type, pageSize, page++);
				if (resources != null) {
					it = (Iterator<T>)resources.iterator();
				}
			}
			if (it != null && it.hasNext()) {
				next = it.next();
			}
		}

		public boolean hasNext() {
			return next != null;
		}

		public T next() {
			if (next == null) {
				throw new NoSuchElementException();
			}
			T toReturn = next;
			loadNext();
			return toReturn;
		}

		public void remove() {
			throw new UnsupportedOperationException("We don't support remove.");
		}
	}
}
