/**********************************************************************************
 * Copyright 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.email.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

/**
 * Holds an attachment for an email message. The attachment will be included with the message.
 *
 * @see javax.activation.DataSource
 * @see javax.activation.FileDataSource
 * @author <a href="mailto:carl.hall@et.gatech.edu">Carl Hall</a>
 */
public class Attachment
{
	/**
	 * file to associated to this attachment
	 */
	private final DataSource dataSource;

	/**
	 * The Content-Type and Content-Disposition MIME headers to be sent with the attachment.
	 * Can be <code>null</code>.
	 */
	private final String contentDisposition;
	private final String contentType;

	public enum ContentDisposition {INLINE, ATTACHMENT}

	/**
	 * Creates an Attachment with some of the MIME headers specified.
	 *
	 * @param dataSource  the data source
	 * @param contentType the Content-Type header, can be <code>null</code>
	 * @param disposition the Content-Disposition header, can be <code>null</code>
	 */
	public Attachment(DataSource dataSource, String contentType, ContentDisposition disposition)
	{
		this.dataSource = dataSource;
		this.contentType = contentType;
		this.contentDisposition = disposition == null ? null : disposition.toString().toLowerCase();
	}

	/**
	 * Creates an Attachment.
	 *
	 * @param dataSource The data source to use for the attachment.
	 */
	public Attachment(DataSource dataSource)
	{
		this(dataSource, null, null);
	}

	/**
	 * Creates a attachment based on a file.
	 * @param file The file to load the contents of the attachement from and to get the mimetype
	 *             from.
	 * @param filename The filename to call the attachment when sent out, doesn't have to match
	 *                 the file from which the content is loaded.
	 * @deprecated {@link org.sakaiproject.email.api.Attachment#Attachment(javax.activation.DataSource)}
	 */
	public Attachment(File file, String filename)
	{
		this(new RenamedDataSource(new FileDataSource(file), filename));
	}


	/**
	 * Creates an attachment supplying an different filename.
	 * @param dataSource The data source.
	 * @param filename The alternative filename.
	 */
	public Attachment(DataSource dataSource, String filename)
	{
		this(new RenamedDataSource(dataSource, filename), null, null);
	}


	/**
	 * Get the file associated to this attachment
	 *
	 * @return file associated with this attachment (created from the DataSource) or <code>null</code> if there
	 * isn't an underlying file for this attachment.
	 * @deprecated As not all Attachments will have an underlying file this method shouldn't be used.
	 */
	public File getFile()
	{
		return (dataSource instanceof FileDataSource)?((FileDataSource)dataSource).getFile():null;
	}

	/**
	 * Get the name of the attached file.
	 *
	 * @return name of the attached file
	 */
	public String getFilename()
	{
		return dataSource.getName();
	}

	/**
	 * The Content-Type MIME header for the attachment, can be <code>null</code>.
	 * This does not return the mime type of the data source as you may wish to supply a different one
	 * or no content-type at all.
	 *
	 * @return the Content-Type header
	 */
	public String getContentTypeHeader()
	{
		return contentType;
	}

	/**
	 * Get the datasource of the attachment
	 * 
	 * @return datasource of the attachment
	 */
	public DataSource getDataSource()
	{
		return dataSource;
	}

	/**
	 * The Content-Disposition MIME header for the attachment, can be <code>null</code>.
	 *
	 * @return the Content-Disposition header
	 */
	public String getContentDispositionHeader()
	{
		return contentDisposition;
	}

	/**
	 * Class which can be used when you wish to rename a file when adding it as an attachment.
	 * All calls are passed through to the original data source apart from the name.
	 */
	public static class RenamedDataSource implements DataSource {

		private final DataSource dataSource;
		private final String name;

		public RenamedDataSource(DataSource dataSource, String name) {
			this.dataSource = dataSource;
			this.name = name;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return dataSource.getInputStream();
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return dataSource.getOutputStream();
		}

		@Override
		public String getContentType() {
			return dataSource.getContentType();
		}

		@Override
		public String getName() {
			return name;
		}
	}
}
