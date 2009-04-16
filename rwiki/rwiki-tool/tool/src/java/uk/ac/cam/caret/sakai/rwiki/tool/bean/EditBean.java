/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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
package uk.ac.cam.caret.sakai.rwiki.tool.bean;

/**
 * Basic value bean to for the edit view
 * 
 * @author andrew
 * @version $Id$
 */
public class EditBean
{

	/**
	 * Parameter name for version. This parameter is sent when saving a
	 * RWikiObject. It is used to know when we have a
	 * <code>VersionException</code>
	 */
	public static final String VERSION_PARAM = "version";

	/**
	 * Parameter name for content that we want resubmit after a
	 * <code>VersionException</code>
	 */
	public static final String SUBMITTED_CONTENT_PARAM = "submittedContent";

	/**
	 * Value of the save parameter that indicates we should overwrite with the
	 * submittedContent.
	 */
	public static final String OVERWRITE_VALUE = "overwrite";

	/**
	 * Parameter name for the content to save
	 */
	public static final String CONTENT_PARAM = "content";

	/**
	 * Value of the save parameter that indicates we wish to preview the content
	 * (NOTE must be lowercase!)
	 */
	public static final String PREVIEW_VALUE = "preview";

	/**
	 * Value of the save parameter that indicates we wish to save the content
	 */
	public static final String SAVE_VALUE = "save";

	/**
	 * Value of the save parameter that indicates we wish to cancel this edit
	 */
	public static final String CANCEL_VALUE = "cancel";

	/**
	 * Value of the save parameter that indicates we wish to add an attachment.
	 * This should be interpretted as a call to the file.helper. (NOTE must be
	 * lowercase!)
	 */
	public static final String LINK_ATTACHMENT_VALUE = "attachlink";

	/**
	 * Value of the save parameter that indicates we wish to embed an
	 * attachment. This should be interpretted as a call to the file.helper.
	 * (NOTE must be lowercase!)
	 */
	public static final String EMBED_ATTACHMENT_VALUE = "attachembed";

	/**
	 * Last position of the caret or highlights, (if available)
	 */
	public static final String STORED_CARET_POSITION = "caretPosition";

	/**
	 * The version string that was sent in the last save.
	 */
	private String previousVersion;

	/**
	 * The content that was sent in the last save. Will become the
	 * submittedContent.
	 */
	private String previousContent;

	/**
	 * Type of save performed last save.
	 */
	private String saveType;

	/**
	 * The revision that we were asked to revert to.
	 */
	private int previousRevision;

	public EditBean()
	{
		// Must have null constructor
		super();
	}

	/**
	 * @return content sent to the last save, if any.
	 */
	public String getPreviousContent()
	{
		return previousContent;
	}

	/**
	 * Set the content sent in the last save
	 * 
	 * @param previousContent
	 *        the previous content
	 */
	public void setPreviousContent(String previousContent)
	{
		this.previousContent = previousContent;
	}

	/**
	 * @return the version string sent in the last save, if any
	 */
	public String getPreviousVersion()
	{
		return previousVersion;
	}

	/**
	 * Set the version string sent in the last save
	 * 
	 * @param version
	 *        the version string
	 */
	public void setPreviousVersion(String version)
	{
		this.previousVersion = version;
	}

	/**
	 * @return the save type used in the last save
	 */
	public String getSaveType()
	{
		return saveType;
	}

	/**
	 * Sets the save type used by the last save.
	 * 
	 * @param saveType
	 */
	public void setSaveType(String saveType)
	{
		this.saveType = saveType;
	}

	/**
	 * Set the revision used for the last save
	 * 
	 * @param previousRevision
	 *        the revision number
	 */
	public void setPreviousRevision(int previousRevision)
	{
		this.previousRevision = previousRevision;
	}

	/**
	 * Get the previous revision number used by the last save, if any
	 * 
	 * @return revision number
	 */
	public int getPreviousRevision()
	{
		return previousRevision;
	}
}
