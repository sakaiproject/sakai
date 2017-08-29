/**
 * Copyright (c) 2003-2014 The Apereo Foundation
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
package org.sakaiproject.util.api;

import java.util.Map.Entry;
import java.util.Set;


/**
 * From SAK-22283:
 * <p>
 * What LinkMigrationHelper does in the two cases mentioned here is
 * to surround certain links kinds of links (Assignments and Msgcntr) links with brackets [link] to indicate to the instructor
 * that something more needs to be done with them before they can be used by the class. This is because after migration
 * in a Duplicate or Import operation from Site Info, the new Assignments and Msgcntr topics need to be "published" before
 * the student can see them. The brackets are there as a reminder. In the case of Tests and Quizzes, assessments
 * are not really migrated. They appear in the new site as working copies, and "publishing" them actually duplicates
 * them with entirely new links, so migrating the links is meaningless. In this case, the link as it would appear in
 * a Syllabus or any other rich text entity is not migrated at all and the label is surrounded by brackets - again as a reminder to the
 * Instructor that they need to publish the quiz and then use the FileManager (accessed through the Link icon on CKeditor
 * and then the browse button) to re-establish the link in its new form.
 * </p>
 */
public interface LinkMigrationHelper {

	/**
	 * This attempts to update links in a HTML message to highlight ones that need updating
	 * (by surrounding them with brackets) and removing ones that no longer make sense.
	 *
	 * @param m The HTML message to update.
	 * @return An updated version of the HTML message with some links re-written.
	 * @throws Exception If there was a problem processing the message.
	 */
	public String bracketAndNullifySelectedLinks(String m) throws Exception;

	/**
	 * This migrates a set of links in the message body from the old versions to the new versions and
	 * returns the updated message body. It also calls out to {@link #bracketAndNullifySelectedLinks(String)} to
	 * update references that need input from the instructor.
	 *
	 * @param entrySet A set of map entries with the keys being the old URLs and the values being the new ones.
	 * @param msgBody  A HTML message which should be updated.
	 * @return The updated message body with all the links updated.
	 */
	public String migrateAllLinks(Set<Entry<String,String>> entrySet, String msgBody);

	/**
	 * This migrates a reference in the message body. All references have space replaced by %20.
	 *
	 * @param fromContextRef   The reference that may exist in the message body.
	 * @param targetContextRef The reference to replace the existing reference with.
	 * @param msgBody          The HTML message in which to scan for the existing reference.
	 * @return The updated message body, or the original one if no changes were made.
	 */
	public String migrateOneLink(String fromContextRef, String targetContextRef, String msgBody);


}
