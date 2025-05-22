/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.provider.user;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.user.api.DisplayAdvisorUDP;
import org.sakaiproject.user.api.ExternalUserSearchUDP;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserFactory;
import org.sakaiproject.user.api.UsersShareEmailUDP;
import org.sakaiproject.user.detail.ValueEncryptionUtilities;

/**
 * SampleUserDirectoryProvider is a sample UserDirectoryProvider.
 * Use this in testing. Do not use this in production.
 */
@Slf4j
public class SampleUserDirectoryProvider implements UserDirectoryProvider, UsersShareEmailUDP, DisplayAdvisorUDP, ExternalUserSearchUDP
{
	private static final String USER_PROP_CANDIDATE_ID = "candidateID";
	private static final String USER_PROP_ADDITIONAL_INFO = "additionalInfo";
	
	private static final String SITE_PROP_USE_INSTITUTIONAL_ANONYMOUS_ID = "useInstitutionalAnonymousID";
	private static final String SITE_PROP_DISPLAY_ADDITIONAL_INFORMATION = "displayAdditionalInformation";
	
	private static final String SYSTEM_PROP_USE_INSTITUTIONAL_ANONYMOUS_ID = "useInstitutionalAnonymousID";
	private static final String SYSTEM_PROP_DISPLAY_ADDITIONAL_INFORMATION = "displayAdditionalInformation";
	
	// Use the standard example domain name for examples.
	public static final String EMAIL_DOMAIN = "@example.edu";

	/** how many students to recognize (1.. this). */
	private int courseStudents = 1000;

	@Setter
	private ValueEncryptionUtilities encryptionUtilities;

	public void setCourseStudents(String count) {
		this.courseStudents = Integer.parseInt(count);
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try {
			DecimalFormat df = new DecimalFormat("0000");
			initializeRealNames();
			initializeDefaultUsers();
			initializeStudents(df);
			initializeInstructorsAndAdmins();

			log.info("init()");
		}
		catch (Throwable t)
		{
			log.warn("Problem creating SampleUserDirectoryProvider", t);
		}
	}
	
	/**
	 * Returns to uninitialized state. You can use this method to release resources thet your Service allocated when Turbine shuts down.
	 */
	public void destroy()
	{

		log.info("destroy()");

	} // destroy

	/** A collection of user ids/names. */
	private final Map<String, Info> userInfo = new ConcurrentHashMap<>();
	
	private Info[] realNames;

	public record Info(String id, String firstName, String lastName, String email) {
		public Info(String firstName, String lastName, String email) {
			this(null, firstName, lastName, email);
		}

		boolean contains(CharSequence sequence) {
			return (id != null && id.contains(sequence)) ||
				   firstName.contains(sequence) ||
				   lastName.contains(sequence) ||
				   email.contains(sequence);
		}
	}

	/**
	 * Construct.
	 */
	public SampleUserDirectoryProvider()
	{
	}

	private void initializeRealNames() {
		try {
			realNames = new Info[]{
				new Info("Victor", "van Dijk", "vvd" + EMAIL_DOMAIN),
				new Info("Peter", "van Keken", "pvk" + EMAIL_DOMAIN),
				new Info("Ben van", "der Pluijm", "bvdp" + EMAIL_DOMAIN),
				new Info("Rob", "van der Voo", "rvdv" + EMAIL_DOMAIN),
				new Info("Aimee", "de L'Aigle", "adlg" + EMAIL_DOMAIN),
				new Info("Wong", "Kar-Wai", "wkw" + EMAIL_DOMAIN),
				new Info("John", "Fitz Gerald", "jfg" + EMAIL_DOMAIN),
				new Info("El", new String("Niño".getBytes(), StandardCharsets.UTF_8), "warmPacificWater" + EMAIL_DOMAIN),
				new Info(new String("Ângeolo".getBytes(), StandardCharsets.UTF_8), "Haslip", "ah" + EMAIL_DOMAIN),
				new Info("Albert", "Zimmerman", "az" + EMAIL_DOMAIN),
				new Info("Albert", "Albertson", "aa" + EMAIL_DOMAIN),
				new Info("Zachary", "Anderson", "za" + EMAIL_DOMAIN),
				new Info("Zachary", "Zurawik", "zz" + EMAIL_DOMAIN),
				new Info("Bhaktavatsalam", "Bhayakridbhayanashanachar", "bb" + EMAIL_DOMAIN)
			};
		} catch (Exception e) {
			log.warn("Error initializing real names", e);
		}
	}

	private void initializeDefaultUsers() {
		userInfo.put("user1", new Info("user1", "One", "User", "user1" + EMAIL_DOMAIN));
		userInfo.put("user2", new Info("user2", "Two", "User", "user2" + EMAIL_DOMAIN));
		userInfo.put("user3", new Info("user3", "Three", "User", "user3" + EMAIL_DOMAIN));
	}

