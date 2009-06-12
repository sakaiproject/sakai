package org.sakaiproject.profile2.tool.models;

import java.io.Serializable;
import java.util.Date;

/**
 * Model for profile information which backs form updates. To be used only by the Profile2 tool.
 * 
 * <p>DO NOT USE THIS YOURSELF.</p>
 * 
 * TODO merge this with the proper API model
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 */
public class UserProfile implements Serializable {

	private static final long serialVersionUID = 1L;
	private String userId;
	private String nickname;
	private String birthday;
	private String birthdayDisplay;
	private Date dateOfBirth;
	private String displayName;
	private String firstName;
	private String middleName;
	private String lastName;
	private String email;
	private String position;
	private String department;
	private String school;
	private String room;
	private String homepage;
	private String workphone;
	private String homephone;
	private String mobilephone;
	private String facsimile;
	private String favouriteBooks;
	private String favouriteTvShows;
	private String favouriteMovies;
	private String favouriteQuotes;
	private String otherInformation;
	private String course;
	private String subjects;
	private boolean locked;
	
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
		
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	public String getBirthdayDisplay() {
		return birthdayDisplay;
	}
	public void setBirthdayDisplay(String birthdayDisplay) {
		this.birthdayDisplay = birthdayDisplay;
	}
	
	public Date getDateOfBirth() {
		return dateOfBirth;
	}
	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getSchool() {
		return school;
	}
	public void setSchool(String school) {
		this.school = school;
	}
	public String getRoom() {
		return room;
	}
	public void setRoom(String room) {
		this.room = room;
	}
	
	public String getHomepage() {
		return homepage;
	}
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}
	public String getWorkphone() {
		return workphone;
	}
	public void setWorkphone(String workphone) {
		this.workphone = workphone;
	}
	public String getHomephone() {
		return homephone;
	}
	public void setHomephone(String homephone) {
		this.homephone = homephone;
	}
	public String getMobilephone() {
		return mobilephone;
	}
	public void setMobilephone(String mobilephone) {
		this.mobilephone = mobilephone;
	}
	public void setFacsimile(String facsimile) {
		this.facsimile = facsimile;
	}
	public String getFacsimile() {
		return facsimile;
	}
	public String getFavouriteBooks() {
		return favouriteBooks;
	}
	public void setFavouriteBooks(String favouriteBooks) {
		this.favouriteBooks = favouriteBooks;
	}
	public String getFavouriteTvShows() {
		return favouriteTvShows;
	}
	public void setFavouriteTvShows(String favouriteTvShows) {
		this.favouriteTvShows = favouriteTvShows;
	}
	public String getFavouriteMovies() {
		return favouriteMovies;
	}
	public void setFavouriteMovies(String favouriteMovies) {
		this.favouriteMovies = favouriteMovies;
	}
	public String getFavouriteQuotes() {
		return favouriteQuotes;
	}
	public void setFavouriteQuotes(String favouriteQuotes) {
		this.favouriteQuotes = favouriteQuotes;
	}
	public void setOtherInformation(String otherInformation) {
		this.otherInformation = otherInformation;
	}
	public String getOtherInformation() {
		return otherInformation;
	}
	public String getCourse() {
		return course;
	}
	public void setCourse(String course) {
		this.course = course;
	}
	public String getSubjects() {
		return subjects;
	}
	public void setSubjects(String subjects) {
		this.subjects = subjects;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	public boolean isLocked() {
		return locked;
	}

	
	/**
	 * Default constructor
	 */
	public UserProfile() {
	
	}
	
	
}
