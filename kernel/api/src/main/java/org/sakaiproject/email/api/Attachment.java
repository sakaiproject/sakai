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
import javax.activation.DataSource;
import javax.activation.FileDataSource;

/**
 * Holds an attachment for an email message. The attachment will be included with the message.
 *
 * TODO: Make available for attachments to be stored in CHS.
 *
 * @author <a href="mailto:carl.hall@et.gatech.edu">Carl Hall</a>
 */
public class Attachment
{
	/**
	 * files to associated to this attachment
	 */
	private DataSource dataSource;
	private final String filename;

	public Attachment(File file, String filename)
	{
		this(new FileDataSource(file),filename);
	}

	public Attachment(DataSource dataSource,String filename) { 
		this.dataSource = dataSource; 
		this.filename = filename; 
	}

	/**
	 * Get the file associated to this attachment
	 *
	 * @return file associated with this attachment (created from the DataSource)
	 */
	public File getFile()
	{
		return ((FileDataSource)dataSource).getFile();
	}

	/**
	 * Get the name of the attached file.
	 *
	 * @return name of the attached file
	 */
	public String getFilename()
	{
		return filename;
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

}