	private void initializeStudents(DecimalFormat df) {
		if (courseStudents > 0) {
			for (int i = 1; i <= courseStudents; i++) {
				String zeroPaddedId = df.format(i);
				String studentId = "student" + zeroPaddedId;
				
				if (i <= realNames.length) {
					Info realName = realNames[i - 1];
					userInfo.put(studentId, new Info(studentId, realName.firstName(), realName.lastName(), realName.email()));
				} else {
					userInfo.put(studentId, new Info(studentId, zeroPaddedId, "Student", studentId + EMAIL_DOMAIN));
				}
			}
		}
	}

	private void initializeInstructorsAndAdmins() {
		userInfo.put("instructor", new Info("instructor", "Sakai", "Instructor", "instructor" + EMAIL_DOMAIN));
		userInfo.put("instructor1", new Info("instructor1", "Sakai", "Instructor1", "instructor1" + EMAIL_DOMAIN));
		userInfo.put("instructor2", new Info("instructor2", "Sakai", "Instructor2", "instructor2" + EMAIL_DOMAIN));
		userInfo.put("da1", new Info("da1", "Dept", "Admin", "da1" + EMAIL_DOMAIN));
		userInfo.put("ta", new Info("ta", "Teaching", "Assistant", "ta" + EMAIL_DOMAIN));

		// SAK-25394 more ta's for testing purposes
		userInfo.put("ta1", new Info("ta1", "Teaching", "Assistant1", "ta1" + EMAIL_DOMAIN));
		userInfo.put("ta2", new Info("ta2", "Teaching", "Assistant2", "ta2" + EMAIL_DOMAIN));
		userInfo.put("ta3", new Info("ta3", "Teaching", "Assistant3", "ta3" + EMAIL_DOMAIN));

		// SAK-25267 used for integration with uPortal
		userInfo.put("student", new Info("student", "Sakai", "Student", "student" + EMAIL_DOMAIN));
		userInfo.put("faculty", new Info("faculty", "Sakai", "Faculty", "faculty" + EMAIL_DOMAIN));
	}

	/**
	 * See if a user by this id exists.
	 * 
	 * @param userId
	 *        The user id string.
	 * @return true if a user by this id exists, false if not.
	 */
	private boolean userExists(String userId) {
		if (userId == null) return false;
		return userId.startsWith("test") || userInfo.containsKey(userId);
	}

	private void addTestUserProperties(UserEdit edit) {
		switch (edit.getEid()) {
			case "student0001" -> {
				edit.getProperties().addProperty(USER_PROP_CANDIDATE_ID, encryptionUtilities.encrypt("user1encrypted", 0));
				edit.getProperties().addPropertyToList(USER_PROP_ADDITIONAL_INFO, encryptionUtilities.encrypt("Additional notes encrypted", 0));
			}
			case "student0002" -> {
				edit.getProperties().addProperty(USER_PROP_CANDIDATE_ID, encryptionUtilities.encrypt("2notes", 20));
				edit.getProperties().addPropertyToList(USER_PROP_ADDITIONAL_INFO, encryptionUtilities.encrypt("Additional notes encrypted student0002", 60));
				edit.getProperties().addPropertyToList(USER_PROP_ADDITIONAL_INFO, encryptionUtilities.encrypt("Additional notes encrypted again", 60));
			}
			case "student0003" -> {
				edit.getProperties().addPropertyToList(USER_PROP_CANDIDATE_ID, encryptionUtilities.encrypt("id1of2", 0));
				edit.getProperties().addPropertyToList(USER_PROP_CANDIDATE_ID, encryptionUtilities.encrypt("id2of2", 0));
				edit.getProperties().addPropertyToList(USER_PROP_ADDITIONAL_INFO, encryptionUtilities.encrypt("Additional notes encrypted again2", 0));
			}
			case "student0004", "student0005" -> {
				edit.getProperties().addProperty(USER_PROP_CANDIDATE_ID, encryptionUtilities.encrypt("", 0));
				edit.getProperties().addPropertyToList(USER_PROP_ADDITIONAL_INFO, encryptionUtilities.encrypt("", 0));
			}
			case "student0006" -> {
				edit.getProperties().addProperty(USER_PROP_CANDIDATE_ID, encryptionUtilities.encrypt(" ", 0));
				edit.getProperties().addPropertyToList(USER_PROP_ADDITIONAL_INFO, encryptionUtilities.encrypt(" ", 0));
			}
			case "student0007" -> {
				edit.getProperties().addProperty(USER_PROP_CANDIDATE_ID, encryptionUtilities.encrypt("student0007", 0));
				String longString = """
					abcdefghijklmnopqrstuvwxyz1234567890 abcdefghijklmnopqrstuvwxyz1234567890 abcdefghijklmnopqrstuvwxyz1234567890 ,\
					abcdefghijklmnopqrstuvwxyz1234567890 abcdefghijklmnopqrstuvwxyz1234567890 abcdefghijklmnopqrstuvwxyz1234567890 ,\
					abcdefghijklmnopqrstuvwxyz1234567890 abcdefghijklmnopqrstuvwxyz1234567890 abcdefghijklmnopqrstuvwxyz1234567890 ,\
					abcdefghijklmnopqrstuvwxyz1234567890 abcdefghijklmnopqrstuvwxyz1234567890 abcdefghijklmnopqrstuvwxyz1234567890 ,\
					abcdefghijklmnopqrstuvwxyz1234567890 abcdefghijklmnopqrstuvwxyz1234567890 abcdefghijklmnopqrstuvwxyz1234567890 ,\
					abcdefghijklmnopqrstuvwxyz1234567890 abcdefghijklmnopqrstuvwxyz1234567890 abcdefghijklmnopqrstuvwxyz1234567890 ,\
					abcdefghijklmnopqrstuvwxyz1234567890 abcdefghijklmnopqrstuvwxyz1234567890 abcdefghijklmnopqrstuvwxyz1234567890 ,\
					abcdefghijklmnopqrstuvwxyz1234567890 abcdefghijklmnopqrstuvwxyz1234567890 abcdefghijklmnopqrstuvwxyz1234567890 ,\
					up_until_10 0 0 _char""";
				edit.getProperties().addPropertyToList(USER_PROP_ADDITIONAL_INFO, encryptionUtilities.encrypt(longString, 0));
			}
		}
	}

