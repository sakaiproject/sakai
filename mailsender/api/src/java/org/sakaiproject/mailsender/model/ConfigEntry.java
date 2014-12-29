/**********************************************************************************
 * Copyright 2008-2009 Sakai Foundation
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
package org.sakaiproject.mailsender.model;

/**
 * Programmatic view of the "Options" page in Mail Sender
 */
public class ConfigEntry
{
	public static final ConfigEntry DEFAULT_CONFIG = new ConfigEntry(
			SubjectPrefixType.system.name(), false, false, false,
			ReplyTo.no_reply_to.name(), false, null, true);

	public enum ReplyTo
	{
		sender, no_reply_to
	}

	public enum SubjectPrefixType
	{
		system, custom
	}

	public enum ConfigParams
	{
		replyto, sendmecopy, appendrecipientlist, emailarchive, subjectprefix, displayinvalidemailaddrs, displayemptygroups
	}

	private String replyTo;
	private boolean displayInvalidEmails;
	private boolean sendMeACopy;
	private boolean appendRecipientList;
	private boolean addToArchive;
	private String subjectPrefixType;
	private String subjectPrefix;
	private boolean displayEmptyGroups = true;

	public ConfigEntry(String subjectPrefixType, boolean sendMeACopy, boolean appendRecipientList,
			boolean addToArchive, String replyTo, boolean displayInvalidEmails,
			String subjectPrefix, boolean displayEmptyGroups)
	{
		setSubjectPrefixType(subjectPrefixType);
		setSubjectPrefix(subjectPrefix);
		setSendMeACopy(sendMeACopy);
		setAppendRecipientList(appendRecipientList);
		setAddToArchive(addToArchive);
		setReplyTo(replyTo);
		setDisplayInvalidEmails(displayInvalidEmails);
		setDisplayEmptyGroups(displayEmptyGroups);
	}

	public String getReplyTo()
	{
		return replyTo;
	}

	public void setReplyTo(String replyTo)
	{
		if ("yes".equals(replyTo))
		{
			this.replyTo = ReplyTo.sender.name();
		}
		else if ("no".equals(replyTo))
		{
			this.replyTo = ReplyTo.no_reply_to.name();
		}
		else
		{
			this.replyTo = ReplyTo.valueOf(replyTo).name();
		}
	}

	public boolean isDisplayInvalidEmails()
	{
		return displayInvalidEmails;
	}

	public void setDisplayInvalidEmails(boolean displayInvalidEmails)
	{
		this.displayInvalidEmails = displayInvalidEmails;
	}

	public boolean isSendMeACopy()
	{
		return sendMeACopy;
	}

	public void setSendMeACopy(boolean sendMeACopy)
	{
		this.sendMeACopy = sendMeACopy;
	}

	public boolean isAppendRecipientList()
	{
		return appendRecipientList;
	}
        
	public void setAppendRecipientList(boolean appendRecipientList)
	{
		this.appendRecipientList = appendRecipientList;
	}
        
	public boolean isAddToArchive()
	{
		return addToArchive;
	}

	public void setAddToArchive(boolean addToArchive)
	{
		this.addToArchive = addToArchive;
	}

	public String getSubjectPrefix()
	{
		return subjectPrefix;
	}

	public void setSubjectPrefix(String subjectPrefix)
	{
		this.subjectPrefix = subjectPrefix;
	}

	public String getSubjectPrefixType()
	{
		return subjectPrefixType;
	}

	public void setSubjectPrefixType(String subjectPrefixType)
	{
		this.subjectPrefixType = SubjectPrefixType.valueOf(subjectPrefixType).name();
	}

	public void setDisplayEmptyGroups(boolean displayEmptyGroups)
	{
		this.displayEmptyGroups = displayEmptyGroups;
	}

	public boolean isDisplayEmptyGroups()
	{
		return displayEmptyGroups;
	}
}