/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.tool.pages.panels;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.profile2.exception.ProfilePrototypeNotDefinedException;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.ProfilePrivacyLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.SocialNetworkingInfo;
import org.sakaiproject.profile2.types.PrivacyType;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

/**
 * Container for viewing the profile of someone else.
 */
@Slf4j
public class ViewProfilePanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
	protected SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfileLogic")
	protected ProfileLogic profileLogic;
	
	@SpringBean(name="org.sakaiproject.profile2.logic.ProfilePrivacyLogic")
	protected ProfilePrivacyLogic privacyLogic;
	
	public ViewProfilePanel(String id, final String userUuid, final String currentUserId,
			ProfilePrivacy privacy, boolean friend) {
		
		super(id);
		
		//get SakaiPerson for the person who's profile we are viewing
		//SakaiPerson returns NULL strings if value is not set, not blank ones
		final SakaiPerson sakaiPerson;
		//if null, they have no profile so just get a prototype
		if(sakaiProxy.getSakaiPerson(userUuid) == null) {
			log.info("No SakaiPerson for " + userUuid);
			sakaiPerson = sakaiProxy.getSakaiPersonPrototype();
			//if its still null, throw exception
			if(sakaiPerson == null) {
				throw new ProfilePrototypeNotDefinedException("Couldn't create a SakaiPerson prototype for " + userUuid);
			}
		} else {
			sakaiPerson = sakaiProxy.getSakaiPerson(userUuid);
		}
		
		//holds number of profile containers that are visible
		int visibleContainerCount = 0;
		
		boolean isBasicInfoAllowed = privacyLogic.isActionAllowed(userUuid,currentUserId, PrivacyType.PRIVACY_OPTION_BASICINFO);
		boolean isContactInfoAllowed = privacyLogic.isActionAllowed(userUuid, currentUserId, PrivacyType.PRIVACY_OPTION_CONTACTINFO);
		boolean isBusinessInfoAllowed = privacyLogic.isActionAllowed(userUuid, currentUserId, PrivacyType.PRIVACY_OPTION_BUSINESSINFO);
		boolean isPersonalInfoAllowed = privacyLogic.isActionAllowed(userUuid, currentUserId, PrivacyType.PRIVACY_OPTION_PERSONALINFO);
		boolean isStaffInfoAllowed = privacyLogic.isActionAllowed(userUuid, currentUserId, PrivacyType.PRIVACY_OPTION_STAFFINFO);
		boolean isStudentInfoAllowed = privacyLogic.isActionAllowed(userUuid, currentUserId, PrivacyType.PRIVACY_OPTION_STUDENTINFO);
		boolean isSocialNetworkingInfoAllowed = privacyLogic.isActionAllowed(userUuid, currentUserId, PrivacyType.PRIVACY_OPTION_SOCIALINFO);
		
		/* BASIC INFO */
		WebMarkupContainer basicInfoContainer = new WebMarkupContainer("mainSectionContainer_basic");
		basicInfoContainer.setOutputMarkupId(true);
		
		//get info
		String nickname = sakaiPerson.getNickname();
		String personalSummary = sakaiPerson.getNotes();
		
		Date dateOfBirth = sakaiPerson.getDateOfBirth();
		String birthday = "";
		int visibleFieldCount_basic = 0;
		
		if(dateOfBirth != null) {
			
			if(privacyLogic.isBirthYearVisible(userUuid)) {
				birthday = ProfileUtils.convertDateToString(dateOfBirth, ProfileConstants.DEFAULT_DATE_FORMAT);
			} else {
				birthday = ProfileUtils.convertDateToString(dateOfBirth, ProfileConstants.DEFAULT_DATE_FORMAT_HIDE_YEAR);
			}
		}
		
		//heading
		basicInfoContainer.add(new Label("mainSectionHeading_basic", new ResourceModel("heading.basic")));
		
		//nickname
		WebMarkupContainer nicknameContainer = new WebMarkupContainer("nicknameContainer");
		nicknameContainer.add(new Label("nicknameLabel", new ResourceModel("profile.nickname")));
		nicknameContainer.add(new Label("nickname", nickname));
		basicInfoContainer.add(nicknameContainer);
		if(StringUtils.isBlank(nickname)) {
			nickname=""; //for the 'add friend' link
			nicknameContainer.setVisible(false);
		} else {
			visibleFieldCount_basic++;
		}
		
		//birthday
		WebMarkupContainer birthdayContainer = new WebMarkupContainer("birthdayContainer");
		birthdayContainer.add(new Label("birthdayLabel", new ResourceModel("profile.birthday")));
		birthdayContainer.add(new Label("birthday", birthday));
		basicInfoContainer.add(birthdayContainer);
		if(StringUtils.isBlank(birthday)) {
			birthdayContainer.setVisible(false);
		} else {
			visibleFieldCount_basic++;
		}
		
		//personal summary
		WebMarkupContainer personalSummaryContainer = new WebMarkupContainer("personalSummaryContainer");
		personalSummaryContainer.add(new Label("personalSummaryLabel", new ResourceModel("profile.summary")));
		personalSummaryContainer.add(new Label("personalSummary", ProfileUtils.processHtml(personalSummary)).setEscapeModelStrings(false));
		basicInfoContainer.add(personalSummaryContainer);
		if(StringUtils.isBlank(personalSummary)) {
			personalSummaryContainer.setVisible(false);
		} else {
			visibleFieldCount_basic++;
		}
		
		add(basicInfoContainer);
		
		//if nothing/not allowed, hide whole panel
		if(visibleFieldCount_basic == 0 || !isBasicInfoAllowed) {
			basicInfoContainer.setVisible(false);
		} else {
			visibleContainerCount++;
		}
		
		/* CONTACT INFO */
		WebMarkupContainer contactInfoContainer = new WebMarkupContainer("mainSectionContainer_contact");
		contactInfoContainer.setOutputMarkupId(true);
		
		//get info
		String email = sakaiProxy.getUserEmail(userUuid); //must come from SakaiProxy
		String homepage = sakaiPerson.getLabeledURI();
		String workphone = sakaiPerson.getTelephoneNumber();
		String homephone = sakaiPerson.getHomePhone();
		String mobilephone = sakaiPerson.getMobile();
		String facsimile = sakaiPerson.getFacsimileTelephoneNumber();
		
		int visibleFieldCount_contact = 0;
		
		//heading
		contactInfoContainer.add(new Label("mainSectionHeading_contact", new ResourceModel("heading.contact")));
		
		//email
		WebMarkupContainer emailContainer = new WebMarkupContainer("emailContainer");
		emailContainer.add(new Label("emailLabel", new ResourceModel("profile.email")));
		emailContainer.add(new Label("email", email));
		contactInfoContainer.add(emailContainer);
		if(StringUtils.isBlank(email)) {
			emailContainer.setVisible(false);
		} else {
			visibleFieldCount_contact++;
		}
		
		//homepage
		WebMarkupContainer homepageContainer = new WebMarkupContainer("homepageContainer");
		homepageContainer.add(new Label("homepageLabel", new ResourceModel("profile.homepage")));
		homepageContainer.add(new ExternalLink("homepage", homepage, homepage));
		contactInfoContainer.add(homepageContainer);
		if(StringUtils.isBlank(homepage)) {
			homepageContainer.setVisible(false);
		} else {
			visibleFieldCount_contact++;
		}
		
		//work phone
		WebMarkupContainer workphoneContainer = new WebMarkupContainer("workphoneContainer");
		workphoneContainer.add(new Label("workphoneLabel", new ResourceModel("profile.phone.work")));
		workphoneContainer.add(new Label("workphone", workphone));
		contactInfoContainer.add(workphoneContainer);
		if(StringUtils.isBlank(workphone)) {
			workphoneContainer.setVisible(false);
		} else {
			visibleFieldCount_contact++;
		}
		
		//home phone
		WebMarkupContainer homephoneContainer = new WebMarkupContainer("homephoneContainer");
		homephoneContainer.add(new Label("homephoneLabel", new ResourceModel("profile.phone.home")));
		homephoneContainer.add(new Label("homephone", homephone));
		contactInfoContainer.add(homephoneContainer);
		if(StringUtils.isBlank(homephone)) {
			homephoneContainer.setVisible(false);
		} else {
			visibleFieldCount_contact++;
		}
		
		//mobile phone
		WebMarkupContainer mobilephoneContainer = new WebMarkupContainer("mobilephoneContainer");
		mobilephoneContainer.add(new Label("mobilephoneLabel", new ResourceModel("profile.phone.mobile")));
		mobilephoneContainer.add(new Label("mobilephone", mobilephone));
		contactInfoContainer.add(mobilephoneContainer);
		if(StringUtils.isBlank(mobilephone)) {
			mobilephoneContainer.setVisible(false);
		} else {
			visibleFieldCount_contact++;
		}
		
		//facsimile
		WebMarkupContainer facsimileContainer = new WebMarkupContainer("facsimileContainer");
		facsimileContainer.add(new Label("facsimileLabel", new ResourceModel("profile.phone.facsimile")));
		facsimileContainer.add(new Label("facsimile", facsimile));
		contactInfoContainer.add(facsimileContainer);
		if(StringUtils.isBlank(facsimile)) {
			facsimileContainer.setVisible(false);
		} else {
			visibleFieldCount_contact++;
		}
		
		add(contactInfoContainer);
		
		//if nothing/not allowed, hide whole panel
		if(visibleFieldCount_contact == 0 || !isContactInfoAllowed) {
			contactInfoContainer.setVisible(false);
		} else {
			visibleContainerCount++;
		}
		
		
		/* STAFF INFO */
		WebMarkupContainer staffInfoContainer = new WebMarkupContainer("mainSectionContainer_staff");
		staffInfoContainer.setOutputMarkupId(true);
		
		//get info
		String department = sakaiPerson.getOrganizationalUnit();
		String position = sakaiPerson.getTitle();
		String school = sakaiPerson.getCampus();
		String room = sakaiPerson.getRoomNumber();
		String staffProfile = sakaiPerson.getStaffProfile();
		String universityProfileUrl = sakaiPerson.getUniversityProfileUrl();
		String academicProfileUrl = sakaiPerson.getAcademicProfileUrl();
		String publications = sakaiPerson.getPublications();
		
		int visibleFieldCount_staff = 0;
		
		//heading
		staffInfoContainer.add(new Label("mainSectionHeading_staff", new ResourceModel("heading.staff")));
		
		//department
		WebMarkupContainer departmentContainer = new WebMarkupContainer("departmentContainer");
		departmentContainer.add(new Label("departmentLabel", new ResourceModel("profile.department")));
		departmentContainer.add(new Label("department", department));
		staffInfoContainer.add(departmentContainer);
		if(StringUtils.isBlank(department)) {
			departmentContainer.setVisible(false);
		} else {
			visibleFieldCount_staff++;
		}
		
		//position
		WebMarkupContainer positionContainer = new WebMarkupContainer("positionContainer");
		positionContainer.add(new Label("positionLabel", new ResourceModel("profile.position")));
		positionContainer.add(new Label("position", position));
		staffInfoContainer.add(positionContainer);
		if(StringUtils.isBlank(position)) {
			positionContainer.setVisible(false);
		} else {
			visibleFieldCount_staff++;
		}
		
		//school
		WebMarkupContainer schoolContainer = new WebMarkupContainer("schoolContainer");
		schoolContainer.add(new Label("schoolLabel", new ResourceModel("profile.school")));
		schoolContainer.add(new Label("school", school));
		staffInfoContainer.add(schoolContainer);
		if(StringUtils.isBlank(school)) {
			schoolContainer.setVisible(false);
		} else {
			visibleFieldCount_staff++;
		}
		
		//room
		WebMarkupContainer roomContainer = new WebMarkupContainer("roomContainer");
		roomContainer.add(new Label("roomLabel", new ResourceModel("profile.room")));
		roomContainer.add(new Label("room", room));
		staffInfoContainer.add(roomContainer);
		if(StringUtils.isBlank(room)) {
			roomContainer.setVisible(false);
		} else {
			visibleFieldCount_staff++;
		}
		
		//staff profile
		WebMarkupContainer staffProfileContainer = new WebMarkupContainer("staffProfileContainer");
		staffProfileContainer.add(new Label("staffProfileLabel", new ResourceModel("profile.staffprofile")));
		staffProfileContainer.add(new Label("staffProfile", staffProfile));
		staffInfoContainer.add(staffProfileContainer);
		if(StringUtils.isBlank(staffProfile)) {
			staffProfileContainer.setVisible(false);
		} else {
			visibleFieldCount_staff++;
		}
		
		//university profile URL
		WebMarkupContainer universityProfileUrlContainer = new WebMarkupContainer("universityProfileUrlContainer");
		universityProfileUrlContainer.add(new Label("universityProfileUrlLabel", new ResourceModel("profile.universityprofileurl")));
		universityProfileUrlContainer.add(new ExternalLink("universityProfileUrl", universityProfileUrl, universityProfileUrl));
		staffInfoContainer.add(universityProfileUrlContainer);
		if(StringUtils.isBlank(universityProfileUrl)) {
			universityProfileUrlContainer.setVisible(false);
		} else {
			visibleFieldCount_contact++;
		}
		
		//academic/research profile URL
		WebMarkupContainer academicProfileUrlContainer = new WebMarkupContainer("academicProfileUrlContainer");
		academicProfileUrlContainer.add(new Label("academicProfileUrlLabel", new ResourceModel("profile.academicprofileurl")));
		academicProfileUrlContainer.add(new ExternalLink("academicProfileUrl", academicProfileUrl, academicProfileUrl));
		staffInfoContainer.add(academicProfileUrlContainer);
		if(StringUtils.isBlank(academicProfileUrl)) {
			academicProfileUrlContainer.setVisible(false);
		} else {
			visibleFieldCount_contact++;
		}
		
		//publications
		WebMarkupContainer publicationsContainer = new WebMarkupContainer("publicationsContainer");
		publicationsContainer.add(new Label("publicationsLabel", new ResourceModel("profile.publications")));
		publicationsContainer.add(new Label("publications", ProfileUtils.processHtml(publications)).setEscapeModelStrings(false));
		staffInfoContainer.add(publicationsContainer);
		if(StringUtils.isBlank(publications)) {
			publicationsContainer.setVisible(false);
		} else {
			visibleFieldCount_staff++;
		}
		
		add(staffInfoContainer);
		
		//if nothing/not allowed, hide whole panel
		if(visibleFieldCount_staff == 0 || !isStaffInfoAllowed) {
			staffInfoContainer.setVisible(false);
		} else {
			visibleContainerCount++;
		}
		
		/* BUSINESS INFO (OPTIONAL) */
		if (sakaiProxy.isBusinessProfileEnabled()) {
			ViewBusiness businessPanel = new ViewBusiness("viewBusiness",
					userUuid, sakaiPerson, isBusinessInfoAllowed);

			if (0 == businessPanel.getVisibleFieldCount() || !isBusinessInfoAllowed) {
				businessPanel.setVisible(false);
			} else {
				visibleContainerCount++;
			}
			add(businessPanel);
			
		} else {
			Panel businessPanel = new EmptyPanel("viewBusiness");
			add(businessPanel);
		}
		
		/* STUDENT INFO*/
		WebMarkupContainer studentInfoContainer = new WebMarkupContainer("mainSectionContainer_student");
		studentInfoContainer.setOutputMarkupId(true);
		
		String course = sakaiPerson.getEducationCourse();
		String subjects = sakaiPerson.getEducationSubjects();
		
		int visibleFieldCount_student = 0;
		
		//heading
		studentInfoContainer.add(new Label("mainSectionHeading_student", new ResourceModel("heading.student")));
		
		//course
		WebMarkupContainer courseContainer = new WebMarkupContainer("courseContainer");
		courseContainer.add(new Label("courseLabel", new ResourceModel("profile.course")));
		courseContainer.add(new Label("course", course));
		studentInfoContainer.add(courseContainer);
		if(StringUtils.isBlank(course)) {
			courseContainer.setVisible(false);
		} else {
			visibleFieldCount_student++;
		}
		
		//subjects
		WebMarkupContainer subjectsContainer = new WebMarkupContainer("subjectsContainer");
		subjectsContainer.add(new Label("subjectsLabel", new ResourceModel("profile.subjects")));
		subjectsContainer.add(new Label("subjects", subjects));
		studentInfoContainer.add(subjectsContainer);
		if(StringUtils.isBlank(subjects)) {
			subjectsContainer.setVisible(false);
		} else {
			visibleFieldCount_student++;
		}
		
		add(studentInfoContainer);
		
		//if nothing/not allowed, hide whole panel
		if(visibleFieldCount_student == 0 || !isStudentInfoAllowed) {
			studentInfoContainer.setVisible(false);
		} else {
			visibleContainerCount++;
		}
		
		/* SOCIAL NETWORKING */
		WebMarkupContainer socialNetworkingInfoContainer = new WebMarkupContainer("mainSectionContainer_socialNetworking");
		socialNetworkingInfoContainer.setOutputMarkupId(true);
		
		//heading
		socialNetworkingInfoContainer.add(new Label("mainSectionHeading_socialNetworking", new ResourceModel("heading.social")));
		
		SocialNetworkingInfo socialNetworkingInfo = profileLogic.getSocialNetworkingInfo(userUuid);
		if (null == socialNetworkingInfo) {
			socialNetworkingInfo = profileLogic.getSocialNetworkingInfo(userUuid);
		}
		String facebookUsername = socialNetworkingInfo.getFacebookUrl();
		String linkedinUsername = socialNetworkingInfo.getLinkedinUrl();
		String myspaceUsername = socialNetworkingInfo.getMyspaceUrl();
		String skypeUsername = socialNetworkingInfo.getSkypeUsername();
		String twitterUsername = socialNetworkingInfo.getTwitterUrl();
				
		int visibleFieldCount_socialNetworking = 0;
		
		//facebook
		WebMarkupContainer facebookContainer = new WebMarkupContainer("facebookContainer");
		facebookContainer.add(new Label("facebookLabel", new ResourceModel("profile.socialnetworking.facebook")));
		facebookContainer.add(new ExternalLink("facebookLink", facebookUsername, facebookUsername));
		socialNetworkingInfoContainer.add(facebookContainer);
		if(StringUtils.isBlank(facebookUsername)) {
			facebookContainer.setVisible(false);
		} else {
			visibleFieldCount_socialNetworking++;
		}
		
		//linkedin
		WebMarkupContainer linkedinContainer = new WebMarkupContainer("linkedinContainer");
		linkedinContainer.add(new Label("linkedinLabel", new ResourceModel("profile.socialnetworking.linkedin")));
		linkedinContainer.add(new ExternalLink("linkedinLink", linkedinUsername, linkedinUsername));
		socialNetworkingInfoContainer.add(linkedinContainer);
		if(StringUtils.isBlank(linkedinUsername)) {
			linkedinContainer.setVisible(false);
		} else {
			visibleFieldCount_socialNetworking++;
		}
		
		//myspace
		WebMarkupContainer myspaceContainer = new WebMarkupContainer("myspaceContainer");
		myspaceContainer.add(new Label("myspaceLabel", new ResourceModel("profile.socialnetworking.myspace")));
		myspaceContainer.add(new ExternalLink("myspaceLink", myspaceUsername, myspaceUsername));
		socialNetworkingInfoContainer.add(myspaceContainer);
		if(StringUtils.isBlank(myspaceUsername)) {
			myspaceContainer.setVisible(false);
		} else {
			visibleFieldCount_socialNetworking++;
		}
		
		//twitter
		WebMarkupContainer twitterContainer = new WebMarkupContainer("twitterContainer");
		twitterContainer.add(new Label("twitterLabel", new ResourceModel("profile.socialnetworking.twitter")));
		twitterContainer.add(new ExternalLink("twitterLink", twitterUsername, twitterUsername));
		socialNetworkingInfoContainer.add(twitterContainer);
		if(StringUtils.isBlank(twitterUsername)) {
			twitterContainer.setVisible(false);
		} else {
			visibleFieldCount_socialNetworking++;
		}
		
		//skypeme
		WebMarkupContainer skypeContainer = new WebMarkupContainer("skypeContainer");
		skypeContainer.add(new Label("skypeLabel", new ResourceModel("profile.socialnetworking.skype")));
		skypeContainer.add(new ExternalLink("skypeLink", ProfileUtils.getSkypeMeURL(skypeUsername), new ResourceModel("profile.socialnetworking.skype.link").getObject()));
		socialNetworkingInfoContainer.add(skypeContainer);
		if(StringUtils.isBlank(skypeUsername)) {
			skypeContainer.setVisible(false);
		} else {
			visibleFieldCount_socialNetworking++;
		}
		
		add(socialNetworkingInfoContainer);
		
		//if nothing/not allowed, hide whole panel
		if(visibleFieldCount_socialNetworking == 0 || !isSocialNetworkingInfoAllowed) {
			socialNetworkingInfoContainer.setVisible(false);
		} else {
			visibleContainerCount++;
		}
		
		/* PERSONAL INFO */
		WebMarkupContainer personalInfoContainer = new WebMarkupContainer("mainSectionContainer_personal");
		personalInfoContainer.setOutputMarkupId(true);
		
		//setup info
				
		// favourites and other
		String favouriteBooks = sakaiPerson.getFavouriteBooks();
		String favouriteTvShows = sakaiPerson.getFavouriteTvShows();
		String favouriteMovies = sakaiPerson.getFavouriteMovies();
		String favouriteQuotes = sakaiPerson.getFavouriteQuotes();

		int visibleFieldCount_personal = 0;
		
		//heading
		personalInfoContainer.add(new Label("mainSectionHeading_personal", new ResourceModel("heading.interests")));
		
		//favourite books
		WebMarkupContainer booksContainer = new WebMarkupContainer("booksContainer");
		booksContainer.add(new Label("booksLabel", new ResourceModel("profile.favourite.books")));
		booksContainer.add(new Label("favouriteBooks", favouriteBooks));
		personalInfoContainer.add(booksContainer);
		if(StringUtils.isBlank(favouriteBooks)) {
			booksContainer.setVisible(false);
		} else {
			visibleFieldCount_personal++;
		}
		
		//favourite tv shows
		WebMarkupContainer tvContainer = new WebMarkupContainer("tvContainer");
		tvContainer.add(new Label("tvLabel", new ResourceModel("profile.favourite.tv")));
		tvContainer.add(new Label("favouriteTvShows", favouriteTvShows));
		personalInfoContainer.add(tvContainer);
		if(StringUtils.isBlank(favouriteTvShows)) {
			tvContainer.setVisible(false);
		} else {
			visibleFieldCount_personal++;
		}
		
		//favourite movies
		WebMarkupContainer moviesContainer = new WebMarkupContainer("moviesContainer");
		moviesContainer.add(new Label("moviesLabel", new ResourceModel("profile.favourite.movies")));
		moviesContainer.add(new Label("favouriteMovies", favouriteMovies));
		personalInfoContainer.add(moviesContainer);
		if(StringUtils.isBlank(favouriteMovies)) {
			moviesContainer.setVisible(false);
		} else {
			visibleFieldCount_personal++;
		}
		
		//favourite quotes
		WebMarkupContainer quotesContainer = new WebMarkupContainer("quotesContainer");
		quotesContainer.add(new Label("quotesLabel", new ResourceModel("profile.favourite.quotes")));
		quotesContainer.add(new Label("favouriteQuotes", favouriteQuotes));
		personalInfoContainer.add(quotesContainer);
		if(StringUtils.isBlank(favouriteQuotes)) {
			quotesContainer.setVisible(false);
		} else {
			visibleFieldCount_personal++;
		}	
		
		add(personalInfoContainer);
		
		//if nothing/not allowed, hide whole panel
		if(visibleFieldCount_personal == 0 || !isPersonalInfoAllowed) {
			personalInfoContainer.setVisible(false);
		} else {
			visibleContainerCount++;
		}
		
		/* NO INFO VISIBLE MESSAGE (hide if some visible) */
		Label noContainersVisible = new Label ("noContainersVisible", new ResourceModel("text.view.profile.nothing"));
		noContainersVisible.setOutputMarkupId(true);
		add(noContainersVisible);
		
		if(visibleContainerCount > 0) {
			noContainersVisible.setVisible(false);
		}
	}
}