	/**
	 * Access a user object. Update the object with the information found.
	 * 
	 * @param edit
	 *        The user object (id is set) to fill in.
	 * @return true if the user object was found and information updated, false if not.
	 */
	@Override
    public boolean getUser(UserEdit edit)
	{
		if (edit == null) return false;
		if (!userExists(edit.getEid())) return false;

		Info info = userInfo.get(edit.getEid());
		if (info == null) {
			edit.setFirstName(edit.getEid());
			edit.setLastName(edit.getEid());
			edit.setEmail(edit.getEid());
			edit.setType("registered");
			// Password will be set during authentication, not during user lookup
		} else {
			addTestUserProperties(edit);
			edit.setFirstName(info.firstName());
			edit.setLastName(info.lastName());
			edit.setEmail(info.email());
			edit.setType("registered");
			// Password will be set during authentication, not during user lookup
		}

		return true;

	} // getUser

	/**
	 * Access a collection of UserEdit objects; if the user is found, update the information, otherwise remove the UserEdit object from the collection.
	 * 
	 * @param users
	 *        The UserEdit objects (with id set) to fill in or remove.
	 */
	@Override
    public void getUsers(Collection<UserEdit> users)
	{
		users.removeIf(user -> !getUser(user));
	}

	/**
	 * Find a user object who has this email address. Update the object with the information found. <br />
	 * Note: this method won't be used, because we are a UsersShareEmailUPD.<br />
	 * This is the sort of method to provide if your external source has only a single user for any email address.
	 * 
	 * @param email
	 *        The email address string.
	 * @return true if the user object was found and information updated, false if not.
	 */
	@Override
    public boolean findUserByEmail(UserEdit edit, String email)
	{
		if ((edit == null) || (email == null)) return false;

		int pos = email.indexOf(EMAIL_DOMAIN);
		if (pos != -1)
		{
			String id = email.substring(0, pos);
			edit.setEid(id);
			return getUser(edit);
		}

		return false;

	} // findUserByEmail

	/**
	 * Find all user objects which have this email address.
	 * 
	 * @param email
	 *        The email address string.
	 * @param factory
	 *        Use this factory's newUser() method to create all the UserEdit objects you populate and return in the return collection.
	 * @return Collection (UserEdit) of user objects that have this email address, or an empty Collection if there are none.
	 */
	@Override
    public Collection<UserEdit> findUsersByEmail(String email, UserFactory factory)
	{
		Collection<UserEdit> rv = new ArrayList<>();

		int pos = email.indexOf(EMAIL_DOMAIN);
		if (pos != -1)
		{
			// get a UserEdit to populate
			String id = email.substring(0, pos);
			UserEdit edit = factory.newUser(id);
			if (getUser(edit)) rv.add(edit);
		}

		return rv;
	}

	@Override
	public List<UserEdit> searchExternalUsers(String criteria, int first, int last, UserFactory factory) {
		Stream<Info> stream = userInfo.values().stream().filter(i -> i.contains(criteria));
		if (first != -1) {
			stream = stream.skip(first);
		}
		if (last != -1) {
			stream = stream.limit(last-first+1);
		}
		return stream.map(i -> {
			UserEdit edit = factory.newUser(i.id);
			return getUser(edit)?edit:null;
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	/**
	 * Authenticate a user / password. If the user edit exists it may be modified, and will be stored if...
	 * 
	 * @param userId
	 *        The user id.
	 * @param edit
	 *        The UserEdit matching the id to be authenticated (and updated) if we have one.
	 * @param password
	 *        The password.
	 * @return true if authenticated, false if not.
	 */
	@Override
    public boolean authenticateUser(String userId, UserEdit edit, String password)
	{
		if ((userId == null) || (password == null)) return false;

		if (userId.startsWith("test")) return userId.equals(password);
        return userExists(userId) && password.equals("sakai");

    } // authenticateUser

	/**
	 * {@inheritDoc}
	 */
	@Override
    public boolean authenticateWithProviderFirst(String id)
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean createUserRecord(String id)
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public String getDisplayId(User user)
	{
		return user.getEid();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public String getDisplayName(User user)
	{
		// punt
		return null;
	}
}