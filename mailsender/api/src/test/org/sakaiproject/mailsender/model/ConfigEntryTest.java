/**********************************************************************************
 * Copyright 2010 Sakai Foundation
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

import static junit.framework.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.mailsender.model.ConfigEntry.ReplyTo;
import org.sakaiproject.mailsender.model.ConfigEntry.SubjectPrefixType;

/**
 * @author chall
 *
 */
@RunWith(value = MockitoJUnitRunner.class)
public class ConfigEntryTest {
	ConfigEntry entry;

	String subjectPrefixType = SubjectPrefixType.system.toString();
	boolean sendMeACopy = false;
	boolean appendRecipientList = false;
	boolean addToArchive = false;
	String replyTo = ReplyTo.no_reply_to.toString();
	boolean displayInvalidEmails = false;
	String subjectPrefix = "[subject]";
	boolean displayEmptyGroups = true;

	@Before
	public void setUp() {
		entry = new ConfigEntry(subjectPrefixType, sendMeACopy, appendRecipientList, addToArchive,
				replyTo, displayInvalidEmails, subjectPrefix, displayEmptyGroups);
	}

	@Test
	public void getDefaults() {
		assertEquals(subjectPrefixType, entry.getSubjectPrefixType());
		assertEquals(sendMeACopy, entry.isSendMeACopy());
		assertEquals(appendRecipientList, entry.isAppendRecipientList());
		assertEquals(addToArchive, entry.isAddToArchive());
		assertEquals(replyTo, entry.getReplyTo());
		assertEquals(displayInvalidEmails, entry.isDisplayInvalidEmails());
		assertEquals(subjectPrefix, entry.getSubjectPrefix());
		assertEquals(displayEmptyGroups, entry.isDisplayEmptyGroups());
	}

	@Test
	public void setReplyTo() {
		entry.setReplyTo("yes");
		assertEquals(ReplyTo.sender.toString(), entry.getReplyTo());

		entry.setReplyTo("no");
		assertEquals(ReplyTo.no_reply_to.toString(), entry.getReplyTo());
	}
}
